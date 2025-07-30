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
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.services.security.ConversationState"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.portal.config.model.Page"%>
<%@ page import="org.exoplatform.portal.config.UserACL"%>
<%@ page import="io.meeds.social.translation.service.TranslationService" %>
<%@ page import="org.exoplatform.portal.localization.LocaleContextInfoUtils" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<%

  String id = "loginForm-" + renderRequest.getWindowID();


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
  params.put(RegisterHandler.REGISTER_ENABLED, securitySettingService.getRegistrationType() != UserRegistrationType.RESTRICTED);

  String portletStorageId = ((String) request.getAttribute("portletStorageId"));
  String settingName = ((String) request.getAttribute("settingName"));
  String translationIdentifier;
  Object translationIdentifierParam = request.getAttribute("translationIdentifier");
  translationIdentifier = translationIdentifierParam instanceof String[] ? ((String[]) translationIdentifierParam)[0]
            : ((String) translationIdentifierParam);
  Page currentPage = rcontext.getPage();
  boolean canEdit = ((boolean) request.getAttribute("canEdit"));
  String pageRef = currentPage.getPageKey().format();

  TranslationService translationService = CommonsUtils.getService(TranslationService.class);

  String welcomeBack = translationService.getTranslationLabelOrDefault("cmsPortlet",
            Long.parseLong(translationIdentifier), "welcomeBack", LocaleContextInfoUtils.getUserLocale(request.getRemoteUser()));

  String newHere = translationService.getTranslationLabelOrDefault("cmsPortlet",
            Long.parseLong(translationIdentifier), "newHere", LocaleContextInfoUtils.getUserLocale(request.getRemoteUser()));

  String createAccount = translationService.getTranslationLabelOrDefault("cmsPortlet",
            Long.parseLong(translationIdentifier), "createAccount", LocaleContextInfoUtils.getUserLocale(request.getRemoteUser()));

  String signinEmailButton = translationService.getTranslationLabelOrDefault("cmsPortlet",
            Long.parseLong(translationIdentifier), "signinEmailButton", LocaleContextInfoUtils.getUserLocale(request.getRemoteUser()));

  String signinOption = request.getAttribute("signinOption") == null ? "loginform" : ((String[]) request.getAttribute("signinOption"))[0];
  boolean displaySigninEmailButtonIcon = request.getAttribute("displaySigninEmailButtonIcon") == null ? true : Boolean.parseBoolean(((String[]) request.getAttribute("displaySigninEmailButtonIcon"))[0]);
  boolean listExternalProviders = request.getAttribute("listExternalProviders") == null ? true : Boolean.parseBoolean(((String[]) request.getAttribute("listExternalProviders"))[0]);
  boolean displayProvidersIcons = request.getAttribute("displayProvidersIcons") == null ? true : Boolean.parseBoolean(((String[]) request.getAttribute("displayProvidersIcons"))[0]);


  params.put("portletStorageId", portletStorageId);
  params.put("settingName", settingName);
  params.put("translationIdentifier", translationIdentifier);
  params.put("canEdit", canEdit);
  params.put("pageRef", pageRef);
  params.put("welcomeBack", welcomeBack);
  params.put("newHere", newHere);
  params.put("createAccount", createAccount);
  params.put("signinOption", signinOption);
  params.put("signinEmailButton", signinEmailButton);
  params.put("displaySigninEmailButtonIcon", displaySigninEmailButtonIcon);
  params.put("listExternalProviders", listExternalProviders);
  params.put("displayProvidersIcons", displayProvidersIcons);

%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light loginForm"
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social/LoginForm'], app =>app.init('<%=id%>',JSON.stringify(<%=params.toString()%>)));
    </script>
  </div>
</div>
