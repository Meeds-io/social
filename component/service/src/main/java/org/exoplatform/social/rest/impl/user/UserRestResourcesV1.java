/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.rest.impl.user;

import io.swagger.annotations.*;
import io.swagger.jaxrs.PATCH;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.rest.UserFieldValidator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.user.UserStateModel;
import org.exoplatform.services.user.UserStateService;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ActivityFile;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.UpdateType;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.model.Attachment;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.rest.api.*;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.json.JSONObject;
import org.picocontainer.Startable;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.exoplatform.social.rest.api.RestUtils.getCurrentUser;
import static org.exoplatform.social.rest.api.RestUtils.getUserIdentity;

/**
 * 
 * Provides REST Services for manipulating jobs related to users.
 * 
 */

@Path(VersionResources.VERSION_ONE + "/social/users")
@Api(tags = VersionResources.VERSION_ONE + "/social/users", value = VersionResources.VERSION_ONE + "/social/users", description = "Operations on users with their activities, connections and spaces")
public class UserRestResourcesV1 implements UserRestResources, Startable {

  public static final String  PROFILE_DEFAULT_BANNER_URL = "/skin/images/banner/DefaultUserBanner.png";

  public static final String  PROFILE_DEFAULT_AVATAR_URL = "/skin/images/avatar/DefaultUserAvatar.png";

  private static final int    DEFAULT_AVATAR_HASH = (int) (Math.random() * Integer.MAX_VALUE);

  private static final int    DEFAULT_BANNER_HASH = (int) (Math.random() * Integer.MAX_VALUE);

  private static final String ONLINE              = "online";

  private static final CacheControl CACHE_CONTROL               = new CacheControl();

  private static final Date         DEFAULT_IMAGES_LAST_MODIFED = new Date();

  // 3 days
  private static final int          CACHE_IN_SECONDS            = 3 * 86400;

  private static final int          CACHE_IN_MILLI_SECONDS      = CACHE_IN_SECONDS * 1000;

  public static final UserFieldValidator       USERNAME_VALIDATOR             = new UserFieldValidator("userName", true, false);

  public static final UserFieldValidator       EMAIL_VALIDATOR                = new UserFieldValidator("email", false, false);

  public static final UserFieldValidator       LASTNAME_VALIDATOR             = new UserFieldValidator("lastName", false, true);

  public static final UserFieldValidator       FIRSTNAME_VALIDATOR            = new UserFieldValidator("firstName", false, true);

  public static final UserFieldValidator       PASSWORD_VALIDATOR             = new UserFieldValidator("password", false, false, 8, 255);

  public static final List<UserFieldValidator> USER_FIELD_VALIDATORS          = Arrays.asList(USERNAME_VALIDATOR,
                                                                                              EMAIL_VALIDATOR,
                                                                                              LASTNAME_VALIDATOR,
                                                                                              FIRSTNAME_VALIDATOR,
                                                                                              PASSWORD_VALIDATOR);

  private static Map<String, UserImportResultEntity> importUsersProcessing       = new HashMap<>();

  private UserACL userACL;

  private OrganizationService organizationService;

  private IdentityManager identityManager;
  
  private RelationshipManager relationshipManager;

  private UserStateService userStateService;

  private SpaceService spaceService;

  public static enum ACTIVITY_STREAM_TYPE {
    all, owner, connections, spaces
  }

  private static final String INVISIBLE = "invisible";

  private static final Log LOG = ExoLogger.getLogger(UserRestResourcesV1.class);

  private byte[]              defaultUserAvatar = null;

  private byte[]              defaultUserBanner = null;

  private UploadService       uploadService;

  private ExecutorService     importExecutorService = null;

  public UserRestResourcesV1(UserACL userACL,
                             OrganizationService organizationService,
                             IdentityManager identityManager,
                             RelationshipManager relationshipManager,
                             UserStateService userStateService,
                             SpaceService spaceService,
                             UploadService uploadService) {
    this.userACL = userACL;
    this.organizationService = organizationService;
    this.identityManager = identityManager;
    this.relationshipManager = relationshipManager;
    this.userStateService = userStateService;
    this.spaceService = spaceService;
    this.uploadService = uploadService;
    this.importExecutorService = Executors.newSingleThreadExecutor();

    CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
  }

  @Override
  public void start() {
    // Nothing to start
  }

  @Override
  public void stop() {
    this.importExecutorService.shutdownNow();
  }

