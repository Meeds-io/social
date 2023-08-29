/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.rest.impl.spacetemplates;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.SpaceTemplate;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.ErrorResource;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.api.SocialRest;
import org.exoplatform.social.service.rest.api.VersionResources;

import javax.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.util.*;

/**
 *
 * Provides REST Services for manipulating space templates.
 *
 */
@Path(VersionResources.VERSION_ONE + "/social/spaceTemplates")
@Tag(name = VersionResources.VERSION_ONE + "/social/spaceTemplates", description = "Managing Spaces Templates")
public class SpaceTemplatesRestResourcesV1 implements SocialRest {

  private static final Log          LOG                         = ExoLogger.getLogger(SpaceTemplatesRestResourcesV1.class);

  private static final CacheControl CACHE_CONTROL               = new CacheControl();

  private static final Date         DEFAULT_IMAGES_LAST_MODIFED = new Date();

  // 1 year
  private static final int          CACHE_IN_SECONDS            = 365 * 86400;

  private static final int          CACHE_IN_MILLI_SECONDS      = CACHE_IN_SECONDS * 1000;

  static {
    CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
  }

  private SpaceTemplateService spaceTemplateService;

  private ConfigurationManager configurationManager;

  public SpaceTemplatesRestResourcesV1(SpaceTemplateService spaceTemplateService, ConfigurationManager configurationManager) {
    this.spaceTemplateService = spaceTemplateService;
    this.configurationManager = configurationManager;
  }

  @GET
  @Path("templates")
  @RolesAllowed("users")
  @Operation(
          summary = "Gets all spaces templates",
          method = "GET",
          description = "This returns space templates details")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse (responseCode = "500", description = "Internal server error")})
  public Response getAllTemplates(@Context UriInfo uriInfo,
                                  @Context HttpServletRequest request) {
    Identity identity = ConversationState.getCurrent().getIdentity();
    String userId = identity.getUserId();
    String lang = request.getLocale() == null ? Locale.ENGLISH.getLanguage() : request.getLocale().getLanguage();
    try {
      List<SpaceTemplate> list = spaceTemplateService.getLabelledSpaceTemplates(userId, lang);
      return EntityBuilder.getResponse(list, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    } catch (Exception e) {
      LOG.error("Cannot get list of templates for user {}, with lang {}", userId, lang, e);
      return EntityBuilder.getResponse(new ErrorResource("Error occurred while getting list of space templates", "space templates permissions not extracted"),
          uriInfo, RestUtils.getJsonMediaType(), Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("{templateName}/banner")
  @RolesAllowed("users")
  @Operation(
          summary = "Gets space template banner",
          method = "GET",
          description = "This returns space template banner input stream")
  @ApiResponses(value = {
          @ApiResponse (responseCode = "200", description = "Request fulfilled"),
          @ApiResponse (responseCode = "404", description = "Resource not found"),
          @ApiResponse (responseCode = "500", description = "Internal server error")})
  public Response getBannerStream(@Context UriInfo uriInfo,
                                  @Context Request request,
                                  @Parameter(description = "Space template name", required = true)
                                  @PathParam("templateName") String templateName,
                                  @Parameter(description = "The value of lastModified parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'")
                                  @QueryParam("lastModified") String lastModified) {
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplateByName(templateName);
    if (spaceTemplate == null) {
      LOG.debug("Cannot find space template: {}", templateName);
      return EntityBuilder.getResponse(new ErrorResource("space template does not exist: " + templateName, "space template not found"),
          uriInfo, RestUtils.getJsonMediaType(), Response.Status.NOT_FOUND);
    }
    String bannerPath = spaceTemplate.getBannerPath();
    if (StringUtils.isNotBlank(bannerPath)) {
      // change once the image will be dynamically loaded from DB,
      // currently, a constant is used instead of last modified date because the banner doesn't change in sources.
      EntityTag eTag = new EntityTag(Integer.toString(templateName.hashCode()));
      Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
      if (builder == null) {
        InputStream bannerStream = null;
        try {
           bannerStream = configurationManager.getInputStream(bannerPath);
        } catch (Exception e) {
          LOG.warn("Error retrieving banner image of template {}", templateName, e);
          return EntityBuilder.getResponse(new ErrorResource("inputStream could not be extracted from path: " + bannerPath, "inputStream not extracted"),
              uriInfo, RestUtils.getJsonMediaType(), Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (bannerStream == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        builder = Response.ok(bannerStream, "image/png");
        builder.tag(eTag);
        builder.cacheControl(CACHE_CONTROL);
        builder.lastModified(DEFAULT_IMAGES_LAST_MODIFED);
        builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
      }
      return builder.build();
    }
    return EntityBuilder.getResponse(new ErrorResource("image does not exist in path: " + bannerPath, "banner not found"),
        uriInfo, RestUtils.getJsonMediaType(), Response.Status.NOT_FOUND);
  }
}
