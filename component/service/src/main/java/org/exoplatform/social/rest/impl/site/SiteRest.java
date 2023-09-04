/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2023 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.rest.impl.site;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteFilter;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(VersionResources.VERSION_ONE + "/social/sites")
@Tag(name = VersionResources.VERSION_ONE + "/social/sites", description = "Manage sites")
public class SiteRest implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(SiteRest.class);

  private LayoutService    layoutService;

  public SiteRest(LayoutService layoutService) {
    this.layoutService = layoutService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Gets sites", description = "Gets sites", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getSites(@Context
  HttpServletRequest request,
                           @Parameter(description = "Portal site type, possible values: PORTAL, GROUP or USER", required = true)
                           @QueryParam("siteType")
                           String siteTypeName,
                           @Parameter(description = "Site name to be excluded")
                           @QueryParam("excludedSiteName")
                           String excludedSiteName,
                           @Parameter(description = "to include empty site in results in portal type case")
                           @DefaultValue("true")
                           @QueryParam("includeEmpty")
                           boolean includeEmpty,
                           @Parameter(description = "to expand site navigations nodes")
                           @DefaultValue("false")
                           @QueryParam("expandNavigations")
                           boolean expandNavigations,
                           @Parameter(description = "to retrieve sites with its displayed status")
                           @DefaultValue("false")
                           @QueryParam("displayed")
                           boolean displayed,
                           @Parameter(description = "to retrieve all sites")
                           @DefaultValue("true")
                           @QueryParam("allSites")
                           boolean allSites,
                           @Parameter(description = "to filter sites by view/edit permissions")
                           @DefaultValue("false")
                           @QueryParam("filterByPermissions")
                           boolean filterByPermission,
                           @Parameter(description = "Offset of results to retrieve")
                           @QueryParam("offset")
                           @DefaultValue("0")
                           int offset,
                           @Parameter(description = "Limit of results to retrieve")
                           @QueryParam("limit")
                           @DefaultValue("0")
                           int limit) {
    try {
      SiteFilter siteFilter = new SiteFilter();
      if (siteTypeName != null) {
        siteFilter.setSiteType(SiteType.valueOf(siteTypeName.toUpperCase()));
      }
      siteFilter.setExcludedSiteName(excludedSiteName);
      siteFilter.setIncludeEmpty(includeEmpty);
      siteFilter.setDisplayed(displayed);
      siteFilter.setAllSites(allSites);
      siteFilter.setExpandNavigations(expandNavigations);
      siteFilter.setFilterByPermission(filterByPermission);
      siteFilter.setLimit(limit);
      siteFilter.setOffset(offset);
      List<PortalConfig> sites = layoutService.getSites(siteFilter);
      return Response.ok(EntityBuilder.buildSiteEntities(sites, request, siteFilter)).build();
    } catch (Exception e) {
      LOG.warn("Error while retrieving sites", e);
      return Response.serverError().build();
    }
  }
}
