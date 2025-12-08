/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.space.template.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.attachment.AttachmentService;

import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.model.SpaceTemplateFilter;
import io.meeds.social.space.template.plugin.attachment.SpaceTemplateBannerAttachmentPlugin;
import io.meeds.social.space.template.plugin.translation.SpaceTemplateTranslationPlugin;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;
import io.meeds.social.translation.service.TranslationService;

@Service
public class SpaceTemplateService {

  public static final String      SPACE_TEMPLATE_CREATED_EVENT  = "space.template.created";

  public static final String      SPACE_TEMPLATE_UPDATED_EVENT  = "space.template.updated";

  public static final String      SPACE_TEMPLATE_DELETED_EVENT  = "space.template.deleted";

  public static final String      SPACE_TEMPLATE_SITE_PROP_NAME = "SPACE_TEMPLATE";

  public static final String      SPACE_TEMPLATE_ID_PROP_NAME   = "SPACE_TEMPLATE_ID";

  public static final String      DEFAULT_SITE_TEMPLATE         = "space";

  private static final Log        LOG                           = ExoLogger.getLogger(SpaceTemplateService.class);

  private TranslationService      translationService;

  private AttachmentService       attachmentService;

  private UserACL                 userAcl;

  private SpaceTemplateStorage    spaceTemplateStorage;

  private UserPortalConfigService userPortalConfigService;

  private LayoutService           layoutService;

  private NavigationService       navigationService;

  private ListenerService         listenerService;

  public SpaceTemplateService(TranslationService translationService,
                              AttachmentService attachmentService,
                              UserPortalConfigService userPortalConfigService,
                              LayoutService layoutService,
                              NavigationService navigationService,
                              ListenerService listenerService,
                              UserACL userAcl,
                              SpaceTemplateStorage spaceTemplateStorage) {
    this.userPortalConfigService = userPortalConfigService;
    this.translationService = translationService;
    this.attachmentService = attachmentService;
    this.userAcl = userAcl;
    this.spaceTemplateStorage = spaceTemplateStorage;
    this.layoutService = layoutService;
    this.navigationService = navigationService;
    this.listenerService = listenerService;
  }

  public List<SpaceTemplate> getSpaceTemplates() {
    return getSpaceTemplates(null, Pageable.unpaged(), false);
  }

  public List<SpaceTemplate> getSpaceTemplates(SpaceTemplateFilter spaceTemplateFilter, Pageable pageable, boolean expand) {
    boolean includeDisabled = spaceTemplateFilter == null || spaceTemplateFilter.isIncludeDisabled();
    List<SpaceTemplate> spaceTemplates = includeDisabled ? spaceTemplateStorage.getSpaceTemplates(pageable)
                                                         : spaceTemplateStorage.getEnabledSpaceTemplates(pageable);
    return spaceTemplates.stream()
                         .map(spaceTemplate -> {
                           if (spaceTemplateFilter != null
                               && !canViewTemplate(spaceTemplate.getId(), spaceTemplateFilter.getUsername())) {
                             return null;
                           } else if (expand) {
                             computeSpaceTemplateAttributes(spaceTemplate,
                                                            spaceTemplateFilter == null ? null :
                                                                                        spaceTemplateFilter.getLocale());
                           }
                           return spaceTemplate;
                         })
                         .filter(Objects::nonNull)
                         .toList();
  }

  public List<Long> getManagingSpaceTemplates(String username) {
    List<SpaceTemplate> spaceTemplates = spaceTemplateStorage.getSpaceTemplates(Pageable.unpaged());
    List<Long> spaceTemplateIds = spaceTemplates.stream()
                                                .filter(t -> canManageSpacesWithTemplate(t, username))
                                                .map(SpaceTemplate::getId)
                                                .toList();
    if (canManageTemplates(username)) {
      spaceTemplateIds = new ArrayList<>(spaceTemplateIds);
      // Include spaces not having an associated template Id
      // which should be visible to an administrator
      spaceTemplateIds.add(0L);
    }
    return spaceTemplateIds;
  }
  
  public List<Long> getTemplateIdsAllowingSubspaces() {
    return getSpaceTemplates().stream()
                              .filter(spaceTemplate -> !spaceTemplate.isDeleted() && spaceTemplate.isEnabled()
                                  && org.apache.commons.collections.CollectionUtils.isNotEmpty(spaceTemplate.getAllowedSubspaceTemplates()))
                              .map(SpaceTemplate::getId)
                              .toList();
  }

