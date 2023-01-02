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
package io.meeds.oauth.provider.spi;

import static io.meeds.oauth.constant.OAuthConstants.PASSWORD_PARAM_PREFIX;
import static io.meeds.oauth.constant.OAuthConstants.PASSWORD_REQUEST_PARAM;
import static io.meeds.oauth.constant.OAuthConstants.USERNAME_REQUEST_PARAM;
import static org.exoplatform.web.security.security.CookieTokenService.EXTERNAL_REGISTRATION_TOKEN;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.gatein.wci.security.Credentials;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.exoplatform.web.security.security.RemindPasswordTokenService;

import io.meeds.oauth.constant.OAuthConstants;
import io.meeds.oauth.model.AccessTokenContext;
import io.meeds.oauth.model.InteractionState;
import io.meeds.oauth.model.OAuthPrincipal;
import io.meeds.oauth.model.OAuthProviderType;
import io.meeds.oauth.service.OAuthProviderTypeRegistry;
import io.meeds.oauth.service.OAuthRegistrationService;
import io.meeds.oauth.service.SocialNetworkService;
import lombok.Setter;

/**
 * Filter to handle OAuth interaction. This filter contains only "generic"
 * common functionality, which is same for all OAuth providers. For specific
 * functionality, you need to override some methods (especially abstract
 * methods)
 *
 * @param  <T> oAuth specific provider {@link AccessTokenContext}
 * @author     <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class OAuthProviderFilter<T extends AccessTokenContext> extends AbstractFilter {

  private static final Log             LOG = ExoLogger.getLogger(OAuthProviderFilter.class);

  protected AuthenticationRegistry     authenticationRegistry;

  protected OAuthProviderTypeRegistry  oAuthProviderTypeRegistry;

  protected SocialNetworkService       socialNetworkService;

  protected OAuthRegistrationService   oAuthRegistrationService;

  protected RemindPasswordTokenService remindPasswordTokenService;

  @Setter
  protected String                     providerKey;

  @Override
  public void doFilter(ServletRequest request, // NOSONAR
                       ServletResponse response,
                       FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    HttpSession session = httpRequest.getSession();

    if (httpRequest.getRemoteUser() != null) {
      // Redirect to portal since already authenticated
      httpResponse.sendRedirect("/" + PortalContainer.getCurrentPortalContainerName());
      return;
    }

    // Restart current state if 'oauthInteraction' param has value 'start'
    String interaction = httpRequest.getParameter(OAuthConstants.PARAM_OAUTH_INTERACTION);
    try {
      if (OAuthConstants.PARAM_OAUTH_INTERACTION_VALUE_START.equals(interaction)) {
        initInteraction(httpRequest, httpResponse);
        saveRememberMe(httpRequest);
        saveInitialURI(httpRequest);
      }

      // Possibility to init interaction with custom scope. It's needed when
      // custom portlets want bigger scope then the one available in
      // configuration
      String scopeToUse = obtainCustomScopeIfAvailable(httpRequest);

      InteractionState<T> interactionState;
      if (scopeToUse == null) {
        interactionState = getOauthProviderProcessor().processOAuthInteraction(httpRequest, httpResponse);
      } else {
        interactionState = getOauthProviderProcessor().processOAuthInteraction(httpRequest, httpResponse, scopeToUse);
      }

      if (InteractionState.State.FINISH.equals(interactionState.getState())) {
        OAuthPrincipal<T> oauthPrincipal = getOAuthPrincipal(httpRequest, httpResponse, interactionState);
        if (oauthPrincipal != null) {
          processPrincipal(httpRequest, httpResponse, chain, oauthPrincipal);
        }
      }
    } catch (Exception e) {
      LOG.warn("Error during OAuth flow with interaction param {}", interaction, e);
      // Save exception to session to be used to display error message to end
      // user
      session.setAttribute(OAuthConstants.ATTRIBUTE_EXCEPTION_OAUTH, e);
      httpResponse.sendRedirect(getLoginUrl());

      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
    }
  }

  protected void processPrincipal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain,
                                  OAuthPrincipal<T> principal) throws IOException {
    User portalUser = oAuthRegistrationService.detectPortalUser(principal);
    if (portalUser == null) {
      handleRedirectToRegistrationForm(request, response, principal);
    } else {
      // This means that user has been successfully authenticated via OAuth and
      // exists in GateIn. So we need to establish SSO context
      // and clean our own local context from AuthenticationRegistry.
      handleRedirectToPortalLogin(request, response, chain, portalUser, principal);
      cleanAuthenticationContext(request);
    }
  }

  protected void handleRedirectToPortalLogin(HttpServletRequest request,
                                             HttpServletResponse response,
                                             FilterChain chain,
                                             User portalUser,
                                             OAuthPrincipal<T> oauthPrincipal) throws IOException, ServletException {
    if (portalUser.isEnabled()) {
      socialNetworkService.updateOAuthAccessToken(oauthPrincipal.getOauthProviderType(),
                                                  portalUser.getUserName(),
                                                  oauthPrincipal.getAccessToken());
    }

    authenticationRegistry.setAttributeOfClient(request,
                                                OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS,
                                                portalUser);

    // Login the user
    HttpServletRequestWrapper servletRequest = wrapRequestForLogin(request, oauthPrincipal);
    chain.doFilter(servletRequest, response);
  }

  protected void handleRedirectToRegistrationForm(HttpServletRequest httpRequest,
                                                  HttpServletResponse httpResponse,
                                                  OAuthPrincipal<T> principal) throws IOException {
    User gateInUser;
    if (StringUtils.isNotBlank(getEmail(principal))) {
      gateInUser = socialNetworkService.findUserByEmail(getEmail(principal));
    } else {
      gateInUser = socialNetworkService.findUserByEmail(principal.getEmail());
    }
    if (gateInUser == null) {
      gateInUser = getOauthProviderProcessor().convertToPortalUser(principal);
    }
    if (oAuthRegistrationService.isRegistrationOnFly(principal.getOauthProviderType())) {

      String oauth = principal.getOauthProviderType().getKey() + "_" + getEmail(principal);
      User user = oAuthRegistrationService.detectGateInUser(principal);
      if (user != null) {

      } else {
        User registeredUser = oAuthRegistrationService.createGateInUser(principal);
        if (registeredUser != null) {
          authenticationRegistry.removeAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
          // send redirect to continue oauth login
          response.sendRedirect(getContext().getContextPath());
          return;
        } else {
          request.getSession().setAttribute(SESSION_KEY_SIGNUP_ON_FLY_ERROR, oauth);
          request.getSession().setAttribute(OAuthConstants.SESSION_KEY_ON_FLY_ERROR, Boolean.TRUE);
        }
      }
    }
    chain.doFilter(request, response);
  }

  private String getEmail(OAuthPrincipal<T> principal) {
    return principal.getUserName();
  }

  }else{authenticationRegistry.setAttributeOfClient(httpRequest,OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER,gateInUser);httpResponse.sendRedirect(httpResponse.encodeRedirectURL(getRegisterUrl(gateInUser.getUserName(),getOAuthAuthenticatorPassword(principal))));}}

  protected void cleanAuthenticationContext(HttpServletRequest httpRequest) {
    authenticationRegistry.removeAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
    authenticationRegistry.removeAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
  }

  private HttpServletRequestWrapper wrapRequestForLogin(HttpServletRequest request,
                                                        OAuthPrincipal<T> principal) {
    User user = oAuthRegistrationService.detectGateInUser(principal);
    // Forward to custom authenticator plugin the required parameters to
    // validate the user
    return new HttpServletRequestWrapper(request) {
      @Override
      public String getParameter(String name) {
        if (StringUtils.equals(name, USERNAME_REQUEST_PARAM)) {
          return user.getUserName();
        } else if (StringUtils.equals(name, PASSWORD_REQUEST_PARAM)) {
          return getCompoundPassword(principal);
        } else {
          return super.getParameter(name);
        }
      }

      @Override
      public String getRequestURI() {
        return getContextPath() + "/login";
      }
    };
  }

  protected void saveInitialURI(HttpServletRequest request) {
    String initialURI = request.getParameter(OAuthConstants.PARAM_INITIAL_URI);
    if (initialURI != null) {
      request.getSession().setAttribute(OAuthConstants.ATTRIBUTE_URL_TO_REDIRECT_AFTER_LINK_SOCIAL_ACCOUNT, initialURI);
    }
  }

  protected void saveRememberMe(HttpServletRequest request) {
    String rememberMe = request.getParameter(OAuthConstants.PARAM_REMEMBER_ME);
    request.getSession().setAttribute(OAuthConstants.ATTRIBUTE_REMEMBER_ME, rememberMe);
  }

  protected String obtainCustomScopeIfAvailable(HttpServletRequest httpRequest) {
    // It's sufficient to use request parameter, because scope is needed only
    // for facebookProcessor.initialInteraction
    String customScope = httpRequest.getParameter(OAuthConstants.PARAM_CUSTOM_SCOPE);
    if (customScope != null) {
      String currentUser = httpRequest.getRemoteUser();
      if (currentUser == null) {
        LOG.warn("Parameter " + OAuthConstants.PARAM_CUSTOM_SCOPE + " found but there is no user available. Ignoring it.");
        return null;
      } else {
        T currentAccessToken = getSocialNetworkService().getOAuthAccessToken(getOAuthProviderType(), currentUser);

        if (currentAccessToken != null) {
          // Add new customScope to set of existing scopes, so accessToken will
          // be obtained for all of them
          currentAccessToken.addScope(customScope);
          return currentAccessToken.getScopesAsString();
        } else {
          return customScope;
        }
      }
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  protected <E extends AccessTokenContext> OAuthProviderType<E> getOauthProvider(String defaultKey, Class<E> c) {
    String key = this.providerKey != null ? this.providerKey : defaultKey;
    return (OAuthProviderType<E>) getOAuthProviderTypeRegistry().getOAuthProvider(key, c);
  }

  protected OAuthProviderProcessor<T> getOauthProviderProcessor() {
    return getOAuthProviderType().getOauthProviderProcessor();
  }

  protected String getLoginUrl() {
    return "/" + PortalContainer.getCurrentPortalContainerName() + "/login";
  }

  protected String getRegisterUrl(String username, String password) {
    String tokenId = remindPasswordTokenService.createToken(new Credentials(username, password),
                                                            EXTERNAL_REGISTRATION_TOKEN);
    return "/" + PortalContainer.getCurrentPortalContainerName() + "/external-registration?token=" + tokenId;
  }

  protected AuthenticationRegistry getAuthenticationRegistry() {
    if (authenticationRegistry == null) {
      authenticationRegistry = PortalContainer.getInstance().getComponentInstanceOfType(AuthenticationRegistry.class);
    }
    return authenticationRegistry;
  }

  protected OAuthProviderTypeRegistry getOAuthProviderTypeRegistry() {
    if (oAuthProviderTypeRegistry == null) {
      oAuthProviderTypeRegistry = PortalContainer.getInstance().getComponentInstanceOfType(OAuthProviderTypeRegistry.class);
    }
    return oAuthProviderTypeRegistry;
  }

  protected SocialNetworkService getSocialNetworkService() {
    if (socialNetworkService == null) {
      socialNetworkService = PortalContainer.getInstance().getComponentInstanceOfType(SocialNetworkService.class);
    }
    return socialNetworkService;
  }

  private String getCompoundPassword(OAuthPrincipal<T> principal) {
    return PASSWORD_PARAM_PREFIX + "@"
        + principal.getOauthProviderType().getKey() + "@"
        + principal.getAccessToken().getAccessToken();
  }

  protected abstract OAuthProviderType<T> getOAuthProviderType();

  protected abstract void initInteraction(HttpServletRequest request, HttpServletResponse response);

  protected abstract OAuthPrincipal<T> getOAuthPrincipal(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         InteractionState<T> interactionState);
}
