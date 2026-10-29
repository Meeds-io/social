/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.organizationalchart.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.social.organizationalchart.rest.model.OrganizationalChartSettingsEntity;
import io.meeds.social.organizationalchart.service.OrganizationalChartSettingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("organizational-chart")
@Tag(name = "/social/rest/organizational-chart", description = "Manages the settings of the organizational chart applications")
public class OrganizationalChartSettingRest {

  private static final String MESSAGE_KEY = "message";

  @Autowired
  private OrganizationalChartSettingService organizationalChartSettingService;

  @PutMapping(path = "{applicationId}/settings", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Secured("users")
  @Operation(summary = "Saves the settings of an organizational chart application", method = "PUT",
             description = "Saves the user the chart is centered on and whether its header title is displayed")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Invalid application id or center user"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public ResponseEntity<Object> saveSettings(HttpServletRequest request,
                                             @Parameter(description = "Organizational chart application id", required = true)
                                             @PathVariable("applicationId")
                                             String applicationId,
                                             @RequestBody
                                             OrganizationalChartSettingsEntity settings) {
    try {
      organizationalChartSettingService.saveSettings(applicationId,
                                                     settings.centerUser(),
                                                     settings.hasHeaderTitle(),
                                                     request.getRemoteUser());
      return ResponseEntity.ok().build();
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, e.getMessage()));
    }
  }

}
