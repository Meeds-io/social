/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.oauth.service;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.gatein.sso.agent.tomcat.ServletAccess;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.auth.AuthenticatorPlugin;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;
import org.exoplatform.web.security.AuthenticationRegistry;

import io.meeds.oauth.constant.OAuthConstants;
import io.meeds.oauth.model.OAuthPrincipal;
import io.meeds.oauth.model.OAuthProviderType;
import io.meeds.oauth.utils.OAuthUtils;

public class OAuthAuthenticator extends AuthenticatorPlugin {

  private static final Log          LOG                               = ExoLogger.getLogger(OAuthAuthenticator.class);

  private static final String       PASSWORD_PARAM                    = "password";

  private static final String       INVITATION_CONFIRM_ERROR_PARAM    = "invitationConfirmError";

  public static final String        CONTROLLER_PARAM_NAME             = "login_controller";

  public static final String        CANCEL_OAUTH                      = "oauth_cancel";

  public static final String        CONFIRM_ACCOUNT                   = "confirm_account";

  public static final String        REGISTER_NEW_ACCOUNT              = "register";

  public static final String        CONFIRM_REGISTER_ACCOUNT          = "submit_register";

  public static final String        SESSION_ATTR_REGISTER_NEW_ACCOUNT = "__oauth_create_new_account";

  private static final String       SESSION_ATTR_IS_ON_FLY_ERROR      = "isOnFlyError";

  private PortalContainer           container;

  private ResourceBundleService     resourceBundleService;

  private AuthenticationRegistry    authenticationRegistry;

  private OAuthProviderTypeRegistry oAuthProviderTypeRegistry;

  public OAuthAuthenticator(PortalContainer container,
                            ResourceBundleService resourceBundleService,
                            AuthenticationRegistry authenticationRegistry,
                            OAuthProviderTypeRegistry oAuthProviderTypeRegistry) {
    this.container = container;
    this.resourceBundleService = resourceBundleService;
    this.authenticationRegistry = authenticationRegistry;
    this.oAuthProviderTypeRegistry = oAuthProviderTypeRegistry;
  }