  @GET
  @RolesAllowed("users")
  @ApiOperation(value = "Gets all users",
                httpMethod = "GET",
                response = Response.class,
                notes = "Using the query param \"q\" to filter the target users, ex: \"q=jo*\" returns all the users beginning by \"jo\"."
                + "Using the query param \"status\" to filter the target users, ex: \"status=online*\" returns the visible online users."
                + "Using the query params \"status\" and \"spaceId\" together to filter the target users, ex: \"status=online*\" and \"spaceId=1*\" returns the visible online users who are member of space with id=1."
                + "The params \"status\" and \"spaceId\" cannot be used with \"q\" param since it will falsify the \"limit\" param which is 20 by default. If these 3 parameters are used together, the parameter \"q\" will be ignored")
  @ApiResponses(value = {
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 404, message = "Resource not found"),
    @ApiResponse (code = 500, message = "Internal server error due to data encoding"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getUsers(@Context UriInfo uriInfo,
                           @ApiParam(value = "User name information to filter, ex: user name, last name, first name or full name", required = false) @QueryParam("q") String q,
                           @ApiParam(value = "User status to filter online users, ex: online", required = false) @QueryParam("status") String status,
                           @ApiParam(value = "Space id to filter only its members, ex: 1", required = false) @QueryParam("spaceId") String spaceId,
                           @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                           @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                           @ApiParam(value = "Returning the number of users found or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                           @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    Identity[] identities;
    int totalSize = 0;

    if (StringUtils.isNotBlank(status) && ONLINE.equals(status)) {
      String userId;
      try {
        userId = ConversationState.getCurrent().getIdentity().getUserId();
      } catch (Exception e) {
        return Response.status(HTTPStatus.UNAUTHORIZED).build();
      }
      if (StringUtils.isBlank(userId)) {
        return Response.status(HTTPStatus.UNAUTHORIZED).build();
      }
      Space space = null;
      if (StringUtils.isNotBlank(spaceId)) {
        space = spaceService.getSpaceById(spaceId);
        if (space != null) {
          identities = getOnlineIdentitiesOfSpace(userId, space, limit);
        } else {
          return EntityBuilder.getResponse(new ErrorResource("space " + spaceId + " does not exist", "space not found"), uriInfo, RestUtils.getJsonMediaType(), Response.Status.NOT_FOUND);
        }
      } else {
        identities = getOnlineIdentities(userId, limit);
      }
    } else {
      ProfileFilter filter = new ProfileFilter();
      filter.setName(q == null || q.isEmpty() ? "" : q);
      filter.setPosition(q == null || q.isEmpty() ? "" : q);
      filter.setSkills(q == null || q.isEmpty() ? "" : q);
      ListAccess<Identity> list = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, false);
      identities = list.load(offset, limit);
      if(returnSize) {
        totalSize = list.getSize();
      }
    }
    List<DataEntity> profileInfos = new ArrayList<DataEntity>();
    for (Identity identity : identities) {
      ProfileEntity profileInfo = EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand);
      //
      profileInfos.add(profileInfo.getDataEntity());
    }
    CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
    if (returnSize) {
      collectionUser.setSize(totalSize);
    }

    return EntityBuilder.getResponse(collectionUser, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Creates a new user",
                httpMethod = "POST",
                response = Response.class,
                notes = "This creates the user if the authenticated user is in the /platform/administrators group.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response addUser(@Context UriInfo uriInfo,
                          @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                          @ApiParam(value = "User object to be created, ex:<br />" +
                                            "{<br />\"username\": \"john\"," +
                                            "<br />\"password\": \"gtngtn\"," +
                                            "<br />\"email\": \"john@exoplatform.com\"," +
                                            "<br />\"firstname\": \"John\"," +
                                            "<br />\"lastname\": \"Smith\"<br />}"
                          		              , required = true) UserEntity model) throws Exception {
    if (model.isNotValid()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    //Check permission of current user
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    //check if the user is already exist
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, model.getUsername());
    if (identity != null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    if(getUserByEmail(model.getEmail()) != null) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    //Create new user
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.createUserInstance(model.getUsername());
    user.setFirstName(model.getFirstname());
    user.setLastName(model.getLastname());
    user.setEmail(model.getEmail());
    user.setPassword(model.getPassword() == null || model.getPassword().isEmpty() ? "exo" : model.getPassword());
    userHandler.createUser(user, true);
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(model.getUsername(), uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a specific user by user name",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 404, message = "Resource not found"),
    @ApiResponse (code = 500, message = "Internal server error due to data encoding"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getUserById(@Context UriInfo uriInfo,
                              @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                              @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
    //
    if (identity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/avatar")
  @ApiOperation(value = "Gets a specific user avatar by username",
          httpMethod = "GET",
          response = Response.class,
          notes = "This can only be done by the logged in user.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 404, message = "Resource not found"),
          @ApiResponse (code = 500, message = "Internal server error due to data encoding"),
          @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getUserAvatarById(@Context UriInfo uriInfo,
                                    @Context Request request,
                                    @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                    @ApiParam(value = "The value of lastModified parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'") @QueryParam("lastModified") String lastModified,
                                    @ApiParam(value = "URL to default avatar Or '404' to return a 404 http code", required = false) @QueryParam("default") String defaultAvatar) throws IOException {

    boolean isDefault = StringUtils.equals(LinkProvider.DEFAULT_IMAGE_REMOTE_ID, id);
    Identity identity = isDefault ? null : identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
    Long lastUpdated = null;

    Response.ResponseBuilder builder = null;
    if (!isDefault) {
      if (identity == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        Profile profile = identity.getProfile();
        if (profile != null) {
          lastUpdated = profile.getAvatarLastUpdated();
        }
      }
    }

    EntityTag eTag = null;
    if (lastUpdated == null) {
      eTag = new EntityTag(String.valueOf(DEFAULT_AVATAR_HASH));
    } else {
      eTag = new EntityTag(String.valueOf(lastUpdated.hashCode()));
    }

    if (identity != null && identity.isEnable() && !identity.isDeleted()) {
      builder = request.evaluatePreconditions(eTag);
      if (builder == null && lastUpdated != null) {
        InputStream stream = identityManager.getAvatarInputStream(identity);
        if (stream != null) {
          /*
           * As recommended in the the RFC1341
           * (https://www.w3.org/Protocols/rfc1341/4_Content-Type.html), we
           * set the avatar content-type to "image/png". So, its data would be
           * recognized as "image" by the user-agent
           */
          builder = Response.ok(stream, "image/png");
        }
      }
    }

    if (builder == null || lastUpdated == null) {
      builder = getDefaultAvatarBuilder(defaultAvatar);
    }

    builder.tag(eTag);
    builder.cacheControl(CACHE_CONTROL);
    builder.lastModified(lastUpdated == null ? DEFAULT_IMAGES_LAST_MODIFED : new Date(lastUpdated));
    // If the query has a lastModified parameter, it means that the client
    // will change the lastModified entry when it really changes
    // Which means that we can cache the image in browser side
    // for a long time
    if (StringUtils.isNotBlank(lastModified)) {
      builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
    }
    return builder.build();
  }

  @GET
  @Path("{id}/banner")
  @ApiOperation(value = "Gets a specific user banner by username",
          httpMethod = "GET",
          response = Response.class,
          notes = "This can only be done by the logged in user.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 404, message = "Resource not found"),
          @ApiResponse (code = 500, message = "Internal server error due to data encoding"),
          @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getUserBannerById(@Context UriInfo uriInfo,
                                    @Context Request request,
                                    @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                    @ApiParam(value = "The value of lastModified parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'") @QueryParam("lastModified") String lastModified,
                                    @ApiParam(value = "URL to default banner Or '404' to return a 404 http code", required = false) @QueryParam("default") String defaultBanner) throws IOException {

    boolean isDefault = StringUtils.equals(LinkProvider.DEFAULT_IMAGE_REMOTE_ID, id);
    Identity identity = isDefault ? null : identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
    Long lastUpdated = null;

    Response.ResponseBuilder builder = null;
    if (!isDefault) {
      if (identity == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        Profile profile = identity.getProfile();
        if (profile != null) {
          lastUpdated = profile.getBannerLastUpdated();
        }
      }
    }

    EntityTag eTag = null;
    if (lastUpdated == null) {
      eTag = new EntityTag(String.valueOf(DEFAULT_BANNER_HASH));
    } else {
      eTag = new EntityTag(String.valueOf(lastUpdated.hashCode()));
    }

    if (identity != null && identity.isEnable() && !identity.isDeleted()) {
      builder = request.evaluatePreconditions(eTag);
      if (builder == null && lastUpdated != null) {
        InputStream stream = identityManager.getBannerInputStream(identity);
        if (stream != null) {
          /*
           * As recommended in the the RFC1341
           * (https://www.w3.org/Protocols/rfc1341/4_Content-Type.html), we
           * set the Banner content-type to "image/png". So, its data would be
           * recognized as "image" by the user-agent
           */
          builder = Response.ok(stream, "image/png");
        }
      }
    }

    if (builder == null || lastUpdated == null) {
      builder = getDefaultBannerBuilder(defaultBanner);
    }

    builder.tag(eTag);
    builder.cacheControl(CACHE_CONTROL);
    builder.lastModified(lastUpdated == null ? DEFAULT_IMAGES_LAST_MODIFED : new Date(lastUpdated));
    // If the query has a lastModified parameter, it means that the client
    // will change the lastModified entry when it really changes
    // Which means that we can cache the image in browser side
    // for a long time
    if (StringUtils.isNotBlank(lastModified)) {
      builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
    }
    return builder.build();
  }

  @PATCH
  @Path("{id}")
  @ApiOperation(value = "Update user property", httpMethod = "PATCH", response = Response.class, notes = "This can only be done by the logged in user.")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Request fulfilled but not content returned"),
      @ApiResponse(code = 500, message = "Internal server error due to data encoding"),
      @ApiResponse(code = 403, message = "Unothorized to modify user profile"),
      @ApiResponse(code = 400, message = "Invalid query input") })
  public Response updateUserProfileAttribute(@ApiParam(value = "User name", required = true) @PathParam("id") String username,
                                             @ApiParam(value = "User profile attribute name", required = true) @FormParam("name") String name,
                                             @ApiParam(value = "User profile attribute value", required = true) @FormParam("value") String value) throws IOException {
    if (StringUtils.isBlank(name)) {
      return Response.status(Status.BAD_REQUEST).entity("'name' parameter is mandatory").build();
    }
    if (value == null) {
      return Response.status(Status.BAD_REQUEST).entity("'value' parameter is mandatory").build();
    }
    if (StringUtils.isBlank(username)) {
      return Response.status(Status.BAD_REQUEST).entity("'username' path parameter is empty").build();
    }
    String currentUser = getCurrentUser();
    if (!StringUtils.equals(currentUser, username)) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    Identity userIdentity = getUserIdentity(username);
    Profile profile = userIdentity.getProfile();
    try {
      String fieldName = ProfileEntity.getFieldName(name);
      updateProfileField(profile, fieldName, value, true);
    } catch (IdentityStorageException e) {
      return Response.serverError().entity(e.getMessageKey()).build();
    } catch (Exception e) {
      return Response.serverError().entity("Can't update avatar, error = " + e.getMessage()).build();
    }
    return Response.noContent().build();
  }

  @PATCH
  @Path("{id}/profile")
  @ApiOperation(value = "Update set of properties in user profile",
                httpMethod = "PATCH",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = {
      @ApiResponse (code = 204, message = "Request fulfilled but not content returned"),
      @ApiResponse (code = 500, message = "Internal server error due to data encoding"),
      @ApiResponse (code = 403, message = "Unothorized to modify user profile"),
      @ApiResponse (code = 400, message = "Invalid query input") })
  public Response updateUserProfileAttributes(@Context HttpServletRequest request,
                                              @ApiParam(value = "User name", required = true) @PathParam("id") String username,
                                              @ApiParam(value = "User profile attributes map", required = true) ProfileEntity profileEntity) throws Exception {
    if (StringUtils.isBlank(username)) {
      return Response.status(Status.BAD_REQUEST).entity("'username' path parameter is empty").build();
    }
    if (profileEntity == null) {
      return Response.status(Status.BAD_REQUEST).entity("Use profile entity is mandatory").build();
    }

    Locale locale = request == null ? Locale.ENGLISH : request.getLocale();

    String firstName = profileEntity.getFirstname();
    String lastName = profileEntity.getLastname();
    String email = profileEntity.getEmail();

    if (StringUtils.isNotBlank(firstName)) {
      String errorMessage = FIRSTNAME_VALIDATOR.validate(locale, firstName);
      if (StringUtils.isNotBlank(errorMessage)) {
        return Response.status(Response.Status.BAD_REQUEST).entity("FIRSTNAME:" + errorMessage).build();
      }
    }
    if (StringUtils.isNotBlank(lastName)) {
      String errorMessage = LASTNAME_VALIDATOR.validate(locale, lastName);
      if (StringUtils.isNotBlank(errorMessage)) {
        return Response.status(Response.Status.BAD_REQUEST).entity("LASTNAME:" + errorMessage).build();
      }
    }
    if (StringUtils.isNoneBlank(email)) {
      String errorMessage = EMAIL_VALIDATOR.validate(locale, email);
      if (StringUtils.isNotBlank(errorMessage)) {
        return Response.status(Response.Status.BAD_REQUEST).entity("EMAIL:" + errorMessage).build();
      }
      // Check if mail address is already used
      Query query = new Query();
      query.setEmail(email);
      ListAccess<User> users = organizationService.getUserHandler().findUsersByQuery(query, UserStatus.ANY);
      if (users.getSize() > 0 && !StringUtils.equals(users.load(0, 1)[0].getUserName(), username)) {
        return Response.status(Response.Status.BAD_REQUEST).entity("EMAIL:ALREADY_EXISTS").build();
      }
    }


    String currentUser = getCurrentUser();
    if (!StringUtils.equals(currentUser, username)) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    String errorMessage = saveProfile(username, profileEntity);
    if (StringUtils.isNotBlank(errorMessage)) {
      return Response.ok(errorMessage).build();
    }
    return Response.noContent().build();
  }

  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific user by user name",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This deletes the user if the authenticated user is in the /platform/administrators group.")
  public Response deleteUserById(@Context UriInfo uriInfo,
                                 @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                 @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    //Check permission of current user
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    identityManager.hardDeleteIdentity(identity);
    identity.setDeleted(true);
    // Deletes the user on Portal side
    UserHandler userHandler = organizationService.getUserHandler();
    userHandler.removeUser(id, false);
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific user by user name",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This updates the user if he is the authenticated user.")
  public Response updateUserById(@Context UriInfo uriInfo,
                                 @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                 @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                                 @ApiParam(value = "User object to be updated, ex:<br />" +
                                            "{<br />\"username\": \"john\"," +
                                            "<br />\"password\": \"gtngtn\"," +
                                            "<br />\"email\": \"john@exoplatform.com\"," +
                                            "<br />\"firstname\": \"John\"," +
                                            "<br />\"lastname\": \"Smith\"<br />}", required = true) UserEntity model) throws Exception {
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.findUserByName(id);
    if (user == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    //Check if the current user is the authenticated user
    if (!ConversationState.getCurrent().getIdentity().getUserId().equals(id)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    if(getUserByEmail(model.getEmail()) != null && 
        !user.getUserName().equals(getUserByEmail(model.getEmail()).getUserName())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    fillUserFromModel(user, model);
    userHandler.saveUser(user, true);
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(id, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("{id}/connections")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets connections of a specific user",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  public Response getConnectionsOfUser(@Context UriInfo uriInfo,
                                      @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                      @ApiParam(value = "User name information to filter, ex: user name, last name, first name or full name", required = false) @QueryParam("q") String q,
                                      @ApiParam(value = "Returning the number of connections or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                      @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    Identity target = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    int limit = RestUtils.getLimit(uriInfo);
    int offset = RestUtils.getOffset(uriInfo);
    
    List<DataEntity> profileInfos = new ArrayList<DataEntity>();
    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setName(q);
    ListAccess<Identity> listAccess = relationshipManager.getConnectionsByFilter(target, profileFilter);
    Identity []identities = listAccess.load(offset, limit);
    for (Identity identity : identities) {
      ProfileEntity profileInfo = EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand);
      //
      profileInfos.add(profileInfo.getDataEntity());
    }
    CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
    if(returnSize) {
      collectionUser.setSize(listAccess.getSize());
    }
    return EntityBuilder.getResponse(collectionUser, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("connections/invitations")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets received invitations of current user", httpMethod = "GET", response = Response.class, notes = "This can only be done by the logged in user.")
  public Response getInvitationsOfUser(@Context UriInfo uriInfo,
                                       @ApiParam(value = "Returning the number of connections or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                       @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity target = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    int limit = RestUtils.getLimit(uriInfo);
    int offset = RestUtils.getOffset(uriInfo);

    List<DataEntity> profileInfos = new ArrayList<>();
    ListAccess<Identity> listAccess = relationshipManager.getIncomingWithListAccess(target);
    Identity[] identities = listAccess.load(offset, limit);
    for (Identity identity : identities) {
      ProfileEntity profileInfo = EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand);
      //
      profileInfos.add(profileInfo.getDataEntity());
    }
    CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
    if (returnSize) {
      collectionUser.setSize(listAccess.getSize());
    }
    return EntityBuilder.getResponse(collectionUser, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("connections/pending")
  @ApiOperation(value = "Gets received invitations of current user", httpMethod = "GET", response = Response.class, notes = "This can only be done by the logged in user.")
  public Response getPendingOfUser(@Context UriInfo uriInfo,
                                   @ApiParam(value = "Returning the number of connections or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                   @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity target = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    int limit = RestUtils.getLimit(uriInfo);
    int offset = RestUtils.getOffset(uriInfo);

    List<DataEntity> profileInfos = new ArrayList<>();
    ListAccess<Identity> listAccess = relationshipManager.getOutgoing(target);
    Identity[] identities = listAccess.load(offset, limit);
    for (Identity identity : identities) {
      ProfileEntity profileInfo = EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand);
      //
      profileInfos.add(profileInfo.getDataEntity());
    }
    CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
    if (returnSize) {
      collectionUser.setSize(listAccess.getSize());
    }
    return EntityBuilder.getResponse(collectionUser, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("{id}/spaces")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets spaces of a specific user",
          httpMethod = "GET",
          response = Response.class,
          notes = "This returns a list of spaces in the following cases: <br/><ul><li>the given user is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  public Response getSpacesOfUser(@Context UriInfo uriInfo,
                                  @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                  @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                  @ApiParam(value = "Returning the number of spaces or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                  @ApiParam(value = "Asking for a full representation of a specific subresource, ex: <em>members</em> or <em>managers</em>", required = false) @QueryParam("expand") String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    Identity target = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
    //Check if the given user exists
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    //Check permission of authenticated user : he must be an admin or he is the given user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (!userACL.getSuperUser().equals(authenticatedUser) && !authenticatedUser.equals(id)) {
      //Check permission of spaces to retrieve owner : authenticated user must be in a confirmed relationship with spaces to retrieve's owner
      Identity authenticatedUserIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);
      Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
      Relationship relationship = relationshipManager.get(authenticatedUserIdentity, userIdentity);
      if (relationship == null || relationship.getStatus() != Relationship.Type.CONFIRMED) {
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }
    }

    List<DataEntity> spaceInfos = new ArrayList<DataEntity>();
    ListAccess<Space> listAccess = CommonsUtils.getService(SpaceService.class).getMemberSpaces(id);

    for (Space space : listAccess.load(offset, limit)) {
      SpaceEntity spaceInfo = EntityBuilder.buildEntityFromSpace(space, id, uriInfo.getPath(), expand);
      //
      spaceInfos.add(spaceInfo.getDataEntity());
    }
    CollectionEntity collectionSpace = new CollectionEntity(spaceInfos, EntityBuilder.SPACES_TYPE, offset, limit);
    if (returnSize) {
      collectionSpace.setSize(listAccess.getSize());
    }

    return EntityBuilder.getResponse(collectionSpace, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("{userId}/spaces/{profileId}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets commons spaces of current user",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns a list of commons spaces in the following cases: <br/><ul><li>the given user is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  public Response getCommonSpacesOfUser(@Context UriInfo uriInfo,
                                        @ApiParam(value = "User Id", required = true) @PathParam("userId") String userId,
                                        @ApiParam(value = "Profile Id", required = true) @PathParam("profileId") String profileId,
                                        @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                        @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                        @ApiParam(value = "Returning the number of spaces or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                        @ApiParam(value = "Asking for a full representation of a specific subresource, ex: <em>members</em> or <em>managers</em>", required = false) @QueryParam("expand") String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
    Identity userProfile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, profileId);
    //Check if the current user and profile user exists
    if (currentUser == null || userProfile == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    //Check permission of authenticated user : he must be an admin or he is the given user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (!userACL.getSuperUser().equals(authenticatedUser) && !authenticatedUser.equals(userId) ) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    List<String> userSpaceInfos = new ArrayList<String>();
    List<String> profileSpaceInfos = new ArrayList<String>();
    List<DataEntity> commonSpaceInfos = new ArrayList<DataEntity>();

    ListAccess<Space> cureentUserListAccess = CommonsUtils.getService(SpaceService.class).getMemberSpaces(userId);
    ListAccess<Space> profileUserlistAccess = CommonsUtils.getService(SpaceService.class).getMemberSpaces(profileId);

    for (Space space : cureentUserListAccess.load(offset, limit)) {
      SpaceEntity spaceInfo = EntityBuilder.buildEntityFromSpace(space, userId, uriInfo.getPath(), expand);
      userSpaceInfos.add(spaceInfo.getId());
    }

    for (Space space : profileUserlistAccess.load(offset, limit)) {
      SpaceEntity spaceInfo = EntityBuilder.buildEntityFromSpace(space, profileId, uriInfo.getPath(), expand);
      profileSpaceInfos.add(spaceInfo.getId());
    }

    List<String> commonSpacesId = userSpaceInfos.stream()
                                                .filter(profileSpaceInfos::contains)
                                                .collect(Collectors.toList());

    for(String id : commonSpacesId){
      SpaceEntity spaceInfo = EntityBuilder.buildEntityFromSpace(spaceService.getSpaceById(id), profileId, uriInfo.getPath(), expand);
      commonSpaceInfos.add(spaceInfo.getDataEntity());
    }

    CollectionEntity collectionCommonSpace = new CollectionEntity(commonSpaceInfos, EntityBuilder.SPACES_TYPE, offset, limit);

    if (returnSize) {
      collectionCommonSpace.setSize( commonSpaceInfos.size());
    }
    return EntityBuilder.getResponse(collectionCommonSpace, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/activities")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets activities of a specific user",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns an activity in the list in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user or one of his connections</li><li>this is a space activity and the authenticated user is a member of the space</li></ul>")
  public Response getActivitiesOfUser(@Context UriInfo uriInfo,
                                      @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                      @ApiParam(value = "Activity stream type, ex: <em>owner, connections, spaces</em> or <em>all</em>", required = false, defaultValue = "all") @QueryParam("type") String type,
                                      @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                      @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                      @ApiParam(value = "Base time to load older activities (yyyy-MM-dd HH:mm:ss)", required = false) @QueryParam("before") String before,
                                      @ApiParam(value = "Base time to load newer activities (yyyy-MM-dd HH:mm:ss)", required = false) @QueryParam("after") String after,
                                      @ApiParam(value = "Returning the number of activities or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                      @ApiParam(value = "Asking for a full representation of a specific subresource, ex: <em>comments</em> or <em>likes</em>", required = false) @QueryParam("expand") String expand) throws Exception {
    
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //Check if the given user doesn't exist
    Identity target = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    ACTIVITY_STREAM_TYPE streamType;
    try {
      streamType = ACTIVITY_STREAM_TYPE.valueOf(type);
    } catch (Exception e) {
      streamType = ACTIVITY_STREAM_TYPE.all;
    }

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    RealtimeListAccess<ExoSocialActivity> listAccess = null;
    List<ExoSocialActivity> activities = null;
    switch (streamType) {
      case all: {
        listAccess = activityManager.getActivityFeedWithListAccess(target);
        break;
      }
      case owner: {
        listAccess = activityManager.getActivitiesWithListAccess(target);
        break;
      }
      case connections: {
        listAccess = activityManager.getActivitiesOfConnectionsWithListAccess(target);
        break;
      }
      case spaces: {
        listAccess = activityManager.getActivitiesOfUserSpacesWithListAccess(target);
        break;
      }
      default:
        break;
    }
    //
    if (after != null && RestUtils.getBaseTime(after) > 0) {
      activities = listAccess.loadNewer(RestUtils.getBaseTime(after), limit);
    } else if (before != null && RestUtils.getBaseTime(before) > 0) {
      activities = listAccess.loadOlder(RestUtils.getBaseTime(before), limit);
    } else {
      activities = listAccess.loadAsList(offset, limit);
    }
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);
    List<DataEntity> activityEntities = new ArrayList<DataEntity>();
    for (ExoSocialActivity activity : activities) {
      DataEntity as = EntityBuilder.getActivityStream(activity, currentUser);
      if (as == null) continue;
      ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand);
      activityEntity.setActivityStream(as);
      //
      activityEntities.add(activityEntity.getDataEntity()); 
    }
    CollectionEntity collectionActivity = new CollectionEntity(activityEntities, EntityBuilder.ACTIVITIES_TYPE,  offset, limit);
    if(returnSize) {
      if (before != null || after != null) {
        collectionActivity.setSize(activities.size());
      } else {
        collectionActivity.setSize(listAccess.getSize());
      }
    }
    return EntityBuilder.getResponse(collectionActivity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @Path("{id}/activities")
  @RolesAllowed("users")
  @ApiOperation(value = "Creates an activity by a specific user",
                httpMethod = "POST",
                response = Response.class,
                notes = "This creates the activity if the given user is the authenticated user.")
  public Response addActivityByUser(@Context UriInfo uriInfo,
                                    @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                    @ApiParam(value = "Asking for a full representation of a specific subresource, ex: <em>comments</em> or <em>likes</em>", required = false) @QueryParam("expand") String expand,
                                    @ApiParam(value = "Activity object to be created, in which the title of activity is required, ex: <br/>{\"title\": \"act4 posted\"}", required = true) ActivityRestIn model) throws Exception {
    if (model == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //Check if the given user doesn't exist
    Identity target = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id);
    if (target == null || !ConversationState.getCurrent().getIdentity().getUserId().equals(id)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(model.getTitle());
    activity.setBody(model.getBody());
    activity.setType(model.getType());
    activity.setUserId(target.getId());
    if(model.getFiles() != null) {
      activity.setFiles(model.getFiles()
              .stream()
              .map(fileModel -> new ActivityFile(fileModel.getId(), fileModel.getUploadId(), fileModel.getStorage(), fileModel.getDestinationFolder()))
              .collect(Collectors.toList()));
    }
    activity.setTemplateParams(model.getTemplateParams());
    CommonsUtils.getService(ActivityManager.class).saveActivityNoReturn(target, activity);

    logMetrics(activity);

    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("csv")
  @RolesAllowed("administrators")
  @ApiOperation(value = "Import users using CSV file that has a header defining user fields names."
      + "exemple of first line of CSV file: userName,firstName,lastName,password,email,groups,aboutMe,timeZone,company,position",
  httpMethod = "POST",
  response = Response.class)
  public Response importUsers(@Context HttpServletRequest request,
                              @ApiParam(value = "CSV File uploadId retrieved after uploading", required = true) @FormParam("uploadId") String uploadId,
                              @ApiParam(value = "Get processing progress percentage of imported file", required = false, defaultValue = "false") @FormParam("progress") boolean progress,
                              @ApiParam(value = "Whether clean file after processing or not", required = false, defaultValue = "false") @FormParam("clean") boolean clean,
                              @ApiParam(value = "Whether process importing users in a sync or async way of current request", required = false, defaultValue = "false") @FormParam("sync") boolean sync) {
    if (StringUtils.isBlank(uploadId)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("UPLOAD_ID:MANDATORY").build();
    }

    UploadResource uploadResource = uploadService.getUploadResource(uploadId);
    if (uploadResource == null) {
      return Response.status(Response.Status.NOT_FOUND).entity("UPLOAD_ID:NOT_FOUND").build();
    }

    UserImportResultEntity existingImportResult = importUsersProcessing.get(uploadId);
    if (clean || progress) {
      if (existingImportResult == null) {
        return Response.status(Response.Status.NOT_FOUND).entity("UPLOAD_ID_PROGRESS:NOT_FOUND").build();
      }
      if (clean) {
        uploadService.removeUploadResource(uploadId);
        importUsersProcessing.remove(uploadId);
      }
      return Response.ok(existingImportResult.clone()).build();
    } else if (existingImportResult != null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("UPLOAD_ID_PROCESSING:ALREADY_PROCESSING").build();
    }

    Locale locale = request == null ? Locale.ENGLISH : request.getLocale();
    Response errorResponse = importUsers(uploadId, uploadResource.getStoreLocation(), locale, sync);
    return errorResponse == null ? Response.noContent().build() : errorResponse;
  }

  private Response importUsers(String uploadId, String fileLocation, Locale locale, boolean sync) {
    UserImportResultEntity userImportResultEntity = new UserImportResultEntity();
    importUsersProcessing.put(uploadId, userImportResultEntity);

    // count file lines
    try (BufferedReader reader = new BufferedReader(new FileReader(fileLocation))) {
      userImportResultEntity.setCount(reader.lines().count() - 1);
    } catch (FileNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity("UPLOAD_ID_FILE:NOT_FOUND").build();
    } catch (IOException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ERROR_READING_FILE").build();
    }

    if (userImportResultEntity.getCount() < 1) {
      return Response.status(Response.Status.BAD_REQUEST).entity("BAD_FORMAT:FILE_EMPTY").build();      
    }

    if (sync) {
      importUsers(fileLocation, userImportResultEntity, locale);
    } else {
      importUsersAsync(fileLocation, userImportResultEntity, locale);
    }
    return null;
  }

  private void importUsersAsync(String fileLocation,
                                UserImportResultEntity userImportResultEntity,
                                Locale locale) {
    importExecutorService.execute(() -> this.importUsers(fileLocation, userImportResultEntity, locale));
  }

  private void importUsers(String fileLocation,
                           UserImportResultEntity userImportResultEntity,
                           Locale locale) {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileLocation))) {
      // Retrieve header line and import others
      String headerLine = null;
      headerLine = reader.readLine();
      if (StringUtils.isBlank(headerLine)) {
        return;
      }
      List<String> fields = Arrays.asList(headerLine.split(","));

      String userCSVLine = reader.readLine();
      while (userCSVLine != null) {
        ExoContainerContext.setCurrentContainer(PortalContainer.getInstance());
        RequestLifeCycle.begin(PortalContainer.getInstance());
        String userName = null;
        try { // NOSONAR
          userImportResultEntity.incrementProcessed();
          if (StringUtils.isBlank(userCSVLine)) {
            continue;
          }

          userName = importUser(userImportResultEntity, locale, fields, userCSVLine);
        } catch (Throwable e) {
          if (StringUtils.isNotBlank(userName)) {
            userImportResultEntity.addErrorMessage(userName, "CREATE_USER_ERROR:" + e.getMessage());
          }
        } finally {
          RequestLifeCycle.end();
        }
        userCSVLine = reader.readLine();
      }
    } catch (Exception e) {
      LOG.error("Error while importing CSV file", e);
    }
  }

  private String importUser(UserImportResultEntity userImportResultEntity,
                            Locale locale,
                            List<String> fields,
                            String userCSVLine) throws Exception {
    List<String> userProperties = Arrays.asList(userCSVLine.split(","));
    JSONObject userObject = new JSONObject();
    for (int i = 0; i < fields.size(); i++) {
      if (i < userProperties.size()) {
        userObject.put(fields.get(i), userProperties.get(i));
      }
    }
    UserImpl user = EntityBuilder.fromJsonString(userObject.toString(), UserImpl.class);
    String userName = user.getUserName();
    if (StringUtils.isBlank(userName)) {
      userImportResultEntity.addErrorMessage(userName, "BAD_LINE_FORMAT:MISSING_USERNAME");
      return userName;
    }
    if (userProperties.size() != fields.size()) {
      userImportResultEntity.addErrorMessage(userName, "BAD_LINE_FORMAT");
      return userName;
    }

    String errorMessage = null;
    try {
      errorMessage = validateUser(userObject, locale);
    } catch (Exception e) {
      errorMessage = "USER_VALIDATION_ERROR:" + e.getMessage();
    }
    if (StringUtils.isNotBlank(errorMessage)) {
      userImportResultEntity.addErrorMessage(userName, errorMessage);
      return userName;
    }

    User existingUser = organizationService.getUserHandler().findUserByName(userName, UserStatus.ANY);
    if (existingUser != null) {
      userImportResultEntity.addErrorMessage(userName, "USERNAME:ALREADY_EXISTS");
      return userName;
    }

    existingUser = getUserByEmail(user.getEmail());
    if (existingUser != null) {
      userImportResultEntity.addErrorMessage(userName, "EMAIL:ALREADY_EXISTS");
      return userName;
    }

    try {
      organizationService.getUserHandler().createUser(user, true);
    } catch (Exception e) {
      userImportResultEntity.addErrorMessage(userName, "CREATE_USER_ERROR:" + e.getMessage());
      return userName;
    }

    String groups = userObject.getString("groups");
    if (StringUtils.isNotBlank(groups)) {
      List<String> groupsList = Arrays.asList(groups.split(";"));
      for (String groupMembershipExpression : groupsList) {
        String membershipType =
                              groupMembershipExpression.contains(":") ? StringUtils.trim(groupMembershipExpression.split(":")[0])
                                                                      : SpaceUtils.MEMBER;
        String groupId = groupMembershipExpression.contains(":") ? StringUtils.trim(groupMembershipExpression.split(":")[1])
                                                                 : groupMembershipExpression;
        Group groupObject = organizationService.getGroupHandler().findGroupById(groupId);
        if (groupObject == null) {
          userImportResultEntity.addWarnMessage(userName, "GROUP_NOT_EXISTS:" + groupId);
          continue;
        }
        MembershipType membershipTypeObject =
                                            organizationService.getMembershipTypeHandler().findMembershipType(membershipType);
        if (membershipTypeObject == null) {
          userImportResultEntity.addWarnMessage(userName, "MEMBERSHIP_TYPE_NOT_EXISTS:" + membershipType);
          continue;
        }
        try {
          organizationService.getMembershipHandler().linkMembership(user, groupObject, membershipTypeObject, true);
        } catch (Exception e) {
          userImportResultEntity.addWarnMessage(userName, "IMPORT_MEMBERSHIP_ERROR:" + e.getMessage());
        }
      }
    }

    // Delete imported User object properties
    userObject.remove("userName");
    userObject.remove("firstName");
    userObject.remove("lastName");
    userObject.remove("password");
    userObject.remove("email");
    userObject.remove("groups");

    ProfileEntity profileEntity = EntityBuilder.fromJsonString(userObject.toString(), ProfileEntity.class);
    String warnMessage = null;
    try {
      warnMessage = saveProfile(userName, profileEntity);
    } catch (Exception e) {
      warnMessage = "CREATE_USER_PROFILE_ERROR:" + e.getMessage();
    }
    if (warnMessage != null) {
      userImportResultEntity.addWarnMessage(userName, warnMessage);
    }
    return userName;
  }

  private String validateUser(JSONObject userObject, Locale locale) throws Exception {
    String errorMessage = null;
    Iterator<UserFieldValidator> iterator = USER_FIELD_VALIDATORS.iterator();
    while (iterator.hasNext() && errorMessage == null) {
      UserFieldValidator userFieldValidator = iterator.next();
      String fieldName = userFieldValidator.getField();
      String fieldValue = userObject.getString(fieldName);
      errorMessage = userFieldValidator.validate(locale, fieldValue);
    }
    return errorMessage;
  }

  /**
   * Log metric about composer usage
   * @param activity The posted activity
   */
  private void logMetrics(ExoSocialActivity activity) {
    ExoFeatureService featureService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ExoFeatureService.class);
    if(!featureService.isActiveFeature("new-composer")) {
      return;
    }

    if(activity == null) {
      return;
    }

    String activityType;
    if(activity.getType() != null) {
      switch (activity.getType()) {
        case "files:spaces":
          activityType = "file";
          break;
        case "LINK_ACTIVITY":
          activityType = "link";
          break;
        default:
          activityType = "message";
          break;
      }
    } else {
      activityType = "message";
    }

    LOG.info("service=composer operation=post parameters=\"composer_type:new,activity_type:{},activity_id:{},user_id:{}\"",
            activityType,
            activity.getId(),
            activity.getPosterId());
  }

  private String saveProfile(String username, ProfileEntity profileEntity) {
    Identity userIdentity = getUserIdentity(username);
    Profile profile = userIdentity.getProfile();

    try {
      Set<Entry<String, Object>> profileEntries = profileEntity.getDataEntity().entrySet();
      for (Entry<String, Object> entry : profileEntries) {
        String name = entry.getKey();
        Object value = entry.getValue();
        String fieldName = ProfileEntity.getFieldName(name);
        updateProfileField(profile, fieldName, value, false);
      }
      identityManager.updateProfile(profile, true);
    } catch (IdentityStorageException e) {
      return e.getMessageKey();
    } catch (Exception e) {
      return "Can't update profile entities, error = " + e.getMessage();
    }
    return null;
  }

  private void fillUserFromModel(User user, UserEntity model) {
    if (model.getFirstname() != null && !model.getFirstname().isEmpty()) {
      user.setFirstName(model.getFirstname());
    }
    if (model.getLastname() != null && !model.getLastname().isEmpty()) {
      user.setLastName(model.getLastname());
    }
    if (model.getEmail() != null && !model.getEmail().isEmpty()) {
      user.setEmail(model.getEmail());
    }
    if (model.getPassword() != null && !model.getPassword().isEmpty()) {
      user.setPassword(model.getPassword());
    }
  }

  /**
   * gets online identities who are members of a space.
   *
   * @param userId The current user.
   * @param space The space of which extract members.
   * @param limit Maximum number of identities to return.
   * @return identity array.
   */
  private Identity[] getOnlineIdentitiesOfSpace(String userId, Space space, int limit) {
    List<Identity> identities = new ArrayList<>();
    String[] spaceMembers = space.getMembers();
    String superUserName = userACL.getSuperUser();
    for (String user : spaceMembers) {
        UserStateModel userModel = userStateService.getUserState(user);
        boolean isOnline = userStateService.isOnline(user);
        if (user.equals(userId) || user.equals(superUserName) || userModel == null || INVISIBLE.equals(userModel.getStatus()) || !isOnline) {
          continue;
        }
        Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, false);
        identities.add(userIdentity);
        if (identities.size() == limit) {
          break;
        }
    }
    return identities.toArray(new Identity[identities.size()]);
  }

  /**
   * gets online identities.
   *
   * @param userId The current user.
   * @param limit Maximum number of identities to return.
   * @return identity array.
   */
  private Identity[] getOnlineIdentities(String userId, int limit) {
    List<Identity> identities = new ArrayList<>();
    List<UserStateModel> users = userStateService.online();
    Collections.reverse(users);
    if (users.size() > limit) {
      users = users.subList(0, limit);
    }
    String superUserName = userACL.getSuperUser();
    for (UserStateModel userModel : users) {
      String user = userModel.getUserId();
      if (user.equals(userId) || user.equals(superUserName) || userModel == null || INVISIBLE.equals(userModel.getStatus()))
        continue;
      Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, false);
      identities.add(userIdentity);
    }
    return identities.toArray(new Identity[identities.size()]);
  }
  
  /**
   * Checks if input email is existing already or not.
   * 
   * @param email Input email to check.
   * @return true if email is existing in system.
   */
  private User getUserByEmail(String email) {
    if (email == null) return null;
    try {
      Query query = new Query();
      query.setEmail(email);
      User[] users = organizationService.getUserHandler().findUsersByQuery(query).load(0, 10);
      return users[0];
    } catch (Exception e) {
      return null;
    }
  }

  private Response.ResponseBuilder getDefaultAvatarBuilder(String avatarUrl) throws IOException {
    if (avatarUrl != null) {
      if (avatarUrl.equals("404")) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      try {
        URL url = new URL(avatarUrl);
        String type = url.openConnection().getHeaderField("Content-Type");
        if (type != null && type.startsWith("image/")) {
          InputStream input = url.openStream();
          return Response.ok(input, type);
        }
      } catch (IOException e) {
        LOG.debug("Could NOT open the default url " + avatarUrl);
      }
    }

    if (defaultUserAvatar == null) {
      InputStream is = PortalContainer.getInstance().getPortalContext().getResourceAsStream(PROFILE_DEFAULT_AVATAR_URL);
      if (is == null) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
      defaultUserAvatar = IOUtil.getStreamContentAsBytes(is);
    }

    return Response.ok(new ByteArrayInputStream(defaultUserAvatar), "image/png");
  }

  private Response.ResponseBuilder getDefaultBannerBuilder(String bannerUrl) throws IOException {
    if (bannerUrl != null) {
      if (bannerUrl.equals("404")) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      
      try {
        URL url = new URL(bannerUrl);
        String type = url.openConnection().getHeaderField("Content-Type");
        if (type != null && type.startsWith("image/")) {
          InputStream input = url.openStream();
          return Response.ok(input, type);
        }
      } catch (IOException e) {
        LOG.debug("Could NOT open the default url " + bannerUrl);
      }
    }

    if (defaultUserBanner == null) {
      InputStream is = PortalContainer.getInstance().getPortalContext().getResourceAsStream(PROFILE_DEFAULT_BANNER_URL);
      if (is == null) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
      defaultUserBanner = IOUtil.getStreamContentAsBytes(is);
    }

    return Response.ok(new ByteArrayInputStream(defaultUserBanner), "image/png");
  }

  private void updateProfileField(Profile profile,
                                  String name,
                                  Object value,
                                  boolean save) throws Exception {
    if (Profile.AVATAR.equals(name) || Profile.BANNER.equals(name)) {
      UploadResource uploadResource = uploadService.getUploadResource(value.toString());
      if (uploadResource == null) {
        throw new IllegalStateException("No uploaded resource found with uploadId = " + value);
      }
      String storeLocation = uploadResource.getStoreLocation();
      try (FileInputStream inputStream = new FileInputStream(storeLocation)) {
        Attachment attachment = null;
        if (Profile.AVATAR.equals(name)) {
          attachment = new AvatarAttachment(null,
                                            uploadResource.getFileName(),
                                            uploadResource.getMimeType(),
                                            inputStream,
                                            System.currentTimeMillis());
          profile.setListUpdateTypes(Arrays.asList(UpdateType.AVATAR));
        } else {
          attachment = new BannerAttachment(null,
                                            uploadResource.getFileName(),
                                            uploadResource.getMimeType(),
                                            inputStream,
                                            System.currentTimeMillis());
          profile.setListUpdateTypes(Arrays.asList(UpdateType.BANNER));
        }
        profile.setProperty(name, attachment);
        if (save) {
          identityManager.updateProfile(profile, true);
        }
      } finally {
        uploadService.removeUploadResource(value.toString());
      }
    } else if (Profile.CONTACT_IMS.equals(name)) {
      @SuppressWarnings("unchecked")
      List<IMEntity> imEntities = (List<IMEntity>) value;
      if (imEntities == null || imEntities.isEmpty()) {
        profile.setProperty(Profile.CONTACT_IMS, Collections.emptyList());
      } else {
        List<Map<String, String>> imMaps = new ArrayList<>();
        for (IMEntity imEntity : imEntities) {
          String imType = imEntity.getImType();
          String imId = imEntity.getImId();
          if (StringUtils.isBlank(imId) || StringUtils.isBlank(imType)) {
            continue;
          }
          Map<String, String> imMap = new HashMap<>();
          imMap.put("key", imType);
          imMap.put("value", imId);
          imMaps.add(imMap);
        }
        profile.setProperty(Profile.CONTACT_IMS, imMaps);
      }

      if (save) {
        identityManager.updateProfile(profile, true);
      }
    } else if (Profile.CONTACT_PHONES.equals(name)) {
      @SuppressWarnings("unchecked")
      List<PhoneEntity> phoneEntities = (List<PhoneEntity>) value;
      if (phoneEntities == null || phoneEntities.isEmpty()) {
        profile.setProperty(Profile.CONTACT_PHONES, Collections.emptyList());
      } else {
        List<Map<String, String>> phoneMaps = new ArrayList<>();
        for (PhoneEntity phoneEntity : phoneEntities) {
          String phoneType = phoneEntity.getPhoneType();
          String phoneNumber = phoneEntity.getPhoneNumber();
          if (StringUtils.isBlank(phoneType) || StringUtils.isBlank(phoneNumber)) {
            continue;
          }
          Map<String, String> phoneMap = new HashMap<>();
          phoneMap.put("key", phoneType);
          phoneMap.put("value", phoneNumber);
          phoneMaps.add(phoneMap);
        }
        profile.setProperty(Profile.CONTACT_PHONES, phoneMaps);
      }

      if (save) {
        identityManager.updateProfile(profile, true);
      }
    } else if (Profile.CONTACT_URLS.equals(name)) {
      @SuppressWarnings("unchecked")
      List<URLEntity> urlEntities = (List<URLEntity>) value;
      if (urlEntities == null || urlEntities.isEmpty()) {
        profile.setProperty(Profile.CONTACT_URLS, Collections.emptyList());
      } else {
        List<Map<String, String>> urlMaps = new ArrayList<>();
        for (URLEntity urlEntity : urlEntities) {
          String url = urlEntity.getUrl();
          if (StringUtils.isBlank(url)) {
            continue;
          }
          Map<String, String> urlMap = new HashMap<>();
          urlMap.put("value", url);
          urlMaps.add(urlMap);
        }
        profile.setProperty(Profile.CONTACT_URLS, urlMaps);
      }

      if (save) {
        identityManager.updateProfile(profile, true);
      }
    } else if (Profile.EXPERIENCES.equals(name)) {
      @SuppressWarnings("unchecked")
      List<ExperienceEntity> experienceEntities = (List<ExperienceEntity>) value;
      if (experienceEntities == null || experienceEntities.isEmpty()) {
        profile.setProperty(Profile.EXPERIENCES, Collections.emptyList());
      } else {
        List<Map<String, Object>> experienceMaps = new ArrayList<>();
        for (ExperienceEntity experienceEntity : experienceEntities) {
          Map<String, Object> experienceMap = new HashMap<>();
          experienceMap.put(Profile.EXPERIENCES_ID, experienceEntity.getId());
          experienceMap.put(Profile.EXPERIENCES_COMPANY, experienceEntity.getCompany());
          experienceMap.put(Profile.EXPERIENCES_DESCRIPTION, experienceEntity.getDescription());
          experienceMap.put(Profile.EXPERIENCES_POSITION, experienceEntity.getPosition());
          experienceMap.put(Profile.EXPERIENCES_SKILLS, experienceEntity.getSkills());
          experienceMap.put(Profile.EXPERIENCES_IS_CURRENT, experienceEntity.getIsCurrent());
          experienceMap.put(Profile.EXPERIENCES_START_DATE, experienceEntity.getStartDate());
          experienceMap.put(Profile.EXPERIENCES_END_DATE, experienceEntity.getEndDate());
          experienceMaps.add(experienceMap);
        }
        profile.setProperty(Profile.EXPERIENCES, experienceMaps);
      }
      if (save) {
        identityManager.updateProfile(profile, true);
      }
    } else {
      profile.setProperty(name, value);
      if (save) {
        identityManager.updateProfile(profile, true);
      }
    }
  }

}
