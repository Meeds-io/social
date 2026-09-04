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
package io.meeds.social.report.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.ConversationState;

import io.meeds.social.report.service.ActivityReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/activities/reports")
@Tag(name = "/social/rest/activities/reports", description = "Reporting space-feed activities and comments for moderation")
public class ActivityReportRest {

  @Autowired
  private ActivityReportService activityReportService;

  @PostMapping("{activityId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Secured("users")
  @Operation(summary = "Reports an activity or a comment", description = "Records a report, made by the authenticated user, on a space-feed activity or comment, and notifies the space managers", method = "POST")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "204", description = "Report recorded"),
                          @ApiResponse(responseCode = "400", description = "Invalid reason, or target not eligible for reporting"),
                          @ApiResponse(responseCode = "403", description = "User can't view the target or is its author"),
                          @ApiResponse(responseCode = "404", description = "Activity or comment not found"),
                          @ApiResponse(responseCode = "409", description = "User already has an active report on this target"),
  })
  public void reportActivity(
                             @Parameter(description = "Activity id, or comment id prefixed with 'comment'", required = true)
                             @PathVariable("activityId")
                             String activityId,
                             @Parameter(description = "Report reason key", required = true)
                             @RequestParam("reason")
                             String reason) {
    try {
      activityReportService.reportActivity(activityId, reason, ConversationState.getCurrent().getIdentity());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    } catch (ObjectAlreadyExistsException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

}
