package org.exoplatform.social.rest.impl.metadata;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
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

@Path(VersionResources.VERSION_ONE + "/social/matadatas")
@Api(
    tags = VersionResources.VERSION_ONE + "/social/matadatas",
    value = VersionResources.VERSION_ONE + "/social/matadatas",
    description = "Managing objects matadatas" // NOSONAR
)
public class MetadataRest implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(MetadataRest.class);

  private MetadataService  metadataService;

  public MetadataRest(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @POST
  @Path("{type}")
  @RolesAllowed("users")
  @ApiOperation(
      value = "Creates a metadata item",
      httpMethod = "GET",
      response = Response.class,
      notes = "Returns empty result"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 204, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input")
      }
  )
  public Response createMetadataItem(
                                     @PathParam("type")
                                     String type,
                                     MetadataItemEntity metadataItemEntity) {
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
      MetadataItem metadataItem = new MetadataItem();
      metadataItem.setObjectId(metadataItemEntity.getObjectId());
      metadataItem.setObjectType(metadataItemEntity.getObjectType());
      metadataItem.setParentObjectId(metadataItemEntity.getParentObjectId());
      metadataItem.setProperties(metadataItemEntity.getProperties());
      metadataItem.setId(metadataItemEntity.getId());

      metadataService.createMetadataItem(metadataItem,
                                         type,
                                         metadataItemEntity.getName(),
                                         metadataItemEntity.getAudienceId(),
                                         userIdentityId);
      return Response.noContent().build();
    } catch (IllegalAccessException e) {
      LOG.warn("User '{}' attempts to create a metadata of type '{}' and name '{}' for audience '{}'",
               RestUtils.getCurrentUser(),
               metadataItemEntity.getName(),
               metadataItemEntity.getAudienceId(),
               e);
      return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.warn("Error creating a metadata item", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

}
