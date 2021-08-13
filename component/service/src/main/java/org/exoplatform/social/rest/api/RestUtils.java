/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.user.UserStateModel;
import org.exoplatform.services.user.UserStateService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.rest.entity.DataEntity;

public class RestUtils {

  public static final int    DEFAULT_LIMIT  = 20;

  public static final int    DEFAULT_OFFSET = 0;

  public static final int    HARD_LIMIT     = 500;

  public static final String SUPPORT_TYPE   = "json";

  public static final String ADMIN_GROUP    = "/platform/administrators";

  public static final String DELEGATED_GROUP    = "/platform/delegated";

  public static final String     INVISIBLE       = "invisible";

  private static IdentityManager identityManager;

  public static String formatISO8601(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return ISO8601.format(calendar);
  }

  /**
   * Gets the json media type
   * 
   * @return a media type
   */
  public static MediaType getJsonMediaType() {
    return getMediaType(SUPPORT_TYPE, new String[]{SUPPORT_TYPE});
  }
  
  /**
   * Gets the media type from an expected format string (usually the input) and an array of supported format strings.
   * If expectedFormat is not found in the supported format array, Status.UNSUPPORTED_MEDIA_TYPE is thrown.
   * The supported format must include one of those format: json, xml, atom or rss, otherwise Status.NOT_ACCEPTABLE
   * could be thrown.
   *
   * @param expectedFormat the expected input format
   * @param supportedFormats the supported format array
   * @return the associated media type
   */
  public static MediaType getMediaType(String expectedFormat, String[] supportedFormats) {

    if (!isSupportedFormat(expectedFormat, supportedFormats)) {
      throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    if (expectedFormat.equals("json") && isSupportedFormat("json", supportedFormats)) {
      return MediaType.APPLICATION_JSON_TYPE;
    } else if (expectedFormat.equals("xml") && isSupportedFormat("xml", supportedFormats)) {
      return MediaType.APPLICATION_XML_TYPE;
    } else if (expectedFormat.equals("atom") && isSupportedFormat("atom", supportedFormats)) {
      return MediaType.APPLICATION_ATOM_XML_TYPE;
    }
    //TODO What's about RSS format?
    throw new WebApplicationException(Status.NOT_ACCEPTABLE);
  }

  /**
   * Checks if an expected format is supported not not.
   *
   * @param expectedFormat the expected format
   * @param supportedFormats the array of supported format
   * @return true or false
   */
  private static boolean isSupportedFormat(String expectedFormat, String[] supportedFormats) {
    for (String supportedFormat : supportedFormats) {
      if (supportedFormat.equals(expectedFormat)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Check if the authenticated user is a member of the admin group
   * 
   * @return
   */
  public static boolean isMemberOfAdminGroup() {
    return ConversationState.getCurrent().getIdentity().isMemberOf(ADMIN_GROUP);
  }

  /**
   * Check if the authenticated user is a member of the delegated group
   *
   * @return
   */
  public static boolean isMemberOfDelegatedGroup() {
    return ConversationState.getCurrent().getIdentity().isMemberOf(DELEGATED_GROUP);
  }
  
  /** 
   * Get base url of rest service
   * 
   * @param type the type of rest service
   * @param id the id of object
   * 
   * @return base rest url like : http://localhost:8080/rest/v1/social/users/123456
   */
  public static String getRestUrl(String type, String id, String restPath) {
    String version = restPath.split("/")[1]; // path /v1/social/identities
    String socialResource = restPath.split("/")[2]; // path /v1/social/identities
    
    return new StringBuffer(getBaseRestUrl())
    .append("/").append(version)
    .append("/").append(socialResource)
    .append("/").append(type)
    .append("/").append(id).toString();
  }

  /** 
   * Get base url of rest service
   * 
   * @return base rest url like : https://localhost:8080/rest
   */
  public static String getBaseRestUrl() {
    return new StringBuffer(getBaseUrl()).append("/")
                                         .append(PortalContainer.getCurrentPortalContainerName())
                                         .append("/")
                                         .append(CommonsUtils.getRestContextName())
                                         .toString();
  }

  /** 
   * Get base url of rest service
   * 
   * @return base rest url like : https://localhost:8080
   */
  public static String getBaseUrl() {
    if (ApplicationContextImpl.getCurrent() == null) {
      return StringUtils.EMPTY;
    }
    String fullUrl = ApplicationContextImpl.getCurrent().getBaseUri().toString();
    return (fullUrl != null && !fullUrl.isEmpty()) ? fullUrl.substring(0, fullUrl.indexOf("/", 9)) : "";
  }

  /**
   * Check if the current user is authenticated
   * 
   * @return true if user not authenticated
   */
  public static boolean isAnonymous() {
    return ConversationState.getCurrent() == null
        || ConversationState.getCurrent().getIdentity() == null
        || IdentityConstants.ANONIM.equals(ConversationState.getCurrent().getIdentity().getUserId());
  }

  public static String getPathParam(UriInfo uriInfo, String name) {
    return uriInfo.getPathParameters().getFirst(name);
  }

  public static String getQueryParam(UriInfo uriInfo, String name) {
    return uriInfo.getQueryParameters().getFirst(name);
  }

  public static int getLimit(UriInfo uriInfo) {
    Integer limit = getIntegerValue(uriInfo, "limit");
    return (limit != null && limit > 0) ? Math.min(HARD_LIMIT, limit) : DEFAULT_LIMIT;
  }

  public static int getOffset(UriInfo uriInfo) {
    Integer offset = getIntegerValue(uriInfo, "offset");
    return (offset != null) ? offset : 0;
  }

  public static Integer getIntegerValue(UriInfo uriInfo, String name) {
    String value = getQueryParam(uriInfo, name);
    if (value == null)
      return null;
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static Long getLongValue(UriInfo uriInfo, String name) {
    String value = getQueryParam(uriInfo, name);
    if (value == null)
      return null;
    try {
      return Long.valueOf(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static boolean isReturnSize(UriInfo uriInfo) {
    return Boolean.parseBoolean(getQueryParam(uriInfo, "returnSize"));
  }
  
  public static DataEntity extractInfo(DataEntity inEntity, List<String> returnedProperties) {
    DataEntity outEntity = new DataEntity();
    for (Map.Entry<String, Object> entry : inEntity.entrySet()) {
      if (returnedProperties.contains(entry.getKey())) {
        outEntity.setProperty(entry.getKey(), entry.getValue());
      }
    }
    return outEntity;
  }
  
  public static long getBaseTime(String baseDateTime) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date date = sdf.parse(baseDateTime);
    return date.getTime(); 
  }

  public static final Identity getUserIdentity(String user) {
    return isAnonymous() ? null : getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, user);
  }

  public static final Identity getCurrentUserIdentity() {
    return isAnonymous() ? null : getUserIdentity(getCurrentUser());
  }

  public static final String getCurrentUser() {
    return isAnonymous() ? null : ConversationState.getCurrent().getIdentity().getUserId();
  }


  /**
   * gets online identities who are members of a space.
   *
   * @param userStateService {@link UserStateService}
   * @param userId The current user.
   * @param space The space of which extract members.
   * @param limit Maximum number of identities to return.
   * @return identity array.
   */
  public static Identity[] getOnlineIdentitiesOfSpace(UserStateService userStateService, String userId, Space space, int limit) {
    List<Identity> identities = new ArrayList<>();
    String[] spaceMembers = space.getMembers();
    String superUserName = ExoContainerContext.getService(UserACL.class).getSuperUser();
    if (userStateService == null) {
      userStateService = ExoContainerContext.getService(UserStateService.class);
    }
    for (String user : spaceMembers) {
      UserStateModel userModel = userStateService.getUserState(user);
      boolean isOnline = userStateService.isOnline(user);
      if (!user.equals(userId) && !user.equals(superUserName) && userModel != null && !INVISIBLE.equals(userModel.getStatus()) && isOnline) {
        Identity userIdentity = getIdentityManager().getOrCreateUserIdentity(user);
        identities.add(userIdentity);
        if (identities.size() == limit) {
          break;
        }
      }
    }
    return identities.toArray(new Identity[identities.size()]);
  }

  /**
   * gets online identities.
   *
   * @param userStateService {@link UserStateService}
   * @param userId The current user.
   * @param limit Maximum number of identities to return.
   * @return identity array.
   */
  public static Identity[] getOnlineIdentities(UserStateService userStateService, String userId, int limit) {
    List<Identity> identities = new ArrayList<>();
    if (userStateService == null) {
      userStateService = ExoContainerContext.getService(UserStateService.class);
    }
    List<UserStateModel> users = userStateService.online();
    Collections.reverse(users);
    if (users.size() > limit) {
      users = users.subList(0, limit);
    }
    UserACL userACL = ExoContainerContext.getService(UserACL.class);
    String superUserName = userACL.getSuperUser();
    for (UserStateModel userModel : users) {
      String user = userModel == null ? null : userModel.getUserId();
      if (StringUtils.isBlank(user) || user.equals(userId) || user.equals(superUserName)
          || INVISIBLE.equals(userModel.getStatus())) {
        continue;
      }
      Identity userIdentity = getIdentityManager().getOrCreateUserIdentity(user);
      identities.add(userIdentity);
    }
    return identities.toArray(new Identity[identities.size()]);
  }

  public static IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = ExoContainerContext.getService(IdentityManager.class);
    }
    return identityManager;
  }
}
