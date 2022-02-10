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
<%@ page import="org.exoplatform.services.organization.impl.UserImpl" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.exoplatform.web.controller.QualifiedName" %>
<%@ page import="org.exoplatform.web.login.recovery.PasswordRecoveryService" %>
<%@ page import="org.exoplatform.portal.resource.SkinConfig" %>
<%@ page import="java.util.Collection" %>
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

    String username = (String)request.getAttribute("username");
    String tokenId = (String)request.getAttribute("tokenId");

    String password = (String)request.getAttribute("password");
    String password2 = (String)request.getAttribute("password2");

    PasswordRecoveryService passRecoveryServ = portalContainer.getComponentInstanceOfType(PasswordRecoveryService.class);
    String forgotPasswordPath = passRecoveryServ.getPasswordRecoverURL(tokenId, I18N.toTagIdentifier(locale));


    List<String> errors = (List<String>)request.getAttribute("errors");
    String success = (String)request.getAttribute("success");
    if (errors == null) {
        errors = new ArrayList<String>();
    }

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
    <title><%=res.getString("gatein.forgotPassword.changePass")%></title>
    <%if (success != null && !success.isEmpty()) {%>
        <meta http-equiv="refresh" content="5; url=<%=contextPath+ "/login"%>" />
    <%}%>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="shortcut icon" type="image/x-icon"  href="<%=contextPath%>/favicon.ico" />
    <link id="brandingSkin" rel="stylesheet" type="text/css" href="<%=brandingCss%>">

    <link href="<%=coreCssPath%>" rel="stylesheet" type="text/css"/>
    <link href="<%=loginCssPath%>" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="/eXoResources/javascript/jquery-3.2.1.js"></script>
    <script type="text/javascript" src="/eXoResources/javascript/eXo/webui/FormValidation.js"></script>
</head>
<body class="modal-open">
<div class="uiPopupWrapper">
    <div class="UIPopupWindow uiPopup uiForgotPassword UIDragObject NormalStyle">
        <div class="popupHeader ClearFix">
            <span class="popupTitle center"><%=res.getString("gatein.forgotPassword.changePass")%></span>
        </div>
        <div class="popupContent">
            <% if (errors.size() > 0) { %>
            <div class="alertForm">
                <div class="alert alert-error mgT0 mgB20">
                    <ul>
                        <% for (String error : errors) { %>
                        <li><i class="uiIconError"></i><span><%=error%></span></li>
                        <%}%>
                    </ul>
                </div>
            </div>
            <%} else if (success != null && !success.isEmpty()) {%>
            <div class="alertForm">
                <div class="alert alert-success">
                    <i class="uiIconSuccess"></i><%=success%>
                </div>
            </div>
            <%}%>
            <form name="registerForm" action="<%= contextPath + forgotPasswordPath %>" method="post" style="margin: 0px;" autocomplete="off">
                <div class="content">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="control-label"><%=res.getString("portal.login.Username")%>:</label>
                            <div class="controls">
                                <input class="username" data-validation="require" name="username" type="text" value="<%=username%>" readonly="readonly" />
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label"><%=res.getString("gatein.forgotPassword.newPassword")%>:</label>
                            <div class="controls">
                                <input class="password" data-validation="require" name="password" type="password" autocomplete="off" value="<%=(password != null ? password : "")%>" /><span class="star">*</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label"><%=res.getString("gatein.forgotPassword.confirmNewPassword")%></label>
                            <div class="controls">
                                <input class="password" data-validation="require" name="password2" type="password" autocomplete="off" value="<%=(password2 != null ? password2 : "")%>" /><span class="star">*</span>
                            </div>
                        </div>
                        <input type="hidden" name="action" value="resetPassword"/>
                    </div>
                </div>
                <div id="UIPortalLoginFormAction" class="uiAction">
                    <button type="submit" class="btn btn-primary disabled" disabled="disabled"><%=res.getString("gatein.forgotPassword.save")%></button>
                    <a href="<%=contextPath+ "/login"%>" class="btn ActionButton LightBlueStyle"><%=res.getString("gatein.forgotPassword.close")%></a>
                </div>
            </form>
        </div>
    </div>
</div>
<script type="text/javascript">
  var $form = $('form[name="registerForm"]');
  $form.on('formValidate', function(e, valid) {
    var $btnSubmit = $form.find('.btn[type="submit"]');
    if (valid) {            
      $btnSubmit.attr('disabled', false).removeClass('disabled');
    } else {
      $btnSubmit.attr('disabled', true).addClass('disabled');
    }
  });
  $form.find('input[type="text"], input[type="password"]').on('keyup', function() {
    $form.validate();
  });
</script>
</body>
</html>
