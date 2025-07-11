package org.exoplatform.social.rest.impl.favorite;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.http.PATCH;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Path(VersionResources.VERSION_ONE + "/social/favorites")
@Tag(name = VersionResources.VERSION_ONE + "/social/favorites", description = "Managing favorites for any type of data")
public class FavoriteRest implements ResourceContainer {

  private static final String FAVORITE_OBJECT_ID_REQUIRED   = "FavoriteObjectIdRequired";

  private static final String FAVORITE_OBJECT_TYPE_REQUIRED = "FavoriteObjectTypeRequired";

  private static final Log    LOG                           = ExoLogger.getLogger(FavoriteRest.class);

  private FavoriteService     favoriteService;

  public FavoriteRest(FavoriteService favoriteService) {
    this.favoriteService = favoriteService;
  }

  @POST
  @Path("{objectType}/{objectId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Creates a favorite object",
      description = "Creates a favorite object",
      method = "POST")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "409", description = "Conflict"),
      }
  )
  public Response createFavorite(
                                 @Parameter(
                                     description = "Object type: activity, comment, notes ...",
                                     required = true
                                 )
                                 @PathParam("objectType")
                                 String objectType,
                                 @Parameter(
                                     description = "Object identifier: technical id to identify object as favorite",
                                     required = true
                                 )
                                 @PathParam("objectId")
                                 String objectId,
                                 @Parameter(
                                     description = "Object parent identifier: technical id to identify "
                                         + "the parent of an object like the activity Id for a comment entity",
                                     required = false
                                 )
                                 @QueryParam("parentObjectId")
                                 String parentObjectId,
                                 @Parameter(
                                     description = "Space technical identitifier of the bookmarked object",
                                     required = false
                                 )
                                 @QueryParam("spaceId")
                                 long spaceId,
                                 @Parameter(
                                     description = "Whether ignore favorite when already exists or return a HTTP 409 code",
                                     required = false
                                 ) @Schema(defaultValue = "false")
                                 @QueryParam("ignoreWhenExisting") boolean ignoreWhenExisting) {
    if (StringUtils.isBlank(objectType)) {
      return Response.status(Status.BAD_REQUEST).entity(FAVORITE_OBJECT_TYPE_REQUIRED).build();
    }
    if (StringUtils.isBlank(objectId)) {
      return Response.status(Status.BAD_REQUEST).entity(FAVORITE_OBJECT_ID_REQUIRED).build();
    }
    Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    if (!favoriteService.canCreateFavorite(authenticatedUserIdentity, objectType, objectId)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      Favorite favorite = new Favorite(objectType, objectId, parentObjectId, userIdentityId, spaceId);
      favoriteService.createFavorite(favorite);
      return Response.noContent().build();
    } catch (ObjectAlreadyExistsException e) {
      if (ignoreWhenExisting) {
        return Response.noContent().build();
      } else {
        return Response.status(Status.CONFLICT).build();
      }
    } catch (Exception e) {
      LOG.warn("Error creating a favorite", e);
      return Response.serverError().build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Retrieves all favorites object of the current user",
      description = "Retrieves all favorites object of the current user",
      method = "GET"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
      }
  )
  public Response getFavoritesList(
                                   @Parameter(description = "Query Offset", required = true)
                                   @QueryParam("offset")
                                   int offset,
                                   @Parameter(description = "Query results limit", required = true)
                                   @QueryParam("limit")
                                   int limit,
                                   @Parameter(description = "Fovorites Object Type")
                                   @QueryParam("type")
                                   String objectType,
                                   @Parameter(description = "Fovorites total size")
                                   @Schema(defaultValue = "false")
                                   @QueryParam("returnSize")
                                   boolean returnSize) {
    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      FavoriteEntity favoriteEntity = new FavoriteEntity();
      List<MetadataItem> myFavorites = StringUtils.isBlank(objectType) ? favoriteService.getFavoriteItemsByCreator(userIdentityId,
                                                                                                                   offset,
                                                                                                                   limit) :
                                                                       favoriteService.getFavoriteItemsByCreatorAndType(objectType,
                                                                                                                        userIdentityId,
                                                                                                                        offset,
                                                                                                                        limit);
      favoriteEntity.setFavoritesItem(myFavorites);
      favoriteEntity.setLimit(limit);
      favoriteEntity.setOffset(offset);
      if (returnSize) {
        if (StringUtils.isBlank(objectType)) {
          favoriteEntity.setSize(favoriteService.getFavoriteItemsSize(userIdentityId));
        } else {
          favoriteEntity.setSize(favoriteService.getFavoriteItemsSize(objectType, userIdentityId));
        }
      }
      return Response.ok(favoriteEntity).build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while getting favorites list", e);
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("{objectType}/{objectId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Deletes a metadata item identified by its id",
      description = "Deletes a metadata item identified by its id",
      method = "DELETE"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "401", description = "Unauthorized"),
      }
  )
  public Response deleteFavorite(
                                 @Parameter(
                                     description = "Object type: activity, comment, notes ...",
                                     required = true
                                 )
                                 @PathParam("objectType")
                                 String objectType,
                                 @Parameter(
                                     description = "Object identifier: technical id to identify object as favorite",
                                     required = true
                                 )
                                 @PathParam("objectId")
                                 String objectId,
                                 @Parameter(
                                     description = "Whether ignore when not existing Metadata item or not."
                                         + "If true, it will return a HTTP code 204 when not existing else a 404.",
                                     required = false)
                                 @Schema(defaultValue = "false")
                                 @QueryParam("ignoreNotExisting")
                                 boolean ignoreNotExisting) {
    if (StringUtils.isBlank(objectType)) {
      return Response.status(Status.BAD_REQUEST).entity(FAVORITE_OBJECT_TYPE_REQUIRED).build();
    }
    if (StringUtils.isBlank(objectId)) {
      return Response.status(Status.BAD_REQUEST).entity(FAVORITE_OBJECT_ID_REQUIRED).build();
    }

    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      Favorite favorite = new Favorite(objectType, objectId, null, userIdentityId, 0);
      favoriteService.deleteFavorite(favorite);
      return Response.noContent().build();
    } catch (ObjectNotFoundException e) {
      if (ignoreNotExisting) {
        return Response.noContent().build();
      } else {
        return Response.status(Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      LOG.warn("Error deleting a favorite", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @PATCH
  @Path("{objectType}/{objectId}/view")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Update favorite object access order to make it listed first",
      method = "PATCH")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "404", description = "Not found"),
      }
  )
  public Response setFavoriteAsLastAccessed(
                                            @Parameter(description = "Object type: activity, comment, notes ...", required = true)
                                            @PathParam("objectType")
                                            String objectType,
                                            @Parameter(description = "Object identifier: technical id to identify object as favorite", required = true)
                                            @PathParam("objectId")
                                            String objectId) {
    if (StringUtils.isBlank(objectType)) {
      return Response.status(Status.BAD_REQUEST).entity(FAVORITE_OBJECT_TYPE_REQUIRED).build();
    }
    if (StringUtils.isBlank(objectId)) {
      return Response.status(Status.BAD_REQUEST).entity(FAVORITE_OBJECT_ID_REQUIRED).build();
    }
    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      favoriteService.setFavoriteAsLastAccessed(objectType, objectId, userIdentityId);
      return Response.noContent().build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

}
