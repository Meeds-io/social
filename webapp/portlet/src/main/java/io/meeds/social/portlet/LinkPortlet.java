package io.meeds.social.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.codec.binary.StringUtils;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;

import io.meeds.social.link.service.LinkService;

public class LinkPortlet extends GenericDispatchedViewPortlet {

  private static final String LINKS_NAME = "name";

  private Boolean             canModifySettings;

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    request.setAttribute("canEdit", canModifySettings());
    super.doView(request, response);
  }

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    PortletPreferences preferences = request.getPreferences();
    String linkName = request.getParameter(LINKS_NAME);
    if (preferences.getValue(LINKS_NAME, null) == null) {
      preferences.setValue(LINKS_NAME, linkName);
      preferences.store();
    }

    UIPage uiPage = Util.getUIPage();
    String pageId = uiPage.getPageId();
    LinkService linkService = ExoContainerContext.getService(LinkService.class);
    linkService.initLinkSetting(linkName, pageId);

    response.setPortletMode(PortletMode.VIEW);
  }

  protected boolean canModifySettings() {
    if (canModifySettings == null) {
      ConversationState current = ConversationState.getCurrent();
      if (current == null || current.getIdentity() == null
          || StringUtils.equals(current.getIdentity().getUserId(), IdentityConstants.ANONIM)) {
        return false;
      }
      UserACL userAcl = ExoContainerContext.getService(UserACL.class);
      UIPage uiPage = Util.getUIPage();
      SiteKey siteKey = Util.getUIPortal().getSiteKey();
      canModifySettings = uiPage != null
          && userAcl.hasEditPermissionOnPage(siteKey.getTypeName(), siteKey.getName(), uiPage.getEditPermission());
    }
    return canModifySettings.booleanValue();
  }

}
