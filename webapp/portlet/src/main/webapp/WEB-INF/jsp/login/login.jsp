<%--

 This file is part of the Meeds project (https://meeds.io/).
 Copyright (C) 2022 Meeds Association
 contact@meeds.io
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

--%>
<%@page import="org.exoplatform.web.ControllerContext"%>
<%@page import="org.exoplatform.portal.resource.SkinConfig"%>
<%@page import="org.exoplatform.portal.resource.SkinService"%>
<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="javax.servlet.http.Cookie"%>
<%@ page import="org.exoplatform.web.login.LoginError"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page
  import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page import="org.gatein.security.oauth.spi.OAuthProviderType"%>
<%@ page
  import="org.gatein.security.oauth.spi.OAuthProviderTypeRegistry"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.exoplatform.services.resources.LocaleConfigService"%>
<%@ page import="org.exoplatform.services.resources.LocaleConfig"%>
<%@ page import="org.exoplatform.services.resources.Orientation"%>
<%@ page import="org.gatein.common.text.EntityEncoder"%>
<%@ page language="java"%>
<%
  String contextPath = request.getContextPath() ;
  ControllerContext controllerContext = (ControllerContext) request.getAttribute("controllerContext");

  String username = request.getParameter("username");
  if(username == null) {
    username = "";
  } else {
    EntityEncoder encoder = EntityEncoder.FULL;
    username = encoder.encode(username);
  }

  PortalContainer portalContainer = PortalContainer.getInstance();
  BrandingService brandingService = portalContainer.getComponentInstanceOfType(BrandingService.class);
  String brandingPrimaryColor = brandingService.getThemeColors().get("primaryColor");
  String brandingThemeUrl = "/" + PortalContainer.getCurrentPortalContainerName() + "/" + PortalContainer.getCurrentRestContextName() + "/v1/platform/branding/css?v=" + brandingService.getLastUpdatedTime();
  String logo = "";
  if (brandingService.getLogo() != null) {
    byte[] logoData = brandingService.getLogo().getData();
    byte[] encodedLogoData = Base64.getEncoder().encode(logoData);
    logo = "data:image/png;base64," + new String(encodedLogoData, "UTF-8");
  }

  SkinService skinService = portalContainer.getComponentInstanceOfType(SkinService.class);
  String skinName = skinService.getDefaultSkin();
  SkinConfig skin = skinService.getSkin("portal/login", skinName);
  String loginCssPath = "";
  if(skin != null) {
    loginCssPath = skin.getCSSPath()+"?v="+ResourceRequestHandler.VERSION;
  } else {
    loginCssPath = skinService.getSkin("portal/login", "Enterprise").getCSSPath()+"?v="+ResourceRequestHandler.VERSION;
  }

  Collection<SkinConfig> portalSkins = new ArrayList<>(skinService.getPortalSkins(skinName));
  Collection<SkinConfig> customSkins = new ArrayList<>(skinService.getCustomPortalSkins(skinName));

  ResourceBundleService service = portalContainer.getComponentInstanceOfType(ResourceBundleService.class);
  ResourceBundle res = service.getResourceBundle(service.getSharedResourceBundleNames(), request.getLocale()) ;

  PasswordRecoveryService passRecoveryServ = portalContainer.getComponentInstanceOfType(PasswordRecoveryService.class);
  String forgotPasswordPath = passRecoveryServ.getPasswordRecoverURL(null, null);

  //
  String email = request.getParameter("email") != null ? request.getParameter("email") : "";
  String uri = (String)request.getAttribute("org.gatein.portal.login.initial_uri");
  boolean error = request.getAttribute("org.gatein.portal.login.error") != null;
  boolean manyUsersWithSameEmailError = request.getAttribute("org.gatein.portal.manyUsersWithSameEmail.error") != null;
  String errorParam = (String)request.getParameter(org.exoplatform.web.login.LoginError.ERROR_PARAM);
  LoginError errorData = null;
  if (errorParam != null) {
      errorData = LoginError.parse(errorParam);
  }
  
  response.setCharacterEncoding("UTF-8"); 
  response.setContentType("text/html; charset=UTF-8");

  String browserLanguage = request.getLocale() == null ? "en" : request.getLocale().getLanguage();
  LocaleConfigService localeConfigService = portalContainer.getComponentInstanceOfType(LocaleConfigService.class);
  LocaleConfig localeConfig = localeConfigService.getLocaleConfig(browserLanguage);
  Orientation orientation = localeConfig == null ? Orientation.LT : localeConfig.getOrientation();
  String direction = orientation.isLT() ? "ltr" : "rtl";
%>
<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
  xml:lang="<%=browserLanguage%>" lang="<%=browserLanguage%>"
  dir="<%=direction%>">
<head>
<title><%=res.getString("UILoginForm.label.Signin")%></title>
  <!-- Metadatas -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="theme-color" content="<%=brandingPrimaryColor%>" />
  <link rel="shortcut icon" type="image/x-icon" href="<%=contextPath%>/favicon.ico" />

  <!-- Styles -->
  <link id="brandingSkin" rel="stylesheet" type="text/css" href="<%=brandingThemeUrl%>">
  <% for(SkinConfig skinConfig : portalSkins) {
       def url = skinConfig.createURL(controllerContext);
       url.setOrientation(orientation); %>
  <link id="${skinConfig.id}" rel="stylesheet" type="text/css" href="$url" skin-type="portal-skin" />
  <% } %>
  <link href="<%=loginCssPath%>" rel="stylesheet" type="text/css"/>
  <% for(SkinConfig customSkin : customSkins) {
       def url = customSkin.createURL(controllerContext);
       url.setOrientation(orientation); %>
  <link id="${customSkin.id}" rel="stylesheet" type="text/css" href="$url" skin-type="custom-skin" />
  <% } %>
</head>
<body>
  <div class="VuetifyApp">
    <div id="loginApplication"></div>
  </div>
</body>
</html>
