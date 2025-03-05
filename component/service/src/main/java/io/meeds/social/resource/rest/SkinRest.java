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
package io.meeds.social.resource.rest;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.portal.resource.SkinConfig;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.services.resources.Orientation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/skins")
@Tag(name = "/skins", description = "Retrieve Skin Resources")
public class SkinRest {

  @Autowired
  private SkinService skinService;

  @GetMapping("{skinType}/{skinId}")
  @Operation(summary = "Redirects to a skin URL which is identified by its id/name", method = "GET")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "404", description = "Not Found"),
    @ApiResponse(responseCode = "302", description = "Found with redirection"),
  })
  public void getSkin(
                      HttpServletResponse response,
                      @Parameter(description = "Skin type: portlet or portal", required = true)
                      @PathVariable("skinType")
                      String skinType,
                      @Parameter(description = "Skin identifier: portal skin name or portlet skin name", required = true)
                      @PathVariable("skinId")
                      String skinId,
                      @Parameter(description = "Skin Name (optional): skin name", required = false)
                      @RequestParam(name = "skinName", required = false)
                      String skinName,
                      @Parameter(description = "Orientation: LT or RT", required = false)
                      @RequestParam(name = "orientation", defaultValue = "LT", required = false)
                      Orientation orientation) throws IOException {
    SkinConfig skin = getSkinConfig(skinType, skinId, skinName);
    if (skin == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        String.format("Skin with name %s/%s wasn't found", skinType, skinId));
    }
    String url = skin.createURL().toString(orientation);
    response.sendRedirect(url);
  }

  private SkinConfig getSkinConfig(String skinType, String skinId, String skinName) {
    if (StringUtils.equals(skinType, "portal")) {
      return skinService.getPortalSkin(skinId, skinName);
    } else {
      return skinService.getSkin(String.format("%s/%s", skinType, skinId), skinName);
    }
  }

}
