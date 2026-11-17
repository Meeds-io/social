/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.space.security.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.portal.security.constant.UserRegistrationType;
import io.meeds.portal.security.service.SecuritySettingService;
import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.security.SpaceSecurityAudit;
import io.meeds.social.space.security.exception.SpaceSecurityChangeNotAppliedException;
import io.meeds.social.space.security.exception.SpaceSecurityRefusedException;
import io.meeds.social.space.security.model.SpaceSecuritySettings;
import io.meeds.social.space.security.model.SpaceSettingsChange;
import io.meeds.social.space.security.model.SpaceSettingsChangeResult;
import io.meeds.social.space.security.plugin.SpaceSecuritySettingsPlugin;
import io.meeds.social.space.service.SpaceExtendedPropertiesNotifier;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;

@Service
public class SpaceSecurityServiceImpl implements SpaceSecurityService, ApplicationContextAware {

  public static final String        NOT_ALLOWED_MESSAGE_CODE = "space.security.notAllowed";

  public static final String        SOURCE_SPACE_SETTINGS    = "spaceSettings";

  private static final Log          LOG                      = ExoLogger.getLogger(SpaceSecurityServiceImpl.class);

  private final SpaceStorage        spaceStorage;

  private final SpaceTemplateStorage spaceTemplateStorage;

  private final SpaceTemplateService spaceTemplateService;

  private final SpaceService        spaceService;

  private final UserACL             userAcl;

  private SecuritySettingService    securitySettingService;

  private ApplicationContext        applicationContext;

  public SpaceSecurityServiceImpl(SpaceStorage spaceStorage,
                                  SpaceTemplateStorage spaceTemplateStorage,
                                  SpaceTemplateService spaceTemplateService,
                                  SpaceService spaceService,
                                  UserACL userAcl) {
    this.spaceStorage = spaceStorage;
    this.spaceTemplateStorage = spaceTemplateStorage;
    this.spaceTemplateService = spaceTemplateService;
    this.spaceService = spaceService;
    this.userAcl = userAcl;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public SpaceSecuritySettings getSettings(long spaceId) throws ObjectNotFoundException {
    return SpaceSecuritySettings.fromProperties(spaceStorage.getSpaceExtendedProperties(spaceId));
  }

  @Override
  public SpaceSecuritySettings getSettings(long spaceId, String username) throws ObjectNotFoundException,
                                                                         IllegalAccessException {
    checkCanView(getExistingSpace(spaceId), username);
    return getSettings(spaceId);
  }

  @Override
  public SpaceSecuritySettings getDisplaySettings(Space space) {
    return SpaceSecuritySettings.fromProperties(space == null ? null : space.getExtendedProperties());
  }

  @Override
  public SpaceSecuritySettings getTemplateSettings(long templateId) throws ObjectNotFoundException {
    return SpaceSecuritySettings.fromProperties(spaceTemplateStorage.getTemplateExtendedProperties(templateId));
  }

  @Override
  public void checkCanUpdateSettings(long spaceId,
                                     SpaceSecuritySettings changes,
                                     String username) throws ObjectNotFoundException, IllegalAccessException {
    Space space = getExistingSpace(spaceId);
    if (changes == null || changes.toProperties().isEmpty()) {
      checkCanView(space, username);
    } else {
      checkCanUpdateSettings(space, changes, username);
    }
  }

  @Override
  public SpaceSecuritySettings updateSettings(long spaceId,
                                              SpaceSecuritySettings changes,
                                              String username) throws ObjectNotFoundException, IllegalAccessException {
    Map<String, String> changedProperties = changes == null ? Collections.emptyMap() : changes.toProperties();
    if (changedProperties.isEmpty()) {
      return getSettings(spaceId, username);
    }
    Space space = getExistingSpace(spaceId);
    checkCanUpdateSettings(space, changes, username);

    SpaceSecuritySettings currentSettings = getSettings(spaceId);
    if (changes.getKeepDocumentsInside() != null
        && !Objects.equals(changes.getKeepDocumentsInside(), currentSettings.getKeepDocumentsInside())) {
      beforeDocumentsKeptInsideChange(space, changes.getKeepDocumentsInside(), username);
    }

    SpaceSettingsChangeResult result = spaceStorage.applySettingsChange(spaceId, new SpaceSettingsChange(changedProperties));
    reportChanges(spaceId, result, username, SOURCE_SPACE_SETTINGS);
    return SpaceSecuritySettings.fromProperties(result.stored());
  }

  @Override
  public boolean isDocumentsKeptInside(SpaceSecuritySettings settings) {
    return settings != null && settings.isKeepDocumentsInsideOn();
  }

  @Override
  public boolean canManageDocumentsAccess(Space space, SpaceSecuritySettings settings, String username) {
    if (settings == null || !settings.isLimitAccessManagementOn()) {
      return true;
    } else if (space == null || StringUtils.isBlank(username)) {
      return false;
    }
    Set<SpaceMembershipStatus> statuses = getMemberStatuses(space.getSpaceId(), username);
    return statuses.contains(SpaceMembershipStatus.MANAGER)
           || statuses.contains(SpaceMembershipStatus.PUBLISHER)
           || isSuperManager(space, username);
  }

  @Override
  public boolean canManageSpace(Space space, String username) {
    if (space == null || StringUtils.isBlank(username)) {
      return false;
    }
    return getMemberStatuses(space.getSpaceId(), username).contains(SpaceMembershipStatus.MANAGER)
           || isSuperManager(space, username);
  }

  @Override
  public Set<SpaceMembershipStatus> getMemberStatuses(long spaceId, String username) {
    return spaceStorage.getMemberStatuses(spaceId, username);
  }

  @Override
  public Set<String> getCommittedMembers(long spaceId, Collection<String> usernames) {
    return spaceStorage.getMembersAmong(spaceId, usernames);
  }

  @Override
  public boolean isExternalFeatureEnabled() {
    SecuritySettingService settingService = getSecuritySettingService();
    return settingService.getRegistrationType() == UserRegistrationType.OPEN
           || settingService.isRegistrationExternalUser();
  }

  @Override
  public boolean isExternalMemberAllowed(SpaceSecuritySettings settings) {
    return !isExternalFeatureEnabled() || settings == null || !Boolean.FALSE.equals(settings.getExternalMembersAllowed());
  }

  private Space getExistingSpace(long spaceId) throws ObjectNotFoundException {
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException("Space with id " + spaceId + " doesn't exist");
    }
    return space;
  }

