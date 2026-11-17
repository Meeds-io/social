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
package io.meeds.social.space.rest;

import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.SpaceStorageException;

import io.meeds.social.space.rest.model.SpaceSecuritySettingsEntity;
import io.meeds.social.space.security.exception.SpaceSecurityChangeNotAppliedException;
import io.meeds.social.space.security.exception.SpaceSecurityRefusedException;
import io.meeds.social.space.security.model.SpaceSecuritySettings;
import io.meeds.social.space.security.service.SpaceSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/space")
@Tag(name = "/social/rest/space", description = "Managing the security settings of a space")
public class SpaceSecurityRest {

  public static final String  INVALID_VALUE_MESSAGE_CODE     = "space.security.invalidValue";

  public static final String  CONCURRENT_UPDATE_MESSAGE_CODE = "space.concurrentUpdate";

  private static final Log    LOG                            = ExoLogger.getLogger(SpaceSecurityRest.class);

  private static final String LIMIT_ACCESS_MANAGEMENT_FIELD  = "limitAccessManagement";

  private static final String EXTERNAL_MEMBERS_ALLOWED_FIELD = "externalMembersAllowed";

  /**
   * The settings a client may change: "Keep documents inside the space" and
   * "External users can join this space" are read here, and changed by no
   * request until the stories that enforce them ship
   */
  private static final Set<String> FIELDS                    = Set.of(LIMIT_ACCESS_MANAGEMENT_FIELD);

  @Autowired
  private SpaceSecurityService spaceSecurityService;

  @Autowired
  private SpaceService         spaceService;

  @GetMapping(path = "{spaceId}/security", produces = MediaType.APPLICATION_JSON_VALUE)
  @Secured("users")
  @Operation(summary = "Gets the security settings of a space", method = "GET",
             description = "Returns the stored security settings of the space and which of them the current user may change through PATCH")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "The space is not visible to the user"),
    @ApiResponse(responseCode = "404", description = "The space doesn't exist"),
  })
  public ResponseEntity<SpaceSecuritySettingsEntity> getSettings(HttpServletRequest request,
                                                                 @Parameter(description = "Space identifier")
                                                                 @PathVariable("spaceId")
                                                                 long spaceId) {
    String username = request.getRemoteUser();
    try {
      SpaceSecuritySettings settings = spaceSecurityService.getSettings(spaceId, username);
      return noStore(toEntity(spaceService.getSpaceById(spaceId), settings, username));
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    } catch (IllegalAccessException e) {
      throw forbidden(e);
    }
  }

  @PatchMapping(path = "{spaceId}/security", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
  @Secured("users")
  @Operation(summary = "Changes security settings of a space", method = "PATCH",
             description = "Changes 'limitAccessManagement', the only setting a request may change, by a space admin")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "A field is unknown, or, for a user who may change it, its value is not a boolean"),
    @ApiResponse(responseCode = "403", description = "The user may not change one of the settings"),
    @ApiResponse(responseCode = "404", description = "The space doesn't exist"),
    @ApiResponse(responseCode = "409", description = "Another change of the space is being written: retry"),
    @ApiResponse(responseCode = "500", description = "The change could not be prepared: nothing was written"),
  })
  public ResponseEntity<SpaceSecuritySettingsEntity> updateSettings(HttpServletRequest request,
                                                                    @Parameter(description = "Space identifier")
                                                                    @PathVariable("spaceId")
                                                                    long spaceId,
                                                                    @RequestBody
                                                                    Map<String, Object> changes) {
    String username = request.getRemoteUser();
    SpaceSecuritySettings changedFields = toChangedFields(changes);
    try {
      spaceSecurityService.checkCanUpdateSettings(spaceId, changedFields, username);
      SpaceSecuritySettings settings = spaceSecurityService.updateSettings(spaceId, toChanges(changes), username);
      return noStore(toEntity(spaceService.getSpaceById(spaceId), settings, username));
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    } catch (IllegalAccessException e) {
      throw forbidden(e);
    } catch (RuntimeException e) {
      if (SpaceStorageException.isConcurrentUpdate(e)) {
        LOG.debug("Concurrent change of space {} refused", spaceId, e);
        throw new ResponseStatusException(HttpStatus.CONFLICT, CONCURRENT_UPDATE_MESSAGE_CODE);
      }
      throw e;
    }
  }

  /**
   * Answers a change the settings' contributions refused, nothing written,
   * with its code in the body: the error page keeps no 5xx message
   */
  @ExceptionHandler(SpaceSecurityChangeNotAppliedException.class)
  public ResponseEntity<Map<String, String>> changeNotApplied(SpaceSecurityChangeNotAppliedException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                         .cacheControl(CacheControl.noStore())
                         .contentType(MediaType.APPLICATION_JSON)
                         .body(Map.of("message", SpaceSecurityChangeNotAppliedException.MESSAGE_CODE));
  }

  private ResponseStatusException forbidden(IllegalAccessException e) {
    return e instanceof SpaceSecurityRefusedException refusal ? new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                                                            refusal.getMessageCode())
                                                              : new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  /**
   * @return the settings the request names, each set whatever its value: what
   *         a user's rights are checked on before the values are
   */
  private SpaceSecuritySettings toChangedFields(Map<String, Object> changes) {
    if (changes == null || changes.isEmpty() || !FIELDS.containsAll(changes.keySet())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_VALUE_MESSAGE_CODE);
    }
    return new SpaceSecuritySettings(Boolean.TRUE, null, null);
  }

  private SpaceSecuritySettings toChanges(Map<String, Object> changes) {
    return new SpaceSecuritySettings(toBoolean(changes, LIMIT_ACCESS_MANAGEMENT_FIELD), null, null);
  }

  private Boolean toBoolean(Map<String, Object> changes, String field) {
    Object value = changes.get(field);
    if (value instanceof Boolean booleanValue) {
      return booleanValue;
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_VALUE_MESSAGE_CODE);
  }

  private SpaceSecuritySettingsEntity toEntity(Space space, SpaceSecuritySettings settings, String username) {
    return new SpaceSecuritySettingsEntity(settings.isLimitAccessManagementOn(),
                                           settings.isKeepDocumentsInsideOn(),
                                           spaceSecurityService.isExternalMemberAllowed(settings),
                                           spaceSecurityService.isExternalFeatureEnabled(),
                                           canChange(LIMIT_ACCESS_MANAGEMENT_FIELD,
                                                     () -> spaceSecurityService.canManageSpace(space, username)),
                                           canChange(EXTERNAL_MEMBERS_ALLOWED_FIELD, () -> spaceService.isSuperManager(username)));
  }

  /**
   * @return true when a request may change the setting and the user holds the
   *         right it needs
   */
  private boolean canChange(String field, BooleanSupplier hasRight) {
    return FIELDS.contains(field) && hasRight.getAsBoolean();
  }

  private <T> ResponseEntity<T> noStore(T body) {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
  }

}
