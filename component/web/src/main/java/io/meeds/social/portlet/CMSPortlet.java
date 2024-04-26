/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.social.portlet;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.webui.Utils;

import io.meeds.social.cms.service.CMSService;

public class CMSPortlet extends GenericDispatchedViewPortlet {

  protected static final String             NAME                 = "name";

  private static final String               PREFIX_UNTITLED_NAME = "Untitled";

  private static final StampedLock          LOCK                 = new StampedLock();

  private static final Map<String, Boolean> INITIALIZED          = new ConcurrentHashMap<>();

  private static final Log                  LOG                  = ExoLogger.getLogger(CMSPortlet.class);

  protected String                          contentType;

  private CMSService                        cmsService;

  @Override
  public void init(PortletConfig config) throws PortletException {
    super.init(config);
    contentType = config.getInitParameter("content-type");
  }

  @Override
  public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    String name = getOrCreateSettingName(request.getPreferences());
    request.setAttribute("canEdit", canEdit(name, getCurrentAclIdentity()));
    request.setAttribute("settingName", name);
    setViewRequestAttributes(name, request, response);
    super.doView(request, response);
  }

  /**
   * Saves associated portlet setting unique identifier (name) in specific
   * Portlet associated API store with the associated page reference
   * 
   * @param name Setting Name
   * @param pageReference {@link PageKey} identifier of current page where the
   *          application is instantiated
   * @param spaceId Space where the page is displayed
   */
  protected void saveSettingName(String name, String pageReference, long spaceId) {
    String identityId = Utils.getViewerIdentityId();
    long stamp = -1;
    try {
      stamp = LOCK.tryWriteLock(2, TimeUnit.SECONDS);
    } catch (Exception e) { // NOSONAR
      LOG.debug("Unable to acquire lock before saving setting", e);
    }
    try {
      if (identityId == null) {
        identityId = getSuperUserIdentityId();
      }
      getCmsService().saveSettingName(contentType,
                                      name,
                                      pageReference,
                                      spaceId,
                                      identityId == null ? 0l : Long.parseLong(identityId));
    } catch (ObjectAlreadyExistsException e) {
      LOG.debug("CMS Setting {}/{} already exists", contentType, name, e);
    } finally {
      INITIALIZED.remove(name);
      if (stamp >= 0) {
        LOCK.unlock(stamp);
      }
    }
  }

  /**
   * Checks whether the setting name exists or not in internal Portlet API store
   * 
   * @param name Setting Name
   * @return true if exists else false
   */
  protected boolean isSettingNameExists(String name) {
    return getCmsService().isSettingNameExists(contentType, name);
  }

  /**
   * @param name Setting Name
   * @param userAclIdentity Current User {@link Identity}
   * @return true if Setting Type and Name are editable by current user, else
   *         false
   */
  protected boolean canEdit(String name, Identity userAclIdentity) {
    return getCmsService().hasEditPermission(getCurrentAclIdentity(), contentType, name);
  }

  protected void setViewRequestAttributes(String name, RenderRequest request, RenderResponse response) {
    // No default behavior
  }

  protected void preSettingInit(PortletPreferences preferences, String name) {
    // No default behavior
  }

  protected void postSettingInit(PortletPreferences preferences, String name) {
    // No default behavior
  }

  protected void savePreference(String name, String value) {
    String storageId = UIPortlet.getCurrentUIPortlet().getStorageId();
    LayoutService layoutService = ExoContainerContext.getService(LayoutService.class);
    Application<Portlet> applicationModel = layoutService.getApplicationModel(storageId);
    ApplicationState<Portlet> state = applicationModel.getState();
    Portlet prefs = layoutService.load(state, ApplicationType.PORTLET);
    prefs.setValue(name, value);
    layoutService.save(state, prefs);
  }

  private String getOrCreateSettingName(PortletPreferences preferences) {
    String name = preferences.getValue(NAME, null);
    if (!isInitialized(name)) {
      if (name == null) {
        name = generateRandomId();
        // In view mode, can't use preferences.store()
        // Thus use storageId
        savePreference(NAME, name);
      }
      preSettingInit(preferences, name);
      saveSettingName(name, getCurrentPageReference(), getCurrentSpaceId());
      postSettingInit(preferences, name);
    }
    return name;
  }

  private boolean isInitialized(String name) {
    if (StringUtils.isBlank(name)) {
      return false;
    } else {
      return INITIALIZED.computeIfAbsent(name, this::isSettingNameExists);
    }
  }

  private String generateRandomId() {
    String storageId = UIPortlet.getCurrentUIPortlet() == null ? "" : UIPortlet.getCurrentUIPortlet().getStorageId();
    String name;
    do {
      name = PREFIX_UNTITLED_NAME + "-" + storageId + "-" + UUID.randomUUID().toString();
    } while (isSettingNameExists(name));
    return name;
  }

  private CMSService getCmsService() {
    if (cmsService == null) {
      cmsService = ExoContainerContext.getService(CMSService.class);
    }
    return cmsService;
  }

  private String getSuperUserIdentityId() {
    UserACL userAcl = ExoContainerContext.getService(UserACL.class);
    IdentityManager identityManager = ExoContainerContext.getService(IdentityManager.class);
    String superUser = userAcl.getSuperUser();
    return identityManager.getOrCreateUserIdentity(superUser).getId();
  }

  private Identity getCurrentAclIdentity() {
    ConversationState conversationState = ConversationState.getCurrent();
    return conversationState == null ? null : conversationState.getIdentity();
  }

  private String getCurrentPageReference() {
    return Util.getUIPage().getPageId();
  }

  private long getCurrentSpaceId() {
    String spaceId = Utils.getSpaceIdByContext();
    return spaceId == null ? 0l : Long.parseLong(spaceId);
  }

}
