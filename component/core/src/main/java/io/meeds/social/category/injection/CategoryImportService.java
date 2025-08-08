/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.category.injection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryDescriptor;
import io.meeds.social.category.model.CategoryDescriptorList;
import io.meeds.social.category.plugin.CategoryTranslationPlugin;
import io.meeds.social.category.storage.CategoryStorage;
import io.meeds.social.util.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.common.ContainerTransactional;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

@Component
@Order()
public class CategoryImportService {

  public static final String               ADMINISTRATORS_GROUP  = "/platform/administrators";

  private static final String              CATEGORY_IMPORT       = "CATEGORY_IMPORT";

  private static final Scope               CATEGORY_IMPORT_SCOPE = Scope.APPLICATION.id(CATEGORY_IMPORT);

  private static final Context             CATEGORY_CONTEXT      = Context.GLOBAL.id("CATEGORY");

  private static final String              CATEGORY_VERSION      = "version";

  private static final Log                 LOG                   = ExoLogger.getLogger(CategoryImportService.class);

  private long                             superUserIdentityId;

  private long                             adminGroupOwnerId;

  @Autowired
  private CategoryTranslationImportService categoryTranslationImportService;

  @Autowired
  private CategoryStorage                  categoryStorage;

  @Autowired
  private IdentityManager                  identityManager;

  @Autowired
  private UserACL                          userAcl;

  @Autowired
  private SettingService                   settingService;

  @Autowired
  private ConfigurationManager             configurationManager;

  @Value("${meeds.categories.import.override:false}")
  private boolean                          forceReimport;

  @Value("${meeds.categories.import.version:1}")
  private long                             categoryImportVersion;

  @PostConstruct
  public void init() {
    CompletableFuture.runAsync(this::importDefaultCategories);
  }

  @ContainerTransactional
  public void importDefaultCategories() {
    if (!forceReimport && getSettingValue(CATEGORY_VERSION) != categoryImportVersion) {
      forceReimport = true;
    }
    LOG.info("Importing categories with version {}, force reimport = {}", categoryImportVersion, forceReimport);

    try {
      Enumeration<URL> templateFiles =
                                     PortalContainer.getInstance().getPortalClassLoader().getResources("default-categories.json");
      Collections.list(templateFiles).stream().map(this::parseDescriptors).flatMap(List::stream).forEach(this::importDescriptor);

      LOG.info("Importing categories finished successfully.");

      LOG.info("Processing Post categories import");
      categoryTranslationImportService.postImport(CategoryTranslationPlugin.OBJECT_TYPE);
      LOG.info("Processing Post categories import finished");

      setSettingValue(CATEGORY_VERSION, categoryImportVersion);
    } catch (Exception e) {
      LOG.warn("An error occurred while importing categories", e);
    }
  }

  protected List<CategoryDescriptor> parseDescriptors(URL url) {
    try (InputStream inputStream = url.openStream()) {
      String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      CategoryDescriptorList list = JsonUtils.fromJsonString(content, CategoryDescriptorList.class);
      return list != null ? list.getDescriptors() : Collections.emptyList();
    } catch (IOException e) {
      LOG.warn("An unknown error happened while parsing categories from url {}", url, e);
      return Collections.emptyList();
    }
  }

  protected void importDescriptor(CategoryDescriptor descriptor) {
    String descriptorId = descriptor.getNameId();
    long existingId = getSettingValue(descriptorId);
    if (forceReimport || existingId == 0) {
      Category rootCategory = categoryStorage.getRootCategory(getAdminGroupIdentityId());
      if (rootCategory == null) {
        rootCategory = new Category(0L,
                                    0L,
                                    null,
                                    getSuperUserIdentityId(),
                                    getAdminGroupIdentityId(),
                                    List.of(ADMINISTRATORS_GROUP));
        rootCategory = categoryStorage.createCategory(rootCategory);
      }
      importCategory(descriptor, existingId, rootCategory.getId());
    } else {
      LOG.debug("Ignore re-importing category {}", descriptorId);
    }
  }

  protected void importCategory(CategoryDescriptor d, long oldId, long parentId) {
    String descriptorId = d.getNameId();
    LOG.info("Importing category {}", descriptorId);
    try {
      Category category = createCategory(d, oldId, parentId);
      if (forceReimport || oldId == 0 || category.getId() != oldId) {
        LOG.info("Importing category {} title translations", descriptorId);
        saveNames(d, category);
        // Mark as imported
        setSettingValue(d.getNameId(), category.getId());
      }
      LOG.info("Importing category {} finished successfully", descriptorId);
    } catch (Exception e) {
      LOG.warn("An error occurred while importing category {}", descriptorId, e);
    }
  }

  protected void saveNames(CategoryDescriptor d, Category category) {
    categoryTranslationImportService.saveTranslationLabels(CategoryTranslationPlugin.OBJECT_TYPE,
                                                           category.getId(),
                                                           CategoryTranslationPlugin.NAME_FIELD,
                                                           d.getNames());
  }

  @SneakyThrows
  protected Category createCategory(CategoryDescriptor d, long oldId, long parentId) {
    Category category = null;
    if (oldId > 0) {
      category = categoryStorage.getCategory(oldId);
    }
    boolean isNew = category == null;
    if (isNew) {
      category = new Category();
    }

    category.setParentId(Math.max(parentId, 0L));
    category.setIcon(StringUtils.isNoneBlank(d.getIcon()) ? d.getIcon() : "fa-th-large");
    category.setCreatorId(getSuperUserIdentityId());
    category.setOwnerId(getSuperUserIdentityId());
    category.setLinkPermissions(d.getLinkPermissions());

    if (isNew) {
      category = categoryStorage.createCategory(category);
    } else {
      category = categoryStorage.updateCategory(category);
    }

    if (CollectionUtils.isNotEmpty(d.getSubCategories())) {
      for (CategoryDescriptor subCategory : d.getSubCategories()) {
        long subOldId = getSettingValue(subCategory.getNameId());
        importCategory(subCategory, subOldId, category.getId());
      }
    }

    return category;
  }

  protected void setSettingValue(String name, long value) {
    settingService.set(CATEGORY_CONTEXT, CATEGORY_IMPORT_SCOPE, name, SettingValue.create(String.valueOf(value)));
  }

  protected long getSettingValue(String name) {
    try {
      SettingValue<?> settingValue = settingService.get(CATEGORY_CONTEXT, CATEGORY_IMPORT_SCOPE, name);
      return settingValue == null || settingValue.getValue() == null ? 0L : Long.parseLong(settingValue.getValue().toString());
    } catch (NumberFormatException e) {
      return 0L;
    }
  }

  private long getSuperUserIdentityId() {
    if (superUserIdentityId == 0) {
      superUserIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(userAcl.getSuperUser()).getId());
    }
    return superUserIdentityId;
  }

  private long getAdminGroupIdentityId() {
    if (adminGroupOwnerId == 0) {
      Identity adminGroupIdentity = identityManager.getOrCreateGroupIdentity(ADMINISTRATORS_GROUP);
      adminGroupOwnerId = adminGroupIdentity == null ? 0L : Long.parseLong(adminGroupIdentity.getId());
    }
    return adminGroupOwnerId;
  }
}
