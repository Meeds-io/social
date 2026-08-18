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
package io.meeds.social.security.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.web.security.service.OtpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/otp")
@Tag(name = "/otp", description = "Manage User API Keys")
public class OtpRest {

  @Autowired
  private OtpService otpService;

  @GetMapping
  @Secured("users")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(summary = "Sends an OTP code using the designated method (email, app ...)", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fullfilled"),
    @ApiResponse(responseCode = "429", description = "An OTP code was already sent recently, retry later"),
  })
  public void sendOtpCode(HttpServletRequest request,
                          @Parameter(description = "OTP Method")
                          @RequestParam("method")
                          String otpMethod) {
    try {
      otpService.sendOtpCode(request.getRemoteUser(), otpMethod);
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
    }
  }

}
