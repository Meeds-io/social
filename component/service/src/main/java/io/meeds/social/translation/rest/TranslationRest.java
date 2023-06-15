/**
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
package io.meeds.social.translation.rest;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.rest.api.RestUtils;

import io.meeds.social.translation.model.TranslationConfiguration;
import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("social/translations")
@Tag(name = "translations", description = "Manages User Translations of stored fields for all type of persisted entities")
public class TranslationRest implements ResourceContainer {

  private TranslationService  translationService;

  private LocaleConfigService localeConfigService;

  public TranslationRest(TranslationService translationService, LocaleConfigService localeConfigService) {
    this.translationService = translationService;
    this.localeConfigService = localeConfigService;
  }

  @GET
  @Path("configuration")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Retrieves the translation configuration, with the default lanfuage and supported languages", method = "GET", description = "Retrieves the translation configuration, with the default lanfuage and supported languages")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
  })
  public Response getTranslationConfiguration() {
    Locale defaultLocale = localeConfigService.getDefaultLocaleConfig().getLocale();
    Map<String, String> supportedLocales = getSupportedLocales(defaultLocale);
    TranslationConfiguration translationConfiguration = new TranslationConfiguration(defaultLocale.toLanguageTag(),
                                                                                     supportedLocales);
    return Response.ok(translationConfiguration).build();
  }

  @PUT
  @Path("configuration/defaultLanguage")
  @RolesAllowed("administrators")
  @Operation(summary = "Saves new default language for product", method = "PUT", description = "Saves new default language for product")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Request fulfilled"),
  })
  public Response saveDefaultLanguage(
                                      @Parameter(description = "Default language to save", required = true)
                                      @FormParam("lang")
                                      String lang) {
    localeConfigService.saveDefaultLocaleConfig(lang);
    return Response.noContent().build();
  }

  @GET
  @Path("{objectType}/{objectId}/{fieldName}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Retrieves the list of translation labels for a given Object's field identified by its type, id and field name", method = "GET", description = "Retrieves the list of translation labels for a given Object's field identified by its type, id and field name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
  })
  public Response getTranslationLabels(
                                       @Context
                                       Request request,
                                       @Parameter(description = "Object type, like 'activity', 'task' ...", required = true)
                                       @PathParam("objectType")
                                       String objectType,
                                       @Parameter(description = "Object technical identifier", required = true)
                                       @PathParam("objectId")
                                       long objectId,
                                       @Parameter(description = "Object field name", required = true)
                                       @PathParam("fieldName")
                                       String fieldName) {

    try {
      TranslationField translationField = translationService.getTranslationField(objectType,
                                                                                 objectId,
                                                                                 fieldName,
                                                                                 RestUtils.getCurrentUser());
      long cacheTime = translationField.getUpdatedDate();
      String eTagValue = String.valueOf(Objects.hash(cacheTime));
      EntityTag eTag = new EntityTag(eTagValue, true);
      Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
      if (builder == null) {
        builder = Response.ok(translationField.getLabels()
                                              .entrySet()
                                              .stream()
                                              .collect(Collectors.toMap(entry -> entry.getKey().toLanguageTag(),
                                                                        Entry::getValue)),
                              MediaType.APPLICATION_JSON);
        builder.tag(eTag);
        builder.lastModified(new Date(cacheTime));
        // Set cache control header to no-cache
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setPrivate(true);
        builder.cacheControl(cacheControl);
      }
      return builder.build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }
  }

  @POST
  @Path("{objectType}/{objectId}/{fieldName}")
  @RolesAllowed("users")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Saves the list of translation labels for a given Object's field identified by its type, id and field name", method = "POST", description = "Saves the list of translation labels for a given Object's field identified by its type, id and field name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
  })
  public Response saveTranslationLabels(
                                        @Parameter(description = "Object type, like 'activity', 'task' ...", required = true)
                                        @PathParam("objectType")
                                        String objectType,
                                        @Parameter(description = "Object technical identifier", required = true)
                                        @PathParam("objectId")
                                        long objectId,
                                        @Parameter(description = "Object field name", required = true)
                                        @PathParam("fieldName")
                                        String fieldName,
                                        @Parameter(description = "Object field name", required = true)
                                        @RequestBody
                                        Map<String, String> labels) {
    try {
      translationService.saveTranslationLabels(objectType,
                                               objectId,
                                               fieldName,
                                               labels.entrySet()
                                                     .stream()
                                                     .collect(Collectors.toMap(entry -> Locale.forLanguageTag(entry.getKey()),
                                                                               Entry::getValue)),
                                               RestUtils.getCurrentUser());
      return Response.noContent().build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }
  }

  private Map<String, String> getSupportedLocales(Locale defaultLocale) {
    return localeConfigService.getLocalConfigs() == null ? Collections.singletonMap(defaultLocale.toLanguageTag(),
                                                                                    getLocaleDisplayName(defaultLocale,
                                                                                                         defaultLocale))
                                                         : localeConfigService.getLocalConfigs()
                                                                              .stream()
                                                                              .filter(localeConfig -> !StringUtils.equals(localeConfig.getLocaleName(),
                                                                                                                          "ma"))
                                                                              .collect(Collectors.toMap(LocaleConfig::getLocaleName,
                                                                                                        localeConfig -> getLocaleDisplayName(defaultLocale,
                                                                                                                                             localeConfig.getLocale())));
  }

  private String getLocaleDisplayName(Locale defaultLocale, Locale locale) {
    return defaultLocale.equals(locale) ? defaultLocale.getDisplayName(defaultLocale)
                                        : locale.getDisplayName(defaultLocale) + " / " + locale.getDisplayName(locale);
  }

}
