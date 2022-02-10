<%--
    Copyright (C) 2020 eXo Platform SAS.

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
<%@ page import="org.exoplatform.services.organization.User"%>
<%@ page import="org.exoplatform.services.organization.impl.UserImpl" %>
<%@ page import="org.exoplatform.web.controller.QualifiedName" %>
<%@ page import="org.exoplatform.web.login.recovery.PasswordRecoveryService" %>
<%@ page import="org.exoplatform.portal.resource.SkinConfig" %>
<%@ page import="org.exoplatform.commons.utils.I18N" %>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService" %>
<%@ page import="org.exoplatform.portal.resource.config.tasks.PortalSkinTask" %>
<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.services.organization.OrganizationService" %>
<%@ page import="org.exoplatform.services.organization.User"%>
<%@ page import="org.exoplatform.commons.utils.PropertyManager"%>
<%@ page import="java.util.*" %>
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
	
	OrganizationService organizationService = portalContainer.getComponentInstanceOfType(OrganizationService.class);
	User user = organizationService.getUserHandler().findUserByName(username);
	String fullUsername = user.getDisplayName();

    PasswordRecoveryService passRecoveryServ = portalContainer.getComponentInstanceOfType(PasswordRecoveryService.class);
    String onboardingPasswordPath = passRecoveryServ.getOnboardingURL(tokenId, I18N.toTagIdentifier(locale));


    List<String> errors = (List<String>)request.getAttribute("errors");
    String success = (String)request.getAttribute("success");
    if (errors == null) {
        errors = new ArrayList<String>();
    }
	
	BrandingService brandingService = portalContainer.getComponentInstanceOfType(BrandingService.class);
	String companyName = brandingService.getCompanyName();
	String logo = "";
	if (brandingService.getLogo() != null) {
		byte[] logoData = brandingService.getLogo().getData();
		byte[] encodedLogoData = Base64.getEncoder().encode(logoData);
		logo= "data:image/png;base64," + new String(encodedLogoData, "UTF-8");
	}
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html; charset=UTF-8");


    String passwordCondition = PropertyManager.getProperty("gatein.validators.passwordpolicy.format.message");

    String browserLanguage = request.getLocale() == null ? "en" : request.getLocale().getLanguage();
    LocaleConfigService localeConfigService = portalContainer.getComponentInstanceOfType(LocaleConfigService.class);
    LocaleConfig localeConfig = localeConfigService.getLocaleConfig(browserLanguage);
    String direction = localeConfig == null || localeConfig.getOrientation() != Orientation.RT ? "ltr" : "rtl";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%=browserLanguage%>" lang="<%=browserLanguage%>" dir="<%=direction%>">
  <head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=res.getString("onboarding.changePass.title")%></title>
    <%if (success != null && !success.isEmpty()) {%>
        <meta http-equiv="refresh" content="0; url=<%=contextPath+ "/login"%>" />
    <%}%>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="shortcut icon" type="image/x-icon"  href="<%=contextPath%>/favicon.ico" />
    <link id="brandingSkin" rel="stylesheet" type="text/css" href="<%=brandingCss%>">
    <link href="<%=loginCssPath%>" rel="stylesheet" type="text/css"/>
    <link rel="stylesheet" type="text/css" href="<%=contextPath%>/skin/onboarding.css"/>
    <script type="text/javascript" src="/eXoResources/javascript/jquery-3.2.1.js"></script>
    <script type="text/javascript" src="/eXoResources/javascript/eXo/webui/FormValidation.js"></script>
  </head>
  <body>
	<div class="loginBGLight"><span></span></div>

    <div class="uiLogin">
		<p class="welcomeContent"><%=companyName%></p>
		<div class="uiLoginCondition">
			<p><%=res.getString("onboarding.login.condition")%></p>
			<p><%=res.getString("onboarding.login.finalizeCreateAccount")%></p>
		</div>
    	<div class="loginContainer">
			<div class="loginContent">
				<div class="centerLoginContent">
					<% if (errors.size() > 0) { %>
					<div class="alertForm">
						<div class="alert alert-error mgT0 mgB20">
              <% for (String error : errors) { %>
                <i class="uiIconError"></i><span><%=error%></span><br/>
              <%}%>
						</div>
					</div>
					<%}%>
					<form name="registerForm" action="<%= contextPath + onboardingPasswordPath %>" method="post">
						<div class="userCredentials">
							<span class="iconUser"></span>
							<input class="username" data-validation="require" name="username" type="text" value="<%=username%>" readonly="readonly">
						</div>
						<div class="userCredentials">
						  <span class="iconPswrd"></span>
						  <input data-validation="require" type="password" name="password" id="password" autocomplete="off" value="<%=(password != null ? password : "")%>"  placeholder="<%=res.getString("portal.login.Password")%>">
					      <i class="uiIconError passwordFormat"></i>
						</div>
						<p class="passwordCondition"><i class="uiIconInfo"></i><%=passwordCondition != null ? passwordCondition : res.getString("onboarding.login.passwordCondition")%></p>
						<div class="userCredentials">
						  <span class="iconPswrd"></span>
						   <input data-validation="require" type="password" name="password2" id="confirm_password" autocomplete="off" value="<%=(password2 != null ? password2 : "")%>"  placeholder="<%=res.getString("onboarding.login.confirmPassword")%>">
						</div>
						<% if (success == null || success.isEmpty()) {%>
						<p class="captchaCondition"></i><%=res.getString("onboarding.login.captchaCondition")%></p>
						<div id="captcha">
							<img src="/portal/on-boarding?serveCaptcha=true" alt="Captcha image for visual validation">
							<br/>
							<input data-validation="require" name="captcha" type="text" id="inputCaptcha"  placeholder="<%=res.getString("onboarding.login.captcha")%>">
						</div>
						<%}%>
						<div id="UIPortalLoginFormAction" class="loginButton">
							<button class="button" type="submit" disabled="disabled"><%=res.getString("onboarding.login.save")%></button>
						</div>
						<input type="hidden" name="action" value="resetPassword"/>
					</form>
					<%/*End form*/%>
				</div>
			</div>
    	</div>
		<div class="alertErrorMessage confirmPwdError hidden"><%=res.getString("onboarding.login.passwordsNotMatch")%></div>
		<div class="alertSuccessMessage confirmPwdSuccess hidden"><%=res.getString("onboarding.login.passwordsMatch")%></div>
    </div>
    <div class="logoImageContent">
      <img src="<%=logo%>" class="logoImage"/>
    </div>
	<script type="text/javascript">
	  var $form = $('form[name="registerForm"]');
	  $form.on('formValidate', function(e, valid) {
		var $btnSubmit = $form.find('.button[type="submit"]');
		if (valid) {            
		  $btnSubmit.attr('disabled', false).removeClass('disabled');
		} else {
		  $btnSubmit.attr('disabled', true).addClass('disabled');
		}
	  });
	  $form.find('input[type="text"], input[type="password"]').on('keyup', function() {
		$form.validate();
	  });

	  $("#password").focus(function () {
		  $('.uiLogin .confirmPwdError').hide();
		  $('.uiLogin .confirmPwdSuccess').hide();
	  });
	  $("#password").blur(function () {
		  if ($('#password').val() != $('#confirm_password').val()) {
			  $('.uiLogin .confirmPwdError').show();
		  }
	  });

	  $("#confirm_password").blur(function () {
		  if ($('#password').val() != $('#confirm_password').val()) {
			  $('.uiLogin .confirmPwdError').show();
		  }
	  });
	  $("#confirm_password").focus(function () {
		  $('.uiLogin .confirmPwdError').hide();
	  });

	  $('#confirm_password').on('keyup', function () {
		  if (($('#password').val() == $('#confirm_password').val()) && $('.uiLogin .passwordFormat').is(":hidden")) {
			  $('#confirm_password').after('<i class="uiIconGreenChecker"></i>');
			  $('.uiLogin .confirmPwdSuccess').show();
		  } else {
			  $('.uiIconGreenChecker').remove();
			  $('.uiLogin .confirmPwdSuccess').hide();
		  }
	  });
	</script>
  </body>
</html>
