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
package io.meeds.social.space.template.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.model.SpaceTemplateFilter;
import io.meeds.social.space.template.service.SpaceTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/space/templates")
@Tag(name = "/social/rest/space/templates", description = "Managing space templates")
public class SpaceTemplateRest {

  @Autowired
  private SpaceTemplateService spaceTemplateService;

  @GetMapping
  @Secured("users")
  @Operation(summary = "Retrieve space templates", method = "GET", description = "This retrieves space templates")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"), })
  public List<SpaceTemplate> getSpaceTemplates(HttpServletRequest request,
                                               Pageable pageable,
                                               @Parameter(description = "Whether include disabled templates or not")
                                               @RequestParam("includeDisabled")
                                               boolean includeDisabled) {
    SpaceTemplateFilter spaceTemplateFilter = new SpaceTemplateFilter(request.getRemoteUser(),
                                                                      request.getLocale(),
                                                                      includeDisabled);
    return spaceTemplateService.getSpaceTemplates(spaceTemplateFilter, pageable, true);
  }

  @GetMapping("{id}")
  @Secured("users")
  @Operation(summary = "Retrieve a Space template designated by its id", method = "GET",
             description = "This will retrieve a Space template designated by its id")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "403", description = "Forbidden"),
                          @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public SpaceTemplate getSpaceTemplate(HttpServletRequest request,
                                        @Parameter(description = "Space template identifier")
                                        @PathVariable("id")
                                        long id) {
    try {
      SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplate(id, request.getRemoteUser(), request.getLocale(), true);
      if (spaceTemplate == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
      return spaceTemplate;
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

  @PostMapping
  @Secured("users")
  @Operation(summary = "Create a Space template", method = "POST", description = "This creates a new Space template")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public SpaceTemplate createSpaceTemplate(HttpServletRequest request,
                                           @RequestBody
                                           SpaceTemplate spaceTemplate) {
    try {
      return spaceTemplateService.createSpaceTemplate(spaceTemplate, request.getRemoteUser());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

  @PutMapping("{id}")
  @Secured("users")
  @Operation(summary = "Update a Space template", method = "PUT", description = "This updates an existing Space template")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "403", description = "Forbidden"),
                          @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public void updateSpaceTemplate(HttpServletRequest request,
                                  @Parameter(description = "Space template identifier")
                                  @PathVariable("id")
                                  long id,
                                  @RequestBody
                                  SpaceTemplate spaceTemplate) {
    try {
      spaceTemplate.setId(id);
      spaceTemplateService.updateSpaceTemplate(spaceTemplate, request.getRemoteUser());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

  @DeleteMapping("{id}")
  @Secured("users")
  @Operation(summary = "Deletes a Space template", method = "DELETE", description = "This deletes an existing Space template")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "403", description = "Forbidden"),
                          @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public void deleteSpaceTemplate(HttpServletRequest request,
                                  @Parameter(description = "Space template identifier")
                                  @PathVariable("id")
                                  long id) {
    try {
      spaceTemplateService.deleteSpaceTemplate(id, request.getRemoteUser());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

}
