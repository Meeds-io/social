package org.exoplatform.social.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;

public class LinkPortlet extends GenericDispatchedViewPortlet {

  private static final String LINKS_NAME = "name";

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    PortletPreferences preferences = request.getPreferences();
    if (preferences.getValue(LINKS_NAME, null) == null) {
      preferences.setValue(LINKS_NAME, request.getParameter(LINKS_NAME));
      preferences.store();
    }
    response.setPortletMode(PortletMode.VIEW);
  }

}
