<%@page import="org.json.JSONObject"%>
<%@page import="java.util.Collections"%>
<%@page import="jakarta.servlet.http.HttpSession"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="org.apache.commons.collections.CollectionUtils"%>
<%@ page import="org.apache.commons.collections.MapUtils"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.web.ControllerContext"%>
<%@ page import="org.exoplatform.web.login.LoginHandler"%>
<%@ page import="org.exoplatform.web.login.UIParamsExtension"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<%
  String id = "externalOnboarding-" + renderRequest.getWindowID();
  JSONObject params = new JSONObject();

  // The registration state, read the way the register page reads it
  PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
  ControllerContext controllerContext = ((PortalRequestContext) PortalRequestContext.getCurrentInstance()).getControllerContext();
  List<UIParamsExtension> paramsExtensions = portalContainer.getComponentInstancesOfType(UIParamsExtension.class);
  if (CollectionUtils.isNotEmpty(paramsExtensions)) {
    paramsExtensions.stream()
                    .filter(extension -> extension.getExtensionNames().contains(LoginHandler.REGISTER_EXTENSION_NAME))
                    .forEach(extension -> {
                      Map<String, Object> extendedParams = extension.extendParameters(controllerContext,
                                                                                     LoginHandler.REGISTER_EXTENSION_NAME);
                      if (MapUtils.isNotEmpty(extendedParams) && Boolean.TRUE.equals(extendedParams.get("registerEnabled"))) {
                        try {
                          params.put("registerEnabled", true);
                        } catch (Exception e) {
                          // no registration state: the page keeps its way back to login
                        }
                      }
                    });
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light externalOnboarding"
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social/ExternalOnboarding'], app =>app.init('<%=id%>', <%=params.toString()%>));
    </script>
  </div>
</div>
