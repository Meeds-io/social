/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package org.exoplatform.social.rest.impl.registry;

import java.util.Collection;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;

@Path("applications")
@Tag(name = "applications", description = "Managing Application Registry")
public class ApplicationRegistryRest implements ResourceContainer {

  private ApplicationRegistryService applicationRegistryService;

  private UserACL                    userAcl;

  public ApplicationRegistryRest(ApplicationRegistryService applicationRegistryService,
                                 UserACL userAcl) {
    this.applicationRegistryService = applicationRegistryService;
    this.userAcl = userAcl;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
             summary = "Gets all applications",
             method = "GET",
             description = "This returns a list of all supported applications of type Portlet.")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled")
  })
  @SneakyThrows
  public Response getApplications() {
    Collection<ApplicationCategory> categories = applicationRegistryService.detectPortletsFromWars();
    List<Application> applications = categories.stream()
                                               .flatMap(c -> c.getApplications().stream())
                                               .filter(this::hasPermission)
                                               .toList();
    return Response.ok(applications).build();
  }

  @GET
  @Path("categories")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
             summary = "Gets all application categories",
             method = "GET",
             description = "This returns a list of application categories as configured in application registry")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled")
  })
  @SneakyThrows
  public Response getApplicationCategories() {
    List<ApplicationCategory> categories = applicationRegistryService.getApplicationCategories()
                                                                     .stream()
                                                                     .filter(this::hasPermission)
                                                                     .map(this::filterApplications)
                                                                     .toList();
    return Response.ok(categories).build();
  }

  private ApplicationCategory filterApplications(ApplicationCategory category) {
    if (category.getApplications() != null) {
      category.setApplications(category.getApplications().stream().filter(this::hasPermission).toList());
    }
    return category;
  }

  private boolean hasPermission(ApplicationCategory category) {
    if (CollectionUtils.isEmpty(category.getAccessPermissions())) {
      return userAcl.isUserInGroup(userAcl.getAdminGroups());
    } else {
      return userAcl.hasPermission(category.getAccessPermissions().toArray(new String[0]));
    }
  }

  private boolean hasPermission(Application application) {
    if (CollectionUtils.isEmpty(application.getAccessPermissions())) {
      return userAcl.isUserInGroup(userAcl.getAdminGroups());
    } else {
      return userAcl.hasPermission(application.getAccessPermissions().toArray(new String[0]));
    }
  }

}
