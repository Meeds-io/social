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
package org.exoplatform.social.rest.impl.tag;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagFilter;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.service.rest.api.VersionResources;

@Path(VersionResources.VERSION_ONE + "/social/tags")
@Tag(name = VersionResources.VERSION_ONE + "/social/tags", description = "Managing tags for any type of data")
public class TagRest implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(TagRest.class);

  private TagService       tagService;

  public TagRest(TagService tagService) {
    this.tagService = tagService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Find list of tags using a search term",
      method = "POST",
      description = "Returns list of tags"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
      }
  )
  public Response findTags(
                           @Parameter(description = "Search term") @Schema(defaultValue = "false")
                           @QueryParam("q")
                           String term,
                           @Parameter(description = "Search results limit") @Schema(defaultValue = "false")
                           @QueryParam("limit") long limit) {
    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      List<TagName> tagNames = tagService.findTags(new TagFilter(term, limit), userIdentityId);
      return Response.ok(tagNames).build();
    } catch (Exception e) {
      LOG.warn("Error getting list of tags", e);
      return Response.serverError().build();
    }
  }

}
