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
package io.meeds.social.usercard.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.social.usercard.model.UserCardSettings;
import io.meeds.social.usercard.service.UserCardSettingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("user-card/settings")
@Tag(name = "/social/rest/user-card/settings", description = "Manages which profile properties the user card displays")
public class UserCardSettingRest {

  private static final String MESSAGE_KEY = "message";

  @Autowired
  private UserCardSettingService userCardSettingService;

  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @Secured("administrators")
  @Operation(summary = "Saves which profile properties the user card displays", method = "PUT",
             description = "Saves the three text properties, the phone and the email the user card displays for all the users")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "A property doesn't exist or can't be displayed at its place"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public ResponseEntity<Object> saveSettings(HttpServletRequest request,
                                             @RequestBody
                                             UserCardSettings settings) {
    try {
      userCardSettingService.saveSettings(settings, request.getRemoteUser());
      return ResponseEntity.ok().build();
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, e.getMessage()));
    }
  }

}
