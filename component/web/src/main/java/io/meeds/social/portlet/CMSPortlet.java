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
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.webui.Utils;

import io.meeds.social.cms.service.CMSService;

public class CMSPortlet extends GenericDispatchedViewPortlet {

  protected static final Random             RANDOM               = new Random();

  protected static final String             NAME                 = "name";

  private static final String               PREFIX_UNTITLED_NAME = "Untitled";

  private static final Map<String, Boolean> INITIALIZED          = new ConcurrentHashMap<>();

  private static final Log                  LOG                  = ExoLogger.getLogger(CMSPortlet.class);

  private CMSService                        cmsService;

  private String                            contentType;

  @Override
  public void init(PortletConfig config) throws PortletException {
    super.init(config);
    contentType = config.getInitParameter("content-type");
  }

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    String name = getOrCreateSettingName(request.getPreferences());
    request.setAttribute("canEdit", canEdit());
    request.setAttribute("settingName", name);
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
    try {
      getCmsService().saveSettingName(contentType,
                                      name,
                                      pageReference,
                                      spaceId,
                                      identityId == null ? 0l : Long.parseLong(identityId));
    } catch (ObjectAlreadyExistsException e) {
      LOG.debug("CMS Setting {}/{} already exists", contentType, name, e);
    }
  }

  /**
   * Checks whether the setting name exists or not in internal Portlet API store
   * 
   * @param name Settin Name
   * @return true if exists else false
   */
  protected boolean isSettingNameExists(String name) {
    return getCmsService().isSettingNameExists(contentType, name);
  }

  protected final boolean canEdit() {
    return getCmsService().hasEditPermission(getCurrentAclIdentity(), getPageReference(), getSpaceId());
  }

  protected final SiteKey getSiteKey() {
    return Util.getUIPortal().getSiteKey();
  }

  protected final String getEditPermission() {
    return Util.getUIPage().getEditPermission();
  }

  protected final String getPageReference() {
    return Util.getUIPage().getPageId();
  }

  protected final long getSpaceId() {
    String spaceId = Utils.getSpaceIdByContext();
    return spaceId == null ? 0l : Long.parseLong(spaceId);
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
      saveSettingName(name, getPageReference(), getSpaceId());
      INITIALIZED.remove(name);
    }
    return name;
  }

  private final boolean isInitialized(String name) {
    if (StringUtils.isBlank(name)) {
      return false;
    } else {
      return INITIALIZED.computeIfAbsent(name, this::isSettingNameExists);
    }
  }

  private void savePreference(String name, String value) {
    String storageId = UIPortlet.getCurrentUIPortlet().getStorageId();
    LayoutService layoutService = ExoContainerContext.getService(LayoutService.class);
    Application<Portlet> applicationModel = layoutService.getApplicationModel(storageId);
    ApplicationState<Portlet> state = applicationModel.getState();
    Portlet prefs = layoutService.load(state, ApplicationType.PORTLET);
    prefs.setValue(name, value);
    layoutService.save(state, prefs);
  }

  private String generateRandomId() {
    String name;
    do {
      name = PREFIX_UNTITLED_NAME + String.valueOf(RANDOM.nextLong());
    } while (isSettingNameExists(name));
    return name;
  }

  private Identity getCurrentAclIdentity() {
    ConversationState conversationState = ConversationState.getCurrent();
    return conversationState == null ? null : conversationState.getIdentity();
  }

  public CMSService getCmsService() {
    if (cmsService == null) {
      cmsService = ExoContainerContext.getService(CMSService.class);
    }
    return cmsService;
  }

}
