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
package io.meeds.social.hidden.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.hidden.model.HiddenApplication;
import io.meeds.social.hidden.service.HiddenApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/hidden-applications")
@Tag(name = "/social/rest/hidden-applications", description = "Managing the page applications each user hid for themselves")
public class HiddenApplicationRest {

  @Autowired
  private HiddenApplicationService hiddenApplicationService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Secured("users")
  @Operation(summary = "Retrieves the page applications the current user hid", method = "GET",
             description = "Retrieves the page applications the current user hid, in the order they were hidden")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled") })
  public ResponseEntity<List<HiddenApplication>> getHiddenApplications(
                                                       HttpServletRequest request,
                                                       @Parameter(description = "User language")
                                                       @RequestParam(name = "lang", required = false)
                                                       String lang) {
    // The caller's own list, at a URL every user shares
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(hiddenApplicationService.getHiddenApplications(request.getRemoteUser(), lang));
  }

  @PutMapping("{type}/{name}")
  @Secured("users")
  @Operation(summary = "Hides a page application from its page for the current user", method = "PUT",
             description = "Hides a page application from its page for the current user, on every device")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "The application doesn't allow hiding"),
    @ApiResponse(responseCode = "404", description = "The application type or the application doesn't exist, or the user can't view the application"), })
  public ResponseEntity<String> hide(
                                   HttpServletRequest request,
                                   @Parameter(description = "Application type", required = true)
                                   @PathVariable("type")
                                   String type,
                                   @Parameter(description = "Application name", required = true)
                                   @PathVariable("name")
                                   String name) {
    try {
      hiddenApplicationService.hide(type, name, request.getRemoteUser());
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      // The message code, which the client shows through its label
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The application type or the application doesn't exist");
    }
  }

  @DeleteMapping("{type}/{name}")
  @Secured("users")
  @Operation(summary = "Shows again a page application the current user hid", method = "DELETE",
             description = "Shows again a page application the current user hid")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Request fulfilled") })
  public ResponseEntity<Void> show(
                                   HttpServletRequest request,
                                   @Parameter(description = "Application type", required = true)
                                   @PathVariable("type")
                                   String type,
                                   @Parameter(description = "Application name", required = true)
                                   @PathVariable("name")
                                   String name) {
    hiddenApplicationService.show(type, name, request.getRemoteUser());
    return ResponseEntity.noContent().build();
  }

}
