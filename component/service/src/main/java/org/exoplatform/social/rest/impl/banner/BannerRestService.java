/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.rest.impl.banner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.banner.BannerService;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.rest.entity.BannerEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path(VersionResources.VERSION_ONE + "/social/banner")
@Tag(name = VersionResources.VERSION_ONE + "/social/banner", description = "Managing Banner")
public class BannerRestService implements ResourceContainer {

  private static final Log           LOG = ExoLogger.getLogger(BannerRestService.class);

  private  BannerService bannerService;

  public BannerRestService(BannerService bannerService) {
    this.bannerService = bannerService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @Operation(summary = "Retrieve banner", method = "GET", description = "This retrieves the banner")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getBanner(@Parameter(description = "upload file id")
  @QueryParam("uploadFileId")
  String uploadFileId) {
    try {
      if (StringUtils.isBlank(uploadFileId)) {
        return Response.status(Response.Status.BAD_REQUEST).entity("uploadFileId is mandatory").build();
      }
      Identity identity = ConversationState.getCurrent().getIdentity();
      Long fileId = bannerService.getBanner(identity.getUserId(), uploadFileId);
      Long lastModifiedDate = System.currentTimeMillis();
      String bannerUrl = LinkProvider.buildBannerURL(String.valueOf(fileId), identity.getUserId(), lastModifiedDate);
      BannerEntity bannerEntity = new BannerEntity(fileId, bannerUrl);
      return Response.ok().entity(bannerEntity).build();
    } catch (Exception e) {
      LOG.error("Error when retrieving banner", e);
      return Response.serverError().build();
    }
  }

  @GET
  @Path("{fileId}")
  @Produces("image/png")
  @RolesAllowed("administrators")
  @Operation(summary = "Retrieve banner stream", method = "GET", description = "This retrieves banner stream")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "403", description = "Forbidden operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getBannerStream(@Context
  Request request,
                                         @Parameter(description = "banner file id")
                                         @PathParam("fileId")
                                         String fileId,
                                         @Parameter(description = "last modified date")
                                         @QueryParam("lastModified")
                                         String lastModified,
                                         @Parameter(description = "A mandatory valid token that is used to authorize anonymous request", required = true)
                                         @QueryParam("r")
                                         String token) {
    try {
      if (StringUtils.isBlank(fileId)) {
        return Response.status(Response.Status.BAD_REQUEST).entity("fileId is mandatory").build();
      }
      Identity identity = ConversationState.getCurrent().getIdentity();
      if (!StringUtils.isBlank(token)
          && !LinkProvider.isAttachmentTokenValid(token, fileId, identity.getUserId(), BannerAttachment.TYPE, lastModified)) {
        LOG.warn("An anonymous user attempts to access banner of portlet without a valid access token");
        return Response.status(Response.Status.FORBIDDEN).build();
      }
      InputStream stream = bannerService.getBannerStream(fileId);
      Response.ResponseBuilder builder = Response.ok(stream, "image/png");
      return builder.build();
    } catch (Exception e) {
      LOG.error("Error when retrieving banner stream", e);
      return Response.serverError().build();
    }
  }
}
