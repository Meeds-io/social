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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteFilter;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(VersionResources.VERSION_ONE + "/social/sites")
@Tag(name = VersionResources.VERSION_ONE + "/social/sites", description = "Manage sites")
public class SiteRest implements ResourceContainer {

  private static final Log    LOG                     = ExoLogger.getLogger(SiteRest.class);

  private LayoutService       layoutService;

  private PortalContainer     portalContainer;

  private static final String SITE_DEFAULT_BANNER_URL = System.getProperty("sites.defaultBannerPath",
                                                                           "/images/sites/banner/defaultSiteBanner.png");

  public SiteRest(LayoutService layoutService, PortalContainer portalContainer) {
    this.layoutService = layoutService;
    this.portalContainer = portalContainer;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Gets sites", description = "Gets sites", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getSites(@Context
  HttpServletRequest request,
                           @Parameter(description = "Portal site types, possible values: PORTAL, GROUP or USER", required = true)
                           @QueryParam("siteType")
                           String siteType,
                           @Parameter(description = "Site type to be excluded")
                           @QueryParam("excludedSiteType")
                           String excludedSiteType,
                           @Parameter(description = "Site name to be excluded")
                           @QueryParam("excludedSiteName")
                           String excludedSiteName,
                           @Parameter(description = "to exclude space sites")
                           @DefaultValue("false")
                           @QueryParam("excludeSpaceSites")
                           boolean excludeSpaceSites,
                           @Parameter(description = "to exclude sites with empty navigation")
                           @DefaultValue("false")
                           @QueryParam("excludeEmptyNavigationSites")
                           boolean excludeEmptyNavigationSites,
                           @Parameter(description = "to expand site navigations nodes")
                           @DefaultValue("false")
                           @QueryParam("expandNavigations")
                           boolean expandNavigations,
                           @Parameter(description = "Multivalued visibilities of navigation nodes to retrieve, possible values: DISPLAYED, HIDDEN, SYSTEM or TEMPORAL. If empty, all visibilities will be used.", required = false)
                           @Schema(defaultValue = "All possible values combined")
                           @QueryParam("visibility")
                           List<String> visibilityNames,
                           @Parameter(description = "to check , in expandNavigations case , the navigation nodes scheduling start and end dates")
                           @DefaultValue("false")
                           @QueryParam("temporalCheck")
                           boolean temporalCheck,
                           @Parameter(description = "to expand site navigations nodes")
                           @DefaultValue("false")
                           @QueryParam("excludeEmptyNavigations")
                           boolean excludeEmptyNavigations,
                           @Parameter(description = "to sort with display order")
                           @DefaultValue("false")
                           @QueryParam("sortByDisplayOrder")
                           boolean sortByDisplayOrder,
                           @Parameter(description = "to filter sites by displayed status")
                           @DefaultValue("false")
                           @QueryParam("filterByDisplayed")
                           boolean filterByDisplayed,
                           @Parameter(description = "to retrieve sites with displayed status")
                           @DefaultValue("false")
                           @QueryParam("displayed")
                           boolean displayed,
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
                           int limit,
                           @Parameter(description = "Used to retrieve the site label and description in the requested language")
                           @QueryParam("lang")
                           String lang) {
    try {
      SiteFilter siteFilter = new SiteFilter();
      if (siteType != null) {
        siteFilter.setSiteType(SiteType.valueOf(siteType.toUpperCase()));
      }
      if (excludedSiteType != null) {
        siteFilter.setExcludedSiteType(SiteType.valueOf(excludedSiteType.toUpperCase()));
      }
      if (excludedSiteName != null) {
        siteFilter.setExcludedSiteName(excludedSiteName);
      }
      siteFilter.setExcludeSpaceSites(excludeSpaceSites);
      siteFilter.setSortByDisplayOrder(sortByDisplayOrder);
      if (filterByDisplayed) {
        siteFilter.setFilterByDisplayed(filterByDisplayed);
        siteFilter.setDisplayed(displayed);
      }
      siteFilter.setLimit(limit);
      siteFilter.setOffset(offset);
      List<PortalConfig> sites = layoutService.getSites(siteFilter);
      return Response.ok(EntityBuilder.buildSiteEntities(sites,
                                                         request,
                                                         expandNavigations,
                                                         visibilityNames,
                                                         excludeEmptyNavigationSites,
                                                         temporalCheck,
                                                         excludeEmptyNavigations,
                                                         filterByPermission,
                                                         sortByDisplayOrder,
                                                         getLocale(lang)))
                     .build();
    } catch (Exception e) {
      LOG.warn("Error while retrieving sites", e);
      return Response.serverError().build();
    }
  }

