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
package io.meeds.social.cms.rest;

import java.util.List;

import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkIdentifier;
import io.meeds.social.cms.model.ContentLinkList;
import io.meeds.social.cms.model.ContentLinkSearchResult;
import io.meeds.social.cms.model.ContentObject;
import io.meeds.social.cms.service.ContentLinkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/contentLinks")
@Tag(name = "/social/rest/contentLinks", description = "Manage Content Links")
public class ContentLinkRest {

  @Autowired
  private ContentLinkService contentLinkService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves Content Link managed extensions", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public List<ContentLinkExtension> getExtensions() {
    return contentLinkService.getExtensions();
  }

  @GetMapping(value = "{objectType}/{objectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves Linked objects to a content", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public List<ContentLink> getLinks(HttpServletRequest request,
                                    @Parameter(description = "Object Type")
                                    @PathVariable(name = "objectType")
                                    String objectType,
                                    @Parameter(description = "Object Id")
                                    @PathVariable(name = "objectId")
                                    String objectId,
                                    @Parameter(description = "Field Name")
                                    @RequestParam(name = "fieldName", required = false)
                                    String fieldName,
                                    @Parameter(description = "Language")
                                    @RequestParam(name = "lang", required = false)
                                    String lang) {
    try {
      return contentLinkService.getLinks(new ContentObject(objectType, objectId, fieldName, LocaleUtils.toLocale(lang)),
                                         request.getLocale(),
                                         request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

  @GetMapping(value = "link/{objectType}/{objectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a single Content Link", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public ContentLink getLink(HttpServletRequest request,
                             @Parameter(description = "Object Type")
                             @PathVariable(name = "objectType")
                             String objectType,
                             @Parameter(description = "Object Id")
                             @PathVariable(name = "objectId")
                             String objectId) {
    try {
      return contentLinkService.getLink(new ContentLinkIdentifier(objectType, objectId, request.getLocale()),
                                        request.getRemoteUser());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

  @GetMapping(value = "{objectType}/search", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Searches for Links of a specific object type", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public List<ContentLinkSearchResult> searchLinks(HttpServletRequest request,
                                                   @Parameter(description = "Object Type")
                                                   @PathVariable(name = "objectType")
                                                   String objectType,
                                                   @Parameter(description = "Search query")
                                                   @RequestParam("query")
                                                   String query,
                                                   @Parameter(description = "Search result offset")
                                                   @RequestParam(name = "fieldName", required = false, defaultValue = "0")
                                                   int offset,
                                                   @Parameter(description = "Search result limit")
                                                   @RequestParam(name = "fieldName", required = false, defaultValue = "10")
                                                   int limit) {
    try {
      return contentLinkService.searchLinks(objectType,
                                            query,
                                            request.getRemoteUser(),
                                            request.getLocale(),
                                            offset,
                                            limit);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  @PutMapping(value = "{objectType}/{objectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  @Operation(summary = "Save Linked objects to a content", method = "PUT")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public void saveLinks(HttpServletRequest request,
                        @Parameter(description = "Object Type")
                        @PathVariable(name = "objectType")
                        String objectType,
                        @Parameter(description = "Object Id")
                        @PathVariable(name = "objectId")
                        String objectId,
                        @Parameter(description = "Field Name")
                        @RequestParam(name = "fieldName", required = false)
                        String fieldName,
                        @Parameter(description = "Language")
                        @RequestParam(name = "lang", required = false)
                        String lang,
                        @RequestBody
                        ContentLinkList linkList) {
    try {
      contentLinkService.saveLinks(new ContentObject(objectType, objectId, fieldName, LocaleUtils.toLocale(lang)),
                                   linkList.getLinks(),
                                   request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

}