  public List<SpaceTemplate> getAllowedSubspaceTemplates(long templateId, String username, Locale locale) throws ObjectNotFoundException,
                                                                                           IllegalAccessException {

    SpaceTemplate parentTemplate = spaceTemplateStorage.getSpaceTemplate(templateId);

    if (parentTemplate == null || parentTemplate.isDeleted()) {
      throw new ObjectNotFoundException("Template not found: " + templateId);
    }

    List<String> allowedTemplates = parentTemplate.getAllowedSubspaceTemplates();
    if (CollectionUtils.isEmpty(allowedTemplates)) {
      return Collections.emptyList();
    }

    List<Long> templateIds = allowedTemplates.stream()
                                             .map(item -> item.split(":")[0])
                                             .filter(part -> !part.isEmpty() && NumberUtils.isCreatable(part))
                                             .map(Long::parseLong)
                                             .toList();
    List<SpaceTemplate> allowedSubspaceTemplates = templateIds.stream().map(id -> getSpaceTemplate(id, locale, true))
            .filter(Objects::nonNull)
            .filter(spaceTemplate -> canViewTemplate(spaceTemplate, username)).toList();

    if (CollectionUtils.isEmpty(allowedSubspaceTemplates) && CollectionUtils.isNotEmpty(templateIds)) {
      throw new IllegalAccessException("User '" + username + "' has no access to allowed subspace templates of template "
          + templateId);
    }
    return allowedSubspaceTemplates;
  }


  public long countManagingSpaceTemplates(String username) {
    List<SpaceTemplate> spaceTemplates = spaceTemplateStorage.getSpaceTemplates(Pageable.unpaged());
    long count = spaceTemplates.stream().filter(t -> canManageSpacesWithTemplate(t, username)).count();
    if (canManageTemplates(username)) {
      // Include spaces not having an associated template Id
      // which should be visible to an administrator
      count++;
    }
    return count;
  }

  public SpaceTemplate getSpaceTemplate(long templateId) {
    return getSpaceTemplate(templateId, null, false);
  }

  public SpaceTemplate getSpaceTemplate(long templateId,
                                        String username,
                                        Locale locale,
                                        boolean expand) throws IllegalAccessException {
    SpaceTemplate spaceTemplate = spaceTemplateStorage.getSpaceTemplate(templateId);
    if (spaceTemplate == null) {
      return null;
    }
    if (!canViewTemplate(spaceTemplate, username)) {
      throw new IllegalAccessException();
    }
    return getSpaceTemplate(templateId, locale, expand);
  }

  public SpaceTemplate getSpaceTemplate(long templateId, Locale locale, boolean expand) {
    SpaceTemplate spaceTemplate = spaceTemplateStorage.getSpaceTemplate(templateId);
    if (expand && spaceTemplate != null && locale != null) {
      computeSpaceTemplateAttributes(spaceTemplate, locale);
    }
    return spaceTemplate;
  }

  public SpaceTemplate getSpaceTemplateByLayout(String layout) {
    return spaceTemplateStorage.getSpaceTemplateByLayout(layout);
  }

  public long getSpaceTemplateBannerId(long templateId) {
    List<String> attachmentFileIds = attachmentService.getAttachmentFileIds(SpaceTemplateBannerAttachmentPlugin.OBJECT_TYPE,
                                                                            String.valueOf(templateId));
    if (CollectionUtils.isNotEmpty(attachmentFileIds)) {
      return Long.parseLong(attachmentFileIds.get(0));
    } else {
      return 0l;
    }
  }

  public boolean canManageSpacesWithTemplate(SpaceTemplate spaceTemplate, String username) {
    if (canManageTemplates(username)) {
      return true;
    } else {
      Identity aclIdentity = userAcl.getUserIdentity(username);
      return aclIdentity != null
             && spaceTemplate.getAdminPermissions()
                             .stream()
                             .anyMatch(expression -> aclIdentity.isMemberOf(getMembershipEntry(expression)));
    }
  }

  public boolean canManageTemplates(String username) {
    return userAcl.isAdministrator(userAcl.getUserIdentity(username));
  }

  public boolean canViewTemplate(long templateId, String username) {
    SpaceTemplate spaceTemplate = getSpaceTemplate(templateId);
    return canViewTemplate(spaceTemplate, username);
  }

  public boolean canCreateSpace(long templateId, String username) {
    SpaceTemplate spaceTemplate = getSpaceTemplate(templateId);
    return spaceTemplate != null && !spaceTemplate.isDeleted() && spaceTemplate.isEnabled() && canViewTemplate(spaceTemplate, username);
  }

