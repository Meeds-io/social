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

import java.util.Collections;
import java.util.Set;

/**
 * Declares a kind of user account that an add-on owns, such as the accounts
 * a bot speaks as. The members of {@link #getGroupId()} are accounts of
 * that kind: social stores the kind on their profile under
 * {@code Profile#ACCOUNT_KIND}, shows the kind's label next to their name, and
 * refuses {@link #getAdminOnlyProperties()} to anyone but an administrator on
 * their profile. The group membership stays the only source of truth for a
 * permission decision; the profile property is a display copy of it.
 * <p>
 * Social writes that copy when a membership in the group is saved or removed,
 * and at no other time: a member who joined the group before its plugin was
 * declared, or while its plugin was refused, shows no label until one of its
 * memberships in the group is saved or removed. Decisions read the
 * membership, so they do not depend on the copy.
 * <p>
 * An add-on contributes a kind by declaring its plugin as a Spring bean
 * annotated {@code @Service}, which {@code AccountKindService} collects with
 * every other plugin bean. From another web application than social's, only an
 * {@code @Service} bean reaches social's context: the class is not final, and
 * the bean has a name social's context does not hold, given explicitly, such
 * as {@code @Service("botAccountKindPlugin")}. A plugin whose kind or group an
 * earlier plugin, in the order of the kinds then of the groups, already holds
 * is refused. Social reads a plugin at its first decision on an account
 * kind, which can come before the contributing add-on's context has started:
 * the bean is then created ahead of its context, so a plugin holds no
 * dependency, and a plugin whose creation fails is refused until the next
 * startup. The label shown for a kind is read
 * under the key {@link #getLabelKey(String)} in the shared resource bundles on
 * the server and in the {@code locale.portlet.Portlets} bundle in the browser,
 * which the add-on ships in both.
 */
public interface AccountKindPlugin {

  /** The prefix of the i18n key of a kind's label, followed by the kind. */
  String LABEL_KEY_PREFIX = "accountKind.label.";

  /**
   * Builds the i18n key of the label shown next to the name of an account of
   * a kind. The key is derived from the kind alone, so that a browser that
   * only reads the kind from a profile can resolve it.
   *
   * @param kind the account kind
   * @return the i18n key of the kind's label
   */
  static String getLabelKey(String kind) {
    return LABEL_KEY_PREFIX + kind;
  }

  /**
   * Gives the kind stored on the profiles of the accounts of this kind.
   * <p>
   * The kind is made of letters, digits, {@code _} and {@code -} only: it
   * becomes an i18n key, a REST value and a value of the profile popover
   * literal. A plugin declaring another kind is refused.
   *
   * @return the account kind, a non-blank identifier such as {@code bot}
   */
  String getKind();

  /**
   * Gives the group whose members are accounts of this kind, whatever their
   * membership type.
   *
   * @return the group id, such as {@code /platform/bots}
   */
  String getGroupId();

  /**
   * Gives the profile properties that only an administrator may change on the
   * profile of an account of this kind.
   *
   * @return the property names, empty by default
   */
  default Set<String> getAdminOnlyProperties() {
    return Collections.emptySet();
  }

}
