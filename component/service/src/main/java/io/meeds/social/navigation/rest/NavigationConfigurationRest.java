/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.navigation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.social.navigation.model.NavigationConfiguration;
import io.meeds.social.navigation.model.SidebarConfiguration;
import io.meeds.social.navigation.model.TopbarConfiguration;
import io.meeds.social.navigation.service.NavigationConfigurationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/navigation/settings")
@Tag(name = "/social/rest/navigation/settings", description = "Managing Topbar and Sidebar Navigation Configuration")
public class NavigationConfigurationRest {

  @Autowired
  private NavigationConfigurationService navigationConfigurationService;

  @GetMapping
  @Secured("users")
  @Operation(summary = "Retrieve Topbar and Sidebar settings",
             method = "GET",
             description = "This retrieves the complete configuration of Topbar and Sidebar")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public NavigationConfiguration getNavigationConfiguration(HttpServletRequest request) {
    return navigationConfigurationService.getConfiguration(request.getRemoteUser(), request.getLocale(), true);
  }

  @GetMapping("/topbar")
  @Secured("users")
  @Operation(summary = "Retrieve Topbar settings",
  method = "GET",
  description = "This retrieves the configuration of Topbar switch user roles")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public TopbarConfiguration getTopbarConfiguration(HttpServletRequest request) {
    return navigationConfigurationService.getTopbarConfiguration(request.getRemoteUser(), request.getLocale());
  }

  @GetMapping("/sidebar")
  @Secured("users")
  @Operation(summary = "Retrieve Sidebar settings",
             method = "GET",
             description = "This retrieves the configuration of Sidebar switch user roles")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public SidebarConfiguration getSidebarConfiguration(HttpServletRequest request) {
    return navigationConfigurationService.getSidebarConfiguration(request.getRemoteUser(), request.getLocale());
  }

  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @Secured("administrators")
  @Operation(summary = "Updates Topbar and Sidebar settings",
             method = "PUT",
             description = "This updates the Topbar and Sidebar Menu settings")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
  })
  public void updateNavigationConfiguration(HttpServletRequest request,
                                            @RequestBody
                                            NavigationConfiguration navigationConfiguration) {
    navigationConfigurationService.updateConfiguration(navigationConfiguration);
  }

}
