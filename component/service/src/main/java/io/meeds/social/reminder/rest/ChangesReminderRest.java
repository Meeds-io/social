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
package io.meeds.social.reminder.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.social.reminder.service.ChangesReminderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("changes-reminder")
@Tag(name = "/social/rest/changes-reminder", description = "Remembers the changes reminders the current user already saw")
public class ChangesReminderRest {

  @Autowired
  private ChangesReminderService changesReminderService;

  @GetMapping("{name}")
  @Secured("users")
  @Operation(summary = "Tells whether the current user already saw a changes reminder", method = "GET",
             description = "Returns true when the current user already saw the changes reminder identified by its name")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Invalid reminder name"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public ResponseEntity<Boolean> isAcknowledged(HttpServletRequest request,
                                                @Parameter(description = "Reminder name", required = true)
                                                @PathVariable("name")
                                                String name) {
    try {
      return ResponseEntity.ok()
                           .cacheControl(CacheControl.noStore())
                           .body(changesReminderService.isAcknowledged(request.getRemoteUser(), name));
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @PutMapping("{name}")
  @Secured("users")
  @Operation(summary = "Marks a changes reminder as seen by the current user", method = "PUT",
             description = "Marks the changes reminder identified by its name as seen by the current user, so that it isn't displayed again")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Invalid reminder name"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public void acknowledge(HttpServletRequest request,
                          @Parameter(description = "Reminder name", required = true)
                          @PathVariable("name")
                          String name) {
    try {
      changesReminderService.acknowledge(request.getRemoteUser(), name);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

}