  public boolean canCreateSpace(String username) {
    return spaceTemplateStorage.getEnabledSpaceTemplates(Pageable.unpaged())
                               .stream()
                               .anyMatch(t -> canViewTemplate(t, username));
  }

  public SpaceTemplate createSpaceTemplate(SpaceTemplate spaceTemplate, String username) throws IllegalAccessException,
                                                                                         ObjectNotFoundException {
    if (!canManageTemplates(username)) {
      throw new IllegalAccessException("User isn't authorized to create a space template");
    }
    return createSpaceTemplate(spaceTemplate);
  }

  public SpaceTemplate createSpaceTemplate(SpaceTemplate spaceTemplate) throws ObjectNotFoundException {
    if (spaceTemplate.getId() != 0) {
      throw new IllegalArgumentException("Space template to create shouldn't have an id");
    }
    String sourceLayout = StringUtils.firstNonBlank(spaceTemplate.getLayout(), DEFAULT_SITE_TEMPLATE);
    SiteKey sourceSiteKey = sourceLayout.contains("::") ? new SiteKey(sourceLayout.split("::")[0], sourceLayout.split("::")[1]) :
                                                        SiteKey.groupTemplate(sourceLayout);
    if (layoutService.getPortalConfig(sourceSiteKey) == null) {
      throw new ObjectNotFoundException(String.format("Space Template layout '%s' wasn't found", sourceLayout));
    }

    SpaceTemplate spaceTemplateToCreate = spaceTemplate.clone();
    spaceTemplateToCreate.setSystem(false);
    spaceTemplateToCreate.setDeleted(false);
    spaceTemplateToCreate.setLayout(null);
    SpaceTemplate createdSpaceTemplate = spaceTemplateStorage.createSpaceTemplate(spaceTemplateToCreate);
    createdSpaceTemplate = createSpaceTemplateLayout(createdSpaceTemplate, sourceSiteKey);
    listenerService.broadcast(SPACE_TEMPLATE_CREATED_EVENT, spaceTemplate, createdSpaceTemplate);
    return createdSpaceTemplate;
  }

  public SpaceTemplate updateSpaceTemplate(SpaceTemplate spaceTemplate,
                                           String username) throws ObjectNotFoundException, IllegalAccessException {
    if (!canManageTemplates(username)) {
      throw new IllegalAccessException("User isn't authorized to update a space template");
    } else if (spaceTemplate.isDeleted()) {
      throw new IllegalArgumentException("Can't mark space template as deleted through update method");
    }
    return updateSpaceTemplate(spaceTemplate);
  }

  public SpaceTemplate updateSpaceTemplate(SpaceTemplate spaceTemplate) throws ObjectNotFoundException {
    SpaceTemplate storedSpaceTemplate = spaceTemplateStorage.getSpaceTemplate(spaceTemplate.getId());
    if (storedSpaceTemplate == null || storedSpaceTemplate.isDeleted()) {
      throw new ObjectNotFoundException("Space Template doesn't exist");
    }
    spaceTemplate.setSystem(storedSpaceTemplate.isSystem());
    spaceTemplate.setDeleted(storedSpaceTemplate.isDeleted());
    spaceTemplate.setLayout(storedSpaceTemplate.getLayout());
    SpaceTemplate updatedSpaceTemplate = spaceTemplateStorage.updateSpaceTemplate(spaceTemplate);
    listenerService.broadcast(SPACE_TEMPLATE_UPDATED_EVENT, storedSpaceTemplate, updatedSpaceTemplate);
    return updatedSpaceTemplate;
  }

  public void deleteSpaceTemplate(long templateId, String username) throws IllegalAccessException, ObjectNotFoundException {
    if (!canManageTemplates(username)) {
      throw new IllegalAccessException("User isn't authorized to create a space template");
    }
    SpaceTemplate spaceTemplate = getSpaceTemplate(templateId);
    if (spaceTemplate != null && spaceTemplate.isSystem()) {
      throw new IllegalAccessException("Can't delete a system space template");
    }
    deleteSpaceTemplate(templateId);
  }

