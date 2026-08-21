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
package io.meeds.social.reaction.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import io.meeds.social.reaction.model.Reaction;
import io.meeds.social.reaction.model.ReactionOption;
import io.meeds.social.reaction.rest.model.ReactionInput;
import io.meeds.social.reaction.rest.model.ReactionList;
import io.meeds.social.reaction.service.ReactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/reactions")
@Tag(name = "/social/rest/reactions", description = "Managing reactions on any type of content")
public class ReactionRest {

  private static final long DEFAULT_LIMIT = 20;

  private static final long MAX_LIMIT     = 100;

  @Autowired
  private ReactionService   reactionService;

  @GetMapping("options")
  @Secured("users")
  @Operation(summary = "Retrieves registered reaction options", method = "GET",
             description = "Retrieves the registered reaction options, sorted by rank, optionally filtered by object type")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled") })
  public List<ReactionOption> getReactionOptions(
                                                 @Parameter(description = "Object type to filter suggested options on")
                                                 @RequestParam(name = "objectType", required = false)
                                                 String objectType) {
    return objectType == null ? reactionService.getReactionOptions() : reactionService.getReactionOptions(objectType);
  }

  @GetMapping("{objectType}/{objectId}")
  @Secured("users")
  @Operation(summary = "Retrieves the reactions of an object", method = "GET",
             description = "Retrieves the paged reactors list of an object with the reactors count per reaction option")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public ReactionList getReactions(
                                   HttpServletRequest request,
                                   @Parameter(description = "Object type: activity for posts and comments", required = true)
                                   @PathVariable("objectType")
                                   String objectType,
                                   @Parameter(description = "Object technical identifier", required = true)
                                   @PathVariable("objectId")
                                   String objectId,
                                   @Parameter(description = "Reaction option id to filter reactors on")
                                   @RequestParam(name = "reactionId", required = false)
                                   String reactionId,
                                   @Parameter(description = "Query offset")
                                   @RequestParam(name = "offset", required = false, defaultValue = "0")
                                   long offset,
                                   @Parameter(description = "Query limit, defaults to 20")
                                   @RequestParam(name = "limit", required = false, defaultValue = "20")
                                   long limit) {
    try {
      if (limit <= 0) {
        limit = DEFAULT_LIMIT;
      } else if (limit > MAX_LIMIT) {
        limit = MAX_LIMIT;
      }
      Map<String, Long> counts = reactionService.countReactionsByOption(objectType, objectId, request.getRemoteUser());
      List<Reaction> reactions = reactionService.getReactions(objectType,
                                                              objectId,
                                                              reactionId,
                                                              offset,
                                                              limit,
                                                              request.getRemoteUser());
      return new ReactionList(counts, reactions);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

  @PutMapping("{objectType}/{objectId}")
  @Secured("users")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Sets the current user reaction on an object", method = "PUT",
             description = "Creates or changes the current user reaction on an object; creating also likes the object")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public void setReaction(
                          HttpServletRequest request,
                          @Parameter(description = "Object type: activity for posts and comments", required = true)
                          @PathVariable("objectType")
                          String objectType,
                          @Parameter(description = "Object technical identifier", required = true)
                          @PathVariable("objectId")
                          String objectId,
                          @RequestBody
                          ReactionInput reactionInput) {
    try {
      reactionService.setReaction(objectType, objectId, reactionInput.getReactionId(), request.getRemoteUser());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

  @DeleteMapping("{objectType}/{objectId}")
  @Secured("users")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Deletes the current user reaction on an object", method = "DELETE",
             description = "Deletes the current user reaction on an object (unlikes it as well)")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
  })
  public void deleteReaction(
                             HttpServletRequest request,
                             @Parameter(description = "Object type: activity for posts and comments", required = true)
                             @PathVariable("objectType")
                             String objectType,
                             @Parameter(description = "Object technical identifier", required = true)
                             @PathVariable("objectId")
                             String objectId) {
    try {
      reactionService.deleteReaction(objectType, objectId, request.getRemoteUser());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

}
