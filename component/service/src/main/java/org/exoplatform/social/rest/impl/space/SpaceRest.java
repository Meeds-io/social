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
package org.exoplatform.social.rest.impl.space;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.deprecation.DeprecatedAPI;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.PageType;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.thumbnail.ImageThumbnailService;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.model.SpaceExternalInvitation;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.BaseEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;
import org.exoplatform.social.rest.impl.activity.ActivityRest;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.login.recovery.PasswordRecoveryService;

import io.meeds.portal.security.constant.UserRegistrationType;
import io.meeds.portal.security.service.SecuritySettingService;
import io.meeds.social.util.JsonUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Path(VersionResources.VERSION_ONE + "/social/spaces")
@Tag(name = VersionResources.VERSION_ONE + "/social/spaces", description = "Operations on spaces with their activities and users")
public class SpaceRest implements ResourceContainer {

  private static final Log             LOG                         = ExoLogger.getLogger(SpaceRest.class);

  private static final String          SPACE_FILTER_TYPE_ALL       = "all";

  private static final String          SPACE_FILTER_TYPE_MEMBER    = "member";

  private static final String          SPACE_FILTER_TYPE_MANAGER   = "manager";

  private static final String          SPACE_FILTER_TYPE_PENDING   = "pending";

  private static final String          SPACE_FILTER_TYPE_INVITED   = "invited";

  private static final String          SPACE_FILTER_TYPE_REQUESTS  = "requests";

  private static final String          SPACE_FILTER_TYPE_FAVORITE  = "favorite";

  private static final String          LAST_VISITED_SPACES         = "lastVisited";

  private static final CacheControl    CACHE_CONTROL               = new CacheControl();

  private static final CacheControl    CACHE_REVALIDATE_CONTROL    = new CacheControl();

  public static final String           PROFILE_DEFAULT_BANNER_URL  = "/skin/images/banner/DefaultSpaceBanner.png";

  private static final Date            DEFAULT_IMAGES_LAST_MODIFED = new Date();

  private static final long            DEFAULT_IMAGES_HASH         = DEFAULT_IMAGES_LAST_MODIFED.getTime();

  // 7 days
  private static final int             CACHE_IN_SECONDS            = 7 * 86400;

  private static final int             CACHE_IN_MILLI_SECONDS      = CACHE_IN_SECONDS * 1000;

  private final ActivityRest           activityRestResourcesV1;

  private final IdentityManager        identityManager;

  private final UploadService          uploadService;

  private final SpaceService           spaceService;

  private final SecuritySettingService securitySettingService;

  private final ImageThumbnailService  imageThumbnailService;

  private byte[]                       defaultSpaceBanner          = null;

  private byte[]                       defaultSpaceAvatar          = null;

  public SpaceRest(ActivityRest activityRestResourcesV1,
                   SpaceService spaceService,
                   IdentityManager identityManager,
                   UploadService uploadService,
                   ImageThumbnailService imageThumbnailService,
                   SecuritySettingService securitySettingService) {
    this.activityRestResourcesV1 = activityRestResourcesV1;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
    this.uploadService = uploadService;
    this.imageThumbnailService = imageThumbnailService;
    this.securitySettingService = securitySettingService;

    CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
    CACHE_REVALIDATE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
    CACHE_REVALIDATE_CONTROL.setMustRevalidate(true);
  }

