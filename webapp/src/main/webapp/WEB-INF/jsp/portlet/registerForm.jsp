<%@page import="org.json.JSONObject"%>
<%@page import="java.util.Collections"%>
<%@page import="jakarta.servlet.http.HttpSession"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.web.login.UIParamsExtension"%>
<%@ page import="org.apache.commons.collections.CollectionUtils"%>
<%@ page import="org.exoplatform.web.login.LoginHandler"%>
<%@page import="org.apache.commons.collections.MapUtils"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.web.ControllerContext"%>
<%@page import="org.json.JSONArray"%>

<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<%
  String id = "registerForm-" + renderRequest.getWindowID();
  JSONObject params = new JSONObject();

  PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
  String contextPath = portalContainer.getPortalContext().getContextPath();
  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  ControllerContext controllerContext = rcontext.getControllerContext();

  List<UIParamsExtension> paramsExtensions = portalContainer.getComponentInstancesOfType(UIParamsExtension.class);
  if (CollectionUtils.isNotEmpty(paramsExtensions)) {
   paramsExtensions.stream()
                   .filter(extension -> extension.getExtensionNames().contains(LoginHandler.REGISTER_EXTENSION_NAME))
                   .forEach(paramsExtension -> {
                     Map<String, Object> extendedParams = paramsExtension.extendParameters(controllerContext,LoginHandler.REGISTER_EXTENSION_NAME);

                     if (MapUtils.isNotEmpty(extendedParams)) {
                       extendedParams.forEach((key, value) -> {
                         try {
                           if (key.equals("extendedAuthProviderType")) {
                             if (params.has("extendedAuthProviderType")) {
                               params.getJSONArray("extendedAuthProviderType").put(value);
                             } else {
                               JSONArray array = new JSONArray();
                               array.put(value);
                               params.put("extendedAuthProviderType", array);
                             }
                           } else {
                             params.put(key, value);
                           }
                         } catch (Exception e) {
                           // Handle potential JSON exceptions
                         }
                       });
                     }
                   });
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light registerForm"
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social/RegisterForm'], app =>app.init('<%=id%>',JSON.stringify(<%=params.toString()%>)));
    </script>
  </div>
</div>
