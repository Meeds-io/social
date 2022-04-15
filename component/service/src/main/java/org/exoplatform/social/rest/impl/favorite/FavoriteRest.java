package org.exoplatform.social.rest.impl.favorite;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.annotations.*;

import java.util.HashMap;
import java.util.Map;

@Path(VersionResources.VERSION_ONE + "/social/favorites")
@Api(
    tags = VersionResources.VERSION_ONE + "/social/favorites",
    value = VersionResources.VERSION_ONE + "/social/favorites",
    description = "Managing favorites for any type of data" // NOSONAR
)
public class FavoriteRest implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(FavoriteRest.class);

  private FavoriteService  favoriteService;

  public FavoriteRest(FavoriteService favoriteService) {
    this.favoriteService = favoriteService;
  }

  @POST
  @Path("{objectType}/{objectId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Creates a favorite object",
      httpMethod = "POST",
      response = Response.class,
      produces = MediaType.APPLICATION_JSON,
      notes = "Returns newly created favorite"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 204, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 409, message = "Conflict"),
      }
  )
  public Response createFavorite(
                                 @ApiParam(
                                     value = "Object type: activity, comment, notes ...",
                                     required = true
                                 )
                                 @PathParam("objectType")
                                 String objectType,
                                 @ApiParam(
                                     value = "Object identifier: technical id to identify object as favorite",
                                     required = true
                                 )
                                 @PathParam("objectId")
                                 String objectId,
                                 @ApiParam(
                                     value = "Object parent identifier: technical id to identify "
                                         + "the parent of an object like the activity Id for a comment entity",
                                     required = false
                                 )
                                 @QueryParam("parentObjectId")
                                 String parentObjectId,
                                 @ApiParam(
                                     value = "Whether ignore favorite when already exists or return a HTTP 409 code",
                                     required = false,
                                     defaultValue = "false"
                                 )
                                 @QueryParam("ignoreWhenExisting")
                                 boolean ignoreWhenExisting) {
    if (StringUtils.isBlank(objectType)) {
      return Response.status(Status.BAD_REQUEST).entity("FavoriteObjectTypeRequired").build();
    }
    if (StringUtils.isBlank(objectId)) {
      return Response.status(Status.BAD_REQUEST).entity("FavoriteObjectIdRequired").build();
    }

    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      Favorite favorite = new Favorite(objectType, objectId, parentObjectId, userIdentityId);
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
  @ApiOperation(
      value = "Retrieves all favorites object of the current user",
      httpMethod = "GET",
      response = Response.class,
      produces = "application/json",
      notes = "Return list of favorites"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error")
      }
  )
  public Response getFavoritesList(
                                      @ApiParam(
                                          value = "Query Offset",
                                          required = true
                                      )
                                      @QueryParam("offset")
                                      int offset,
                                      @ApiParam(
                                          value = "Query results limit",
                                          required = true
                                      )
                                      @QueryParam("limit")
                                      int limit,
                                      @ApiParam(
                                          value = "Fovorites total size",
                                          defaultValue = "false")
                                      @QueryParam("returnSize")
                                      boolean returnSize) {
    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      FavoriteEntity favoriteEntity = new FavoriteEntity();
      List<MetadataItem> myFavorites = favoriteService.getFavoriteItemsByCreator(userIdentityId, offset, limit);
      favoriteEntity.setFavoritesItem(myFavorites);
      favoriteEntity.setLimit(limit);
      favoriteEntity.setOffset(offset);
      if (returnSize) {
        favoriteEntity.setSize(favoriteService.getFavoriteItemsSize(userIdentityId));
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
  @ApiOperation(
      value = "Deletes a metadata item identified by its id",
      httpMethod = "DELETE",
      response = Response.class,
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON,
      notes = "Returns deleted Metadata item"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 204, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 404, message = "Not found"),
          @ApiResponse(code = 401, message = "Unauthorized"),
      }
  )
  public Response deleteFavorite(
                                 @ApiParam(
                                     value = "Object type: activity, comment, notes ...",
                                     required = true
                                 )
                                 @PathParam("objectType")
                                 String objectType,
                                 @ApiParam(
                                     value = "Object identifier: technical id to identify object as favorite",
                                     required = true
                                 )
                                 @PathParam("objectId")
                                 String objectId,
                                 @ApiParam(
                                     value = "Whether ignore when not existing Metadata item or not."
                                         + "If true, it will return a HTTP code 204 when not existing else a 404.",
                                     required = false,
                                     defaultValue = "false"
                                 )
                                 @QueryParam("ignoreNotExisting")
                                 boolean ignoreNotExisting) {
    if (StringUtils.isBlank(objectType)) {
      return Response.status(Status.BAD_REQUEST).entity("FavoriteObjectTypeRequired").build();
    }
    if (StringUtils.isBlank(objectId)) {
      return Response.status(Status.BAD_REQUEST).entity("FavoriteObjectIdRequired").build();
    }

    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      Favorite favorite = new Favorite(objectType, objectId, null, userIdentityId);
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

}
