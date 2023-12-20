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
package io.meeds.oauth.web;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.gatein.sso.agent.filter.api.AbstractSSOInterceptor;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.security.AuthenticationRegistry;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.spi.AccessTokenContext;
import io.meeds.oauth.spi.InteractionState;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthProviderProcessor;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;
import io.meeds.oauth.spi.SocialNetworkService;
import io.meeds.oauth.utils.OAuthUtils;

/**
 * Filter to handle OAuth interaction. This filter contains only "generic"
 * common functionality, which is same for all OAuth providers. For specific
 * functionality, you need to override some methods (especially abstract
 * methods)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class OAuthProviderFilter<T extends AccessTokenContext> extends AbstractSSOInterceptor {

  protected final Log               log = ExoLogger.getLogger(getClass());

  private AuthenticationRegistry    authenticationRegistry;

  private OAuthProviderProcessor<T> oauthProviderProcessor;

  private OAuthProviderTypeRegistry oAuthProviderTypeRegistry;

  private SocialNetworkService      socialNetworkService;

  protected String                  providerKey;

  @Override
  protected void initImpl() {
    this.providerKey = this.getInitParameter("providerKey");

    authenticationRegistry = getExoContainer().getComponentInstanceOfType(AuthenticationRegistry.class);
    oAuthProviderTypeRegistry = getExoContainer().getComponentInstanceOfType(OAuthProviderTypeRegistry.class);
    socialNetworkService = getExoContainer().getComponentInstanceOfType(SocialNetworkService.class);
    oauthProviderProcessor = getOAuthProvider().getOauthProviderProcessor();
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    HttpSession session = httpRequest.getSession();

    if (httpRequest.getRemoteUser() != null) {
      // direct access to Auth url, redirect to context url, as user is already
      // identified
      httpResponse.sendRedirect(httpRequest.getContextPath());
      return;
    }

    // Restart current state if 'oauthInteraction' param has value 'start'
    String interaction = httpRequest.getParameter(OAuthConstants.PARAM_OAUTH_INTERACTION);
    if (OAuthConstants.PARAM_OAUTH_INTERACTION_VALUE_START.equals(interaction)) {
      initInteraction(httpRequest, httpResponse);
      saveRememberMe(httpRequest);
      saveInitialURI(httpRequest);

      authenticationRegistry.removeAttributeOfClient(httpRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
    }

    // Possibility to init interaction with custom scope. It's needed when
    // custom portlets want bigger scope then the one available in configuration
    String scopeToUse = obtainCustomScopeIfAvailable(httpRequest);

    InteractionState<T> interactionState;

    try {
      if (scopeToUse == null) {
        interactionState = getOauthProviderProcessor().processOAuthInteraction(httpRequest, httpResponse);
      } else {
        if (log.isTraceEnabled()) {
          log.trace("Process oauth interaction with scope: " + scopeToUse);
        }
        interactionState = getOauthProviderProcessor().processOAuthInteraction(httpRequest, httpResponse, scopeToUse);
      }
    } catch (OAuthException | ExecutionException | InterruptedException ex) {
      log.error("Error during OAuth flow with: " + ex.getMessage(), ex);

      // Save exception to session and redirect to portal. Exception will be
      // processed later on portal side
      session.setAttribute(OAuthConstants.ATTRIBUTE_EXCEPTION_OAUTH, ex);
      redirectAfterOAuthError(httpRequest, httpResponse);
      return;
    }

    if (InteractionState.State.FINISH.equals(interactionState.getState())) {
      OAuthPrincipal<T> oauthPrincipal = getOAuthPrincipal(httpRequest, httpResponse, interactionState);

      if (oauthPrincipal != null) {
        if (httpRequest.getRemoteUser() == null) {
          // Save authenticated OAuthPrincipal to authenticationRegistry in case
          // that we are anonymous user
          // Other filter should take care of processing it and perform GateIn
          // login or registration
          authenticationRegistry.setAttributeOfClient(httpRequest,
                                                      OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL,
                                                      oauthPrincipal);
        } else {
          // For authenticated user, we will save it as request attribute and
          // process it by other filter, which will update
          // userProfile with new username and accessToken
          httpRequest.setAttribute(OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL, oauthPrincipal);
        }

        // Continue with request
        chain.doFilter(request, response);
      }
    }
  }

  protected AuthenticationRegistry getAuthenticationRegistry() {
    return authenticationRegistry;
  }

  protected OAuthProviderProcessor<T> getOauthProviderProcessor() {
    return oauthProviderProcessor;
  }

  protected OAuthProviderTypeRegistry getOAuthProviderTypeRegistry() {
    return oAuthProviderTypeRegistry;
  }

  protected SocialNetworkService getSocialNetworkService() {
    return socialNetworkService;
  }

  protected String obtainCustomScopeIfAvailable(HttpServletRequest httpRequest) {
    // It's sufficient to use request parameter, because scope is needed only
    // for facebookProcessor.initialInteraction
    String customScope = httpRequest.getParameter(OAuthConstants.PARAM_CUSTOM_SCOPE);
    if (customScope != null) {
      String currentUser = httpRequest.getRemoteUser();
      if (currentUser == null) {
        log.warn("Parameter " + OAuthConstants.PARAM_CUSTOM_SCOPE + " found but there is no user available. Ignoring it.");
        return null;
      } else {
        T currentAccessToken = socialNetworkService.getOAuthAccessToken(getOAuthProvider(), currentUser);

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

  protected void redirectAfterOAuthError(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Similar code like in OAuthLinkAccountFilter
    HttpSession session = request.getSession();
    String urlToRedirect = OAuthUtils.getURLToRedirectAfterLinkAccount(request, session);

    if (log.isTraceEnabled()) {
      log.trace("Will redirect user to URL: " + urlToRedirect);
    }

    response.sendRedirect(response.encodeRedirectURL(urlToRedirect));
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

  protected <T extends AccessTokenContext> OAuthProviderType<T> getOauthProvider(String defaultKey, Class<T> c) {
    String key = this.providerKey != null ? this.providerKey : defaultKey;
    return getOAuthProviderTypeRegistry().getOAuthProvider(key, c);
  }

  protected abstract OAuthProviderType<T> getOAuthProvider();

  protected abstract void initInteraction(HttpServletRequest request, HttpServletResponse response);

  protected abstract OAuthPrincipal<T> getOAuthPrincipal(HttpServletRequest request, HttpServletResponse response,
                                                         InteractionState<T> interactionState);
}
