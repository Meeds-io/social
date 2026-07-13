/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.space.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.space.model.SpacePermissions;
import io.meeds.social.space.model.SpaceTemplatePatch;
import io.meeds.social.space.service.SpaceAdministrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/space/administration")
@Tag(name = "/social/rest/space/administration", description = "Managing spaces by platform administrators")
public class SpaceAdministrationRest {

  @Autowired
  private SpaceAdministrationService spacesAdministrationService;

  @GetMapping("{spaceId}/permissions")
  @Secured("administrators")
  @Operation(summary = "Gets a space permissions settings", method = "GET", description = "This returns the space permissions settings: <br/><ul><li>Delete Permissions</li><li>Layout Permissions</li><li>Public Site Permissions</li></ul>")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public SpacePermissions getSpacePermissions(
                                              @Parameter(description = "Space  identifier")
                                              @PathVariable("spaceId")
                                              long spaceId) {
    try {
      return spacesAdministrationService.getSpacePermissions(spaceId);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping(value = "{spaceId}/extendedProperties")
  @Secured("administrators")
  @Operation(summary = "Gets a space extended settings", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public Map<String, String> getSpaceExtendedProperties(
                                                        @Parameter(description = "Space  identifier")
                                                        @PathVariable("spaceId")
                                                        long spaceId) {
    try {
      return spacesAdministrationService.getSpaceExtendedProperties(spaceId);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping(path = "{spaceId}/permissions", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Secured("administrators")
  @Operation(summary = "Update a space permissions settings", method = "PUT", description = "This updates a space permissions settings: <br/><ul><li>Delete Permissions</li><li>Layout Permissions</li><li>Public Site Permissions</li></ul>")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public void updateSpacePermissions(
                                     @Parameter(description = "Space  identifier")
                                     @PathVariable("spaceId")
                                     long spaceId,
                                     @RequestBody(required = true)
                                     SpacePermissions permissions) {
    try {
      spacesAdministrationService.updateSpacePermissions(spaceId, permissions);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @PatchMapping(path = "{spaceId}/extendedProperties", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Secured("administrators")
  @Operation(summary = "Update a space extended property by key/value pair", method = "PATCH")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public void updateSpaceExtendedProperties(
                                          @Parameter(description = "Space  identifier")
                                          @PathVariable("spaceId")
                                          long spaceId,
                                          @RequestBody
                                          Map<String, String> extendedProperties) {
    try {
      spacesAdministrationService.updateSpaceExtendedProperties(spaceId, extendedProperties);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping(path = "{spaceId}/template", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Secured("administrators")
  @Operation(summary = "Update a space by applying a chosen template characteristics", method = "PUT", description = "This updates a space a space by applying a chosen template characteristics: <br/><ul><li>Layout</li><li>Editorial mode</li><li>Delete Permissions</li><li>Layout Permissions</li><li>Public Site Permissions</li></ul>")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public void applySpaceTemplate(
                                 @Parameter(description = "Space  identifier")
                                 @PathVariable("spaceId")
                                 long spaceId,
                                 @RequestBody(required = true)
                                 SpaceTemplatePatch templatePatch) {
    try {
      spacesAdministrationService.applySpaceTemplate(spaceId, templatePatch);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

}
