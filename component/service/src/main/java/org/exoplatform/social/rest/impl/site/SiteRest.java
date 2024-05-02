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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
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

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteFilter;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.http.PATCH;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.entity.SiteEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Path(VersionResources.VERSION_ONE + "/social/sites")
@Tag(name = VersionResources.VERSION_ONE + "/social/sites", description = "Manage sites")
public class SiteRest implements ResourceContainer {

  private static final Log          LOG                    = ExoLogger.getLogger(SiteRest.class);

  private LayoutService             layoutService;

  private UserACL                   userACL;

  private static final int          CACHE_IN_SECONDS       = 604800;

  private static final int          CACHE_IN_MILLI_SECONDS = CACHE_IN_SECONDS * 1000;

  private static final CacheControl SITES_CACHE_CONTROL          = new CacheControl();
  
  private static final CacheControl SITE_CACHE_CONTROL          = new CacheControl();

  private static final CacheControl BANNER_CACHE_CONTROL   = new CacheControl();

  static {
    SITES_CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
    SITE_CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
    BANNER_CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
    BANNER_CACHE_CONTROL.setPrivate(false);
  }

  public SiteRest(LayoutService layoutService,
                  UserACL userACL) {
    this.layoutService = layoutService;
    this.userACL = userACL;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Gets sites", description = "Gets sites", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getSites(
                           @Context
                           HttpServletRequest httpServletRequest,
                           @Context
                           Request request,
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
                           @Parameter(description = "to check, in expandNavigations case, the navigation nodes scheduling start and end dates")
                           @DefaultValue("false")
                           @QueryParam("temporalCheck")
                           boolean temporalCheck,
                           @Parameter(description = "to exclude group nodes without page child nodes")
                           @DefaultValue("false")
                           @QueryParam("excludeGroupNodesWithoutPageChildNodes")
                           boolean excludeGroupNodesWithoutPageChildNodes,
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
      List<SiteEntity> siteEntities = EntityBuilder.buildSiteEntities(sites,
                                                                      httpServletRequest,
                                                                      expandNavigations,
                                                                      visibilityNames,
                                                                      excludeEmptyNavigationSites,
                                                                      temporalCheck,
                                                                      excludeGroupNodesWithoutPageChildNodes,
                                                                      filterByPermission,
                                                                      sortByDisplayOrder,
                                                                      getLocale(lang));
      EntityTag eTag = new EntityTag(String.valueOf(Objects.hash(siteEntities,
                                                                 siteFilter,
                                                                 expandNavigations,
                                                                 visibilityNames,
                                                                 excludeEmptyNavigationSites,
                                                                 temporalCheck,
                                                                 excludeGroupNodesWithoutPageChildNodes,
                                                                 filterByPermission,
                                                                 sortByDisplayOrder,
                                                                 getLocale(lang))));
      Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
      if (builder == null) {
        builder = Response.ok(siteEntities);
      }
      builder.tag(eTag);
      builder.cacheControl(SITES_CACHE_CONTROL);
      return builder.build();
    } catch (Exception e) {
      LOG.warn("Error while retrieving sites", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @GET
  @Path("{siteId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Gets a specific site by id", description = "Gets site by id", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getSiteById(
                              @Context
                              HttpServletRequest httpServletRequest,
                              @Context
                              Request request,
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
                              @Parameter(description = "to check, in expandNavigations case, the navigation nodes scheduling start and end dates")
                              @DefaultValue("false")
                              @QueryParam("temporalCheck")
                              boolean temporalCheck,
                              @Parameter(description = "to exclude group nodes without page child nodes")
                              @DefaultValue("false")
                              @QueryParam("excludeGroupNodesWithoutPageChildNodes")
                              boolean excludeGroupNodesWithoutPageChildNodes,
                              @Parameter(description = "to exclude sites with empty navigation")
                              @DefaultValue("false")
                              @QueryParam("excludeEmptyNavigationSites")
                              boolean excludeEmptyNavigationSites,
                              @Parameter(description = "Used to retrieve the site label and description in the requested language")
                              @QueryParam("lang")
                              String lang) {
    try {
      PortalConfig site = layoutService.getPortalConfig(Long.parseLong(siteId));
      if (site == null) {
        return Response.status(Status.NOT_FOUND).build();
      }
      if (!userACL.hasAccessPermission(site)) {
        return Response.status(Status.UNAUTHORIZED).build();
      }
      SiteEntity siteEntity = EntityBuilder.buildSiteEntity(site,
                                                            httpServletRequest,
                                                            expandNavigations,
                                                            visibilityNames,
                                                            excludeEmptyNavigationSites,
                                                            temporalCheck,
                                                            excludeGroupNodesWithoutPageChildNodes,
                                                            getLocale(lang));
      EntityTag eTag = new EntityTag(String.valueOf(Objects.hash(siteId,
                                                                 siteEntity,
                                                                 expandNavigations,
                                                                 visibilityNames,
                                                                 excludeEmptyNavigationSites,
                                                                 temporalCheck,
                                                                 excludeGroupNodesWithoutPageChildNodes,
                                                                 getLocale(lang))));
      Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
      if (builder == null) {
        builder = Response.ok(siteEntity);
        builder.tag(eTag);
      }
      builder.cacheControl(SITE_CACHE_CONTROL);
      return builder.build();
    } catch (Exception e) {
      LOG.warn("Error while retrieving site", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @PATCH
  @Path("{siteId}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Operation(summary = "Updates a specific site attribute by id", description = "Updates a specific site attribute by id", method = "PATCH")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public Response updateSiteById(
                                 @Parameter(description = "site Id", required = true)
                                 @PathParam("siteId")
                                 String siteId,
                                 @Parameter(description = "Site attribute name", required = true)
                                 @FormParam("name")
                                 String name,
                                 @Parameter(description = "Site attribute value", required = true)
                                 @FormParam("value")
                                 String value) {
    try {
      PortalConfig site = layoutService.getPortalConfig(Long.parseLong(siteId));
      if (site == null) {
        return Response.status(Status.NOT_FOUND).build();
      }
      if (!userACL.hasEditPermission(site)) {
        return Response.status(Status.UNAUTHORIZED).build();
      }
      if (StringUtils.equals(name, "accessPermissions")) {
        site.setAccessPermissions(StringUtils.split(value, ","));
        layoutService.save(site);
        return Response.status(Status.NO_CONTENT).build();
      } else {
        return Response.status(Status.BAD_REQUEST).build();
      }
    } catch (Exception e) {
      LOG.warn("Error while saving site attribute {} with value {}", name, value, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @GET
  @Path("{siteName}/banner")
  @Produces("image/png")
  @Operation(summary = "Gets a site banner", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "403", description = "Forbidden request"),
      @ApiResponse(responseCode = "404", description = "Resource not found") })
  public Response getSiteBanner(
                                @Context
                                Request request,
                                @Parameter(description = "site name", required = true)
                                @PathParam("siteName")
                                String siteName,
                                @Parameter(description = "site banner id", required = false)
                                @QueryParam("bannerId")
                                long bannerId) {
    try {
      PortalConfig site = layoutService.getPortalConfig(siteName);
      if (site == null) {
        return Response.status(Status.NOT_FOUND).build();
      }
      if (!userACL.hasAccessPermission(site)) {
        return Response.status(Status.UNAUTHORIZED).build();
      }
      boolean isDefault = bannerId == 0;
      EntityTag eTag = !isDefault ? new EntityTag(String.valueOf(bannerId)) : new EntityTag(siteName);
      Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
      if (builder == null) {
        InputStream stream = isDefault ? layoutService.getDefaultSiteBannerStream(siteName)
                                       : layoutService.getSiteBannerStream(siteName);
        builder = Response.ok(stream, "image/png");
        builder.tag(eTag);
      }
      builder.cacheControl(BANNER_CACHE_CONTROL);
      builder.lastModified(new Date(System.currentTimeMillis()));
      builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
      return builder.build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (IOException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  private Locale getLocale(String lang) {
    return StringUtils.isBlank(lang) ? null : Locale.forLanguageTag(lang);
  }
}