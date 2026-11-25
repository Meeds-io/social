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
package io.meeds.social.logging.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.portal.logging.model.LoggingLevelStatus;
import io.meeds.portal.logging.service.LoggingLevelService;
import io.meeds.social.logging.rest.model.LoggerLevelEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Every endpoint is for the users who have one of the roles of
 * {@code meeds.logging.levels.roles} (default {@code administrators}). A system
 * administrator can also switch the feature off, or to read only, in
 * {@code exo.properties}: the service then refuses with a 403 and a message
 * code.
 */
@RestController
@RequestMapping("logging")
@PreAuthorize("hasAnyAuthority(@loggingLevelConfiguration.roles)")
@Tag(name = "/social/rest/logging", description = "Changes the level of the loggers while the server runs")
public class LoggingLevelRest {

  private static final String MESSAGE_KEY = "message";

  @Autowired
  private LoggingLevelService loggingLevelService;

  @GetMapping("status")
  @Operation(summary = "Tells what the page may do", method = "GET",
             description = "Whether the levels can be listed at all, and whether they can be changed")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public LoggingLevelStatus getStatus() {
    return loggingLevelService.getStatus();
  }

  @GetMapping("loggers")
  @Operation(summary = "Lists the loggers", method = "GET",
             description = "Lists the loggers the application created: the changed ones first, then the others by name, with the level of each")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "Forbidden: not a role allowed, or the feature is disabled (logging.disabled)"),
  })
  public ResponseEntity<Object> getLoggers(@Parameter(description = "A part of the logger name")
                                    @RequestParam(name = "query", required = false)
                                    String query,
                                    @Parameter(description = "Keeps the loggers that have a level of their own")
                                    @RequestParam(name = "explicitOnly", required = false, defaultValue = "false")
                                    boolean explicitOnly,
                                    @Parameter(description = "Index of the first logger")
                                    @RequestParam(name = "offset", required = false, defaultValue = "0")
                                    int offset,
                                    @Parameter(description = "Number of loggers to return")
                                    @RequestParam(name = "limit", required = false, defaultValue = "20")
                                    int limit) {
    try {
      return ResponseEntity.ok(loggingLevelService.getLoggers(query, explicitOnly, offset, limit));
    } catch (IllegalAccessException e) {
      return forbidden(e);
    }
  }

  @PutMapping(path = "loggers/{name}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Sets the level of a logger", method = "PUT",
             description = "Sets the level of a package or a class at runtime, a new one when no code created it yet")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Invalid logger name or level"),
    @ApiResponse(responseCode = "403", description = "Forbidden: not a role allowed, or disabled (logging.disabled) or read only (logging.readOnly)"),
  })
  public ResponseEntity<Object> setLevel(HttpServletRequest request,
                                         @Parameter(description = "Logger name: a package, a class or ROOT", required = true)
                                         @PathVariable("name")
                                         String name,
                                         @RequestBody
                                         LoggerLevelEntity body) {
    try {
      loggingLevelService.setLevel(name, body == null ? null : body.level(), request.getRemoteUser());
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE_KEY, e.getMessage()));
    } catch (IllegalAccessException e) {
      return forbidden(e);
    }
  }

  @DeleteMapping(path = "loggers/{name}")
  @Operation(summary = "Gives a logger the level of logback.xml", method = "DELETE")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Invalid logger name"),
    @ApiResponse(responseCode = "403", description = "Forbidden: not a role allowed, or disabled (logging.disabled) or read only (logging.readOnly)"),
    @ApiResponse(responseCode = "404", description = "The logger has no level set at runtime"),
  })
  public ResponseEntity<Object> resetLevel(HttpServletRequest request,
                                           @Parameter(description = "Logger name", required = true)
                                           @PathVariable("name")
                                           String name) {
    try {
      loggingLevelService.resetLevel(name, request.getRemoteUser());
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE_KEY, e.getMessage()));
    } catch (ObjectNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
    } catch (IllegalAccessException e) {
      return forbidden(e);
    }
  }

  @DeleteMapping("loggers")
  @Operation(summary = "Gives every logger the level of logback.xml", method = "DELETE")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "Forbidden: not a role allowed, or disabled (logging.disabled) or read only (logging.readOnly)"),
  })
  public ResponseEntity<Object> resetAll(HttpServletRequest request) {
    try {
      loggingLevelService.resetAll(request.getRemoteUser());
      return ResponseEntity.ok().build();
    } catch (IllegalAccessException e) {
      return forbidden(e);
    }
  }

  private static ResponseEntity<Object> forbidden(IllegalAccessException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(MESSAGE_KEY, e.getMessage()));
  }

}