  @Override
  public String validateUser(Credential[] credentials) {
    if (!oAuthProviderTypeRegistry.isOAuthEnabled()) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("OAuth is disabled. Ignoring this login module");
      }
      return null;
    }

    HttpServletRequest request = ServletAccess.getRequest();
    if (request == null) {
      LOG.warn("HttpServletRequest is null. OAuthLoginModule will be ignored.");
      return null;
    }

    User portalUser = getOAuthAuthenticatedUser(request);
    if (portalUser == null) {
      return null;
    }

    String username = ((NameCallback) credentials[0]).getName();
    boolean authenticated = StringUtils.equals(username, portalUser.getUserName());
    if (authenticated) {
      return portalUser.getUserName();
    } else {
      return username;
    }
  }

  private User getOAuthAuthenticatedUser(HttpServletRequest request) {
    User portalUser = (User) authenticationRegistry.getAttributeOfClient(request,
                                                                         OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS);
    if (portalUser != null) {
      // We do not need this attribute any more
      authenticationRegistry.removeAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS);
    }
    return portalUser;
  }

  @Override
  protected void executeFilter(HttpServletRequest request,
                               HttpServletResponse response,
                               FilterChain chain) throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");

    Boolean isOnFlyError = (Boolean) request.getSession().getAttribute(OAuthConstants.SESSION_KEY_ON_FLY_ERROR);
    if (isOnFlyError != null) {
      request.getSession().removeAttribute(OAuthConstants.SESSION_KEY_ON_FLY_ERROR);
      request.setAttribute(SESSION_ATTR_IS_ON_FLY_ERROR, isOnFlyError);
    }

    User portalUser = (User) authenticationRegistry.getAttributeOfClient(request,
                                                                         OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
    User detectedUser = (User) authenticationRegistry.getAttributeOfClient(request,
                                                                           OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_DETECTED);
    request.setAttribute("portalUser", portalUser);
    if (detectedUser != null) {
      request.setAttribute("detectedUser", detectedUser);
    }

    final String controller = request.getParameter(CONTROLLER_PARAM_NAME);
    if (CANCEL_OAUTH.equalsIgnoreCase(controller)) {
      cancelOauth(request, response, authenticationRegistry);
      return;
    } else if (CONFIRM_ACCOUNT.equalsIgnoreCase(controller)) {
      confirmExistingAccount(request, response, messageResolver, authenticationRegistry);
      return;
    } else if (CONFIRM_REGISTER_ACCOUNT.equalsIgnoreCase(controller)) {
      processCreateNewAccount(request, response, messageResolver, authenticationRegistry, portalUser);
      return;
    } else if (REGISTER_NEW_ACCOUNT.equalsIgnoreCase(controller)) {
      request.getSession(true).setAttribute(SESSION_ATTR_REGISTER_NEW_ACCOUNT, 1);
    }

    Integer createNewAccount = (Integer) request.getSession().getAttribute(SESSION_ATTR_REGISTER_NEW_ACCOUNT);
    if (detectedUser != null && createNewAccount == null) {
      RequestDispatcher invitation = context.getRequestDispatcher(OAUTH_INVITATION_JSP_PATH);
      if (invitation != null) {
        invitation.forward(request, response);
        return;
      }
    } else {
      request.setAttribute("SSO.Login.Status", "USER_ACCOUNT_NOT_FOUND");
      request.removeAttribute("portalUser");
      authenticationRegistry.removeAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
      authenticationRegistry.removeAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
      LOG.warn("User {} does not have an account and On the fly registration is disabled, could not login !",
               portalUser.getDisplayName());
    }

    chain.doFilter(request, response);
  }

  private void cancelOauth(HttpServletRequest req, HttpServletResponse res, AuthenticationRegistry authReg) throws IOException {
    req.getSession().removeAttribute(SESSION_ATTR_REGISTER_NEW_ACCOUNT);
    authReg.removeAttributeOfClient(req, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
    authReg.removeAttributeOfClient(req, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
    authReg.removeAttributeOfClient(req, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_DETECTED);

    // . Redirect to last URL
    String initialURL = OAuthUtils.getURLToRedirectAfterLinkAccount(req, req.getSession());
    if (initialURL == null) {
      initialURL = req.getServletPath();
    }
    res.sendRedirect(res.encodeRedirectURL(initialURL));
  }

  private void confirmExistingAccount(HttpServletRequest request,
                                      HttpServletResponse response,
                                      MessageResolver bundle,
                                      AuthenticationRegistry authenticationRegistry) throws IOException, ServletException {
    String username = getUserName(request, authenticationRegistry);
    if (StringUtils.isBlank(username)) {
      request.setAttribute(INVITATION_CONFIRM_ERROR_PARAM, bundle.resolve("UIOAuthInvitationForm.message.not-in-oauth-login"));
      getContext().getRequestDispatcher(OAUTH_INVITATION_JSP_PATH).forward(request, response);
      return;
    }

    String password = request.getParameter(PASSWORD_PARAM);
    Credential[] credentials =
                             new Credential[] {
                                 new UsernameCredential(username),
                                 new PasswordCredential(password)
                             };
    Authenticator authenticator = getService(Authenticator.class);
    try {
      boolean isAuthenticated = StringUtils.isNotBlank(authenticator.validateUser(credentials));
      if (isAuthenticated) {
        OAuthRegistrationService oAuthRegistrationService = getService(OAuthRegistrationService.class);

        // Update authentication
        OAuthPrincipal<?> principal = getPrincipal(request, authenticationRegistry);
        OAuthProviderType<?> providerType = principal.getOauthProviderType();
        oAuthRegistrationService.updateUserProfileAttributes(username, providerType);

        // . Redirect to last URL
        authenticationRegistry.removeAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
        authenticationRegistry.removeAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_DETECTED);
        String initURL = OAuthUtils.getURLToRedirectAfterLinkAccount(request, request.getSession());
        if (initURL == null) {
          initURL = request.getServletPath();
        }
        response.sendRedirect(response.encodeRedirectURL(initURL));
      }
    } catch (LoginException ex) {
      Exception e = authenticator.getLastExceptionOnValidateUser();
      if (e instanceof DisabledUserException) {
        request.setAttribute(INVITATION_CONFIRM_ERROR_PARAM, bundle.resolve("UIOAuthInvitationForm.message.userDisabled"));
      } else {
        request.setAttribute(INVITATION_CONFIRM_ERROR_PARAM, bundle.resolve("UIOAuthInvitationForm.message.loginFailure"));
      }
    } catch (Exception ex) {
      request.setAttribute(INVITATION_CONFIRM_ERROR_PARAM, bundle.resolve("UIOAuthInvitationForm.message.loginUnknowException"));
      LOG.warn("Exception while checking password of user", ex);
    }

    getContext().getRequestDispatcher(OAUTH_INVITATION_JSP_PATH).forward(request, response);
  }

  private OAuthPrincipal<?> getPrincipal(HttpServletRequest request) {
    return (OAuthPrincipal<?>) authenticationRegistry.getAttributeOfClient(request,
                                                                           OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
  }

}
