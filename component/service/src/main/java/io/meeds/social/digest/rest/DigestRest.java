/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.digest.rest;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.commons.digest.DigestService;
import io.meeds.commons.digest.model.DigestUserSettings;
import io.meeds.commons.digest.plugin.DigestCategoryProvider;
import io.meeds.social.digest.rest.model.DigestCategoryEntity;
import io.meeds.social.digest.rest.model.DigestSettingsEntity;
import io.meeds.social.digest.rest.model.DigestUserSettingsEntity;
import io.meeds.commons.digest.service.DigestLabelResolver;
import io.meeds.social.timezone.service.UserTimeZoneService;

import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("notifications/digest")
@Tag(name = "/social/rest/notifications/digest", description = "Managing digest mail notification settings")
public class DigestRest {

  @Autowired
  private DigestService               digestService;

  @Autowired
  private UserTimeZoneService         userTimeZoneService;

  @Autowired
  private DigestLabelResolver         labelResolver;

  @GetMapping("settings")
  @Secured("users")
  @Operation(summary = "Retrieves the digest mail notification settings", method = "GET",
             description = "Retrieves the platform-wide administrator switch state, the categories offered by the installed addons and the choices of the current user")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public DigestSettingsEntity getSettings(HttpServletRequest request) {
    boolean digestAllowed = digestService.isDigestAllowed();
    if (!digestAllowed) {
      // Nothing to display, the categories and the user choices are useless
      return new DigestSettingsEntity(false, List.of(), false, List.of(), false, List.of());
    }
    DigestUserSettings userSettings = digestService.getUserSettings(request.getRemoteUser());
    return new DigestSettingsEntity(true,
                                    toCategoryEntities(digestService.getCategories(), request.getLocale()),
                                    userSettings.isDaily(),
                                    userSettings.getDailyCategories(),
                                    userSettings.isWeekly(),
                                    userSettings.getWeeklyCategories());
  }

  @PatchMapping(path = "settings", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Secured("users")
  @Operation(summary = "Saves the digest mail notification choices of the current user", method = "PATCH",
             description = "Saves the frequencies and the categories the current user chose, and enrolls him in the digest sending")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Invalid query input"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public void saveUserSettings(HttpServletRequest request,
                               @RequestBody
                               DigestUserSettingsEntity settings) {
    if (!digestService.isDigestAllowed()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Digest mail notifications are not allowed");
    }
    String username = request.getRemoteUser();
    try {
      digestService.saveUserSettings(username,
                                     new DigestUserSettings(settings.isDaily(),
                                                            settings.getDailyCategories(),
                                                            settings.isWeekly(),
                                                            settings.getWeeklyCategories()),
                                     userTimeZoneService.getUserTimeZone(username));
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @PatchMapping(path = "allowed", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @Secured("administrators")
  @Operation(summary = "Saves the administrator switch allowing users to set digest mail notifications", method = "PATCH",
             description = "Saves the administrator switch allowing users to set digest mail notifications. Administrators only.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public void saveDigestAllowed(@Parameter(description = "true to allow users to set digest mail notifications", required = true)
                                @RequestParam("allowed")
                                boolean allowed) {
    digestService.saveDigestAllowed(allowed);
  }

  private List<DigestCategoryEntity> toCategoryEntities(List<DigestCategoryProvider> categories, Locale locale) {
    return categories.stream()
                     .map(category -> new DigestCategoryEntity(category.getId(), labelResolver.categoryLabel(category, locale)))
                     .toList();
  }

}
