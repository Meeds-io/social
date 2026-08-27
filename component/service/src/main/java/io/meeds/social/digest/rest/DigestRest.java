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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.commons.digest.DigestService;
import io.meeds.social.digest.rest.model.DigestSettingsEntity;

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
  private DigestService digestService;

  @GetMapping("settings")
  @Secured("users")
  @Operation(summary = "Retrieves the digest mail notification settings", method = "GET",
             description = "Retrieves the digest mail notification settings, holding the platform-wide administrator switch state")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public DigestSettingsEntity getSettings() {
    return new DigestSettingsEntity(digestService.isDigestAllowed());
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

}
