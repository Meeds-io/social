/*
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
package io.meeds.social.databind.rest;

import io.meeds.social.databind.service.DatabindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@RestController
@RequestMapping("/databind")
@Tag(name = "/social/rest/databind", description = "Export & Import Templates")
public class DatabindRest {

  @Autowired
  private DatabindService databindService;

  @SneakyThrows
  @GetMapping("serialize")
  @Secured("administrators")
  @Operation(summary = "Export Template by object type and object id", method = "POST", description = "Export Template by object type and object id")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Bad Request"),
      @ApiResponse(responseCode = "404", description = "Not found"), })
  public ResponseEntity<InputStreamResource> serialize(HttpServletRequest request,
                                                       @Parameter(description = "Template Object Type.")
                                                       @RequestParam(name = "objectType")
                                                       String objectType,
                                                       @Parameter(description = "Template Object Ids")
                                                       @RequestParam(name = "objectId")
                                                       List<String> objectIds) {
    try {
      File zipFile = databindService.serialize(objectType, objectIds, request.getRemoteUser());
      InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile));
      return ResponseEntity.ok()
                           .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + objectType + ".zip")
                           .contentType(MediaType.APPLICATION_OCTET_STREAM)
                           .body(resource);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }
}
