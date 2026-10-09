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
package io.meeds.social.core.identity.util;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

/**
 * Agent accounts: the user accounts that are members of the
 * {@link #PLATFORM_AGENTS_GROUP} group. That membership is the only source of
 * truth for a security decision; the profile property
 * {@code Profile#AGENT} is a display
 * copy of it.
 */
public final class AgentUserUtils {

  /** The group whose members are agent accounts. */
  public static final String PLATFORM_AGENTS_GROUP = "/platform/agents";

  /**
   * Utility class, not instantiated.
   */
  private AgentUserUtils() {
  }

  /**
   * Tells whether a user is an agent account, from its memberships in
   * {@link #PLATFORM_AGENTS_GROUP}, whatever their membership type.
   *
   * @param organizationService the organization service to read the
   *          memberships from
   * @param userName the user name to check
   * @return true when the user holds at least one membership in
   *         {@link #PLATFORM_AGENTS_GROUP}
   * @throws Exception when the memberships cannot be read; a caller deciding a
   *           permission treats that as a refusal
   */
  public static boolean isAgentUser(OrganizationService organizationService, String userName) throws Exception { // NOSONAR
    if (StringUtils.isBlank(userName)) {
      return false;
    }
    Collection<Membership> memberships = organizationService.getMembershipHandler()
                                                            .findMembershipsByUserAndGroup(userName, PLATFORM_AGENTS_GROUP);
    return memberships != null && !memberships.isEmpty();
  }

  /**
   * Refuses a change of the {@code Profile#MANAGER} property of an agent
   * account to anyone but an administrator: the agent itself cannot change
   * who it reports to. A failure to read the memberships refuses the change.
   *
   * @param organizationService the organization service to read the
   *          memberships from
   * @param profile the profile being updated
   * @param administrator whether the user who makes the change is an
   *          administrator
   * @throws IllegalAccessException when the profile is an agent account's and
   *           the modifier is not an administrator, or when its memberships
   *           cannot be read
   */
  public static void checkManagerUpdate(OrganizationService organizationService,
                                        Profile profile,
                                        boolean administrator) throws IllegalAccessException {
    if (administrator) {
      return;
    }
    Identity identity = profile == null ? null : profile.getIdentity();
    String userName = identity == null ? null : identity.getRemoteId();
    boolean agent;
    try {
      agent = isAgentUser(organizationService, userName);
    } catch (Exception e) {
      throw new IllegalAccessException("Cannot check whether the profile of " + userName + " is an agent account's");
    }
    if (agent) {
      throw new IllegalAccessException("Not allowed to update the MANAGER field of an agent account");
    }
  }

}
