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
package io.meeds.social.portlet.model;

import java.util.Collections;
import java.util.List;

/**
 * What the widget renders from. {@code parentSpace} false means the widget
 * hides its whole application: the other fields are then empty and no listing
 * query ran.
 * <p>
 * {@code settings} is what the preferences <em>hold</em>, not what a client
 * asked to store: it is how the drawer learns whether its save was applied.
 * The action phase answers 200 whatever happens — a {@code PortletException}
 * thrown by {@code SubspacesListPortlet.processAction} is rethrown by the portal's
 * {@code UIPortletActionListener} and swallowed by
 * {@code PortalRequestHandler}, which logs it and commits the response
 * untouched — so the transport cannot tell a stored value from a refused
 * one, and only reading the preferences back can.
 */
public record SubspacesEnvelope(boolean parentSpace,
                                boolean canManageSpace,
                                boolean canCreateSubspace,
                                List<SubspaceItem> subspaces,
                                StoredSettings settings) {
  public static SubspacesEnvelope notParentSpace() {
    return new SubspacesEnvelope(false, false, false, Collections.emptyList(), null);
  }
}
