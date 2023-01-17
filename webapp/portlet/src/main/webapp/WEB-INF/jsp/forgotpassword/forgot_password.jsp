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
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page language="java"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.List"%>
<%@ page import="org.gatein.portal.controller.resource.ResourceRequestHandler"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.services.resources.LocaleConfig"%>
<%@ page import="org.exoplatform.services.resources.Orientation"%>
<%
  response.setCharacterEncoding("UTF-8");
  response.setContentType("text/html; charset=UTF-8");

  String contextPath = request.getContextPath();

  // Styles
  List<String> skinUrls = (List<String>) request.getAttribute("skinUrls");

  // Scripts
  List<String> headerScripts = (List<String>) request.getAttribute("headerScripts");
  Set<String> pageScripts = (Set<String>) request.getAttribute("pageScripts");
  String jsConfig = (String) request.getAttribute("jsConfig");
  String inlineScripts = (String) request.getAttribute("inlineScripts");

  // Branding
  String brandingPrimaryColor = (String) request.getAttribute("brandingPrimaryColor");
  String brandingThemeUrl = (String) request.getAttribute("brandingThemeUrl");

  // Locale
  LocaleConfig localeConfig = (LocaleConfig) request.getAttribute("localeConfig");
  String browserLanguage = localeConfig.getLocale().getLanguage();
  Orientation orientation = localeConfig.getOrientation();
  String direction = orientation.isLT() ? "ltr" : "rtl";

  PortalContainer portalContainer = PortalContainer.getInstance();
  ResourceBundleService resourceBundleService = portalContainer.getComponentInstanceOfType(ResourceBundleService.class);
  ResourceBundle res = resourceBundleService.getResourceBundle(resourceBundleService.getSharedResourceBundleNames(), localeConfig.getLocale());
%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
  xml:lang="<%=browserLanguage%>"
  lang="<%=browserLanguage%>"
  dir="<%=direction%>">
<head>
  <%-- Embedded Title that will change once page loaded --%>
  <title><%=res.getString("gatein.forgotPassword.title")%></title>
  <!-- Metadatas -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta charset="UTF-8">
  <meta name="theme-color" content="<%=brandingPrimaryColor%>" />
  <!-- Preload Styles & Fonts & Scripts for HTTP/2 optimizations -->
  <link rel="preload" href="/eXoSkin/skin/fonts/vuetify/flUhRq6tzZclQEJ-Vdg-IuiaDsNc.woff2" as="font" type="font/woff2" crossorigin />
  <link rel="preload" href="/eXoSkin/skin/fonts/vuetify/fa-solid-900.woff2" as="font" type="font/woff2" crossorigin />
  <link rel="preload" href="/eXoSkin/skin/fonts/vuetify/fa-regular-400.woff2" as="font" type="font/woff2" crossorigin />
  <link rel="preload" href="/eXoSkin/skin/fonts/vuetify/materialdesignicons-webfont.woff2?v=5.9.55" as="font" type="font/woff2" crossorigin />
  <link rel="preload" href="/eXoSkin/skin/fonts/Ionic/ionicons.ttf" as="font" type="font/ttf" crossorigin />
  <link rel="preload" href="/eXoSkin/skin/fonts/PLF-FONT-ICONS.ttf?-m9uidt" as="font" type="font/ttf" crossorigin />
  <link rel="preload" as="style" type="text/css" href="<%=brandingThemeUrl%>" />
  <% for(String skinUrl : skinUrls) { %>
  <link rel="preload" href= "<%=skinUrl%>" as="style" type="text/css" />
  <% } %>
  <% for (String url : pageScripts) { %>
  <link rel="preload" href= "<%=url%>" as="script" type="text/javascript" />
  <% } %>
  <!-- Styles -->
  <link rel="shortcut icon" type="image/x-icon" href="<%= request.getAttribute("brandingFavicon") %>" />
  <link id="brandingSkin" rel="stylesheet" type="text/css" href="<%=brandingThemeUrl%>" />
  <% for(String skinUrl : skinUrls) { %>
  <link rel="stylesheet" type="text/css" href="<%=skinUrl%>" />
  <% } %>
  <!-- Scripts -->
  <script type="text/javascript">
   var require = <%= jsConfig %>;
  </script>
  <% for (String url : headerScripts) { %>
  <script type="text/javascript" src="<%= url %>"></script>
  <% } %>
  <script type="text/javascript">
   require(['SHARED/bootstrap'], function() {
     eXo.env.portal.context = "<%=contextPath%>";
     eXo.env.portal.containerName = "<%=PortalContainer.getInstance().getName()%>";
     eXo.env.portal.language='<%= browserLanguage %>';
     eXo.env.portal.orientation='<%= direction %>';
     eXo.env.portal.rest = '<%= PortalContainer.getCurrentRestContextName() %>';
     eXo.env.server.context = "<%=contextPath%>";
     eXo.env.client.assetsVersion = "<%=ResourceRequestHandler.VERSION%>";
     eXo.env.portal.vuetifyPreset = {
       dark: true,
       silent: true,
       iconfont: 'mdi',
       rtl: eXo.env.portal.orientation === 'rtl',
       theme: { disable: true },
     };
     eXo.developing = <%=org.exoplatform.commons.utils.PropertyManager.isDevelopping()%>;
     <%=inlineScripts%>;
   });
  </script>
</head>
<body>
  <div class="VuetifyApp">
    <div id="forgotPasswordApplication"></div>
  </div>
</body>
</html>