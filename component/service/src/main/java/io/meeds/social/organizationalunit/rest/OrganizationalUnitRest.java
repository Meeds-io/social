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
package io.meeds.social.organizationalunit.rest;

import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.social.organizationalunit.model.OrganizationalUnit;
import io.meeds.social.organizationalunit.service.OrganizationalUnitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/organizational-units")
@Tag(name = "/organizational-units", description = "Manage groups Organizational Unit designation")
public class OrganizationalUnitRest {

  @Autowired
  private OrganizationalUnitService organizationalUnitService;

  @GetMapping
  @Secured("users")
  @Operation(summary = "Check whether a group is designated as an Organizational Unit", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fullfilled") })
  public boolean isOrganizationalUnit(@Parameter(description = "Group id")
                                      @RequestParam("groupId")
                                      String groupId) {
    return organizationalUnitService.isOrganizationalUnit(groupId);
  }

  @GetMapping("/mine")
  @Secured("users")
  @Operation(summary = "Retrieve the Organizational Units the current user manages", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fullfilled") })
  public List<OrganizationalUnit> getMyOrganizationalUnits(HttpServletRequest request) {
    return organizationalUnitService.getManagedOrganizationalUnits(request.getRemoteUser());
  }

  @PutMapping
  @Secured("administrators")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Mark or unmark a group as an Organizational Unit", method = "PUT")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Request fullfilled"),
          @ApiResponse(responseCode = "404", description = "Group not found"),
  })
  public void setOrganizationalUnit(@Parameter(description = "Group id")
                                    @RequestParam("groupId")
                                    String groupId,
                                    @Parameter(description = "Whether the group should be designated as an Organizational Unit")
                                    @RequestParam("organizationalUnit")
                                    boolean organizationalUnit) throws Exception {
    try {
      organizationalUnitService.setOrganizationalUnit(groupId, organizationalUnit);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

}
