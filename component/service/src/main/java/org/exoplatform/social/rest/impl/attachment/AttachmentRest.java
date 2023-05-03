/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.rest.impl.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.metadata.attachment.AttachmentService;
import org.exoplatform.social.metadata.attachment.model.ObjectUploadResourceList;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentList;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentOperationReport;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(VersionResources.VERSION_ONE + "/social/attachments")
@Tag(name = VersionResources.VERSION_ONE + "/social/attachments", description = "Managing attachments for any type of data")
public class AttachmentRest implements ResourceContainer {

  private static final CacheControl CACHE_CONTROL          = new CacheControl();

  // 7 days
  private static final int          CACHE_IN_SECONDS       = 7 * 86400;

  private static final int          CACHE_IN_MILLI_SECONDS = CACHE_IN_SECONDS * 1000;

  private AttachmentService         attachmentService;

  public AttachmentRest(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;

    CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Attach files to a given object identified by its id", description = "Attach files to a given object identified by its id", method = "POST")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized request")
  })
  public Response createAttachments(
                                    @RequestBody(description = "Attached files to be created", required = true)
                                    ObjectUploadResourceList attachment) {
    String objectType = attachment.getObjectType();
    if (StringUtils.isBlank(objectType)) {
      return Response.status(Status.BAD_REQUEST).entity("attachment.objectTypeRequired").type(MediaType.TEXT_PLAIN).build();
    }
    String objectId = attachment.getObjectId();
    if (StringUtils.isBlank(objectId)) {
      return Response.status(Status.BAD_REQUEST).entity("attachment.objectIdRequired").type(MediaType.TEXT_PLAIN).build();
    }
    if (CollectionUtils.isEmpty(attachment.getUploadIds())) {
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity("attachment.uploadIdRequired")
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    }
    attachment.setUserIdentityId(RestUtils.getCurrentUserIdentityId());
    try {
      Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
      ObjectAttachmentOperationReport report = attachmentService.createAttachments(attachment, authenticatedUserIdentity);
      return Response.ok(report).type(MediaType.APPLICATION_JSON).build();
    } catch (IllegalAccessException e) {
      return Response.status(Status.UNAUTHORIZED).entity("attachment.unauthorizedAccess").type(MediaType.TEXT_PLAIN).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity("attachment.objectNotFound").type(MediaType.TEXT_PLAIN).build();
    }
  }

  @GET
  @Path("{objectType}/{objectId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Retrieves files of a given object identified by its id", description = "Retrieves files of a given object identified by its id", method = "GET")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized request")
  })
  public Response getAttachments(
                                 @Parameter(description = "Object type: activity, task, notes ...", required = true)
                                 @PathParam("objectType")
                                 String objectType,
                                 @Parameter(description = "Identifier of object to which attachment will be associated", required = true)
                                 @PathParam("objectId")
                                 String objectId) {
    if (StringUtils.isBlank(objectType)) {
      return Response.status(Status.BAD_REQUEST).entity("attachment.objectTypeRequired").type(MediaType.APPLICATION_JSON).build();
    }
    if (StringUtils.isBlank(objectId)) {
      return Response.status(Status.BAD_REQUEST).entity("attachment.objectIdRequired").type(MediaType.APPLICATION_JSON).build();
    }
    try {
      Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
      ObjectAttachmentList attachmentList = attachmentService.getAttachments(objectType, objectId, authenticatedUserIdentity);
      return Response.ok(attachmentList).build();
    } catch (IllegalAccessException e) {
      return Response.status(Status.UNAUTHORIZED).entity("attachment.unauthorizedAccess").type(MediaType.TEXT_PLAIN).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity("attachment.objectNotFound").type(MediaType.TEXT_PLAIN).build();
    }
  }

  @GET
  @Path("{objectType}/{objectId}/{fileId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Retrieves files of a given object identified by its id", description = "Retrieves files of a given object identified by its id", method = "GET")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized request")
  })
  public Response getAttachment(
                                @Context
                                UriInfo uriInfo,
                                @Context
                                Request request,
                                @Parameter(description = "Object type: activity, task, notes ...", required = true)
                                @PathParam("objectType")
                                String objectType,
                                @Parameter(description = "Identifier of object to which attachment will be associated", required = true)
                                @PathParam("objectId")
                                String objectId,
                                @Parameter(description = "Identifier of attached file", required = true)
                                @PathParam("fileId")
                                String fileId,
                                @Parameter(description = "The value of lastModified parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'")
                                @QueryParam("lastModified")
                                String lastModified,
                                @Parameter(description = "Resized avatar size. Use 0x0 for original size.")
                                @DefaultValue("45x45")
                                @QueryParam("size")
                                String size,
                                @Parameter(description = "Whether to add HTTP Header for download or not", required = true)
                                @PathParam("download")
                                boolean download) {
    if (StringUtils.isBlank(objectType)) {
      return Response.status(Status.BAD_REQUEST).entity("attachment.objectTypeRequired").build();
    }
    if (StringUtils.isBlank(objectId)) {
      return Response.status(Status.BAD_REQUEST).entity("attachment.objectIdRequired").build();
    }
    Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();

    ObjectAttachmentDetail attachmentDetail;
    try {
      attachmentDetail = attachmentService.getAttachment(objectType, objectId, fileId, authenticatedUserIdentity);
      if (attachmentDetail == null) {
        return Response.status(Status.NOT_FOUND).entity("attachment.attachmentNotFound").build();
      }
    } catch (IllegalAccessException e) {
      return Response.status(Status.UNAUTHORIZED).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity("attachment.objectNotFound").build();
    }

    Response.ResponseBuilder builder = null;
    Long lastUpdated = attachmentDetail.getUpdated();
    EntityTag eTag = new EntityTag(lastUpdated.hashCode() + "-" + size);
    builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      InputStream attachmentInputStream;
      try {
        attachmentInputStream = attachmentService.getAttachmentInputStream(objectType,
                                                                           objectId,
                                                                           fileId,
                                                                           size,
                                                                           authenticatedUserIdentity);
      } catch (IllegalAccessException e) {
        return Response.status(Status.UNAUTHORIZED).build();
      } catch (ObjectNotFoundException e) {
        return Response.status(Status.NOT_FOUND).entity("attachment.objectNotFound").build();
      } catch (IOException e) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("attachment.fileReadingError").build();
      }

      builder = Response.ok(attachmentInputStream, "image/png");
      builder.cacheControl(CACHE_CONTROL);
      builder.lastModified(new Date(lastUpdated));
      builder.tag(eTag);
    }

    if (download) {
      String fileName = URLEncoder.encode(attachmentDetail.getName(), StandardCharsets.UTF_8).replace("+", "%20");
      builder.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + fileName);
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

}
