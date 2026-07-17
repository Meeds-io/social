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
package io.meeds.social.cms.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.social.cms.storage.elasticsearch.PageContentSearchConnector;
import io.meeds.social.search.model.PageSearchResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/pages")
@Tag(name = "/social/rest/pages", description = "Searches pages carrying an indexed content block")
public class PageSearchRest {

  @Autowired
  private PageContentSearchConnector pageContentSearchConnector;

  @GetMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Searches pages carrying an indexed content block", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public List<PageSearchResult> search(HttpServletRequest request,
                                       @Parameter(description = "Search term")
                                       @RequestParam("q")
                                       String term,
                                       @Parameter(description = "Search result offset")
                                       @RequestParam(name = "offset", required = false, defaultValue = "0")
                                       int offset,
                                       @Parameter(description = "Search result limit")
                                       @RequestParam(name = "limit", required = false, defaultValue = "20")
                                       int limit) {
    return pageContentSearchConnector.search(term, offset, limit, request.getLocale());
  }

  @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a single indexed page by its storage id", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public PageSearchResult getById(HttpServletRequest request,
                                  @Parameter(description = "Page storage id")
                                  @PathVariable(name = "id")
                                  String id) {
    PageSearchResult result = pageContentSearchConnector.getById(id, request.getLocale());
    if (result == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
    }
    return result;
  }

}
