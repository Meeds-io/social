<%--

    Copyright (C) 2009 eXo Platform SAS.
    
    This is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.
    
    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser General Public License for more details.
    
    You should have received a copy of the GNU Lesser General Public
    License along with this software; if not, write to the Free
    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    02110-1301 USA, or see the FSF site: http://www.fsf.org.

--%>

<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page import="org.exoplatform.portal.resource.SkinService"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.exoplatform.services.organization.User"%>
<%@ page import="java.util.Collection" %>
<%@ page import="org.exoplatform.portal.resource.SkinConfig" %>
<%@ page import="java.util.Locale" %>
<%@ page import="org.exoplatform.commons.utils.I18N" %>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService" %>
<%@ page import="org.exoplatform.portal.resource.config.tasks.PortalSkinTask" %>
<%@ page import="org.gatein.portal.controller.resource.ResourceRequestHandler" %>
<%@ page import="org.exoplatform.services.resources.LocaleConfigService"%>
<%@ page import="org.exoplatform.services.resources.LocaleConfig"%>
<%@ page import="org.exoplatform.services.resources.Orientation"%>

<%@ page language="java" %>
<%
    PortalContainer portalContainer = PortalContainer.getCurrentInstance(session.getServletContext());
    ResourceBundleService service = portalContainer.getComponentInstanceOfType(ResourceBundleService.class);
    Locale locale = (Locale)request.getAttribute("request_locale");
    if (locale == null) {
        locale = request.getLocale();
    }
    ResourceBundle res = service.getResourceBundle(service.getSharedResourceBundleNames(), locale);
    String contextPath = portalContainer.getPortalContext().getContextPath();

    SkinService skinService = PortalContainer.getCurrentInstance(session.getServletContext())
            .getComponentInstanceOfType(SkinService.class);

    UserPortalConfigService userPortalConfigService = portalContainer.getComponentInstanceOfType(UserPortalConfigService.class);
    String skinName = userPortalConfigService.getDefaultPortalSkinName();
    String loginCssPath = skinService.getSkin("portal/login", skinName).getCSSPath()+"?v="+ResourceRequestHandler.VERSION;
    String coreCssPath = skinService.getPortalSkin(PortalSkinTask.DEFAULT_MODULE_NAME, skinName).getCSSPath()+"?v="+ResourceRequestHandler.VERSION;
    String brandingCss = "/rest/v1/platform/branding/css?v="+ResourceRequestHandler.VERSION;

    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html; charset=UTF-8");

    String browserLanguage = request.getLocale() == null ? "en" : request.getLocale().getLanguage();
    LocaleConfigService localeConfigService = portalContainer.getComponentInstanceOfType(LocaleConfigService.class);
    LocaleConfig localeConfig = localeConfigService.getLocaleConfig(browserLanguage);
    String direction = localeConfig == null || localeConfig.getOrientation() != Orientation.RT ? "ltr" : "rtl";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%=browserLanguage%>" lang="<%=browserLanguage%>" dir="<%=direction%>">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= res.getString("gatein.forgotPassword.information") %></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link id="brandingSkin" rel="stylesheet" type="text/css" href="<%=brandingCss%>">
    <link rel="shortcut icon" type="image/x-icon"  href="<%=contextPath%>/favicon.ico" />
    <link href="<%=coreCssPath%>" rel="stylesheet" type="text/css"/>
    <link href="<%=loginCssPath%>" rel="stylesheet" type="text/css"/>
</head>
<body class="modal-open">
<div class="uiPopupWrapper">
    <div class="UIPopupWindow modal uiOauthInvitation uiPopup UIDragObject NormalStyle" style="width: 430px; margin-left: -215px; border-radius: 4px">
        <div class="popupHeader ClearFix">
            <a href="<%= contextPath + "/login"%>" class="uiIconClose pull-right" aria-hidden="true" ></a>
            <span class="PopupTitle popupTitle"><%= res.getString("gatein.forgotPassword.information") %></span>
        </div>
        <div class="PopupContent popupContent">
            <div class="content mgT5">
                <p><i class="uiIconInformation uiIconBlue"></i><%=res.getString("gatein.forgotPassword.linkExpired")%></p>
            </div>
            <div class="uiAction uiActionBorder">
                <a class="btn ActionButton LightBlueStyle" href="<%= contextPath + "/login"%>"><%=res.getString("gatein.forgotPassword.ok")%></a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
