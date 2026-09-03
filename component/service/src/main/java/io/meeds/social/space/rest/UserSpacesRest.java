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

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.constant.UserSpacesScope;
import io.meeds.social.space.rest.model.UserSpace;
import io.meeds.social.space.rest.model.UserSpaceList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Lists the spaces of a profile owner as they are visible to the current user.
 * <p>
 * {@code @Secured("users")} is the correct and sufficient annotation, and an
 * external viewer is deliberately served by it: the roles extractor adds the
 * users role to any identity already carrying externals, so the annotation
 * cannot discriminate an external viewer. That discrimination lives in the
 * Service, which is also where the requested scope is narrowed (eXIP 7.3.0.18,
 * note 50524, decision D5). This layer validates parameters and nothing else.
 */
@RestController
@RequestMapping("/users")
@Tag(name = "/social/rest/users", description = "Managing the spaces listed on a user profile")
public class UserSpacesRest {

  private static final int   MAX_LIMIT = 100;

  @Autowired
  private SpaceService       spaceService;

  @GetMapping("/{username}/spaces")
  @Secured("users")
  @Operation(summary = "Retrieve the spaces of a user profile", method = "GET",
             description = "This retrieves the spaces of the designated user, restricted to what the currently authenticated user is allowed to see")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
                          @ApiResponse(responseCode = "401", description = "Unauthorized"),
                          @ApiResponse(responseCode = "404", description = "User not found"),
  })
  public UserSpaceList getUserSpaces(HttpServletRequest request,
                                     @Parameter(description = "User name of the profile owner", required = true)
                                     @PathVariable("username")
                                     String username,
                                     @Parameter(description = "Whether to list the spaces shared with the viewer only, or every space of the profile owner the viewer may see. A scope the viewer may not use is narrowed by the service, it is not refused.")
                                     @RequestParam(name = "scope", required = false)
                                     UserSpacesScope scope,
                                     @Parameter(description = "Offset of the first returned space")
                                     @RequestParam(name = "offset", required = false, defaultValue = "0")
                                     int offset,
                                     @Parameter(description = "Maximum number of returned spaces")
                                     @RequestParam(name = "limit", required = false, defaultValue = "20")
                                     int limit,
                                     @Parameter(description = "Whether to return the total size of the listing, which costs an extra query")
                                     @RequestParam(name = "returnSize", required = false, defaultValue = "false")
                                     boolean returnSize) {
    if (offset < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "space.offsetMustBePositive");
    }
    if (limit <= 0 || limit > MAX_LIMIT) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "space.limitOutOfRange");
    }
    String viewerUsername = request.getRemoteUser();
    try {
      List<UserSpace> spaces = spaceService.getUserSpaces(viewerUsername, username, scope, offset, limit)
                                           .stream()
                                           .map(space -> toUserSpace(space, viewerUsername))
                                           .toList();
      Integer size = returnSize ? spaceService.countUserSpaces(viewerUsername, username, scope) : null;
      return new UserSpaceList(spaces, size);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  private UserSpace toUserSpace(Space space, String viewerUsername) {
    return new UserSpace(space.getSpaceId(),
                         space.getDisplayName(),
                         space.getPrettyName(),
                         space.getUrl(),
                         space.getAvatarUrl(),
                         space.getVisibility(),
                         ArrayUtils.getLength(space.getMembers()),
                         spaceService.isMember(space, viewerUsername));
  }

}
