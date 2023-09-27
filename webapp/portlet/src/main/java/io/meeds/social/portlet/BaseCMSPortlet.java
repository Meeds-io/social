package io.meeds.social.portlet;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;

public abstract class BaseCMSPortlet extends GenericDispatchedViewPortlet {

  protected static final Random             RANDOM      = new Random();

  protected static final String             NAME        = "name";

  private static final Map<String, Boolean> INITIALIZED = new ConcurrentHashMap<>();

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    request.setAttribute("canEdit", canEdit());
    String name = request.getPreferences().getValue("name", request.getWindowID());
    request.setAttribute("settingName", name);
    request.setAttribute("isInitialized", isInitialized(name));
    super.doView(request, response);
  }

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    PortletPreferences preferences = request.getPreferences();
    String name = getName(request, preferences);
    if (getName(preferences) == null) {
      preferences.setValue(NAME, name);
      preferences.store();
    }
    if (!isInitialized(name)) {
      saveSettingName(name, getPageReference());
      INITIALIZED.remove(name);
    }
    response.setPortletMode(PortletMode.VIEW);
  }

  /**
   * Saves associated portlet setting unique identifier (name) in specific
   * Portlet associated API store with the associated page reference
   * 
   * @param name Setting Name
   * @param pageReference {@link PageKey} identifier of current page where the
   *          application is instantiated
   */
  protected abstract void saveSettingName(String name, String pageReference);

  /**
   * Checks whether the setting name exists or not in internal Portlet API store
   * 
   * @param name Settin Name
   * @return true if exists else false
   */
  protected abstract boolean isSettingNameExists(String name);

  protected boolean canEdit() {
    ConversationState current = ConversationState.getCurrent();
    if (current == null || current.getIdentity() == null
        || StringUtils.equals(current.getIdentity().getUserId(), IdentityConstants.ANONIM)) {
      return false;
    }
    SiteKey siteKey = getSiteKey();
    return ExoContainerContext.getService(UserACL.class)
                              .hasEditPermissionOnPage(siteKey.getTypeName(), siteKey.getName(), getEditPermission());
  }

  protected SiteKey getSiteKey() {
    return Util.getUIPortal().getSiteKey();
  }

  protected String getEditPermission() {
    return Util.getUIPage().getEditPermission();
  }

  protected String getPageReference() {
    return Util.getUIPage().getPageId();
  }

  protected boolean isInitialized(String name) {
    if (StringUtils.isBlank(name) || !INITIALIZED.containsKey(name)) {
      INITIALIZED.put(name, StringUtils.isNotBlank(name) && isSettingNameExists(name));
    }
    return INITIALIZED.get(name).booleanValue();
  }

  private String getName(ActionRequest request, PortletPreferences preferences) {
    String name = getName(preferences);
    if (StringUtils.isBlank(name)) {
      name = request.getParameter(NAME);
      if (StringUtils.isBlank(name)) {
        name = generateRandomId();
      }
    }
    return name;
  }

  private String getName(PortletPreferences preferences) {
    return preferences.getValue(NAME, null);
  }

  private String generateRandomId() {
    String name;
    do {
      name = String.valueOf(RANDOM.nextLong());
    } while (!isSettingNameExists(name));
    return name;
  }

}