  @GET
  @Path("{siteId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Gets a specific site by id", description = "Gets site by id", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getSiteById(@Context
  HttpServletRequest request,
                              @Parameter(description = "site Id", required = true)
                              @PathParam("siteId")
                              String siteId,
                              @Parameter(description = "to expand site navigations nodes")
                              @DefaultValue("false")
                              @QueryParam("expandNavigations")
                              boolean expandNavigations,
                              @Parameter(description = "Multivalued visibilities of navigation nodes to retrieve, possible values: DISPLAYED, HIDDEN, SYSTEM or TEMPORAL. If empty, all visibilities will be used.", required = false)
                              @Schema(defaultValue = "All possible values combined")
                              @QueryParam("visibility")
                              List<String> visibilityNames,
                              @Parameter(description = "to check , in expandNavigations case , the navigation nodes scheduling start and end dates")
                              @DefaultValue("false")
                              @QueryParam("temporalCheck")
                              boolean temporalCheck,
                              @Parameter(description = "to expand site navigations nodes")
                              @DefaultValue("false")
                              @QueryParam("excludeEmptyNavigation")
                              boolean excludeEmptyNavigations,
                              @Parameter(description = "to exclude sites with empty navigation")
                              @DefaultValue("false")
                              @QueryParam("excludeEmptyNavigationSites")
                              boolean excludeEmptyNavigationSites,
                              @Parameter(description = "Used to retrieve the site label and description in the requested language")
                              @QueryParam("lang")
                              String lang) {
    try {
      PortalConfig site = layoutService.getPortalConfig(Long.parseLong(siteId));
      return Response.ok(EntityBuilder.buildSiteEntity(site,
                                                       request,
                                                       expandNavigations,
                                                       visibilityNames,
                                                       excludeEmptyNavigationSites,
                                                       temporalCheck,
                                                       excludeEmptyNavigations,
                                                       getLocale(lang)))
                     .build();
    } catch (Exception e) {
      LOG.warn("Error while retrieving site", e);
      return Response.serverError().build();
    }
  }

  @GET
  @Path("{siteName}/banner")
  @RolesAllowed("users")
  @Produces("image/png")
  @Operation(summary = "Gets a site banner", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "403", description = "Forbidden request"),
      @ApiResponse(responseCode = "404", description = "Resource not found") })
  public Response getSiteBanner(@Context
  Request request,
                                @Parameter(description = "site name", required = true)
                                @PathParam("siteName")
                                String siteName,
                                @Parameter(description = "A mandatory valid token that is used to authorize anonymous request", required = true)
                                @QueryParam("r")
                                String token,
                                @Parameter(description = "A mandatory valid token that is used to determinate if to use default banner", required = true)
                                @QueryParam("isDefault")
                                boolean isDefault) {
    try {
      if (!isDefault && RestUtils.isAnonymous()
          && !LinkProvider.isSiteBannerTokenValid(token, siteName, LinkProvider.ATTACHMENT_BANNER_TYPE)) {
        LOG.warn("An anonymous user attempts to access banner of site {} without a valid access token", siteName);
        return Response.status(Response.Status.FORBIDDEN).build();
      }
      InputStream stream = isDefault ? getDefaultBannerInputStream(siteName) : layoutService.getSiteBannerStream(siteName);
      return Response.ok(stream, "image/png").build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (IOException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  private InputStream getDefaultBannerInputStream(String siteName) {
    InputStream defaultSiteBanner = new ByteArrayInputStream(new byte[] {});
    InputStream is = portalContainer.getPortalContext()
                                    .getResourceAsStream(System.getProperty("sites." + siteName
                                        + ".defaultBannerPath", "/images/sites/banner/" + siteName.toLowerCase() + ".png"));
    if (is == null) {
      is = portalContainer.getPortalContext().getResourceAsStream(SITE_DEFAULT_BANNER_URL);
      if (is != null) {
        defaultSiteBanner = is;
      }
    } else {
      defaultSiteBanner = is;
    }
    return defaultSiteBanner;
  }

  private Locale getLocale(String lang) {
    return StringUtils.isBlank(lang) ? null : Locale.forLanguageTag(lang);
  }
}