  @GET
  @RolesAllowed("users")
  @Operation(
             summary = "Gets spaces of user",
             method = "GET",
             description = "This returns a list of spaces switch request parameters")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getSpaces( // NOSONAR
                            @Context
                            UriInfo uriInfo,
                            @Context
                            Request request,
                            @Parameter(description = "Space name search information", required = false)
                            @QueryParam("q")
                            String q,
                            @Parameter(description = "Type of spaces to retrieve: all, userSpaces, invited, pending or requests",
                                       required = false)
                            @Schema(defaultValue = SPACE_FILTER_TYPE_ALL)
                            @QueryParam("filterType")
                            String filterType,
                            @Parameter(description = "Offset", required = false)
                            @Schema(defaultValue = "0")
                            @QueryParam("offset")
                            int offset,
                            @Parameter(description = "Limit, if equals to 0, it will not retrieve spaces", required = false)
                            @Schema(defaultValue = "20")
                            @QueryParam("limit")
                            int limit,
                            @Parameter(description = "Sort", required = false)
                            @QueryParam("sort")
                            String sort,
                            @Parameter(description = "Order", required = false)
                            @QueryParam("order")
                            String order,
                            @Parameter(description = "Returning the number of spaces found or not")
                            @Schema(defaultValue = "false")
                            @QueryParam("returnSize")
                            boolean returnSize,
                            @Parameter(description = "Returning the favorite spaces of current user not not")
                            @Schema(defaultValue = "false")
                            @QueryParam("favorites")
                            boolean favorites,
                            @Parameter(description = "Tag names used to search spaces", required = true)
                            @QueryParam(
                              "tags"
                            ) List<String> tagNames,
                            @Parameter(description = "Asking for a full representation of a specific subresource, ex: members or managers",
                                       required = false)
                            @QueryParam("expand")
                            String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit >= 0 ? limit : RestUtils.getLimit(uriInfo);

    if (StringUtils.isBlank(filterType)) {
      filterType = SPACE_FILTER_TYPE_ALL;
    }

    ListAccess<Space> listAccess = null;
    SpaceFilter spaceFilter = new SpaceFilter();

    if (StringUtils.isNotBlank(q)) {
      spaceFilter.setSpaceNameSearchCondition(StringUtils.trim(q));
    }
    spaceFilter.setTagNames(tagNames);

    if (StringUtils.isNotBlank(sort)) {
      SortBy sortBy = Sorting.SortBy.valueOf(sort.toUpperCase());
      OrderBy orderBy = Sorting.OrderBy.ASC;
      if (StringUtils.isNotBlank(order)) {
        orderBy = Sorting.OrderBy.valueOf(order.toUpperCase());
      }
      spaceFilter.setSorting(new Sorting(sortBy, orderBy));
    }
    spaceFilter.setFavorite(favorites);
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_ALL, filterType)) {
      listAccess = spaceService.getVisibleSpacesWithListAccess(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_MEMBER, filterType)) {
      listAccess = spaceService.getMemberSpacesByFilter(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_MANAGER, filterType)) {
      listAccess = spaceService.getManagerSpacesByFilter(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_PENDING, filterType)) {
      listAccess = spaceService.getPendingSpacesByFilter(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_INVITED, filterType)) {
      listAccess = spaceService.getInvitedSpacesByFilter(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_REQUESTS, filterType)) {
      listAccess = spaceService.getPendingSpaceRequestsToManage(authenticatedUser);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_FAVORITE, filterType)) {
      listAccess = spaceService.getFavoriteSpacesByFilter(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(LAST_VISITED_SPACES, filterType)) {
      listAccess = spaceService.getLastAccessedSpace(authenticatedUser);
    } else {
      return Response.status(400).entity("Unrecognized space filter type").build();
    }

    List<Space> spaces;
    if (limit > 0) {
      spaces = Arrays.asList(listAccess.load(offset, limit));
    } else {
      spaces = Collections.emptyList();
    }
    CollectionEntity collectionSpace = EntityBuilder.buildEntityFromSpaces(spaces, authenticatedUser, offset, limit, expand, uriInfo);
    if (returnSize) {
      collectionSpace.setSize(listAccess.getSize());
    }

    EntityTag eTag = new EntityTag(String.valueOf(JsonUtils.toJsonString(collectionSpace).hashCode()));
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      builder = EntityBuilder.getResponseBuilder(collectionSpace, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
      builder.tag(eTag);
      builder.lastModified(new Date());
      builder.cacheControl(CACHE_REVALIDATE_CONTROL);
    }
    return builder.build();
  }

  /**
   * {@inheritDoc}
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
             summary = "Creates a space",
             method = "POST",
             description = "This can only be done by the logged in user.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response createSpace(
                              @Context
                              UriInfo uriInfo,
                              @Parameter(description = "Asking for a full representation of a specific subresource, ex: members or managers",
                                         required = false)
                              @QueryParam("expand")
                              String expand,
                              @RequestBody(description = "Space object to be created, ex:<br />" +
                                  "{<br />\"displayName\": \"My space\"," +
                                  "<br />\"description\": \"This is my space\"," +
                                  "<br />\"groupId\": \"/spaces/my_space\"," +
                                  "<br />\"visibility\": \"private\"," +
                                  "<br />\"subscription\": \"validation\"<br />}", required = true)
                              SpaceEntity model) throws Exception {
    if (model == null || model.getDisplayName() == null
        || model.getDisplayName().length() < 3
        || model.getDisplayName().length() > 200) {
      throw new SpaceException(SpaceException.Code.INVALID_SPACE_NAME);
    }

    // validate the display name
    if (spaceService.getSpaceByDisplayName(model.getDisplayName()) != null) {
      throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    Space space = new Space();
    fillSpaceFromModel(space, model);
    space.setEditor(authenticatedUser);

    String[] managers = new String[] { authenticatedUser };
    String[] members = new String[] { authenticatedUser };
    space.setManagers(managers);
    space.setMembers(members);

    //
    spaceService.createSpace(space, authenticatedUser, model.getInvitedMembers());

    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("{spaceId}/checkExternals")
  @RolesAllowed("users")
  @Operation(
             summary = "Checks if a specific a space contains an external members",
             method = "GET",
             description = "This returns the space if it contains external members")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "404", description = "Resource not found"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response isSpaceContainsExternals(
                                           @Context
                                           UriInfo uriInfo,
                                           @Context
                                           Request request,
                                           @Parameter(description = "Space Id", required = true)
                                           @PathParam("spaceId")
                                           String spaceId) {
    if (spaceId == null) {
      return Response.status(Status.BAD_REQUEST).entity("space Id is mandatory").build();
    }

    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      return Response.status(Status.NOT_FOUND).entity("space not found").build();
    }
    boolean hasExternals;
    try {
      hasExternals = spaceService.isSpaceContainsExternals(Long.valueOf(spaceId));
    } catch (Exception e) {
      LOG.error("Error while checking external members on space {}", space.getGroupId(), e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("server internal error occurred").build();
    }

    return Response.ok(String.valueOf(hasExternals)).type(MediaType.TEXT_PLAIN).build();
  }

  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @Operation(
             summary = "Gets a specific space by id",
             method = "GET",
             description = "This returns the space in the following cases: <br/><ul><li>the authenticated user is a member of the space</li><li>the space is \"public\"</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getSpaceById(
                               @Context
                               UriInfo uriInfo,
                               @Context
                               Request request,
                               @Parameter(description = "Space id", required = true)
                               @PathParam("id")
                               String id,
                               @Parameter(description = "Asking for a full representation of a specific subresource, ex: members or managers",
                                          required = false)
                               @QueryParam("expand")
                               String expand) throws Exception {
    Space space = spaceService.getSpaceById(id);
    return buildSpaceResponse(space, expand, uriInfo, request);
  }

  @GET
  @Path("byPrettyName/{prettyName}")
  @RolesAllowed("users")
  @Operation(
             summary = "Gets a specific space by pretty name",
             method = "GET",
             description = "This returns the space in the following cases: <br/><ul><li>the authenticated user is a member of the space</li><li>the space is \"public\"</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(
                value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getSpaceByPrettyName(
                                       @Context
                                       UriInfo uriInfo,
                                       @Context
                                       Request request,
                                       @Parameter(description = "Space id", required = true)
                                       @PathParam("prettyName")
                                       String prettyName,
                                       @Parameter(
                                                  description = "Asking for a full representation of a specific subresource, ex: members or managers",
                                                  required = false)
                                       @QueryParam("expand")
                                       String expand) throws Exception {

    Space space = spaceService.getSpaceByPrettyName(prettyName);
    return buildSpaceResponse(space, expand, uriInfo, request);
  }

  @GET
  @Path("byGroupSuffix/{groupSuffix}")
  @RolesAllowed("users")
  @Operation(
             summary = "Gets a specific space by its group id without /spaces/ prefix",
             method = "GET",
             description = "This returns the space in the following cases: <br/><ul><li>the authenticated user is a member of the space</li><li>the space is \"public\"</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(
                value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getSpaceByGroupSuffix(
                                        @Context
                                        UriInfo uriInfo,
                                        @Context
                                        Request request,
                                        @Parameter(description = "Space id", required = true)
                                        @PathParam("groupSuffix")
                                        String groupSuffix,
                                        @Parameter(description = "Asking for a full representation of a specific subresource, ex: members or managers",
                                                   required = false)
                                        @QueryParam("expand")
                                        String expand) {
    Space space = spaceService.getSpaceByGroupId(SpaceUtils.SPACE_GROUP + "/" + groupSuffix);
    return buildSpaceResponse(space, expand, uriInfo, request);
  }

  @GET
  @Path("byDisplayName/{displayName}")
  @RolesAllowed("users")
  @Operation(
             summary = "Gets a specific space by display name",
             method = "GET",
             description = "This returns the space in the following cases: <br/><ul><li>the authenticated user is a member of the space</li><li>the space is \"public\"</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(
                value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getSpaceByDisplayName(
                                        @Context
                                        UriInfo uriInfo,
                                        @Context
                                        Request request,
                                        @Parameter(description = "Space id", required = true)
                                        @PathParam("displayName")
                                        String displayName,
                                        @Parameter(
                                                   description = "Asking for a full representation of a specific subresource, ex: members or managers",
                                                   required = false)
                                        @QueryParam("expand")
                                        String expand) throws Exception {

    Space space = spaceService.getSpaceByDisplayName(displayName);
    return buildSpaceResponse(space, expand, uriInfo, request);
  }

  @GET
  @Path("{id}/avatar")
  @Operation(
             summary = "Gets a space avatar by pretty name",
             method = "GET",
             description = "This can only be done by the logged in user.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "404", description = "Resource not found") })
  public Response getSpaceAvatarById(
                                     @Context
                                     UriInfo uriInfo,
                                     @Context
                                     Request request,
                                     @Parameter(description = "The value of lastModified parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'")
                                     @QueryParam("lastModified")
                                     String lastModified,
                                     @Parameter(description = "Space pretty name or space id", required = true)
                                     @PathParam("id")
                                     String id,
                                     @Parameter(description = "Whether to retrieve avatar by id or pretty name", required = false)
                                     @DefaultValue("false")
                                     @QueryParam("byId")
                                     boolean byId,
                                     @Parameter(description = "Resized avatar size. Use 0x0 for original size.")
                                     @DefaultValue("100x100")
                                     @QueryParam("size")
                                     String size,
                                     @Parameter(
                                                description = "A mandatory valid token that is used to authorize anonymous request",
                                                required = false)
                                     @QueryParam("r")
                                     String token) throws IOException {

    boolean isDefault = StringUtils.equals(LinkProvider.DEFAULT_IMAGE_REMOTE_ID, id);
    Response.ResponseBuilder builder = null;
    Long lastUpdated = null;
    EntityTag eTag = null;

    if (!isDefault) {
      if (RestUtils.isAnonymous() && !LinkProvider.isAttachmentTokenValid(token,
                                                                          SpaceIdentityProvider.NAME,
                                                                          id,
                                                                          AvatarAttachment.TYPE,
                                                                          lastModified)) {
        LOG.warn("An anonymous user attempts to access avatar of space {} without a valid access token", id);
        return Response.status(Status.NOT_FOUND).build();
      }

      String authenticatedUser = RestUtils.getCurrentUser();
      Space space = byId ? spaceService.getSpaceById(id) : spaceService.getSpaceByPrettyName(id);
      if (space == null
          || (Space.HIDDEN.equals(space.getVisibility())
              && !spaceService.canViewSpace(space, authenticatedUser))) {
        return Response.status(Status.NOT_FOUND).build();
      }
      Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      //
      Profile profile = identity.getProfile();
      if (profile != null) {
        lastUpdated = profile.getAvatarLastUpdated();
      }
      if (lastUpdated != null) {
        eTag = new EntityTag(lastUpdated.hashCode() + "-" + size);
      }
      //
      builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
      if (builder == null) {
        int[] dimension = Utils.parseDimension(size);
        try {
          byte[] avatarContent = null;
          if (identityManager.getAvatarFile(identity) != null) {
            avatarContent = imageThumbnailService.getOrCreateThumbnail(identityManager.getAvatarFile(identity),
                                                                       dimension[0],
                                                                       dimension[1])
                                                 .getAsByte();
          }
          if (avatarContent != null) {
            builder = Response.ok(avatarContent, "image/png");
          }
        } catch (Exception e) {
          LOG.error("Error while resizing avatar of space identity with Id {}, original Image will be returned",
                    identity.getId(),
                    e);
        }
      }

    }

    if (builder == null) {
      builder = getDefaultAvatarBuilder();
    }

    builder.cacheControl(CACHE_CONTROL);
    builder.lastModified(lastUpdated == null ? DEFAULT_IMAGES_LAST_MODIFED : new Date(lastUpdated));
    if (eTag != null) {
      builder.tag(eTag);
    }
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
  @Operation(
             summary = "Gets a space banner by id",
             method = "GET",
             description = "This can only be done by the logged in user.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "404", description = "Resource not found") })
  public Response getSpaceBannerById(
                                     @Context
                                     UriInfo uriInfo,
                                     @Context
                                     Request request,
                                     @Parameter(description = "The value of lastModified parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'")
                                     @QueryParam("lastModified")
                                     String lastModified,
                                     @Parameter(description = "Space id", required = true)
                                     @PathParam("id")
                                     String id,
                                     @Parameter(description = "Whether to retrieve banner by id or pretty name", required = false)
                                     @DefaultValue("false")
                                     @QueryParam("byId")
                                     boolean byId,
                                     @Parameter(
                                                description = "A mandatory valid token that is used to authorize anonymous request",
                                                required = false)
                                     @QueryParam("r")
                                     String token) throws IOException {
    boolean isDefault = StringUtils.equals(LinkProvider.DEFAULT_IMAGE_REMOTE_ID, id);
    Identity identity = null;
    Long lastUpdated = null;

    if (!isDefault
        && RestUtils.isAnonymous()
        && !LinkProvider.isAttachmentTokenValid(token,
                                                                        SpaceIdentityProvider.NAME,
                                                                        id,
                                                                        BannerAttachment.TYPE,
                                                                        lastModified)) {
      LOG.warn("An anonymous user attempts to access banner of space {} without a valid access token", id);
      return Response.status(Status.NOT_FOUND).build();
    }

    if (!isDefault) {
      String authenticatedUser = RestUtils.getCurrentUser();
      Space space = byId ? spaceService.getSpaceById(id) : spaceService.getSpaceByPrettyName(id);
      if (space == null
          || (Space.HIDDEN.equals(space.getVisibility())
              && !spaceService.canViewSpace(space, authenticatedUser))) {
        return Response.status(Status.NOT_FOUND).build();
      }
      identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      //
      Profile profile = identity.getProfile();
      if (profile != null) {
        lastUpdated = profile.getBannerLastUpdated();
      }
    }
    EntityTag eTag = null;
    if (isDefault) {
      eTag = new EntityTag(String.valueOf(DEFAULT_IMAGES_HASH));
    } else if (lastUpdated != null) {
      eTag = new EntityTag(Integer.toString(lastUpdated.hashCode()));
    }

    //
    Response.ResponseBuilder builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
    if (builder == null) {
      if (isDefault) {
        builder = getDefaultBannerBuilder();
      } else {
        InputStream stream = identityManager.getBannerInputStream(identity);
        if (stream == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        /*
         * As recommended in the the RFC1341
         * (https://www.w3.org/Protocols/rfc1341/4_Content-Type.html), we set the
         * banner content-type to "image/png". So, its data would be recognized as
         * "image" by the user-agent.
         */
        builder = Response.ok(stream, "image/png");
        builder.tag(eTag);
      }
    }
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

  /**
   * {@inheritDoc}
   */
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
             summary = "Updates a specific space by id",
             method = "PUT",
             description = "This updates the space in the following cases: <br/><ul><li>the authenticated user is a manager of the space</li><li>the authenticated user is the owner of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response updateSpaceById(
                                  @Context
                                  UriInfo uriInfo,
                                  @Parameter(description = "Space id", required = true)
                                  @PathParam("id")
                                  String id,
                                  @Parameter(description = "Asking for a full representation of a specific subresource, ex: members or managers",
                                             required = false)
                                  @QueryParam("expand")
                                  String expand,
                                  @RequestBody(description = "Space object to be updated", required = true)
                                  SpaceEntity model) throws Exception {

    if (model == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    Space space = spaceService.getSpaceById(id);
    if (!spaceService.canManageSpace(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if (model.getDisplayName() != null && (model.getDisplayName().length() < 3 || model.getDisplayName().length() > 200)) {
      throw new SpaceException(SpaceException.Code.INVALID_SPACE_NAME);
    }

    if (StringUtils.isNotBlank(model.getPublicSiteVisibility())) {
      spaceService.saveSpacePublicSite(id,
                                       model.getPublicSiteVisibility(),
                                       authenticatedUser);
      space = spaceService.getSpaceById(id);
    }

    if (StringUtils.isNotBlank(model.getDisplayName()) && !StringUtils.equals(space.getDisplayName(), model.getDisplayName())) {
      spaceService.renameSpace(space, model.getDisplayName(), authenticatedUser);
    }

    if (model.getExternalInvitedUsers() != null
        && (securitySettingService.getRegistrationType() == UserRegistrationType.OPEN
            || securitySettingService.isRegistrationExternalUser())) {
      String uri = uriInfo.getBaseUri()
                          .toString()
                          .substring(0,
                                     uriInfo.getBaseUri()
                                            .toString()
                                            .lastIndexOf("/"));
      StringBuilder url = new StringBuilder(uri);

      PasswordRecoveryService passwordRecoveryService = CommonsUtils.getService(PasswordRecoveryService.class);
      LocaleConfigService localeConfigService = CommonsUtils.getService(LocaleConfigService.class);
      Locale locale = null;
      try {
        String defaultLanguage = localeConfigService.getDefaultLocaleConfig().getLocale().toLanguageTag();
        locale = new Locale(defaultLanguage);
      } catch (Exception e) {
        LOG.error("Failure to retrieve portal config", e);
      }
      for (String externalInvitedUser : model.getExternalInvitedUsers()) {
        String tokenId = passwordRecoveryService.sendExternalRegisterEmail(authenticatedUser,
                                                                           externalInvitedUser,
                                                                           locale,
                                                                           space.getDisplayName(),
                                                                           url);
        spaceService.saveSpaceExternalInvitation(space.getId(), externalInvitedUser, tokenId);
      }
    }

    fillSpaceFromModel(space, model);
    space.setEditor(authenticatedUser);
    spaceService.updateSpace(space, model.getInvitedMembers());

    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromSpace(spaceService.getSpaceById(id),
                                                                        authenticatedUser,
                                                                        uriInfo.getPath(),
                                                                        expand),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  /**
   * {@inheritDoc}
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @Operation(
             summary = "Deletes a specific space by id",
             method = "DELETE",
             description = "This deletes the space in the following cases: <br/><ul><li>the authenticated user is a manager of the space</li><li>the authenticated user is the owner of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response deleteSpaceById(
                                  @Context
                                  UriInfo uriInfo,
                                  @Parameter(description = "Space id", required = true)
                                  @PathParam("id")
                                  String id,
                                  @Parameter(description = "Asking for a full representation of a specific subresource if any",
                                             required = false)
                                  @QueryParam("expand")
                                  String expand) throws Exception {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    Space space = spaceService.getSpaceById(id);
    if (!spaceService.canManageSpace(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    space.setEditor(authenticatedUser);
    spaceService.deleteSpace(space);

    return Response.ok().build();
  }

  @GET
  @Path("{id}/users")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
             summary = "Gets users of a specific space",
             method = "GET",
             description = "This returns a list of users if the authenticated user is a member or manager of the space or a spaces super manager.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  @Deprecated(forRemoval = true, since = "7.0")
  @DeprecatedAPI(value = "Use SpaceMembershipRest.getSpacesMemberships instead", insist = true)
  public Response getSpaceMembers(
                                  @Context
                                  UriInfo uriInfo,
                                  @Context
                                  Request request,
                                  @Parameter(description = "Space id", required = true)
                                  @PathParam("id")
                                  String id,
                                  @Parameter(description = "User name search information", required = false)
                                  @QueryParam("q")
                                  String q,
                                  @Parameter(description = "Role of the target user in this space: manager, member, invited and pending",
                                             required = false)
                                  @Schema(defaultValue = "member")
                                  @QueryParam("role")
                                  String role,
                                  @Parameter(description = "Offset", required = false)
                                  @Schema(defaultValue = "0")
                                  @QueryParam("offset")
                                  int offset,
                                  @Parameter(description = "Limit", required = false)
                                  @Schema(defaultValue = "20")
                                  @QueryParam("limit")
                                  int limit,
                                  @Parameter(description = "Returning the number of users or not")
                                  @Schema(defaultValue = "false")
                                  @QueryParam("returnSize")
                                  boolean returnSize,
                                  @Parameter(description = "Asking for a full representation of a specific subresource if any",
                                             required = false)
                                  @QueryParam("expand")
                                  String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    Space space = spaceService.getSpaceById(id);
    if (!spaceService.canViewSpace(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    if (StringUtils.isBlank(role)) {
      role = SpaceMemberFilterListAccess.Type.MEMBER.name();
    }
    SpaceMemberFilterListAccess.Type type = SpaceMemberFilterListAccess.Type.valueOf(role.toUpperCase());

    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setName(q);

    ListAccess<Identity> spaceIdentitiesListAccess = identityManager.getSpaceIdentityByProfileFilter(space,
                                                                                                     profileFilter,
                                                                                                     type,
                                                                                                     true);
    Identity[] spaceIdentities = spaceIdentitiesListAccess.load(offset, limit);

    List<DataEntity> profileInfos = null;
    if (spaceIdentities == null || spaceIdentities.length == 0) {
      profileInfos = Collections.emptyList();
    } else {
      profileInfos = Arrays.stream(spaceIdentities)
                           .map(identity -> EntityBuilder.buildEntityProfile(space,
                                                                             identity.getProfile(),
                                                                             uriInfo.getPath(),
                                                                             expand)
                                                         .getDataEntity())
                           .toList();
    }

    CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
    if (returnSize) {
      collectionUser.setSize(spaceIdentitiesListAccess.getSize());
    }

    EntityTag eTag = new EntityTag(String.valueOf(JsonUtils.toJsonString(collectionUser).hashCode()));
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      builder = Response.ok(collectionUser, MediaType.APPLICATION_JSON);
      builder.tag(eTag);
      builder.lastModified(new Date());
      builder.expires(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)));
    }
    return builder.build();
  }

  /**
   * Checks if is the given userId is a space member.
   *
   * @param uriInfo the uri info
   * @param id the space id
   * @param userId the user id
   * @return the response
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}/users/{userId}")
  @RolesAllowed("users")
  @Operation(
             summary = "Checks if the given user is a member of a specific space or not",
             method = "GET",
             description = "This Checks if user is a member of a specific spacer o not.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  @Deprecated(forRemoval = true, since = "7.0")
  @DeprecatedAPI(value = "Use SpaceMembershipRest.getSpacesMemberships instead", insist = true)
  public Response isSpaceMember(
                                @Context
                                UriInfo uriInfo,
                                @Parameter(description = "Space id", required = true)
                                @PathParam("id")
                                String id,
                                @Parameter(description = "User id", required = true)
                                @PathParam("userId")
                                String userId) {
    Space space = spaceService.getSpaceById(id);
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    boolean isMember = spaceService.isMember(space, userId);
    return Response.ok().entity("{\"isMember\":\"" + isMember + "\"}").build();
  }

  @GET
  @Path("{id}/navigations")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Return list of navigations of a space", method = "GET",
             description = "Return list of navigations of a space")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "401", description = "Unauthorized") })
  @Deprecated(forRemoval = true, since = "7.0")
  @DeprecatedAPI(value = "Use NavigationRest.getSiteTypeNavigations instead", insist = true)
  public Response getSpaceNavigations(
                                      @Context
                                      HttpServletRequest httpRequest,
                                      @Context
                                      Request request,
                                      @Parameter(description = "Space id", required = true)
                                      @PathParam("id")
                                      String spaceId) {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Space space = spaceService.getSpaceById(spaceId);
    if (!spaceService.canViewSpace(space, authenticatedUser)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    List<UserNode> navigations = SpaceUtils.getSpaceNavigations(space, httpRequest.getLocale(), authenticatedUser);
    if (CollectionUtils.isEmpty(navigations)) {
      return Response.ok(Collections.emptyList()).build();
    }
    List<DataEntity> spaceNavigations = navigations.stream().map(node -> {
      BaseEntity app = new BaseEntity(node.getId());
      app.setProperty("label", node.getResolvedLabel());
      app.setProperty("icon", node.getIcon());
      app.setProperty("uri", node.getURI());
      app.setProperty("target", node.getTarget());
      if (node.getPageRef() != null) {
        Page navigationNodePage = SpaceUtils.getLayoutService().getPage(node.getPageRef());
        if (PageType.LINK.equals(PageType.valueOf(navigationNodePage.getType()))) {
          app.setProperty("link", navigationNodePage.getLink());
        }
      }
      return app.getDataEntity();
    }).toList();
    return Response.ok(spaceNavigations, MediaType.APPLICATION_JSON).build();
  }

  @PUT
  @Path("layout/{appId}/{spaceId}")
  @RolesAllowed("users")
  @Operation(
             summary = "Restores space Page Layout switch associated space template",
             method = "PUT",
             description = "This operation will restores the default page layout of a designated space switch its space template as if it was a new space creation." +
                 "The applications data will not be change, only the layout definition and structure of the page." +
                 "This endpoint is accessible only for spaces managers.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "401", description = "User not authorized to call this endpoint"),
                          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response restoreSpacePageLayout(
                                         @Context
                                         UriInfo uriInfo,
                                         @Parameter(description = "Space application identifier to reset. Can be 'home' or any page name.",
                                                    required = true)
                                         @PathParam("appId")
                                         String appId,
                                         @Parameter(description = "Space technical identifier", required = true)
                                         @PathParam("spaceId")
                                         String spaceId) {
    try {
      spaceService.restoreSpacePageLayout(spaceId, appId, ConversationState.getCurrent().getIdentity());
      return Response.ok().build();
    } catch (IllegalAccessException e) {
      return Response.status(Status.UNAUTHORIZED).build();
    } catch (SpaceException e) {
      return Response.serverError().entity(e.getLocalizedMessage()).build();
    }
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}/activities")
  @RolesAllowed("users")
  @Operation(
             summary = "Gets space activities by space id",
             method = "GET",
             description = "This returns the space's activities if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  @Deprecated
  @DeprecatedAPI(value = "Use activityRestResourcesV1.getActivities instead", insist = true)
  public Response getSpaceActivitiesById(
                                         @Context
                                         UriInfo uriInfo,
                                         @Parameter(description = "Space id", required = true)
                                         @PathParam("id")
                                         String id,
                                         @Parameter(description = "Offset")
                                         @Schema(defaultValue = "0")
                                         @QueryParam("offset")
                                         int offset,
                                         @Parameter(description = "Limit")
                                         @Schema(defaultValue = "20")
                                         @QueryParam("limit")
                                         int limit,
                                         @Parameter(description = "Base time to load older activities (yyyy-MM-dd HH:mm:ss)")
                                         @QueryParam("before")
                                         String before,
                                         @Parameter(description = "Base time to load newer activities (yyyy-MM-dd HH:mm:ss)")
                                         @QueryParam("after")
                                         String after,
                                         @Parameter(description = "Returning the number of activities or not")
                                         @Schema(defaultValue = "false")
                                         @QueryParam("returnSize")
                                         boolean returnSize,
                                         @Parameter(description = "Asking for a full representation of a specific subresource, ex: comments or likes")
                                         @QueryParam("expand")
                                         String expand) {
    return activityRestResourcesV1.getActivities(uriInfo, id, before, after, offset, limit, returnSize, expand, null);
  }

  /**
   * {@inheritDoc}
   */
  @POST
  @Path("{id}/activities")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
             summary = "Posts an activity to a specific space",
             method = "POST",
             description = "This posts the activity if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  @Deprecated
  @DeprecatedAPI(value = "Use activityRestResourcesV1.postActivity instead", insist = true)
  public Response postActivityOnSpace(
                                      @Context
                                      UriInfo uriInfo,
                                      @Parameter(description = "Space id", required = true)
                                      @PathParam("id")
                                      String id,
                                      @Parameter(description = "Asking for a full representation of a specific subresource, ex: comments or likes",
                                                 required = false)
                                      @QueryParam("expand")
                                      String expand,
                                      @Parameter(description = "Activity object to be created", required = true)
                                      ActivityEntity model) {
    return activityRestResourcesV1.postActivity(uriInfo, id, expand, model);
  }

  private void fillSpaceFromModel(Space space, SpaceEntity model) throws IOException {
    space.setPriority(Space.INTERMEDIATE_PRIORITY);

    if (StringUtils.isNotBlank(model.getDisplayName())) {
      space.setDisplayName(model.getDisplayName());
      space.setDescription(model.getDescription());

      if (StringUtils.isBlank(space.getPrettyName())) {
        space.setPrettyName(model.getDisplayName());
      }
    } else if (StringUtils.isNotBlank(model.getPrettyName())) {
      space.setPrettyName(model.getPrettyName());
      space.setDescription(model.getDescription());

      if (StringUtils.isBlank(space.getDisplayName())) {
        space.setDisplayName(model.getPrettyName());
      }
    }

    if (StringUtils.isNotBlank(model.getTemplate())) {
      space.setTemplate(model.getTemplate());
    }

    if (StringUtils.isNotBlank(model.getBannerId())) {
      updateProfileField(space, Profile.BANNER, model.getBannerId());
    }

    if (StringUtils.isNotBlank(model.getAvatarId())) {
      updateProfileField(space, Profile.AVATAR, model.getAvatarId());
    }

    if (StringUtils.equalsIgnoreCase(Space.HIDDEN, model.getVisibility())) {
      space.setVisibility(Space.HIDDEN);
    } else if (StringUtils.equalsIgnoreCase(Space.PRIVATE, model.getVisibility())) {
      space.setVisibility(Space.PRIVATE);
    } else if (StringUtils.equalsIgnoreCase(Space.PUBLIC, model.getVisibility())) {
      space.setVisibility(Space.PUBLIC);
    } else if (StringUtils.isBlank(model.getVisibility()) && space.getId() == null) {
      space.setVisibility(Space.PRIVATE);
    }

    if (StringUtils.equalsIgnoreCase(Space.OPEN, model.getSubscription())) {
      space.setRegistration(Space.OPEN);
    } else if (StringUtils.equalsIgnoreCase(Space.CLOSED, model.getSubscription())) {
      space.setRegistration(Space.CLOSED);
    } else if (StringUtils.equalsIgnoreCase(Space.VALIDATION, model.getSubscription())) {
      space.setRegistration(Space.VALIDATION);
    } else if (StringUtils.isBlank(model.getSubscription()) && space.getId() == null) {
      space.setRegistration(Space.VALIDATION);
    }
  }

  @GET
  @Path("{id}/externalInvitations")
  @RolesAllowed("users")
  @Operation(
             summary = "Gets external invitations of a specific space",
             method = "GET",
             description = "This returns a list of external invitations if the authenticated user is a member or manager of the space or a spaces super manager.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getSpaceExternalInvitations(@Context
  UriInfo uriInfo,
                                              @Parameter(description = "Space id", required = true)
                                              @PathParam("id")
                                              String id) {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    Space space = spaceService.getSpaceById(id);
    if (!spaceService.canManageSpace(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<SpaceExternalInvitation> spaceExternalInvitations = spaceService.findSpaceExternalInvitationsBySpaceId(id);
    return EntityBuilder.getResponseBuilder(spaceExternalInvitations, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK)
                        .build();
  }

  @DELETE
  @Path("externalInvitations/{invitationId}")
  @RolesAllowed("users")
  @Operation(
             summary = "Delete a specific external invitation from a specific space",
             method = "DELETE",
             description = "This Delete a specific external invitation from a specific space if the authenticated user is a member or manager of the space or a spaces super manager.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response declineExternalInvitations(@Context
  UriInfo uriInfo,
                                             @Parameter(description = "invitation id", required = true)
                                             @PathParam("invitationId")
                                             String invitationId) {

    SpaceExternalInvitation spaceExternalInvitation = spaceService.getSpaceExternalInvitationById(invitationId);
    if (spaceExternalInvitation == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    Space space = spaceService.getSpaceById(spaceExternalInvitation.getSpaceId());
    if (space == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (!spaceService.canManageSpace(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    try {
      spaceService.deleteSpaceExternalInvitation(invitationId);
    } catch (Exception e) {
      LOG.error("Unknown error occurred while deleting invitation", e);
      return Response.serverError().build();
    }
    return Response.noContent().build();
  }

  private void updateProfileField(Space space,
                                  String name,
                                  String value) throws IOException {
    if (Profile.BANNER.equals(name) && StringUtils.equals(value, "DEFAULT_BANNER")) {
      space.setBannerAttachment(null);
      spaceService.updateSpaceBanner(space, RestUtils.getCurrentUser());
    } else if (Profile.AVATAR.equals(name) || Profile.BANNER.equals(name)) {
      UploadResource uploadResource = uploadService.getUploadResource(value);
      if (uploadResource == null) {
        throw new IllegalStateException("No uploaded resource found with uploadId = " + value);
      }
      String storeLocation = uploadResource.getStoreLocation();
      try (FileInputStream inputStream = new FileInputStream(storeLocation)) {
        if (Profile.AVATAR.equals(name)) {
          AvatarAttachment attachment = new AvatarAttachment(null,
                                                             uploadResource.getFileName(),
                                                             uploadResource.getMimeType(),
                                                             inputStream,
                                                             System.currentTimeMillis());
          space.setAvatarAttachment(attachment);
          spaceService.updateSpaceAvatar(space, RestUtils.getCurrentUser());
        } else {
          BannerAttachment attachment = new BannerAttachment(null,
                                                             uploadResource.getFileName(),
                                                             uploadResource.getMimeType(),
                                                             inputStream,
                                                             System.currentTimeMillis());
          space.setBannerAttachment(attachment);
          spaceService.updateSpaceBanner(space, RestUtils.getCurrentUser());
        }
      } finally {
        uploadService.removeUploadResource(value);
      }
    }
  }

  private Response.ResponseBuilder getDefaultAvatarBuilder() throws IOException {
    if (defaultSpaceAvatar == null) {
      InputStream is = PortalContainer.getInstance()
                                      .getPortalContext()
                                      .getResourceAsStream("/skin/images/avatar/DefaultSpaceAvatar.png");
      if (is == null) {
        defaultSpaceAvatar = new byte[] {};
      } else {
        defaultSpaceAvatar = IOUtil.getStreamContentAsBytes(is);
      }
    }
    return Response.ok(new ByteArrayInputStream(defaultSpaceAvatar), "image/png");
  }

  private Response buildSpaceResponse(Space space, String expand, UriInfo uriInfo, Request request) {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (space == null
        || (Space.HIDDEN.equals(space.getVisibility())
            && !spaceService.canViewSpace(space, authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    SpaceEntity spaceEntity = EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand);
    EntityTag eTag = new EntityTag(String.valueOf(spaceEntity.hashCode()));
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      builder = Response.ok(spaceEntity.getDataEntity(), MediaType.APPLICATION_JSON);
      builder.tag(eTag);
      builder.lastModified(new Date());
      builder.cacheControl(CACHE_REVALIDATE_CONTROL);
    }
    return builder.build();
  }

  private Response.ResponseBuilder getDefaultBannerBuilder() throws IOException {
    if (defaultSpaceBanner == null) {
      InputStream is = PortalContainer.getInstance().getPortalContext().getResourceAsStream(PROFILE_DEFAULT_BANNER_URL);
      if (is == null) {
        LOG.warn("Can't find default user banner file in location {}", PROFILE_DEFAULT_BANNER_URL);
        defaultSpaceBanner = new byte[] {};
      } else {
        defaultSpaceBanner = IOUtil.getStreamContentAsBytes(is);
      }
    }

    ResponseBuilder builder = Response.ok(new ByteArrayInputStream(defaultSpaceBanner), "image/png");
    builder.lastModified(DEFAULT_IMAGES_LAST_MODIFED);
    EntityTag eTag = new EntityTag(String.valueOf(DEFAULT_IMAGES_HASH));
    builder.tag(eTag);
    return builder;
  }

}
