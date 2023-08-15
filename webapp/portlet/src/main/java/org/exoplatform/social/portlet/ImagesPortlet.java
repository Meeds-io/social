package org.exoplatform.social.portlet;

import org.apache.commons.lang3.StringUtils;

import javax.portlet.*;
import java.io.IOException;
import java.util.Enumeration;

public class ImagesPortlet extends GenericPortlet {

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
    PortletPreferences preferences = request.getPreferences();
    Enumeration<String> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String name = parameterNames.nextElement();
      if (StringUtils.equals(name, "action") || StringUtils.contains(name, "portal:")) {
        continue;
      }
      String value = request.getParameter(name);
      preferences.setValue(name, value);
    }
    preferences.store();
  }

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/portlet/portletBanner.jsp");
    dispatcher.forward(request, response);
  }

}