  public void deleteSpaceTemplate(long templateId) throws ObjectNotFoundException {
    SpaceTemplate spaceTemplate = spaceTemplateStorage.getSpaceTemplate(templateId);
    if (spaceTemplate == null || spaceTemplate.isDeleted()) {
      throw new ObjectNotFoundException(String.format("Space template with id %s doesn't exist", templateId));
    }

    try {
      attachmentService.deleteAttachments(SpaceTemplateBannerAttachmentPlugin.OBJECT_TYPE, String.valueOf(templateId));
    } catch (Exception e) {
      LOG.debug("Error while deleting attachments of deleted Page Template", e);
    }
    try {
      translationService.deleteTranslationLabels(SpaceTemplateTranslationPlugin.OBJECT_TYPE, templateId);
    } catch (ObjectNotFoundException e) {
      LOG.debug("Error while deleting translation labels of deleted Page Template", e);
    }
    PortalConfig portalConfig = layoutService.getPortalConfig(SiteKey.groupTemplate(spaceTemplate.getLayout()));
    if (portalConfig != null) {
      SiteKey siteKey = SiteKey.portal(portalConfig.getName());
      navigationService.destroyNavigation(siteKey);
      layoutService.removePages(siteKey);
      layoutService.remove(portalConfig);
    }
    spaceTemplate.setDeleted(true);
    spaceTemplateStorage.updateSpaceTemplate(spaceTemplate);
    listenerService.broadcast(SPACE_TEMPLATE_DELETED_EVENT, spaceTemplate, spaceTemplate);
  }
  
  public List<Long> getParentSpaceTemplateIds(long subTemplateId) {
    return getSpaceTemplates().stream()
                              .filter(template -> template.getAllowedSubspaceTemplates() != null)
                              .filter(template -> template.getAllowedSubspaceTemplates()
                                                          .stream()
                                                          .map(subspaceId -> subspaceId.split(":")[0])
                                                          .filter(part -> !part.isEmpty() && NumberUtils.isCreatable(part))
                                                          .mapToLong(Long::parseLong)
                                                          .anyMatch(id -> id == subTemplateId))
                              .map(SpaceTemplate::getId)
                              .toList();
  }

  private SpaceTemplate createSpaceTemplateLayout(SpaceTemplate spaceTemplate,
                                                  SiteKey sourceSiteKey) throws ObjectNotFoundException {
    SiteKey targetSiteKey = SiteKey.groupTemplate(String.valueOf(spaceTemplate.getId()));
    userPortalConfigService.createSiteFromTemplate(sourceSiteKey, targetSiteKey);
    PortalConfig targetPortalConfig = layoutService.getPortalConfig(targetSiteKey);
    targetPortalConfig.setProperty(SPACE_TEMPLATE_SITE_PROP_NAME, "true");
    targetPortalConfig.setProperty(SPACE_TEMPLATE_ID_PROP_NAME, targetSiteKey.getName());
    layoutService.save(targetPortalConfig);
    spaceTemplate.setLayout(targetSiteKey.getName());
    return spaceTemplateStorage.updateSpaceTemplate(spaceTemplate);
  }

  private void computeSpaceTemplateAttributes(SpaceTemplate spaceTemplate, Locale locale) {
    spaceTemplate.setName(translationService.getTranslationLabelOrDefault(SpaceTemplateTranslationPlugin.OBJECT_TYPE,
                                                                          spaceTemplate.getId(),
                                                                          SpaceTemplateTranslationPlugin.NAME_FIELD_NAME,
                                                                          locale));
    spaceTemplate.setDescription(translationService.getTranslationLabelOrDefault(SpaceTemplateTranslationPlugin.OBJECT_TYPE,
                                                                                 spaceTemplate.getId(),
                                                                                 SpaceTemplateTranslationPlugin.DESCRIPTION_FIELD_NAME,
                                                                                 locale));
    spaceTemplate.setBannerFileId(getSpaceTemplateBannerId(spaceTemplate.getId()));
  }

  private boolean canViewTemplate(SpaceTemplate spaceTemplate, String username) {
    if (spaceTemplate == null || spaceTemplate.isDeleted()) {
      return false;
    } else if (userAcl.isAnonymousUser(username)) {
      return CollectionUtils.containsAny(spaceTemplate.getPermissions(), UserACL.EVERYONE);
    } else if (canManageTemplates(username)) {
      return true;
    } else if (!spaceTemplate.isEnabled()) {
      // Only when not manager,
      // checked in previous step
      return false;
    }
    Identity aclIdentity = userAcl.getUserIdentity(username);
    return aclIdentity != null && (spaceTemplate.getPermissions()
                                                .stream()
                                                .anyMatch(expression -> UserACL.EVERYONE.equals(expression)
                                                    || aclIdentity.isMemberOf(getMembershipEntry(expression)))
        || spaceTemplate.getAdminPermissions()
                        .stream()
                        .anyMatch(expression -> aclIdentity.isMemberOf(getMembershipEntry(expression))));
  }

  private MembershipEntry getMembershipEntry(String expression) {
    return expression.contains(":") ? MembershipEntry.parse(expression) : new MembershipEntry(expression);
  }

}
