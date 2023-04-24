package org.exoplatform.social.rest.impl.attachement;

import java.util.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.exoplatform.social.rest.api.RestUtils;

import org.exoplatform.social.metadata.attachement.AttachementService;
import org.exoplatform.social.metadata.attachement.model.Attachement;

@Path(VersionResources.VERSION_ONE + "/social/attachements")
@Tag(name = VersionResources.VERSION_ONE + "/social/attachements", description = "Managing attachements for any type of data")
public class AttachementRest implements ResourceContainer {

  private static final Log   LOG = ExoLogger.getLogger(AttachementRest.class);

  private AttachementService attachementService;

  public AttachementRest(AttachementService attachementService) {
    this.attachementService = attachementService;
  }

  @POST
  @Path("{objectType}/{objectId}/{spaceId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Attach files to an activity", description = "Attach files to an activity", method = "POST")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "409", description = "Conflict"), })
  public Response createAttachements(@Parameter(description = "Object type: activity, comment, notes ...", required = true)
  @PathParam("objectType")
  String objectType,
                                     @Parameter(description = "", required = true)
                                     @PathParam("objectId")
                                     String objectId,
                                     @Parameter(description = "", required = false)
                                     @QueryParam("spaceId")
                                     long spaceId) {
    if (StringUtils.isBlank(objectType)) {
      return Response.status(Status.BAD_REQUEST).entity("AttachementObjectTypeRequired").build();
    }
    if (StringUtils.isBlank(objectId)) {
      return Response.status(Status.BAD_REQUEST).entity("AttachementObjectIdRequired").build();
    }
    Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();

    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      Attachement attachement = new Attachement(objectType, objectId, null, userIdentityId, spaceId);
      attachementService.attachFiles(attachement);
      return Response.noContent().build();
    }  catch (Exception e) {
      LOG.warn("Error creating a favorite", e);
      return Response.serverError().build();
    }
  }

}
