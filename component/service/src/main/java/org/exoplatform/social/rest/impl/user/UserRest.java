/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.rest.impl.user;

import static org.exoplatform.social.rest.api.RestUtils.getCurrentUser;
import static org.exoplatform.social.rest.api.RestUtils.getOnlineIdentities;
import static org.exoplatform.social.rest.api.RestUtils.getOnlineIdentitiesOfSpace;
import static org.exoplatform.social.rest.api.RestUtils.getUserIdentity;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.organization.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.picocontainer.Startable;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.rest.UserFieldValidator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.rest.http.PATCH;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.services.thumbnail.ImageThumbnailService;
import org.exoplatform.services.user.UserStateService;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.UpdateType;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.model.Attachment;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.ErrorResource;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.ExperienceEntity;
import org.exoplatform.social.rest.entity.IMEntity;
import org.exoplatform.social.rest.entity.PhoneEntity;
import org.exoplatform.social.rest.entity.ProfileEntity;
import org.exoplatform.social.rest.entity.ProfilePropertySettingEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;
import org.exoplatform.social.rest.entity.URLEntity;
import org.exoplatform.social.rest.entity.UserEntity;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.login.recovery.PasswordRecoveryService;

import io.meeds.social.core.identity.model.UserExportFilter;
import io.meeds.social.core.identity.model.UserExportResult;
import io.meeds.social.core.identity.model.UserImportResult;
import io.meeds.social.core.identity.service.UserExportService;
import io.meeds.social.core.identity.service.UserImportService;
import io.meeds.social.image.plugin.FileThumbnailPlugin;
import io.meeds.web.security.service.OtpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;

/**
 * Provides REST Services for manipulating jobs related to users.
 */

@Path(VersionResources.VERSION_ONE + "/social/users")
@Tag(name = VersionResources.VERSION_ONE +
    "/social/users", description = "Operations on users with their activities, connections and spaces")
public class UserRest implements ResourceContainer, Startable {

  private static final String UPLOAD_ID_PROGRESS_NOT_FOUND = "UPLOAD_ID_PROGRESS:NOT_FOUND";

  private static final String             EMAIL_MSG_PREFIX            = "EMAIL:";

  private static final String             FIRSTNAME_MSG_PREFIX        = "FIRSTNAME:";

  private static final String             LASTNAME_MSG_PREFIX         = "LASTNAME:";

  private static final String             PASSWORD_MSG_PREFIX         = "PASSWORD:";

  private static final String             USERNAME_MSG_PREFIX         = "USERNAME:";

  private static final String             IMAGE_PNG_MEDIA_TYPE        = "image/png";

  private static final String             THIRD_USER_FIELD            = "thirdField";

  private static final String             USER_DISPLAYED_PHONE        = "displayedPhone";

  private static final String             USER_DISPLAYED_EMAIL        = "displayedEmail";

  private static final String             SECOND_USER_FIELD           = "secondField";

  private static final String             FIRST_USER_FIELD            = "firstField";

  public static final String              PROFILE_DEFAULT_BANNER_URL  = "/skin/images/banner/DefaultUserBanner.png";

  public static final String              PROFILE_DEFAULT_AVATAR_URL  = "/skin/images/avatar/DefaultUserAvatar.png";

  private static final String             ONLINE                      = "online";

  private static final String             INTERNAL                    = "internal";

  private static final String             CONNECTED                   = "connected";

  public static final String              USER_CARD_SETTINGS          = "UserCardSettings";

  private static final CacheControl       CACHE_CONTROL               = new CacheControl();

  private static final Date               DEFAULT_IMAGES_LAST_MODIFED = new Date();

  private static final long               DEFAULT_IMAGES_HASH         = DEFAULT_IMAGES_LAST_MODIFED.getTime();

  // 3 days
  private static final int                CACHE_IN_SECONDS            = 3 * 86400;

  private static final int                CACHE_IN_MILLI_SECONDS      = CACHE_IN_SECONDS * 1000;

  private static final UserFieldValidator USERNAME_VALIDATOR          = new UserFieldValidator("userName", true, false);

  private static final UserFieldValidator EMAIL_VALIDATOR             = new UserFieldValidator("email", false, false);

  private static final UserFieldValidator LASTNAME_VALIDATOR          = new UserFieldValidator("lastName", false, true);

  private static final UserFieldValidator FIRSTNAME_VALIDATOR         = new UserFieldValidator("firstName", false, true);

  private static final UserFieldValidator PASSWORD_VALIDATOR          = new UserFieldValidator("password", false, false, 8, 255);

  private UserACL                         userACL;

  private OrganizationService             organizationService;

  private IdentityManager                 identityManager;

  private RelationshipManager             relationshipManager;

  private UserStateService                userStateService;

  private UserExportService               userExportService;

  private UserImportService               userImportService;

  private SpaceService                    spaceService;

  private ImageThumbnailService           imageThumbnailService;

  private ProfilePropertyService          profilePropertyService;

  private PasswordRecoveryService         passwordRecoveryService;

  private LocaleConfigService             localeConfigService;

  private static final Log                LOG                         = ExoLogger.getLogger(UserRest.class);

  private byte[]                          defaultUserAvatar           = null;

  private byte[]                          defaultUserBanner           = null;

  private UploadService                   uploadService;

  private SettingService                  settingService;

  private OtpService                      otpService;

  private ExecutorService                 importExecutorService       = null;

  public UserRest(UserACL userACL, // NOSONAR
                  OrganizationService organizationService,
                  IdentityManager identityManager,
                  RelationshipManager relationshipManager,
                  UserStateService userStateService,
                  SpaceService spaceService,
                  UploadService uploadService,
                  UserExportService userExportService,
                  UserImportService userImportService,
                  ImageThumbnailService imageThumbnailService,
                  ProfilePropertyService profilePropertyService,
                  PasswordRecoveryService passwordRecoveryService,
                  LocaleConfigService localeConfigService,
                  SettingService settingService,
                  OtpService otpService) {
    this.userACL = userACL;
    this.organizationService = organizationService;
    this.identityManager = identityManager;
    this.relationshipManager = relationshipManager;
    this.userStateService = userStateService;
    this.spaceService = spaceService;
    this.uploadService = uploadService;
    this.userExportService = userExportService;
    this.userImportService = userImportService;
    this.imageThumbnailService = imageThumbnailService;
    this.profilePropertyService = profilePropertyService;
    this.passwordRecoveryService = passwordRecoveryService;
    this.localeConfigService = localeConfigService;
    this.otpService = otpService;
    this.importExecutorService = Executors.newSingleThreadExecutor();
    this.settingService = settingService;

    CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
  }

  @Override
  public void stop() {
    this.importExecutorService.shutdownNow();
  }

