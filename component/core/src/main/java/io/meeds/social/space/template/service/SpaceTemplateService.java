/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.core.space.SpacesAdministrationService;

import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.model.SpaceTemplateFilter;
import io.meeds.social.space.template.plugin.attachment.SpaceTemplateBannerAttachmentPlugin;
import io.meeds.social.space.template.plugin.translation.SpaceTemplateTranslationPlugin;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;
import io.meeds.social.translation.service.TranslationService;

@Service
public class SpaceTemplateService {

  public static final String          SPACE_TEMPLATE_CREATED_EVENT = "space.template.created";

  public static final String          SPACE_TEMPLATE_UPDATED_EVENT = "space.template.updated";

  public static final String          SPACE_TEMPLATE_DELETED_EVENT = "space.template.deleted";

  public static final String          DEFAULT_SITE_TEMPLATE        = "space";

  private static final Log            LOG                          = ExoLogger.getLogger(SpaceTemplateService.class);

  private TranslationService          translationService;

  private AttachmentService           attachmentService;

  private SpacesAdministrationService spacesAdministrationService;

  private UserACL                     userAcl;

  private SpaceTemplateStorage        spaceTemplateStorage;

  private UserPortalConfigService     userPortalConfigService;

  private LayoutService               layoutService;

  private ListenerService             listenerService;

  public SpaceTemplateService(TranslationService translationService,
                              AttachmentService attachmentService,
                              UserPortalConfigService userPortalConfigService,
                              LayoutService layoutService,
                              ListenerService listenerService,
                              UserACL userAcl,
                              SpacesAdministrationService spacesAdministrationService,
                              SpaceTemplateStorage spaceTemplateStorage) {
    this.userPortalConfigService = userPortalConfigService;
    this.translationService = translationService;
    this.attachmentService = attachmentService;
    this.userAcl = userAcl;
    this.spacesAdministrationService = spacesAdministrationService;
    this.spaceTemplateStorage = spaceTemplateStorage;
    this.layoutService = layoutService;
    this.listenerService = listenerService;
  }

  public List<SpaceTemplate> getSpaceTemplates() {
    return getSpaceTemplates(null, Pageable.unpaged(), false);
  }

  public List<SpaceTemplate> getSpaceTemplates(SpaceTemplateFilter spaceTemplateFilter, Pageable pageable, boolean expand) {
    if (spaceTemplateFilter != null
        && StringUtils.isBlank(spaceTemplateFilter.getUsername())) {
      return Collections.emptyList();
    } else {
      boolean includeDisabled = spaceTemplateFilter == null || spaceTemplateFilter.isIncludeDisabled();
      List<SpaceTemplate> spaceTemplates = includeDisabled ? spaceTemplateStorage.getSpaceTemplates(pageable) :
                                                           spaceTemplateStorage.getEnabledSpaceTemplates(pageable);
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
  }

  public SpaceTemplate getSpaceTemplate(long templateId) {
    return spaceTemplateStorage.getSpaceTemplate(templateId);
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
    if (expand) {
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

  public boolean canManageTemplates(String username) {
    return spacesAdministrationService.isSuperManager(username);
  }

  public boolean canViewTemplate(long templateId, String username) {
    SpaceTemplate spaceTemplate = getSpaceTemplate(templateId);
    return canViewTemplate(spaceTemplate, username);
  }

  public boolean canCreateSpace(long templateId, String username) {
    SpaceTemplate spaceTemplate = getSpaceTemplate(templateId);
    return canViewTemplate(spaceTemplate, username) && spaceTemplate.isEnabled();
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
    String layout = StringUtils.firstNonBlank(spaceTemplate.getLayout(), DEFAULT_SITE_TEMPLATE);
    if (layoutService.getPortalConfig(SiteKey.groupTemplate(layout)) == null) {
      throw new ObjectNotFoundException(String.format("Space Template layout '%s' wasn't found", layout));
    }

    SpaceTemplate spaceTemplateToCreate = spaceTemplate.clone();
    spaceTemplateToCreate.setSystem(false);
    spaceTemplateToCreate.setDeleted(false);
    spaceTemplateToCreate.setLayout(null);
    SpaceTemplate createdSpaceTemplate = spaceTemplateStorage.createSpaceTemplate(spaceTemplateToCreate);
    createdSpaceTemplate = createSpaceTemplateLayout(createdSpaceTemplate, layout);
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
    spaceTemplate.setDeleted(true);
    spaceTemplateStorage.updateSpaceTemplate(spaceTemplate);

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
    listenerService.broadcast(SPACE_TEMPLATE_DELETED_EVENT, spaceTemplate, spaceTemplate);
  }

  private SpaceTemplate createSpaceTemplateLayout(SpaceTemplate spaceTemplate,
                                                  String sourceLayout) throws ObjectNotFoundException {
    String targetLayout = String.valueOf(spaceTemplate.getId());
    userPortalConfigService.createSiteFromTemplate(SiteKey.groupTemplate(sourceLayout),
                                                   SiteKey.groupTemplate(targetLayout));
    spaceTemplate.setLayout(targetLayout);
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
    if (spaceTemplate == null
        || spaceTemplate.isDeleted()
        || userAcl.isAnonymousUser(username)) {
      return false;
    } else if (canManageTemplates(username)) {
      return true;
    } else if (!spaceTemplate.isEnabled()) {
      // Only when not manager,
      // checked in previous step
      return false;
    }
    Identity aclIdentity = userAcl.getUserIdentity(username);
    return aclIdentity != null
           && spaceTemplate.getPermissions()
                           .stream()
                           .anyMatch(expression -> aclIdentity.isMemberOf(getMembershipEntry(expression)));
  }

  private MembershipEntry getMembershipEntry(String expression) {
    return expression.contains(":") ? MembershipEntry.parse(expression) : new MembershipEntry(expression);
  }

}
