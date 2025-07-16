<%@ page import="org.exoplatform.web.login.recovery.PasswordRecoveryService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.web.login.UIParamsExtension"%>
<%@ page import="org.apache.commons.collections.CollectionUtils"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="org.exoplatform.web.login.LoginHandler"%>
<%@page import="org.exoplatform.web.ControllerContext"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.apache.commons.collections.MapUtils"%>
<%@page import="io.meeds.portal.security.service.SecuritySettingService"%>
<%@page import="org.exoplatform.web.register.RegisterHandler"%>
<%@page import="io.meeds.portal.security.constant.UserRegistrationType"%>
<%@page import="org.json.JSONObject"%>

<%

  JSONObject params = new JSONObject();

  PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
  String contextPath = portalContainer.getPortalContext().getContextPath();

  PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
  ControllerContext controllerContext = rcontext.getControllerContext();

  PasswordRecoveryService passwordRecoveryService = CommonsUtils.getService(PasswordRecoveryService.class);
  String forgotPasswordPath = contextPath + passwordRecoveryService.getPasswordRecoverURL(null, null);
  params.put("forgotPasswordPath", forgotPasswordPath);

  SecuritySettingService securitySettingService = CommonsUtils.getService(SecuritySettingService.class);

  List<UIParamsExtension> paramsExtensions = portalContainer.getComponentInstancesOfType(UIParamsExtension.class);
  if (CollectionUtils.isNotEmpty(paramsExtensions)) {
    paramsExtensions.stream()
                    .filter(extension -> extension.getExtensionNames().contains(LoginHandler.LOGIN_EXTENSION_NAME))
                    .forEach(paramsExtension -> {
                      Map<String, Object> extendedParams = paramsExtension.extendParameters(controllerContext,
                                                                                            LoginHandler.LOGIN_EXTENSION_NAME);
                      if (MapUtils.isNotEmpty(extendedParams)) {
                        extendedParams.forEach((key, value) -> {
                          try {
                            params.put(key, value);
                          } catch (Exception e) {
                            // Handle potential JSON exceptions
                          }
                        });
                      }
                    });
  }
  // Force disabling Register Form when the platform access is restricted
  if (securitySettingService.getRegistrationType() == UserRegistrationType.RESTRICTED) {
    params.put(RegisterHandler.REGISTER_ENABLED, false);
  }

%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light loginForm"
    id="loginFormApplication">
    <script type="text/javascript">
      require(['PORTLET/social/LoginForm'], app =>app.init(JSON.stringify(<%=params.toString()%>)));
    </script>
  </div>
</div>
