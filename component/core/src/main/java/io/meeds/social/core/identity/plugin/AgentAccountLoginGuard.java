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
package io.meeds.social.core.identity.plugin;

import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.plugin.SecurityCheckAuthenticationPlugin;

import io.meeds.social.core.identity.util.AgentUserUtils;

/**
 * Refuses a password login to an agent account. The authenticator runs this
 * check before the password check, for a user name it resolves, and turns the
 * refusal into the same login failure as a disabled account's. An agent
 * account is a member of {@link AgentUserUtils#PLATFORM_AGENTS_GROUP}; the
 * profile property is never read here.
 */
public class AgentAccountLoginGuard extends SecurityCheckAuthenticationPlugin {

  /** The organization service that holds the agents group memberships. */
  private final OrganizationService organizationService;

  /**
   * Builds the guard.
   *
   * @param organizationService the organization service that holds the agents
   *          group memberships
   */
  public AgentAccountLoginGuard(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  /**
   * Refuses the login of an agent account. A failure to read the memberships
   * refuses the login too.
   *
   * @param user the user who attempts to log in
   * @throws DisabledUserException when the user is an agent account, or when
   *           its memberships cannot be read
   */
  @Override
  public void doCheck(User user) throws Exception {
    if (user == null) {
      return;
    }
    boolean agent;
    try {
      agent = AgentUserUtils.isAgentUser(organizationService, user.getUserName());
    } catch (Exception e) {
      throw new DisabledUserException(user.getUserName(), e);
    }
    if (agent) {
      throw new DisabledUserException(user.getUserName());
    }
  }

  /**
   * Nothing to do on a failed login.
   *
   * @param userName the user name of the failed login
   */
  @Override
  public void onCheckFail(String userName) {
    // No state to update
  }

  /**
   * Nothing to do on a successful login.
   *
   * @param userName the user name of the successful login
   */
  @Override
  public void onCheckSuccess(String userName) {
    // No state to update
  }

}
