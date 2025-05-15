package io.meeds.social.portlet;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import javax.portlet.*;
import java.io.IOException;

public class SpaceCreationPortlet extends GenericDispatchedViewPortlet {

  private UserACL userAcl;

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    if (!canModifySettings()) {
      throw new PortletException("User is not allowed to save settings");
    }

    PortletPreferences preferences = request.getPreferences();
    String settings = request.getParameter("settings");
    preferences.setValue("settings", settings);
    preferences.store();
  }

  private Identity getCurrentIdentity() {
    return ConversationState.getCurrent() == null ? null : ConversationState.getCurrent().getIdentity();
  }

  private boolean canModifySettings() {
    Page currentPage = getCurrentPage();
    return currentPage != null && getUserAcl().hasEditPermission(currentPage, getCurrentIdentity());
  }

  private UserACL getUserAcl() {
    if (userAcl == null) {
      userAcl = ExoContainerContext.getService(UserACL.class);
    }
    return userAcl;
  }

  private Page getCurrentPage() {
    PortalRequestContext requestContext = PortalRequestContext.getCurrentInstance();
    return requestContext == null ? null : requestContext.getPage();
  }
}
