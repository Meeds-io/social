package org.exoplatform.social.rest.impl.attachement;

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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.metadata.attachement.AttachmentService;
import org.exoplatform.social.metadata.attachement.model.Attachment;

@Path(VersionResources.VERSION_ONE + "/social/attachements")
@Tag(name = VersionResources.VERSION_ONE + "/social/attachements", description = "Managing attachements for any type of data")
public class AttachmentRest implements ResourceContainer {

  private static final Log LOG=ExoLogger.getLogger(AttachmentRest.class);

  private AttachmentService attachmentService;

  public AttachmentRest(AttachmentService attachmentService) {
    this.attachmentService=attachmentService;
  }

  @POST
  @Path("{objectType}/{objectId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Attach files to an activity", description = "Attach files to an activity", method = "POST")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "409", description = "Conflict"),})
  public Response createAttachments(@Parameter(description = "Object type: activity, task, notes ...", required = true)
                                    @PathParam("objectType")
                                    String objectType,
                                    @Parameter(description = "", required = true)
                                    @PathParam("objectId")
                                    String objectId,
                                    @RequestBody(description = "Attached files to be created", required = true)
                                    Attachment attachment) {
    if (StringUtils.isBlank(objectType)) {
      return Response.status(Status.BAD_REQUEST).entity("AttachementObjectTypeRequired").build();
    }
    if (StringUtils.isBlank(objectId)) {
      return Response.status(Status.BAD_REQUEST).entity("AttachementObjectIdRequired").build();
    }
    if (attachment == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Attachement files are mandatory").build();
    }

    Identity authenticatedUserIdentity=ConversationState.getCurrent().getIdentity();
    if (!attachmentService.hasAccessPermission(authenticatedUserIdentity, objectType, objectId)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    long userIdentityId=RestUtils.getCurrentUserIdentityId();

    attachment.setUserIdentityId(userIdentityId);
    attachment.setObjectId(objectId);
    attachment.setObjectType(objectType);
    try {
      attachmentService.attachFiles(attachment);
      return Response.noContent().build();
    } catch (Exception e) {
      LOG.warn("Error while attaching files", e);
      return Response.serverError().build();
    }
  }

}
