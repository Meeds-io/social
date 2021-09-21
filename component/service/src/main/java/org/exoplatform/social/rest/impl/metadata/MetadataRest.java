package org.exoplatform.social.rest.impl.metadata;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.MetadataItemEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.annotations.*;

@Path(VersionResources.VERSION_ONE + "/social/metadatas")
@Api(
    tags = VersionResources.VERSION_ONE + "/social/metadatas",
    value = VersionResources.VERSION_ONE + "/social/metadatas",
    description = "Managing objects metadatas" // NOSONAR
)
public class MetadataRest implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(MetadataRest.class);

  private MetadataService  metadataService;

  public MetadataRest(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @POST
  @Path("{type}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Creates a metadata item",
      httpMethod = "POST",
      response = Response.class,
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON,
      notes = "Returns newly created Metadata item"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 401, message = "Unauthorized"),
      }
  )
  public Response createMetadataItem(
                                     @PathParam("type")
                                     String type,
                                     MetadataItemEntity metadataItemEntity) {
    if (StringUtils.isBlank(type)) {
      return Response.status(Status.BAD_REQUEST).entity("MetadataTypeRequired").build();
    }
    if (metadataItemEntity == null) {
      return Response.status(Status.BAD_REQUEST).entity("MetadataItemRequired").build();
    }
    if (StringUtils.isBlank(metadataItemEntity.getObjectType())) {
      return Response.status(Status.BAD_REQUEST).entity("MetadataItemObjectTypeRequired").build();
    }
    if (StringUtils.isBlank(metadataItemEntity.getObjectId())) {
      return Response.status(Status.BAD_REQUEST).entity("MetadataItemObjectIdRequired").build();
    }

    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      MetadataItem metadataItem = fromEntity(metadataItemEntity);

      metadataItem = metadataService.createMetadataItem(metadataItem,
                                                        type,
                                                        metadataItemEntity.getName(),
                                                        metadataItemEntity.getAudienceId(),
                                                        userIdentityId);
      return Response.ok(toEntity(metadataItem)).build();
    } catch (IllegalAccessException e) {
      LOG.warn("User '{}' attempts to create a metadata item of type '{}' and name '{}' for audience '{}'",
               RestUtils.getCurrentUser(),
               type,
               metadataItemEntity.getName(),
               metadataItemEntity.getAudienceId(),
               e);
      return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.warn("Error creating a metadata item", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @DELETE
  @Path("{type}/{name}/{id}")
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
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 401, message = "Unauthorized"),
      }
  )
  public Response deleteMetadataItem(
                                     @PathParam("type")
                                     String type,
                                     @PathParam("name")
                                     String name,
                                     @PathParam("id")
                                     long itemId) {
    if (StringUtils.isBlank(type)) {
      return Response.status(Status.BAD_REQUEST).entity("MetadataTypeRequired").build();
    }
    if (StringUtils.isBlank(name)) {
      return Response.status(Status.BAD_REQUEST).entity("MetadataNameRequired").build();
    }
    if (itemId <= 0) {
      return Response.status(Status.BAD_REQUEST).entity("MetadataItemIdRequired").build();
    }

    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      MetadataItem metadataItem = metadataService.deleteMetadataItem(type,
                                                                     name,
                                                                     itemId,
                                                                     userIdentityId);
      return Response.ok(toEntity(metadataItem)).build();
    } catch (IllegalAccessException e) {
      LOG.warn("User '{}' attempts to delete a metadata item of type '{}' and name '{}' with id '{}'",
               RestUtils.getCurrentUser(),
               type,
               name,
               itemId,
               e);
      return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.warn("Error deleting a metadata item", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  private MetadataItemEntity toEntity(MetadataItem metadataItem) {
    MetadataItemEntity metadataItemEntity = new MetadataItemEntity();
    metadataItemEntity.setName(metadataItem.getMetadata().getName());
    metadataItemEntity.setAudienceId(metadataItem.getMetadata().getAudienceId());
    metadataItemEntity.setId(metadataItem.getId());
    metadataItemEntity.setCreatorId(metadataItem.getCreatorId());
    metadataItemEntity.setObjectId(metadataItem.getObjectId());
    metadataItemEntity.setObjectType(metadataItem.getObjectType());
    metadataItemEntity.setParentObjectId(metadataItem.getParentObjectId());
    metadataItemEntity.setProperties(metadataItem.getProperties());
    return metadataItemEntity;
  }

  private MetadataItem fromEntity(MetadataItemEntity metadataItemEntity) {
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setId(metadataItemEntity.getId());
    metadataItem.setObjectId(metadataItemEntity.getObjectId());
    metadataItem.setObjectType(metadataItemEntity.getObjectType());
    metadataItem.setParentObjectId(metadataItemEntity.getParentObjectId());
    metadataItem.setProperties(metadataItemEntity.getProperties());
    return metadataItem;
  }

}
