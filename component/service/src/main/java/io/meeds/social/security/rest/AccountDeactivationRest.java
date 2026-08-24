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
package io.meeds.social.security.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.social.security.model.AccountDeactivationRequest;
import io.meeds.social.security.service.AccountDeactivationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/account/deactivation")
@Tag(name = "/account/deactivation", description = "Manage current user account deactivation request")
public class AccountDeactivationRest {

  @Autowired
  private AccountDeactivationService accountDeactivationService;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @Secured("users")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(summary = "Deactivates the current user account after validating the OTP code", method = "POST")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Account deactivated and sessions invalidated"),
    @ApiResponse(responseCode = "401", description = "OTP code is blank, invalid or tentatives are exhausted"),
    @ApiResponse(responseCode = "403", description = "Account deactivation or deletion is not allowed for the current user"),
  })
  public void requestDeactivation(HttpServletRequest request,
                                  @Parameter(description = "OTP method, code and deletion option")
                                  @RequestBody
                                  AccountDeactivationRequest deactivationRequest) {
    try {
      accountDeactivationService.requestDeactivation(request.getRemoteUser(),
                                                     deactivationRequest.getOtpMethod(),
                                                     deactivationRequest.getOtpCode(),
                                                     deactivationRequest.isDeleteAccount());
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP_CODE_INVALID");
    }
  }

}
