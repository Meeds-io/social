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

import java.lang.reflect.Method;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.UserStatus;

import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.model.AccessTokenContext;
import io.meeds.oauth.model.OAuthProviderType;
import io.meeds.oauth.provider.spi.OAuthProviderProcessor;

/**
 * Service for handling persistence of OAuth data (usernames, access tokens)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SocialNetworkService {

  private static final Log    LOG = ExoLogger.getLogger(SocialNetworkService.class);

  private PortalContainer     container;

  private OrganizationService organizationService;

  public SocialNetworkService(PortalContainer container,
                              OrganizationService organizationService) {
    this.container = container;
    this.organizationService = organizationService;
  }

  /**
   * Find user from Identity DB by oauth provider username
   *
   * @param  oauthProviderType
   * @param  oauthProviderUsername
   * @return                       portal {@link User}
   */
  public User findUserByOAuthProviderUsername(OAuthProviderType<?> oauthProviderType, String oauthProviderUsername) {
    begin();
    try {
      UserHandler userHandler = organizationService.getUserHandler();
      // TODO: Ugly, but it's used due to OrganizationService API limitations
      // because it doesn't allow to find user by unique userProfile attribute
      Method m = userHandler.getClass().getMethod("findUserByUniqueAttribute", String.class, String.class, UserStatus.class);
      return (User) m.invoke(userHandler, oauthProviderType.getUserNameAttrName(), oauthProviderUsername, UserStatus.ANY);
    } catch (NoSuchMethodException e) {
      LOG.error("Method findUserByUniqueAttribute(String, String, boolean) is not available on userHandler");
      throw new OAuthException(OAuthExceptionCode.PERSISTENCE_ERROR, e);
    } catch (Exception e) {
      throw new OAuthException(OAuthExceptionCode.PERSISTENCE_ERROR, e);
    } finally {
      end();
    }
  }

  /**
   * Save access token of given user into DB
   */
  public <T extends AccessTokenContext> void updateOAuthAccessToken(OAuthProviderType<T> oauthProviderType,
                                                                    String username,
                                                                    T accessToken) {
    begin();
    try {
      UserProfileHandler userProfileHandler = organizationService.getUserProfileHandler();
      UserProfile userProfile = userProfileHandler.findUserProfileByName(username);
      if (userProfile == null) {
        userProfile = userProfileHandler.createUserProfileInstance(username);
      }

      OAuthProviderProcessor<T> oauthProviderProcessor = oauthProviderType.getOauthProviderProcessor();
      oauthProviderProcessor.saveAccessTokenAttributesToUserProfile(userProfile, accessToken);
      userProfileHandler.saveUserProfile(userProfile, true);
    } catch (OAuthException oauthEx) {
      throw oauthEx;
    } catch (Exception e) {
      throw new OAuthException(OAuthExceptionCode.PERSISTENCE_ERROR, e);
    } finally {
      end();
    }
  }

  /**
   * Obtain access token of given user from DB
   */
  public <T extends AccessTokenContext> T getOAuthAccessToken(OAuthProviderType<T> oauthProviderType, String username) {
    begin();
    try {
      UserProfileHandler userProfileHandler = organizationService.getUserProfileHandler();
      UserProfile userProfile = userProfileHandler.findUserProfileByName(username);
      if (userProfile == null) {
        // If use have not profile, he also have not OauthAccessToken
        return null;
      }

      OAuthProviderProcessor<T> oauthProviderProcessor = oauthProviderType.getOauthProviderProcessor();
      return oauthProviderProcessor.getAccessTokenFromUserProfile(userProfile);
    } catch (Exception e) {
      throw new OAuthException(OAuthExceptionCode.PERSISTENCE_ERROR, e);
    } finally {
      end();
    }
  }

  /**
   * Remove access token of given user from DB
   */
  public <T extends AccessTokenContext> void removeOAuthAccessToken(OAuthProviderType<T> oauthProviderType, String username) {
    begin();
    try {
      UserProfileHandler userProfileHandler = organizationService.getUserProfileHandler();
      UserProfile userProfile = userProfileHandler.findUserProfileByName(username);
      if (userProfile == null) {
        // Don't need to remove OauthAccessToken if user-profile does not exists
        return;
      }

      OAuthProviderProcessor<T> oauthProviderProcessor = oauthProviderType.getOauthProviderProcessor();
      oauthProviderProcessor.removeAccessTokenFromUserProfile(userProfile);

      userProfileHandler.saveUserProfile(userProfile, true);
    } catch (Exception e) {
      throw new OAuthException(OAuthExceptionCode.PERSISTENCE_ERROR, e);
    } finally {
      end();
    }
  }

  /**
   * Locates a user by its email address. If no user is found or more than one
   * user has that email, it returns null
   * 
   * @param  email
   * @return       User having provided email
   */
  public User findUserByEmail(String email) {
    begin();
    try {
      UserHandler userHandler = organizationService.getUserHandler();
      Query queryByEmail = new Query();
      queryByEmail.setEmail(email);
      ListAccess<User> users = userHandler.findUsersByQuery(queryByEmail);
      if (users == null || users.getSize() == 0 || users.getSize() > 1) {
        return null;
      } else if (users.getSize() == 1) {
        return users.load(0, 1)[0];
      }
    } catch (OAuthException oauthEx) {
      throw oauthEx;
    } catch (Exception e) {
      throw new OAuthException(OAuthExceptionCode.PERSISTENCE_ERROR, e);
    } finally {
      end();
    }
    return null;
  }

  /**
   * Save OAuth informations (both username and access token) into DB
   */
  public <T extends AccessTokenContext> void updateOAuthInfo(OAuthProviderType<T> oauthProviderType,
                                                             String username,
                                                             String oauthUsername,
                                                             T accessToken) {
    begin();
    try {
      UserProfileHandler userProfileHandler = organizationService.getUserProfileHandler();
      UserProfile userProfile = userProfileHandler.findUserProfileByName(username);
      if (userProfile == null) {
        userProfile = userProfileHandler.createUserProfileInstance(username);
      }

      userProfile.setAttribute(oauthProviderType.getUserNameAttrName(), oauthUsername);

      OAuthProviderProcessor<T> oauthProviderProcessor = oauthProviderType.getOauthProviderProcessor();
      oauthProviderProcessor.saveAccessTokenAttributesToUserProfile(userProfile, accessToken);
      userProfileHandler.saveUserProfile(userProfile, true);
    } catch (OAuthException oauthEx) {
      throw oauthEx;
    } catch (Exception e) {
      throw new OAuthException(OAuthExceptionCode.PERSISTENCE_ERROR, e);
    } finally {
      end();
    }
  }

  private void begin() {
    RequestLifeCycle.begin(container);
  }

  private void end() {
    RequestLifeCycle.end();
  }

}
