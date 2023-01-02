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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;

import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.model.AccessTokenContext;
import io.meeds.oauth.model.InteractionState;
import io.meeds.oauth.model.OAuthPrincipal;
import io.meeds.oauth.utils.OAuthUtils;

/**
 * Processor to call operations on given OAuth provider (Social network)
 * 
 * @param  <T> {@link AccessTokenContext}
 * @author     <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class OAuthProviderProcessor<T extends AccessTokenContext> {

  private AbstractCodec codec;

  protected OAuthProviderProcessor(CodecInitializer codecInitializer) throws TokenServiceInitializationException {
    this.codec = codecInitializer.getCodec();
  }

  /**
   * Process OAuth workflow for this OAuth provider (social network). Workflow
   * is finished if returned {@link io.meeds.oauth.model.InteractionState} is in
   * state {@link io.meeds.oauth.model.InteractionState.State#FINISH} and in
   * this case, InteractionState should also have accessToken filled. If
   * {@link io.meeds.oauth.model.InteractionState} is in state
   * {@link io.meeds.oauth.model.InteractionState.State#AUTH}, then more
   * redirections are needed. In this case, given {@link HttpServletResponse}
   * should be already committed and prepared for redirection.
   *
   * @param  httpRequest
   * @param  httpResponse
   * @return                InteractionState with state of OAuth interaction
   * @throws IOException    if IO error occured (for example if
   *                          httpResponse.sendRedirect failed)
   * @throws OAuthException in case of some other error, which may be specific
   *                          for this OAuth processor (Details are available in
   *                          error code) Caller should be able to handle at
   *                          least
   *                          {@link io.meeds.oauth.exception.OAuthExceptionCode#USER_DENIED_SCOPE}
   *                          which happens when user denied scope
   *                          (authorization screen in web of given social
   *                          network)
   */
  public abstract InteractionState<T> processOAuthInteraction(HttpServletRequest httpRequest,
                                                              HttpServletResponse httpResponse) throws IOException,
                                                                                                OAuthException,
                                                                                                ExecutionException,
                                                                                                InterruptedException;

  /**
   * Possibility to create new OAuth interaction with custom scope (not just the
   * scope which is provided in configuration of this OAuth processor)
   * 
   * @see                   #processOAuthInteraction(javax.servlet.http.HttpServletRequest,
   *                        javax.servlet.http.HttpServletResponse)
   * @param  httpRequest
   * @param  httpResponse
   * @param  scope          custom scope, which contains all scopes in single
   *                          String divided by
   *                          {@link AccessTokenContext#DELIMITER}
   * @return
   * @throws IOException
   * @throws OAuthException
   */
  public abstract InteractionState<T> processOAuthInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                                              String scope) throws IOException, OAuthException,
                                                                            ExecutionException,
                                                                            InterruptedException;

  /**
   * Revoke given access token on OAuth provider side, so application is removed
   * from list of supported applications for given user
   *
   * @param  accessToken    access token to revoke
   * @throws OAuthException with code
   *                          {@link io.meeds.oauth.exception.OAuthExceptionCode#TOKEN_REVOCATION_FAILED}
   *                          if remote revocation of access token failed for
   *                          some reason
   */
  public abstract void revokeToken(T accessToken) throws OAuthException;

  /**
   * Send request to OAuth Provider to validate if given access token is valid
   * and ask for scopes, which are available for given accessToken. Returned
   * access token should be always valid and prepared for invoke other
   * operations
   *
   * @param  accessToken    accessToken which will be used to ask OAuthProvider
   *                          about validation and for available scopes
   * @return                accessTokenContext, which will be quite same as the
   *                        one from accessToken parameter. It could have some
   *                        info updated (like scopes)
   * @throws OAuthException usually with codes: -
   *                          {@link io.meeds.oauth.exception.OAuthExceptionCode#ACCESS_TOKEN_ERROR}
   *                          if invalid access is used as argument -
   *                          {@link io.meeds.oauth.exception.OAuthExceptionCode#IO_ERROR}
   *                          if IO error occurs
   */
  public abstract T validateTokenAndUpdateScopes(T accessToken) throws OAuthException;

  /**
   * Return object, which can be used to call some operations on this Social
   * network. For example "Plus" object for Google+ network
   *
   * @param  socialApiObjectType Type of object, which we wanted to return.
   *                               Method will return null if this type is not
   *                               supported by this processor
   * @param  accessToken         access token used to initialize object
   * @return                     initialized object of required type or null if
   *                             type wasn't found (supported) by this processor
   */
  public abstract <C> C getAuthorizedSocialApiObject(T accessToken, Class<C> socialApiObjectType);

  // OPERATIONS FOR ACCESS TOKEN PERSISTENCE

  /**
   * Save accessToken data to given userProfile. Note that we are not calling
   * any DB save operations, just filling data into given userProfile
   *
   * @param userProfile where data about access token will be filled
   * @param accessToken specific access token for this OAuth processor
   */
  public abstract void saveAccessTokenAttributesToUserProfile(UserProfile userProfile, T accessToken);

  /**
   * Obtain needed data from given userProfile and create accessToken from them
   *
   * @param  userProfile where data from access token will be obtained
   * @param  codec       to decode data from userProfile
   * @return             accesstoken or null if accessToken is not found in
   *                     persistent storage
   */
  public abstract T getAccessTokenFromUserProfile(UserProfile userProfile);

  /**
   * Remove data about access token from this user profile
   * 
   * @param userProfile from which data will be removed
   */
  public abstract void removeAccessTokenFromUserProfile(UserProfile userProfile);

  public abstract User getPortalUser(OAuthPrincipal<?> principal) {
    // TODO how to know exactly what user it is ???? !!!!!!!!! A possible hack here !!!!
    return OAuthUtils.convertOAuthPrincipalToGateInUser(principal);
  }

  public User createPortalUser(OAuthPrincipal<?> principal) {
    return OAuthUtils.convertOAuthPrincipalToGateInUser(principal);
  }

  public String encodeString(String input) {
    if (input == null) {
      return null;
    } else {
      return codec.encode(input);
    }
  }

  public String decodeString(String input) {
    if (input == null) {
      return null;
    } else {
      return codec.decode(input);
    }
  }

}
