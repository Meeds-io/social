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
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.web.security.service.ApiKeyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/apiKey")
@Tag(name = "/apiKey", description = "Manage User API Keys")
public class ApiKeyRest {

  @Autowired
  private ApiKeyService keyService;

  @GetMapping
  @Secured("users")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(summary = "Sends an OTP code using the designated method (email, app ...)", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Request fullfilled"),
  })
  public void sendOtpCode(HttpServletRequest request,
                          @Parameter(description = "OTP Method")
                          @RequestParam("method")
                          String otpMethod) {
    keyService.sendOtpCode(request.getRemoteUser(), otpMethod);
  }

  @Secured("users")
  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @Operation(summary = "Retrieves clear password for current user if OTP is valid", method = "POST")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fullfilled"),
  })
  public String getPassword(HttpServletRequest request,
                            @Parameter(description = "OTP Method")
                            @RequestParam("method")
                            String otpMethod,
                            @Parameter(description = "OTP Code")
                            @RequestParam("code")
                            String otpCode,
                            @Parameter(description = "OTP Code")
                            @RequestParam(value = "renew", required = false, defaultValue = "false")
                            boolean renew) {
    try {
      return keyService.getPassword(request.getRemoteUser(), otpMethod, otpCode, renew);
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

}
