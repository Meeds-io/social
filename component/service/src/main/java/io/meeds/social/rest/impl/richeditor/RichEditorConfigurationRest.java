/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.rest.impl.richeditor;

import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.meeds.social.core.richeditor.RichEditorConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("richeditor/configuration")
@Tag(name = "richeditor/configuration", description = "Retrieves Rich Editor configurations")
public class RichEditorConfigurationRest implements ResourceContainer {

  private static final CacheControl      CACHE_CONTROL             = new CacheControl();

  private static final Date              DEFAULT_LAST_MODIFED      = new Date();

  private static final long              DEFAULT_LAST_MODIFED_HASH = DEFAULT_LAST_MODIFED.getTime();

  // 365 days
  private static final int               CACHE_IN_SECONDS          = 365 * 86400;

  private static final int               CACHE_IN_MILLI_SECONDS    = CACHE_IN_SECONDS * 1000;

  private RichEditorConfigurationService richEditorConfigurationService;

  private boolean                        useCache;

  public RichEditorConfigurationRest(RichEditorConfigurationService richEditorConfigurationService) {
    this.richEditorConfigurationService = richEditorConfigurationService;
    this.useCache = !PropertyManager.isDevelopping();
  }

  @GET
  @RolesAllowed("users")
  @Operation(summary = "Retrieves rich editor configuration Javascript file", method = "GET", description = "Returns list of tags")
  @Produces("text/javascript")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public Response getRichEditorConfiguration(
                                             @Context
                                             Request request,
                                             @Parameter(description = "Rich Editor Type")
                                             @QueryParam("type")
                                             String type,
                                             @Parameter(description = "The value of v parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'")
                                             @QueryParam("v")
                                             String version) {
    EntityTag eTag = new EntityTag(String.valueOf(DEFAULT_LAST_MODIFED_HASH));
    Response.ResponseBuilder builder = this.useCache ? request.evaluatePreconditions(eTag) : null;
    if (builder == null) {
      String richEditorConfiguration = this.richEditorConfigurationService.getRichEditorConfiguration(type);
      builder = Response.ok(richEditorConfiguration);
    }
    if (this.useCache) {
      builder.tag(eTag);
      builder.lastModified(DEFAULT_LAST_MODIFED);
      builder.cacheControl(CACHE_CONTROL);
      // If the query has a lastModified parameter, it means that the client
      // will change the lastModified entry when it really changes
      // Which means that we can cache the image in browser side
      // for a long time
      if (StringUtils.isNotBlank(version)) {
        builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
      }
    }
    return builder.build();
  }

}
