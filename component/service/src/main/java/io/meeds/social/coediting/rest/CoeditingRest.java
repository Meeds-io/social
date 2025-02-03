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
package io.meeds.social.coediting.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.social.coediting.model.CoeditionObjectDraft;
import io.meeds.social.coediting.model.CoeditionObjectKey;
import io.meeds.social.coediting.service.CoeditingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/coediting")
@Tag(name = "/coediting", description = "Managing coedition of any object")
public class CoeditingRest {

  @Autowired
  private CoeditingService coeditingService;

  @GetMapping("/{objectType}/{objectId}/locks")
  @Secured("users")
  @Operation(summary = "Retrieve object lock holders", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled") })
  public List<String> getLockHolders(
                                     @Parameter(description = "Object Type: navigation, page layout ...")
                                     @PathVariable("objectType")
                                     String objectType,
                                     @Parameter(description = "Object Type's specific element identifier")
                                     @PathVariable("objectId")
                                     String objectId) {
    return coeditingService.getLockHolders(new CoeditionObjectKey(objectType, objectId));
  }

  @PostMapping(value = "/{objectType}/{objectId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @Secured("users")
  @Operation(summary = "Set a lock on current object being edited", method = "POST")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled") })
  public void setLock(
                      HttpServletRequest request,
                      @Parameter(description = "Object Type: navigation, page layout ...")
                      @PathVariable("objectType")
                      String objectType,
                      @Parameter(description = "Object Type's specific element identifier")
                      @PathVariable("objectId")
                      String objectId,
                      @Parameter(description = "Current revision being edited by current user")
                      @RequestParam("revision")
                      String revision) {
    coeditingService.setLock(request.getRemoteUser(), new CoeditionObjectKey(objectType, objectId), revision);
  }

  @GetMapping("/{objectType}/{objectId}")
  @Secured("users")
  @Operation(summary = "Retrieve object revision for current user", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled") })
  public CoeditionObjectDraft getRevision(
                                          HttpServletRequest request,
                                          @Parameter(description = "Object Type: navigation, page layout ...")
                                          @PathVariable("objectType")
                                          String objectType,
                                          @Parameter(description = "Object Type's specific element identifier")
                                          @PathVariable("objectId")
                                          String objectId) {
    return coeditingService.getRevision(request.getRemoteUser(), new CoeditionObjectKey(objectType, objectId));
  }

  @DeleteMapping("/{objectType}/{objectId}")
  @Secured("users")
  @Operation(summary = "Delete object revision for current user", method = "DELETE")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled") })
  public void removeRevision(
                             HttpServletRequest request,
                             @Parameter(description = "Object Type: navigation, page layout ...")
                             @PathVariable("objectType")
                             String objectType,
                             @Parameter(description = "Object Type's specific element identifier")
                             @PathVariable("objectId")
                             String objectId) {
    coeditingService.removeRevision(request.getRemoteUser(), new CoeditionObjectKey(objectType, objectId));
  }

}