  private void checkCanView(Space space, String username) throws IllegalAccessException {
    if (StringUtils.isBlank(username) || !spaceService.canViewSpace(space, username)) {
      throw new SpaceSecurityRefusedException(NOT_ALLOWED_MESSAGE_CODE);
    }
  }

  /**
   * Decides on the committed rows, which a space admin or a platform
   * administrator, who both view the space, passes
   */
  private void checkCanUpdateSettings(Space space, SpaceSecuritySettings changes, String username) throws IllegalAccessException {
    boolean documentsSettingChanged = changes.getLimitAccessManagement() != null || changes.getKeepDocumentsInside() != null;
    if (documentsSettingChanged && !canManageSpace(space, username)) {
      throw new SpaceSecurityRefusedException(NOT_ALLOWED_MESSAGE_CODE);
    }
    if (changes.getExternalMembersAllowed() != null && !isPlatformAdministrator(username)) {
      throw new SpaceSecurityRefusedException(NOT_ALLOWED_MESSAGE_CODE);
    }
  }

  private void beforeDocumentsKeptInsideChange(Space space, boolean newValue, String username) {
    if (applicationContext == null) {
      return;
    }
    for (SpaceSecuritySettingsPlugin plugin : applicationContext.getBeansOfType(SpaceSecuritySettingsPlugin.class).values()) {
      try {
        plugin.beforeDocumentsKeptInsideChange(space, newValue, username);
      } catch (Exception e) {
        LOG.warn("Change of the setting {} of space {} to {} not applied: {} failed",
                 SpaceSecuritySettings.KEEP_DOCUMENTS_INSIDE_KEY,
                 space.getId(),
                 newValue,
                 plugin.getClass().getName(),
                 e);
        throw new SpaceSecurityChangeNotAppliedException(e);
      }
    }
  }

  private void reportChanges(long spaceId, SpaceSettingsChangeResult result, String username, String source) {
    SpaceSecuritySettings previousSettings = SpaceSecuritySettings.fromProperties(result.previous());
    SpaceSecuritySettings storedSettings = SpaceSecuritySettings.fromProperties(result.stored());
    Map<String, String> previousValues = previousSettings.toProperties();
    Map<String, String> storedValues = storedSettings.toProperties();
    List<String> changedKeys = new ArrayList<>();
    for (String key : SpaceSecuritySettings.KEYS) {
      if (!Objects.equals(previousValues.get(key), storedValues.get(key))) {
        changedKeys.add(key);
        SpaceSecurityAudit.settingChanged(spaceId, key, previousValues.get(key), storedValues.get(key), username, source);
      }
    }
    if (!changedKeys.isEmpty()) {
      Space space = spaceService.getSpaceById(spaceId);
      if (space != null) {
        notifyExtendedPropertiesUpdated(space, changedKeys, username);
      }
    }
  }

  private void notifyExtendedPropertiesUpdated(Space space, List<String> changedKeys, String username) {
    if (spaceService instanceof SpaceExtendedPropertiesNotifier notifier) {
      notifier.extendedPropertiesUpdated(space, changedKeys, username);
    } else {
      LOG.warn("Change of the settings {} of space {} not reported: {} reports no extended properties change",
               changedKeys,
               space.getId(),
               spaceService.getClass().getName());
    }
  }

  private boolean isSuperManager(Space space, String username) {
    return isPlatformAdministrator(username) || spaceTemplateService.isTemplateAdministrator(space.getTemplateId(), username);
  }

  private boolean isPlatformAdministrator(String username) {
    return userAcl.isAdministrator(userAcl.getUserIdentity(username));
  }

  private SecuritySettingService getSecuritySettingService() {
    if (securitySettingService == null) {
      securitySettingService = ExoContainerContext.getService(SecuritySettingService.class);
    }
    return securitySettingService;
  }

}
