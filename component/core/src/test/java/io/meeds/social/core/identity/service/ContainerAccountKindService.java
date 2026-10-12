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
package io.meeds.social.core.identity.service;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.OrganizationService;

import io.meeds.social.core.identity.plugin.AccountKindPlugin;

/**
 * The {@link AccountKindService} of the kernel-container tests, which have no
 * Spring context to collect the plugin beans from: it is built by the kernel,
 * whose constructor injection cannot resolve a list, and hands the service
 * every {@link AccountKindPlugin} component the test configuration declares,
 * so that the tests run the service's own collection of the plugins.
 */
public class ContainerAccountKindService extends AccountKindService {

  /**
   * Builds the service over the plugin components of the container.
   *
   * @param organizationService the organization service
   * @param userAcl the ACL service
   * @param containerContext the context of the container the plugins are
   *          declared in
   */
  public ContainerAccountKindService(OrganizationService organizationService,
                                     UserACL userAcl,
                                     ExoContainerContext containerContext) {
    super(organizationService,
          userAcl,
          containerContext.getContainer().getComponentInstancesOfType(AccountKindPlugin.class));
  }

}