  @GET
  @RolesAllowed("users")
  @Operation(summary = "Gets all users", method = "GET", description = "Using the query param \"q\" to filter the target users, ex: \"q=jo*\" returns all the users beginning by \"jo\"." +
      "Using the query param \"status\" to filter the target users, ex: \"status=online*\" returns the visible online users." +
      "Using the query params \"status\" and \"spaceId\" together to filter the target users, ex: \"status=online*\" and \"spaceId=1*\" returns the visible online users who are member of space with id=1." +
      "The params \"status\" and \"spaceId\" cannot be used with \"q\" param since it will falsify the \"limit\" param which is 20 by default. If these 3 parameters are used together, the parameter \"q\" will be ignored," +
      "the current user \"excludeCurrentUser\" will be excluded")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Resource not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error due to data encoding"),
    @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getUsers( // NOSONAR
                           @Context
                           UriInfo uriInfo,
                           @Context
                           HttpServletRequest request,
                           @Parameter(description = "User name information to filter, ex: user name, last name, first name or full name")
                           @QueryParam("q")
                           String q,
                           @Parameter(description = "Is search with email")
                           @QueryParam("searchEmail")
                           boolean searchEmail,
                           @Parameter(description = "Is search with username")
                           @QueryParam("searchUsername")
                           boolean searchUsername,
                           @Parameter(description = "User status to filter online users, ex: online")
                           @QueryParam("status")
                           String status,
                           @Parameter(description = "User type to filter, ex: internal, external")
                           @DefaultValue("internal")
                           @QueryParam("userType")
                           String userType,
                           @Parameter(description = "Is connected users")
                           @QueryParam("isConnected")
                           String isConnected,
                           @Parameter(description = "Space id to filter only its members, ex: 1")
                           @QueryParam("spaceId")
                           List<Long> spaceIds,
                           @Parameter(description = "Group id to filter only its members, ex: /platform")
                           @QueryParam("groupId")
                           List<String> groupIds,
                           @Parameter(description = "Is disabled users")
                           @Schema(defaultValue = "false")
                           @QueryParam("isDisabled")
                           boolean isDisabled,
                           @Parameter(description = "Enrollment status, ex: enrolled, not enrolled, no possible enrollment")
                           @QueryParam("enrollmentStatus")
                           String enrollmentStatus,
                           @Parameter(description = "the current user will be excluded in the list")
                           @Schema(defaultValue = "false")
                           @QueryParam("excludeCurrentUser")
                           boolean excludeCurrentUser,
                           @Parameter(description = "List of included users")
                           @Schema(defaultValue = "false")
                           @QueryParam("includeUser")
                           List<String> includeUsers,
                           @Parameter(description = "Returning the progress of a ")
                           @Schema(defaultValue = "false")
                           @QueryParam("exportId")
                           String exportId,
                           @Parameter(description = "Whether to export users or not")
                           @Schema(defaultValue = "false")
                           @QueryParam("export")
                           boolean exportFile,
                           @Parameter(description = "Whether to download exported users")
                           @Schema(defaultValue = "false")
                           @QueryParam("download")
                           boolean download,
                           @Parameter(description = "sort Field", required = false)
                           @QueryParam("sortField")
                           String sortField,
                           @Parameter(description = "sort Direction", required = false)
                           @QueryParam("sortDirection")
                           String sortDirection,
                           @Parameter(description = "Offset")
                           @Schema(defaultValue = "0")
                           @QueryParam("offset")
                           int offset,
                           @Parameter(description = "Limit")
                           @Schema(defaultValue = "20")
                           @QueryParam("limit")
                           int limit,
                           @Parameter(description = "Returning the number of users found or not")
                           @Schema(defaultValue = "false")
                           @QueryParam("returnSize")
                           boolean returnSize,
                           @Parameter(description = "Asking for a full representation of a specific subresource if any")
                           @QueryParam("expand")
                           String expand) throws Exception {

    String username = request.getRemoteUser();
    if (!userACL.getSuperUser().equals(username)
        && !RestUtils.isMemberOfAdminGroup()
        && !RestUtils.isMemberOfDelegatedGroup()
        && userType != null
        && !userType.equals(INTERNAL)
        && !groupIds.isEmpty()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    if (isDisabled && StringUtils.isNotBlank(q)) {
        Map<String, String> error = Map.of("error", "Unsupported operation: Can't search for disabled users!");
        return Response.status(Response.Status.BAD_REQUEST)
                        .entity(error)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
    }

    if (StringUtils.isNotBlank(exportId) && download) {
      try {
        InputStream inputStream = userExportService.downloadUsersExport(exportId, username);
        return Response.ok(AutoCloseInputStream.builder().setInputStream(inputStream).get())
                       .header("Content-Disposition", "attachment; filename=users.csv")
                       .header("Content-Type", "text/csv")
                       .build();
      } catch (IllegalAccessException | ObjectNotFoundException | IllegalStateException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(UPLOAD_ID_PROGRESS_NOT_FOUND).build();
      }
    } else if (StringUtils.isNotBlank(exportId)) {
      try {
        UserExportResult exportResult = userExportService.getUsersExportResult(exportId, username);
        if (exportResult == null) {
          return Response.status(Response.Status.NOT_FOUND).entity(UPLOAD_ID_PROGRESS_NOT_FOUND).build();
        } else {
          return Response.ok(exportResult).type(RestUtils.getJsonMediaType()).build();
        }
      } catch (IllegalAccessException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(UPLOAD_ID_PROGRESS_NOT_FOUND).build();
      }
    } else if (exportFile) {
      UserExportFilter userExportFilter = new UserExportFilter(q,
                                                               includeUsers,
                                                               searchEmail,
                                                               searchUsername,
                                                               status,
                                                               userType,
                                                               isConnected,
                                                               spaceIds,
                                                               isDisabled,
                                                               enrollmentStatus,
                                                               excludeCurrentUser,
                                                               sortField,
                                                               sortDirection);
      UserExportResult exportResult = userExportService.exportUsers(userExportFilter, username);
      return Response.ok(exportResult).type(RestUtils.getJsonMediaType()).build();
    }
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    Identity[] identities;
    int totalSize = 0;

    if (StringUtils.isNotBlank(status) && ONLINE.equals(status)) {
      Space space = null;
      if (CollectionUtils.isNotEmpty(spaceIds)) {
        List<Identity> allIdentities = new ArrayList<>();
        for (Long spaceId : spaceIds) {
          space = spaceService.getSpaceById(spaceId);
          if (space != null) {
            Identity[] onlineIdentity = getOnlineIdentitiesOfSpace(userStateService, username, space, limit);
            allIdentities.addAll(Arrays.asList(onlineIdentity));
          } else {
            return EntityBuilder.getResponse(new ErrorResource("space " + spaceId + " does not exist", "space not found"),
                                             uriInfo,
                                             RestUtils.getJsonMediaType(),
                                             Response.Status.NOT_FOUND);
          }
        }
        identities = allIdentities.toArray(new Identity[0]);
      } else {
        identities = getOnlineIdentities(userStateService, username, limit);
      }
    } else {
      Identity target = identityManager.getOrCreateUserIdentity(username);
      ProfileFilter filter = new ProfileFilter();
      filter.setName(q == null || q.isEmpty() ? "" : q);
      filter.setSearchEmail(searchEmail);
      filter.setSearchUserName(searchUsername);
      filter.setEnabled(!isDisabled);
      if (CollectionUtils.isNotEmpty(spaceIds)) {
        List<String> spaceIdsString = spaceIds.stream().map(String::valueOf).toList();
        filter.setSpaceIdentityIds(SpaceUtils.getSpaceIdentityIds(target.getRemoteId(), spaceIdsString));
      }
      if (CollectionUtils.isNotEmpty(groupIds)) {
        filter.setGroupIds(groupIds);
      }
      if (StringUtils.isNotBlank(sortField)) {
        Sorting.SortBy sortBy = Sorting.SortBy.valueOf(sortField.toUpperCase());
        Sorting.OrderBy orderBy = Sorting.OrderBy.ASC;
        if (StringUtils.isNotBlank(sortDirection)) {
          orderBy = Sorting.OrderBy.valueOf(sortDirection.toUpperCase());
        }
        filter.setSorting(new Sorting(sortBy, orderBy));
      }
      if (target != null && excludeCurrentUser) {
        filter.setViewerIdentity(target);
      }
      filter.setUserType(userType);
      filter.setConnected(isConnected != null ? isConnected.equals(CONNECTED) : null);
      filter.setEnrollmentStatus(enrollmentStatus);
      ListAccess<Identity> list = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, true);
      identities = list.load(offset, limit);
      if (returnSize) {
          totalSize = list.getSize();
      }
    }
    List<DataEntity> profileInfos = new ArrayList<>();
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
  @Path("/advancedfilter")
  @RolesAllowed("users")
  @Operation(summary = "Gets all users or connections by advanced filter", method = "POST", description = "")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Invalid query input"),
    @ApiResponse(responseCode = "404", description = "Resource not found") })
  public Response getUsersOrConnectionsByAdvancedFilter(// NOSONAR
                                                        @Context
                                                        UriInfo uriInfo,
                                                        @Parameter(description = "User type to filter, ex: internal, external")
                                                        @DefaultValue("internal")
                                                        @QueryParam("userType")
                                                        String userType,
                                                        @Parameter(description = "Filter type to filter , ex all , connection")
                                                        @DefaultValue("all")
                                                        @QueryParam("filterType")
                                                        String filterType,
                                                        @Parameter(description = "Is disabled users")
                                                        @Schema(defaultValue = "false")
                                                        @QueryParam("isDisabled")
                                                        boolean isDisabled,
                                                        @Parameter(description = "Offset")
                                                        @Schema(defaultValue = "0")
                                                        @QueryParam("offset")
                                                        int offset,
                                                        @Parameter(description = "Limit")
                                                        @Schema(defaultValue = "20")
                                                        @QueryParam("limit")
                                                        int limit,
                                                        @Parameter(description = "Returning the number of users found or not")
                                                        @Schema(defaultValue = "false")
                                                        @QueryParam("returnSize")
                                                        boolean returnSize,
                                                        @Parameter(description = "Asking for a full representation of a specific subresource if any")
                                                        @QueryParam("expand")
                                                        String expand,
                                                        @RequestBody(description = "pam user settings profile", required = true)
                                                        Map<String, String> settings,
                                                        @Parameter(description = "User name information to filter, ex: user name, last name, first name or full name")
                                                        @QueryParam("q")
                                                        String q,
                                                        @Parameter(description = "Whether to search for exact word or words containing it")
                                                        @QueryParam("wildCardSearch")
                                                        String wildcardSearch,
                                                        @Parameter(description = "Whether to exclude current user from search result")
                                                        @QueryParam("excludeCurrentUser")
                                                        boolean excludeCurrentUser) throws Exception {

    String userId;
    try {
      userId = ConversationState.getCurrent().getIdentity().getUserId();
    } catch (Exception e) {
      return Response.status(HTTPStatus.UNAUTHORIZED).build();
    }
    if (StringUtils.isBlank(userId)) {
      return Response.status(HTTPStatus.UNAUTHORIZED).build();
    }

    try {
      Identity target = identityManager.getOrCreateUserIdentity(userId);
      if (target == null) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }

      if (!userACL.getSuperUser().equals(userId) && !RestUtils.isMemberOfAdminGroup()
          && !RestUtils.isMemberOfDelegatedGroup()
          && userType != null
          && !userType.equals(INTERNAL)) {
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }

      offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
      limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
      Identity[] identities;
      int totalSize = 0;
      ProfileFilter filter = new ProfileFilter();
      filter.setName(q == null || q.isEmpty() ? "" : q);
      if (filterType.equals("all")) {
        filter.setEnabled(!isDisabled);
        if (!isDisabled) {
          filter.setUserType(userType);
        }
      }
      if (excludeCurrentUser) {
        filter.setExcludedIdentityList(Collections.singletonList(target));
      }
      if (settings != null) {
        settings.replaceAll((key, value) -> value.trim());
      }
      filter.setProfileSettings(settings);
      if (StringUtils.isNotBlank(wildcardSearch)) {
        filter.setWildcardSearch(Boolean.parseBoolean(wildcardSearch));
      }
      ListAccess<Identity> list =
                                filterType.equals("all") ?
                                                         identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME,
                                                                                                      filter,
                                                                                                      true) :
                                                         relationshipManager.getConnectionsByFilter(target, filter);
      identities = list.load(offset, limit);
      if (returnSize) {
        totalSize = list.getSize();
      }
      List<DataEntity> profileInfos = new ArrayList<>();
      for (Identity identity : identities) {
        if (identity != null) {
          ProfileEntity profileInfo = EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand);
          //
          profileInfos.add(profileInfo.getDataEntity());
        }
      }
      CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
      if (returnSize) {
        collectionUser.setSize(totalSize);
      }

      return EntityBuilder.getResponse(collectionUser, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    } catch (Exception e) {
      LOG.error("Unable to get users or connections with advanced filter", e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).entity(e.getMessage()).build();
    }
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @Operation(summary = "Creates a new user", method = "POST", description = "This creates the user if the authenticated user is in the /platform/administrators group.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response addUser(
                          @Context
                          UriInfo uriInfo,
                          @Context
                          HttpServletRequest request,
                          @Parameter(description = "Asking for a full representation of a specific subresource if any")
                          @QueryParam("expand")
                          String expand,
                          @RequestBody(description = "User object to be created, ex:<br />" +
                              "{<br />\"username\": \"john\"," +
                              "<br />\"password\": \"gtngtn\"," +
                              "<br />\"email\": \"john@exoplatform.com\"," +
                              "<br />\"firstname\": \"John\"," +
                              "<br />\"lastname\": \"Smith\"<br />}", required = true)
                          UserEntity model) throws Exception {
    if (model.isNotValid()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    // check if the user is already exist
    Identity identity = identityManager.getOrCreateUserIdentity(model.getUsername());
    if (identity != null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    if (isEmailAlreadyExists(model.getUsername(), model.getEmail())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    Locale locale = request == null ? Locale.ENGLISH : request.getLocale();

    String errorMessage = USERNAME_VALIDATOR.validate(locale, model.getUsername());
    if (StringUtils.isNotBlank(errorMessage)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(USERNAME_MSG_PREFIX + errorMessage).build();
    }

    errorMessage = PASSWORD_VALIDATOR.validate(locale, model.getPassword());
    if (StringUtils.isNotBlank(errorMessage)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(PASSWORD_MSG_PREFIX + errorMessage).build();
    }

    errorMessage = LASTNAME_VALIDATOR.validate(locale, model.getLastname());
    if (StringUtils.isNotBlank(errorMessage)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(LASTNAME_MSG_PREFIX + errorMessage).build();
    }

    errorMessage = FIRSTNAME_VALIDATOR.validate(locale, model.getFirstname());
    if (StringUtils.isNotBlank(errorMessage)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(FIRSTNAME_MSG_PREFIX + errorMessage).build();
    }

    errorMessage = EMAIL_VALIDATOR.validate(locale, model.getEmail());
    if (StringUtils.isNotBlank(errorMessage)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(EMAIL_MSG_PREFIX + errorMessage).build();
    }

    // Create new user
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.createUserInstance(model.getUsername());
    user.setFirstName(model.getFirstname());
    user.setLastName(model.getLastname());
    user.setEmail(model.getEmail());
    user.setPassword(model.getPassword());
    userHandler.createUser(user, true);
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(model.getUsername(), uriInfo.getPath(), expand),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Gets a specific user by user name", method = "GET", description = "This can only be done by the logged in user.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Resource not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error due to data encoding"),
    @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getUserById(
                              @Context
                              UriInfo uriInfo,
                              @Context
                              Request request,
                              @Parameter(description = "User name", required = true)
                              @PathParam("id")
                              String id,
                              @Parameter(description = "Asking for a full representation of a specific subresource if any")
                              @QueryParam("expand")
                              String expand) {
    Identity identity = identityManager.getOrCreateUserIdentity(id);
    //
    if (identity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();

    String expandedSettings = expand;
    if (expand != null && expand.contains("settings")) {
      expandedSettings =
                       String.valueOf(Objects.hash(EntityBuilder.buildEntityProfilePropertySettingList(profilePropertyService.getPropertySettings()
                                                                                                                             .stream()
                                                                                                                             .filter(prop -> prop.isVisible()
                                                                                                                                             || prop.isEditable())
                                                                                                                             .toList(),
                                                                                                       profilePropertyService,
                                                                                                       ProfilePropertyService.LABELS_OBJECT_TYPE,
                                                                                                       Long.parseLong(identity.getId()))));
    }

    // Get configured properties for cache
    // Get values of properties configured for the user card
    List<String> configuredCardProperties = new ArrayList<>();
    SettingValue<?> userCardFirstFieldSetting =
                                              settingService.get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                 new org.exoplatform.commons.api.settings.data.Scope(org.exoplatform.commons.api.settings.data.Scope.GLOBAL.getName(),
                                                                                                                     USER_CARD_SETTINGS),
                                                                 "UserCardFirstFieldSetting");
    SettingValue<?> userCardSecondFieldSetting =
                                               settingService.get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                  new org.exoplatform.commons.api.settings.data.Scope(org.exoplatform.commons.api.settings.data.Scope.GLOBAL.getName(),
                                                                                                                      USER_CARD_SETTINGS),
                                                                  "UserCardSecondFieldSetting");
    SettingValue<?> userCardThirdFieldSetting =
                                              settingService.get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                 new org.exoplatform.commons.api.settings.data.Scope(org.exoplatform.commons.api.settings.data.Scope.GLOBAL.getName(),
                                                                                                                     USER_CARD_SETTINGS),
                                                                 "UserCardThirdFieldSetting");
    if (userCardFirstFieldSetting != null) {
      configuredCardProperties.add((String) userCardFirstFieldSetting.getValue());
    } else {
      configuredCardProperties.add("position");
    }
    if (userCardSecondFieldSetting != null) {
      configuredCardProperties.add((String) userCardSecondFieldSetting.getValue());
    } else {
      configuredCardProperties.add("team");
    }
    if (userCardThirdFieldSetting != null) {
      configuredCardProperties.add((String) userCardThirdFieldSetting.getValue());
    } else {
      configuredCardProperties.add("city");
    }

    long cacheTime = identity.getCacheTime();
    String eTagValue = String.valueOf(Objects.hash(cacheTime, authenticatedUser, expandedSettings, configuredCardProperties));

    EntityTag eTag = new EntityTag(eTagValue, true);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      ProfileEntity profileInfo = EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand);
      builder = Response.ok(profileInfo.getDataEntity(), MediaType.APPLICATION_JSON);
      builder.tag(eTag);
      builder.lastModified(new Date(cacheTime));
      builder.expires(new Date(cacheTime));
    }
    return builder.build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("email/{email}")
  @RolesAllowed("users")
  @Operation(summary = "Gets a specific user by user email", method = "GET", description = "This can only be done by the logged in user.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Resource not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error due to data encoding"),
    @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getUserByEmail(
                                 @Context
                                 UriInfo uriInfo,
                                 @Parameter(description = "User email", required = true)
                                 @PathParam("email")
                                 String email) throws JSONException {
    User user = getUserByEmail(email);
    if (user == null) {
      return Response.ok().entity("{\"id\":\"" + null + "\"}").build();
    }
    Identity identity = identityManager.getOrCreateUserIdentity(user.getUserName());

    JSONObject jsonProfile = new JSONObject();
    jsonProfile.put("avatarUrl", identity.getProfile().getAvatarUrl());
    jsonProfile.put("fullName", identity.getProfile().getFullName());

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", identity.toString());
    jsonObject.put("profile", jsonProfile);
    jsonObject.put("providerId", identity.getProviderId());
    jsonObject.put("remoteId", identity.getRemoteId());

    return Response.ok(jsonObject.toString()).build();

  }

  @GET
  @Path("{id}/avatar")
  @Operation(summary = "Gets a specific user avatar by username", method = "GET", description = "The user avatar will be returned only if there is a currently authenticated user or an anonymous user that has a valid token generated by a Server encryption key.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Resource not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error due to data encoding"),
    @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getUserAvatarById( // NOSONAR
                                    @Context
                                    UriInfo uriInfo,
                                    @Context
                                    Request request,
                                    @Parameter(description = "User name", required = true)
                                    @PathParam("id")
                                    String id,
                                    @Parameter(description = "Whether to retrieve avatar by identity id or username", required = true)
                                    @DefaultValue("false")
                                    @QueryParam("byId")
                                    boolean byId,
                                    @Parameter(description = "The value of lastModified parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'")
                                    @QueryParam("lastModified")
                                    String lastModified,
                                    @Parameter(description = "Resized avatar size. Use 0x0 for original size.")
                                    @DefaultValue("100x100")
                                    @QueryParam("size")
                                    String size,
                                    @Parameter(description = "A mandatory valid token that is used to authorize anonymous request")
                                    @QueryParam("r")
                                    String token) throws IOException {

    boolean isDefault = StringUtils.equals(LinkProvider.DEFAULT_IMAGE_REMOTE_ID, id);
    Identity identity = null;
    Long lastUpdated = null;

    Response.ResponseBuilder builder = null;
    if (isDefault) {
      lastUpdated = DEFAULT_IMAGES_LAST_MODIFED.getTime();
    } else {
      identity = byId ? identityManager.getIdentity(Long.parseLong(id)) : identityManager.getOrCreateUserIdentity(id);
      if (identity == null || !identity.isUser()) {
        LOG.debug("Identity of user {} is not found, thus no avatar will be returned", id);
        return Response.status(Status.NOT_FOUND).build();
      } else {
        Profile profile = identity.getProfile();
        if (profile != null) {
          lastUpdated = profile.getAvatarLastUpdated();
        }
      }
    }

    EntityTag eTag = null;
    if (isDefault) {
      eTag = new EntityTag(String.valueOf(DEFAULT_IMAGES_HASH));
    } else if (lastUpdated != null) {
      eTag = new EntityTag(lastUpdated + "-" + size);
    }

    builder = eTag == null ? null : request.evaluatePreconditions(eTag);
    if (builder == null) {
      if (isDefault || lastUpdated == null) {
        builder = getDefaultAvatarBuilder();
      } else {
        if (RestUtils.isAnonymous() && !LinkProvider.isAttachmentTokenValid(token,
                                                                            OrganizationIdentityProvider.NAME,
                                                                            id,
                                                                            AvatarAttachment.TYPE,
                                                                            lastModified)) {
          LOG.warn("An anonymous user attempts to access avatar of user {} without a valid access token", id);
          return Response.status(Status.NOT_FOUND).build();
        }

        if (identity.isEnable() && !identity.isDeleted()) {
          int[] dimension = Utils.parseDimension(size);
          byte[] avatarContent = null;
          try {
            FileItem avatarFile = identityManager.getAvatarFile(identity);
            if (identityManager.getAvatarFile(identity) != null) {
              if (dimension[0] == 0 || dimension[1] == 0) {
                avatarContent = avatarFile.getAsByte();
              } else {
                FileInfo fileInfo = avatarFile.getFileInfo();
                FileItem file = imageThumbnailService.getOrCreateThumbnail(FileThumbnailPlugin.FILE_TYPE,
                                                                           Long.toString(fileInfo.getId()),
                                                                           fileInfo.getUpdater(),
                                                                           dimension[0],
                                                                           dimension[1]);
                avatarContent = file != null ? file.getAsByte() : avatarFile.getAsByte();
              }
            }
          } catch (Exception e) {
            LOG.error("Error while resizing avatar of user identity with Id {}, original Image will be returned",
                      identity.getId(),
                      e);
          }
          if (avatarContent != null) {
            builder = Response.ok(avatarContent, IMAGE_PNG_MEDIA_TYPE);
          }
        }
      }
    }

    if (builder == null) {
      InputStream stream = identityManager.getAvatarInputStream(identity);
      builder = Response.ok(stream, IMAGE_PNG_MEDIA_TYPE);
    }
    builder.lastModified(lastUpdated == null ? DEFAULT_IMAGES_LAST_MODIFED : new Date(lastUpdated));
    if (eTag != null) {
      builder.tag(eTag);
    }
    builder.cacheControl(CACHE_CONTROL);
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
  @Operation(summary = "Gets a specific user banner by username", method = "GET", description = "The user avatar will be returned only if there is a currently authenticated user or an anonymous user that has a valid token generated by a Server encryption key.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Resource not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error due to data encoding"),
    @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getUserBannerById( // NOSONAR
                                    @Context
                                    UriInfo uriInfo,
                                    @Context
                                    Request request,
                                    @Parameter(description = "User name", required = true)
                                    @PathParam("id")
                                    String id,
                                    @Parameter(description = "Whether to retrieve banner by identity id or username", required = true)
                                    @DefaultValue("false")
                                    @QueryParam("byId")
                                    boolean byId,
                                    @Parameter(description = "The value of lastModified parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'")
                                    @QueryParam("lastModified")
                                    String lastModified,
                                    @Parameter(description = "A mandatory valid token that is used to authorize anonymous request")
                                    @QueryParam("r")
                                    String token) throws IOException {

    boolean isDefault = StringUtils.equals(LinkProvider.DEFAULT_IMAGE_REMOTE_ID, id);
    Identity identity = null;
    Long lastUpdated = null;

    Response.ResponseBuilder builder = null;
    if (isDefault) {
      lastUpdated = DEFAULT_IMAGES_LAST_MODIFED.getTime();
    } else {
      identity = byId ? identityManager.getIdentity(Long.parseLong(id)) : identityManager.getOrCreateUserIdentity(id);
      if (identity == null || !identity.isUser()) {
        LOG.debug("Identity of user {} is not found, thus no banner will be returned", id);
        return Response.status(Status.NOT_FOUND).build();
      } else {
        Profile profile = identity.getProfile();
        if (profile != null) {
          lastUpdated = profile.getBannerLastUpdated();
        }
      }
    }

    EntityTag eTag = null;
    if (isDefault) {
      eTag = new EntityTag(String.valueOf(DEFAULT_IMAGES_HASH));
    } else if (lastUpdated != null) {
      eTag = new EntityTag(String.valueOf(lastUpdated));
    }

    builder = eTag == null ? null : request.evaluatePreconditions(eTag);
    if (builder == null) {
      if (isDefault) {
        builder = getDefaultBannerBuilder();
      } else {
        if (RestUtils.isAnonymous() && !LinkProvider.isAttachmentTokenValid(token,
                                                                            OrganizationIdentityProvider.NAME,
                                                                            id,
                                                                            BannerAttachment.TYPE,
                                                                            lastModified)) {
          LOG.warn("An anonymous user attempts to access banner of user {} without a valid access token", id);
          return Response.status(Status.NOT_FOUND).build();
        }

        if (identity.isEnable() && !identity.isDeleted()) {
          InputStream stream = identityManager.getBannerInputStream(identity);
          if (stream != null) {
            /*
             * As recommended in the the RFC1341
             * (https://www.w3.org/Protocols/rfc1341/4_Content-Type.html), we
             * set the banner content-type to "image/png". So, its data would be
             * recognized as "image" by the user-agent
             */
            builder = Response.ok(stream, IMAGE_PNG_MEDIA_TYPE);
            builder.lastModified(lastUpdated == null ? DEFAULT_IMAGES_LAST_MODIFED : new Date(lastUpdated));

            if (eTag != null) {
              builder.tag(eTag);
            }
          } else {
            builder = getDefaultBannerBuilder();
          }
        } else {
          builder = getDefaultBannerBuilder();
        }
      }
    }

    builder.cacheControl(CACHE_CONTROL);
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
  @Operation(summary = "Update user property", method = "PATCH", description = "This can only be done by the logged in user.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fulfilled but not content returned"),
    @ApiResponse(responseCode = "500", description = "Internal server error due to data encoding"),
    @ApiResponse(responseCode = "403", description = "Unothorized to modify user profile"),
    @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response updateUserProfileAttribute(@Context
  HttpServletRequest request,
                                             @Parameter(description = "User name", required = true)
                                             @PathParam("id")
                                             String username,
                                             @Parameter(description = "User profile attribute name", required = true)
                                             @FormParam("name")
                                             String name,
                                             @Parameter(description = "User profile attribute value", required = true)
                                             @FormParam("value")
                                             String value,
                                             @Parameter(description = "OTP Method", required = false)
                                             @FormParam("otpMethod")
                                             String otpMethod,
                                             @Parameter(description = "OTP Code", required = false)
                                             @FormParam("otpCode")
                                             String otpCode) {
    if (StringUtils.isBlank(name)) {
      return Response.status(Status.BAD_REQUEST).entity("'name' parameter is mandatory").build();
    }
    if (value == null) {
      return Response.status(Status.BAD_REQUEST).entity("'value' parameter is mandatory").build();
    }
    String currentUser = getCurrentUser();
    if (!StringUtils.equals(currentUser, username) && !RestUtils.isMemberOfAdminGroup()) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    Locale locale = request == null ? Locale.ENGLISH : request.getLocale();

    Identity userIdentity = getUserIdentity(username);
    Profile profile = userIdentity.getProfile();
    try {
      String fieldName = ProfileEntity.getFieldName(name);
      if (Profile.FIRST_NAME.equals(fieldName)) {
        String errorMessage = FIRSTNAME_VALIDATOR.validate(locale, value);
        if (StringUtils.isNotBlank(errorMessage)) {
          return Response.status(Response.Status.BAD_REQUEST).entity(FIRSTNAME_MSG_PREFIX + errorMessage).build();
        }
      }
      if (Profile.LAST_NAME.equals(fieldName)) {
        String errorMessage = LASTNAME_VALIDATOR.validate(locale, value);
        if (StringUtils.isNotBlank(errorMessage)) {
          return Response.status(Response.Status.BAD_REQUEST).entity(LASTNAME_MSG_PREFIX + errorMessage).build();
        }
      }
      if (Profile.EMAIL.equals(fieldName)) {
        Response response = checkEmail(username, value, otpMethod, otpCode, locale);
        if (response != null) {
          return response;
        }
      }
      if (value.equals("DEFAULT_BANNER")) {
        profile.setListUpdateTypes(Arrays.asList(UpdateType.BANNER));
        profile.setBannerUrl("DEFAULT_BANNER");
        profile.removeProperty(name);
        identityManager.updateProfile(profile, getCurrentUser(), true);
      } else {
        updateProfileField(profile, fieldName, value, true,currentUser);
      }
    } catch (IllegalAccessException e) {
      LOG.error("User {} is not allowed to update attribute {}", currentUser, name);
      return Response.status(Status.UNAUTHORIZED).build();
    } catch (IdentityStorageException e) {
      return Response.serverError().entity(e.getMessageKey()).build();
    } catch (Exception e) {
      return Response.serverError().entity("Can't update Banner, error = " + e.getMessage()).build();
    }
    return Response.noContent().build();
  }

  @PATCH
  @Path("{id}/profile")
  @Operation(summary = "Update set of properties in user profile", method = "PATCH", description = "This can only be done by the logged in user.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fulfilled but not content returned"),
    @ApiResponse(responseCode = "500", description = "Internal server error due to data encoding"),
    @ApiResponse(responseCode = "403", description = "Unothorized to modify user profile"),
    @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response updateUserProfileAttributes(
                                              @Context
                                              HttpServletRequest request,
                                              @Parameter(description = "User name", required = true)
                                              @PathParam("id")
                                              String username,
                                              @Parameter(description = "OTP Method", required = false)
                                              @QueryParam("otpMethod")
                                              String otpMethod,
                                              @Parameter(description = "OTP Code", required = false)
                                              @QueryParam("otpCode")
                                              String otpCode,
                                              @RequestBody(description = "User profile attributes map", required = true)
                                              ProfileEntity profileEntity) throws Exception {
    if (StringUtils.isBlank(username)) {
      return Response.status(Status.BAD_REQUEST).entity("'username' path parameter is empty").build();
    }
    if (profileEntity == null) {
      return Response.status(Status.BAD_REQUEST).entity("Use profile entity is mandatory").build();
    }

    String currentUser = getCurrentUser();
    if (!StringUtils.equals(currentUser, username) && !RestUtils.isMemberOfAdminGroup()) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    Locale locale = request == null ? Locale.ENGLISH : request.getLocale();

    String firstName = profileEntity.getFirstname();
    String lastName = profileEntity.getLastname();
    String email = profileEntity.getEmail();

    if (firstName != null) {
      String errorMessage = FIRSTNAME_VALIDATOR.validate(locale, firstName);
      if (StringUtils.isNotBlank(errorMessage)) {
        return Response.status(Response.Status.BAD_REQUEST).entity(FIRSTNAME_MSG_PREFIX + errorMessage).build();
      }
    }
    if (lastName != null) {
      String errorMessage = LASTNAME_VALIDATOR.validate(locale, lastName);
      if (StringUtils.isNotBlank(errorMessage)) {
        return Response.status(Response.Status.BAD_REQUEST).entity(LASTNAME_MSG_PREFIX + errorMessage).build();
      }
    }
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    if (email != null
        && !StringUtils.equals(email, identity.getProfile().getEmail())) {
      Response response = checkEmail(username, email, otpMethod, otpCode, locale);
      if (response != null) {
        return response;
      }
    }

    try {
      Map<String, Object> userProfileProperties = extractPropertiesFromEntities(profileEntity);
      saveProfile(username, userProfileProperties, currentUser);
    } catch (IllegalAccessException e) {
      LOG.error("User {} is not allowed to update attributes", currentUser);
      return Response.status(Status.UNAUTHORIZED).build();
    } catch (Exception e) {
      LOG.error("Error updating user {} attributes", currentUser, e);
      return Response.serverError().build();
    }
    return Response.noContent().build();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> extractPropertiesFromEntities(ProfileEntity profileEntity) { // NOSONAR
    Map<String, Object> userProfileProperties = new HashMap<>();
    for (String key : profileEntity.getDataEntity().keySet()) {
      if (profileEntity.getDataEntity().get(key) instanceof List<?>) {
        List<Map<String, String>> properties = new ArrayList<>();
        if (key.equalsIgnoreCase(Profile.CONTACT_URLS)) {
          List<URLEntity> urlEntities = (List<URLEntity>) profileEntity.getDataEntity().get(key);
          for (URLEntity url : urlEntities) {
            Map<String, String> urlMap = new HashMap<>();
            urlMap.put(url.getUrl(), url.getUrl());
            properties.add(urlMap);
          }
        } else if (key.equalsIgnoreCase(Profile.EXPERIENCES)) {
          List<ExperienceEntity> experienceEntities = (List<ExperienceEntity>) profileEntity.getDataEntity().get(key);
          for (ExperienceEntity experienceEntity : experienceEntities) {
            Map<String, String> experienceMap = new HashMap<>();
            if (StringUtils.isNotBlank(experienceEntity.getId())) {
              experienceMap.put(Profile.EXPERIENCES_ID, experienceEntity.getId());
            }
            experienceMap.put(Profile.EXPERIENCES_COMPANY, experienceEntity.getCompany());
            experienceMap.put(Profile.EXPERIENCES_DESCRIPTION, experienceEntity.getDescription());
            experienceMap.put(Profile.EXPERIENCES_SKILLS, experienceEntity.getSkills());
            experienceMap.put(Profile.EXPERIENCES_START_DATE, experienceEntity.getStartDate());
            experienceMap.put(Profile.EXPERIENCES_END_DATE, experienceEntity.getEndDate());
            experienceMap.put(Profile.EXPERIENCES_POSITION, experienceEntity.getPosition());
            experienceMap.put(Profile.EXPERIENCES_IS_CURRENT, String.valueOf(experienceEntity.getIsCurrent()));
            properties.add(experienceMap);
          }
        }
        userProfileProperties.put(key, properties);
      } else {
        userProfileProperties.put(key, profileEntity.getDataEntity().get(key));
      }
    }
    return userProfileProperties;
  }

  @PATCH
  @Path("{id}/profile/properties")
  @Operation(summary = "Update set of properties in user profile", method = "PATCH", description = "This can only be done by the logged in user.")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Request fulfilled but not content returned"),
    @ApiResponse(responseCode = "500", description = "Internal server error due to data encoding"),
    @ApiResponse(responseCode = "403", description = "Unothorized to modify user profile"),
    @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response updateUserProfileAttributes(@Context // NOSONAR
  HttpServletRequest request,
                                              @Parameter(description = "User name", required = true)
                                              @PathParam("id")
                                              String username,
                                              @RequestBody(description = "User profile attributes map", required = true)
                                              List<ProfilePropertySettingEntity> profilePropertySettingEntities) {
    if (StringUtils.isBlank(username)) {
      return Response.status(Status.BAD_REQUEST).entity("'username' path parameter is empty").build();
    }
    if (profilePropertySettingEntities == null || profilePropertySettingEntities.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Use profile properties are mandatory").build();
    }

    String currentUser = getCurrentUser();
    if (!StringUtils.equals(currentUser, username) && !RestUtils.isMemberOfAdminGroup()) {
      return Response.status(Status.UNAUTHORIZED).build();
    }
    Locale locale = request == null ? Locale.ENGLISH : request.getLocale();
    Identity userIdentity = getUserIdentity(username);
    Profile profile = userIdentity.getProfile();

    for (ProfilePropertySettingEntity profileProperty : profilePropertySettingEntities) {

      if (profileProperty.getPropertyName().equals(Profile.FIRST_NAME)) {
        String errorMessage = FIRSTNAME_VALIDATOR.validate(locale, profileProperty.getValue());
        if (StringUtils.isNotBlank(errorMessage)) {
          return Response.status(Response.Status.BAD_REQUEST).entity(FIRSTNAME_MSG_PREFIX + errorMessage).build();
        }
      }
      if (profileProperty.getPropertyName().equals(Profile.LAST_NAME)) {
        String errorMessage = LASTNAME_VALIDATOR.validate(locale, profileProperty.getValue());
        if (StringUtils.isNotBlank(errorMessage)) {
          return Response.status(Response.Status.BAD_REQUEST).entity(LASTNAME_MSG_PREFIX + errorMessage).build();
        }
      }
      if (profileProperty.getPropertyName().equals(Profile.EMAIL)
          && !StringUtils.equals(profile.getEmail(), profileProperty.getValue())) {
        return Response.status(Response.Status.BAD_REQUEST).entity("EMAIL:OTP_CODE_MANDATORY").build();
      }
      try {
        if (!(profileProperty.isMultiValued() || !profileProperty.getChildren().isEmpty())) {
          updateProfileField(profile, profileProperty.getPropertyName(), profileProperty.getValue(), false, currentUser);
          updateProfilePropertyVisibility(userIdentity, profileProperty);
        } else {
          List<Map<String, String>> maps = new ArrayList<>();
          profileProperty.getChildren().forEach(profilePropertySettingEntity -> {
            if (profilePropertySettingEntity.getValue() != null && !profilePropertySettingEntity.getValue().isBlank()
                && (profilePropertySettingEntity.getPropertyName() != null
                    && !profilePropertySettingEntity.getPropertyName().isBlank()
                    || profileProperty.isMultiValued())) {
              Map<String, String> childrenMap = new HashMap<>();
              if (profilePropertySettingEntity.getPropertyName() != null) {
                childrenMap.put("key", profilePropertySettingEntity.getPropertyName());
              }
              childrenMap.put("value", profilePropertySettingEntity.getValue());
              maps.add(childrenMap);
            }
          });
          updateProfileField(profile, profileProperty.getPropertyName(), maps, false, currentUser);
          updateProfilePropertyVisibility(userIdentity, profileProperty);
        }
      } catch (IllegalAccessException e) {
        LOG.error("User {} is not allowed to update attributes", currentUser);
        return Response.status(Status.UNAUTHORIZED).build();
      } catch (Exception e) {
        LOG.error("Error updating user {} attributes", currentUser, e);
        return Response.serverError().build();
      }
    }
    identityManager.updateProfile(profile, getCurrentUser(), true);
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @Operation(summary = "Deletes a specific user by user name", method = "DELETE", description = "This deletes the user if the authenticated user is in the /platform/administrators group.")
  public Response deleteUserById(
                                 @Context
                                 UriInfo uriInfo,
                                 @Parameter(description = "User name", required = true)
                                 @PathParam("id")
                                 String id,
                                 @Parameter(description = "Asking for a full representation of a specific subresource if any")
                                 @QueryParam("expand")
                                 String expand) throws Exception {
    // Check permission of current user
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    Identity identity = identityManager.getOrCreateUserIdentity(id);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    identityManager.hardDeleteIdentity(identity);
    identity.setDeleted(true);
    // Deletes the user on Portal side
    UserHandler userHandler = organizationService.getUserHandler();
    userHandler.removeUser(id, false);
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Updates a specific user by user name", method = "PUT", description = "This updates the user if he is the authenticated user.")
  public Response updateUserById(
                                 @Context
                                 UriInfo uriInfo,
                                 @Parameter(description = "User name", required = true)
                                 @PathParam("id")
                                 String id,
                                 @Parameter(description = "Asking for a full representation of a specific subresource if any")
                                 @QueryParam("expand")
                                 String expand,
                                 @RequestBody(description = "User object to be updated, ex:<br />" +
                                     "{<br />\"username\": \"john\"," +
                                     "<br />\"password\": \"gtngtn\"," +
                                     "<br />\"firstname\": \"John\"," +
                                     "<br />\"lastname\": \"Smith\"<br />}", required = true)
                                 UserEntity model) throws Exception {
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.findUserByName(id);
    if (user == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    // Check if the current user is the authenticated user
    if (!ConversationState.getCurrent().getIdentity().getUserId().equals(id)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    fillUserFromModel(user, model);
    userHandler.saveUser(user, true);
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(id, uriInfo.getPath(), expand),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @PATCH
  @Path("onboard/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Send onBoarding email to a specific user", method = "PATCH", description = "This send onBoarding email to a specific user.")
  public Response sendOnBoardingEmail(@Context
  HttpServletRequest request,
                                      @Parameter(description = "User name", required = true)
                                      @PathParam("id")
                                      String id) throws Exception {
    if (!RestUtils.isMemberOfAdminGroup() && !RestUtils.isMemberOfDelegatedGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.findUserByName(id);
    if (user == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    String url = getUrl(request);
    sendOnBoardingEmail((UserImpl) user, new StringBuilder(url));
    return Response.ok().build();
  }

  @PATCH
  @Path("bulk/{action}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Make action on list of users", method = "PATCH", description = "This will realize the action on the list of users if possible")
  public Response bulk(@Context
  HttpServletRequest request,
                       @Parameter(description = "Action", required = true)
                       @PathParam("action")
                       String action,
                       @Parameter(description = "User List", required = true)
                       List<String> users) throws Exception {

    if (!RestUtils.isMemberOfAdminGroup() && !RestUtils.isMemberOfDelegatedGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    String currentUsername = request.getRemoteUser();
    List<String> updatedUsers = new ArrayList<>();
    switch (action) {
    case "onboard":
      String url = getUrl(request);
      for (String username : users) {
        onboardUser(username, updatedUsers, url);
      }
      break;
    case "enable":
      for (String username : users) {
        enableUser(username, updatedUsers);
      }
      break;
    case "disable":
      for (String username : users) {
        disableUser(username, updatedUsers, currentUsername);
      }
      break;
    default:
      return Response.status(Response.Status.BAD_REQUEST).entity("Not Supported Action").build();
    }
    return Response.ok(updatedUsers).build();
  }

  @GET
  @Path("{id}/connections")
  @RolesAllowed("users")
  @Operation(summary = "Gets connections of a specific user", method = "GET", description = "This can only be done by the logged in user.")
  public Response getConnectionsOfUser(
                                       @Context
                                       UriInfo uriInfo,
                                       @Parameter(description = "User name", required = true)
                                       @PathParam("id")
                                       String id,
                                       @Parameter(description = "User name information to filter, ex: user name, last name, first name or full name", required = false)
                                       @QueryParam("q")
                                       String q,
                                       @Parameter(description = "Returning the number of connections or not")
                                       @Schema(defaultValue = "false")
                                       @QueryParam("returnSize")
                                       boolean returnSize,
                                       @Parameter(description = "Asking for a full representation of a specific subresource if any", required = false)
                                       @QueryParam("expand")
                                       String expand) throws Exception {
    Identity target = identityManager.getOrCreateUserIdentity(id);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    int limit = RestUtils.getLimit(uriInfo);
    int offset = RestUtils.getOffset(uriInfo);

    List<DataEntity> profileInfos = new ArrayList<>();
    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setName(q);
    ListAccess<Identity> listAccess = relationshipManager.getConnectionsByFilter(target, profileFilter);
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
  @Path("connections/invitations")
  @RolesAllowed("users")
  @Operation(summary = "Gets received invitations of current user", method = "GET", description = "This can only be done by the logged in user.")
  public Response getInvitationsOfUser(
                                       @Context
                                       UriInfo uriInfo,
                                       @Parameter(description = "Returning the number of connections or not")
                                       @Schema(defaultValue = "false")
                                       @QueryParam("returnSize")
                                       boolean returnSize,
                                       @Parameter(description = "Asking for a full representation of a specific subresource if any", required = false)
                                       @QueryParam("expand")
                                       String expand) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity target = identityManager.getOrCreateUserIdentity(currentUser);
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
  @Operation(summary = "Gets received invitations of current user", method = "GET", description = "This can only be done by the logged in user.")
  public Response getPendingOfUser(
                                   @Context
                                   UriInfo uriInfo,
                                   @Parameter(description = "Returning the number of connections or not")
                                   @Schema(defaultValue = "false")
                                   @QueryParam("returnSize")
                                   boolean returnSize,
                                   @Parameter(description = "Asking for a full representation of a specific subresource if any", required = false)
                                   @QueryParam("expand")
                                   String expand) throws Exception {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity target = identityManager.getOrCreateUserIdentity(currentUser);
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
  @Operation(summary = "Gets spaces of a specific user", method = "GET", description = "This returns a list of spaces in the following cases: <br/><ul><li>the given user is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  public Response getSpacesOfUser(
                                  @Context
                                  UriInfo uriInfo,
                                  @Parameter(description = "User name", required = true)
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
                                  @Parameter(description = "Returning the number of spaces or not")
                                  @Schema(defaultValue = "false")
                                  @QueryParam("returnSize")
                                  boolean returnSize,
                                  @Parameter(description = "Asking for a full representation of a specific subresource, ex: <em>members</em> or <em>managers</em>")
                                  @QueryParam("expand")
                                  String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    Identity target = identityManager.getOrCreateUserIdentity(id);
    // Check if the given user exists
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    // Check permission of authenticated user : he must be an admin or he is the
    // given user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (!userACL.getSuperUser().equals(authenticatedUser) && !authenticatedUser.equals(id)) {
      // Check permission of spaces to retrieve owner : authenticated user must
      // be in a confirmed relationship with spaces to retrieve's owner
      Identity authenticatedUserIdentity = identityManager.getOrCreateUserIdentity(authenticatedUser);
      Identity userIdentity = identityManager.getOrCreateUserIdentity(id);
      Relationship relationship = relationshipManager.get(authenticatedUserIdentity, userIdentity);
      if (relationship == null || relationship.getStatus() != Relationship.Type.CONFIRMED) {
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }
    }

    List<DataEntity> spaceInfos = new ArrayList<>();
    ListAccess<Space> listAccess = spaceService.getMemberSpaces(id);

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
  @Operation(summary = "Gets commons spaces of current user", method = "GET", description = "This returns a list of commons spaces in the following cases: <br/><ul><li>the given user is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  public Response getCommonSpacesOfUser(
                                        @Context
                                        UriInfo uriInfo,
                                        @Parameter(description = "User Id", required = true)
                                        @PathParam("userId")
                                        String userId,
                                        @Parameter(description = "Profile Id", required = true)
                                        @PathParam("profileId")
                                        String profileId,
                                        @Parameter(description = "Offset")
                                        @Schema(defaultValue = "0")
                                        @QueryParam("offset")
                                        int offset,
                                        @Parameter(description = "Limit")
                                        @Schema(defaultValue = "20")
                                        @QueryParam("limit")
                                        int limit,
                                        @Parameter(description = "Returning the number of spaces or not")
                                        @Schema(defaultValue = "false")
                                        @QueryParam("returnSize")
                                        boolean returnSize,
                                        @Parameter(description = "Asking for a full representation of a specific subresource, ex: <em>members</em> or <em>managers</em>")
                                        @QueryParam("expand")
                                        String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    Identity currentUser = identityManager.getOrCreateUserIdentity(userId);
    Identity userProfile = identityManager.getOrCreateUserIdentity(profileId);
    // Check if the current user and profile user exists
    if (currentUser == null || userProfile == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    // Check permission of authenticated user : he must be an admin or he is the
    // given user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (!userACL.getSuperUser().equals(authenticatedUser) && !authenticatedUser.equals(userId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    ListAccess<Space> commonSpacesAccessList = spaceService.getCommonSpaces(userId, profileId);

    List<DataEntity> commonSpaceInfos = Arrays.stream(commonSpacesAccessList.load(offset, limit))
                                              .map(space -> EntityBuilder.buildEntityFromSpace(space,
                                                                                               userId,
                                                                                               uriInfo.getPath(),
                                                                                               expand)
                                                                         .getDataEntity())
                                              .toList();
    CollectionEntity collectionSpace = new CollectionEntity(commonSpaceInfos, EntityBuilder.SPACES_TYPE, offset, limit);
    if (returnSize) {
      collectionSpace.setSize(commonSpacesAccessList.getSize());
    }
    return EntityBuilder.getResponse(collectionSpace, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("csv")
  @RolesAllowed("administrators")
  @Operation(summary = "Import users using CSV file", description = "Import users using CSV file that has a header defining user fields names." +
      "exemple of first line of CSV file: userName,firstName,lastName,password,email,groups,aboutMe,timeZone,company,position", method = "POST")
  public Response importUsers(@Context
  HttpServletRequest request,
                              @Parameter(description = "CSV File uploadId retrieved after uploading", required = true)
                              @FormParam("uploadId")
                              String uploadId,
                              @Parameter(description = "Get processing progress percentage of imported file")
                              @Schema(defaultValue = "false")
                              @FormParam("progress")
                              boolean progress,
                              @Parameter(description = "Whether clean file after processing or not")
                              @Schema(defaultValue = "false")
                              @FormParam("clean")
                              boolean clean,
                              @Parameter(description = "Whether process importing users in a sync or async way of current request")
                              @Schema(defaultValue = "false")
                              @FormParam("sync")
                              boolean sync) {
    if (StringUtils.isBlank(uploadId)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("UPLOAD_ID:MANDATORY").build();
    }

    Locale locale = request.getLocale();
    String url = getUrl(request);
    if (clean) {
      userImportService.cleanUsersImportResult(uploadId);
      return Response.noContent().build();
    } else if (progress) {
      UserImportResult importResult = userImportService.getUsersImportResult(uploadId);
      if (importResult == null) {
        return Response.status(Response.Status.NOT_FOUND).entity(UPLOAD_ID_PROGRESS_NOT_FOUND).build();
      } else {
        return Response.ok(importResult).build();
      }
    } else {
      try {
        userImportService.importUsers(uploadId,
                                      request.getRemoteUser(),
                                      locale,
                                      url,
                                      sync);
        return Response.noContent().build();
      } catch (IllegalArgumentException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      } catch (Exception e) {
        LOG.warn("Error while importing users", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
    }
  }

  @GET
  @Path("userCardSettings")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Gets the field settings of a user card", description = "Gets the field settings of a user card", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Invalid query input"),
    @ApiResponse(responseCode = "401", description = "User does not have permissions to get it"),
    @ApiResponse(responseCode = "404", description = "Setting does not exist"),
    @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getUserCardSettings(@Context
  Request request) {

    SettingValue<?> userCardFirstFieldSetting = settingService.get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                   new Scope(Scope.GLOBAL.getName(), USER_CARD_SETTINGS),
                                                                   "UserCardFirstFieldSetting");
    SettingValue<?> userCardSecondFieldSetting = settingService.get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                    new Scope(Scope.GLOBAL.getName(), USER_CARD_SETTINGS),
                                                                    "UserCardSecondFieldSetting");
    SettingValue<?> userCardThirdFieldSetting = settingService.get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                   new Scope(Scope.GLOBAL.getName(), USER_CARD_SETTINGS),
                                                                   "UserCardThirdFieldSetting");
    SettingValue<?> userDisplayedPhoneFieldSetting = settingService.get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                    new Scope(Scope.GLOBAL.getName(), USER_CARD_SETTINGS),
                                                                    "UserDisplayedPhonePropertySetting");
    SettingValue<?> userDisplayedEmailFieldSetting = settingService.get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                     new Scope(Scope.GLOBAL.getName(), USER_CARD_SETTINGS),
                                                                     "UserDisplayedEmailPropertySetting");
    Map<String, Object> userCardSettings = new HashMap<>();
    if (userCardFirstFieldSetting != null) {
      userCardSettings.put(FIRST_USER_FIELD, userCardFirstFieldSetting.getValue());
    } else {
      userCardSettings.put(FIRST_USER_FIELD, "position");
    }
    if (userCardSecondFieldSetting != null) {
      userCardSettings.put(SECOND_USER_FIELD, userCardSecondFieldSetting.getValue());
    } else {
      userCardSettings.put(SECOND_USER_FIELD, "team");
    }
    if (userCardThirdFieldSetting != null) {
      userCardSettings.put(THIRD_USER_FIELD, userCardThirdFieldSetting.getValue());
    } else {
      userCardSettings.put(THIRD_USER_FIELD, "city");
    }
    if (userDisplayedPhoneFieldSetting != null) {
      userCardSettings.put(USER_DISPLAYED_PHONE, userDisplayedPhoneFieldSetting.getValue());
    }
    if (userDisplayedEmailFieldSetting != null) {
      userCardSettings.put(USER_DISPLAYED_EMAIL, userDisplayedEmailFieldSetting.getValue());
    }

    String eTagValue = String.valueOf(Objects.hash(userCardSettings.get(FIRST_USER_FIELD),
                                                   userCardSettings.get(SECOND_USER_FIELD),
                                                   userCardSettings.get(THIRD_USER_FIELD),
                                                   userCardSettings.get(USER_DISPLAYED_PHONE),
                                                   userCardSettings.get(USER_DISPLAYED_EMAIL)));
    EntityTag eTag = new EntityTag(eTagValue, true);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      builder = Response.ok(new JSONObject(userCardSettings).toString(), MediaType.APPLICATION_JSON);
      builder.tag(eTag);
      builder.cacheControl(CACHE_CONTROL);
    }
    return builder.build();
  }

  private void updateProfilePropertyVisibility(Identity userIdentity, ProfilePropertySettingEntity profileProperty) {
    if (profileProperty.isToHide()) {
      profilePropertyService.hidePropertySetting(Long.parseLong(userIdentity.getId()), profileProperty.getId());
    } else if (profileProperty.isToShow()) {
      profilePropertyService.showPropertySetting(Long.parseLong(userIdentity.getId()), profileProperty.getId());
    }
  }

  private boolean isEmailAlreadyExists(String username, String email) throws Exception {
    Query query = new Query();
    query.setEmail(email);
    ListAccess<User> users = organizationService.getUserHandler().findUsersByQuery(query, UserStatus.ANY);
    int usersLength = users.getSize();
    return usersLength > 1 || (usersLength == 1 && !StringUtils.equals(users.load(0, 1)[0].getUserName(), username));
  }

  private void saveProfile(String username, Map<String, Object> profileProperties, String modifierUsername) throws IllegalAccessException,
                                                                                   ObjectNotFoundException {
    Identity userIdentity = getUserIdentity(username);
    if (userIdentity == null) {
      throw new ObjectNotFoundException("User identity of " + username + " wasn't found. It can be due to a disabled user.");
    } else {
      Profile profile = userIdentity.getProfile();

      Set<Entry<String, Object>> profileEntries = profileProperties.entrySet();
      for (Entry<String, Object> entry : profileEntries) {
        String name = entry.getKey();
        Object value = entry.getValue();
        String fieldName = ProfileEntity.getFieldName(name);
        updateProfileField(profile, fieldName, value, false, modifierUsername);
      }
      identityManager.updateProfile(profile, getCurrentUser(), true);
    }
  }

  private void fillUserFromModel(User user, UserEntity model) {
    if (model.getFirstname() != null && !model.getFirstname().isEmpty()) {
      user.setFirstName(model.getFirstname());
    }
    if (model.getLastname() != null && !model.getLastname().isEmpty()) {
      user.setLastName(model.getLastname());
    }
  }

  /**
   * Checks if input email is existing already or not.
   *
   * @param email Input email to check.
   * @return true if email is existing in system.
   */
  private User getUserByEmail(String email) {
    if (email == null)
      return null;
    try {
      Query query = new Query();
      query.setEmail(email);
      User[] users = organizationService.getUserHandler().findUsersByQuery(query).load(0, 10);
      return users[0];
    } catch (Exception e) {
      return null;
    }
  }

  private Response.ResponseBuilder getDefaultAvatarBuilder() throws IOException {
    if (defaultUserAvatar == null) {
      InputStream is = PortalContainer.getInstance().getPortalContext().getResourceAsStream(PROFILE_DEFAULT_AVATAR_URL);
      if (is == null) {
        LOG.warn("Can't find default user avatar file in location {}", PROFILE_DEFAULT_AVATAR_URL);
        defaultUserAvatar = new byte[] {};
      } else {
        defaultUserAvatar = IOUtil.getStreamContentAsBytes(is);
      }
    }

    ResponseBuilder builder = Response.ok(new ByteArrayInputStream(defaultUserAvatar), IMAGE_PNG_MEDIA_TYPE);
    builder.lastModified(DEFAULT_IMAGES_LAST_MODIFED);
    EntityTag eTag = new EntityTag(String.valueOf(DEFAULT_IMAGES_HASH));
    builder.tag(eTag);
    return builder;
  }

  private Response.ResponseBuilder getDefaultBannerBuilder() throws IOException {
    if (defaultUserBanner == null) {
      InputStream is = PortalContainer.getInstance().getPortalContext().getResourceAsStream(PROFILE_DEFAULT_BANNER_URL);
      if (is == null) {
        LOG.warn("Can't find default user banner file in location {}", PROFILE_DEFAULT_BANNER_URL);
        defaultUserBanner = new byte[] {};
      } else {
        defaultUserBanner = IOUtil.getStreamContentAsBytes(is);
      }
    }

    ResponseBuilder builder = Response.ok(new ByteArrayInputStream(defaultUserBanner), IMAGE_PNG_MEDIA_TYPE);
    builder.lastModified(DEFAULT_IMAGES_LAST_MODIFED);
    EntityTag eTag = new EntityTag(String.valueOf(DEFAULT_IMAGES_HASH));
    builder.tag(eTag);
    return builder;
  }

  private void sendOnBoardingEmail(UserImpl user, StringBuilder url) throws IllegalAccessException {
    Locale locale = localeConfigService.getDefaultLocaleConfig().getLocale();
    boolean onBoardingEmailSent = passwordRecoveryService.sendOnboardingEmail(user, locale, url);
    if (onBoardingEmailSent) {
      Identity userIdentity = identityManager.getOrCreateUserIdentity(user.getUserName());
      Profile profile = userIdentity.getProfile();
      updateProfileField(profile, Profile.ENROLLMENT_DATE, String.valueOf(Calendar.getInstance().getTimeInMillis()), true,null);
    }
  }

  @SneakyThrows
  private void updateProfileField(Profile profile,
                                  String name,
                                  Object value,
                                  boolean save,
                                  String modifierUsername) throws IllegalAccessException {
    ProfilePropertySetting propertySetting = profilePropertyService.getProfileSettingByName(name);
    if (propertySetting != null && !propertySetting.isEditable() && (modifierUsername==null || !userACL.getUserIdentity(modifierUsername).isMemberOf(userACL.getAdminGroups()))) {
      throw new IllegalAccessException(String.format("Not allowed to update non modifiable field '%s'", name));
    } else if (Profile.EXTERNAL.equals(name)) {
      throw new IllegalAccessException("Not allowed to update EXTERNAL field");
    } else if (Profile.USERNAME.equals(name)) {
      throw new IllegalAccessException("Not allowed to update USERNAME field");
    } else if (Profile.AVATAR.equals(name) || Profile.BANNER.equals(name)) {
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
          identityManager.updateProfile(profile, getCurrentUser(), true);
        }
      } finally {
        uploadService.removeUploadResource(value.toString());
      }
    } else {
      profile.setProperty(name, value);
      if (save) {
        identityManager.updateProfile(profile, getCurrentUser(), true);
      }
    }
  }

  private Response checkEmail(String username, String email, String otpMethod, String otpCode, Locale locale) throws Exception {
    if (StringUtils.isBlank(otpCode)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("EMAIL:OTP_CODE_MANDATORY").build();
    } else if (StringUtils.isBlank(otpMethod)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("EMAIL:OTP_METHOD_MANDATORY").build();
    }
    try {
      otpService.validateOtp(username, otpMethod, otpCode);
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.BAD_REQUEST).entity("EMAIL:OTP_CODE_WRONG").build();
    }
    String errorMessage = EMAIL_VALIDATOR.validate(locale, email);
    if (StringUtils.isNotBlank(errorMessage)) {
      return Response.status(Response.Status.BAD_REQUEST).entity(EMAIL_MSG_PREFIX + errorMessage).build();
    }
    // Check if mail address is already used
    if (isEmailAlreadyExists(username, email)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity("EMAIL:ALREADY_EXISTS").build();
    }
    return null;
  }

  @SneakyThrows
  private void onboardUser(String username, List<String> updatedUsers, String url) throws IllegalAccessException {
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.findUserByName(username);
    if (user == null) {
      LOG.warn("Cannot find user by username {} for onboarding, he is disabled or not existing", username);
    } else {
      Identity identity = identityManager.getOrCreateUserIdentity(username);
      if (Util.isExternal(identity.getId())) {
        LOG.warn("User {} is external, he cannot be enrolled.", username);
      } else if (!user.getLastLoginTime().equals(user.getCreatedDate())) {
        LOG.warn("User {} is already logged in, he cannot be enrolled", username);
      } else {
        sendOnBoardingEmail((UserImpl) user, new StringBuilder(url));
        updatedUsers.add(username);
      }
    }
  }

  private void enableUser(String username, List<String> updatedUsers) throws Exception {
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.findUserByName(username, UserStatus.DISABLED);
    if (user == null) {
      LOG.warn("Username {} is not found in disabled user list. He does not exists, or he is already enabled", username);
    } else {
      organizationService.getUserHandler().setEnabled(username, true, true);
      updatedUsers.add(username);
    }
  }

  private void disableUser(String username, List<String> updatedUsers, String currentUsername) throws Exception {
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.findUserByName(username, UserStatus.ENABLED);
    if (user == null) {
      LOG.warn("Username {} is not found in enabled user list. He does not exists, or he is already disabled", username);
    } else if (StringUtils.equals(currentUsername, user.getUserName())) {
      LOG.warn("User {} tries to suspend his own account. Not allowed", currentUsername);
    } else if (StringUtils.equals(userACL.getSuperUser(), user.getUserName())) {
      LOG.warn("Try to suspend superuser account {}. Not allowed", username);
    } else {
      organizationService.getUserHandler().setEnabled(username, false, true);
      updatedUsers.add(username);
    }
  }

  private String getUrl(HttpServletRequest request) {
    StringBuilder url = new StringBuilder();
    if (request != null) {
      url.append(request.getScheme()).append("://").append(request.getServerName());
      if (request.getServerPort() != 80 && request.getServerPort() != 443) {
        url.append(':').append(request.getServerPort());
      }
      PortalContainer container = PortalContainer.getCurrentInstance(request.getServletContext());
      url.append(container.getPortalContext().getContextPath());
    }
    return url.toString();
  }
}
