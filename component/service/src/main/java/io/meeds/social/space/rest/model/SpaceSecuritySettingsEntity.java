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
package io.meeds.social.space.rest.model;

/**
 * The space security settings as the space settings and the documents
 * application read them, with what the requesting user may change.
 *
 * @param limitAccessManagement "Limit Access Management" (ID 4)
 * @param keepDocumentsInside "Keep documents inside the space" (ID 3)
 * @param externalMembersAllowed "External users can join this space" (ID 2),
 *          effective: true when the platform lets no new guest in
 * @param externalFeatureEnabled whether the platform lets new guests be invited
 * @param canManageSettings whether the user may change ID 3 and ID 4
 * @param canManageExternals whether the user may change ID 2
 */
public record SpaceSecuritySettingsEntity(boolean limitAccessManagement,
                                          boolean keepDocumentsInside,
                                          boolean externalMembersAllowed,
                                          boolean externalFeatureEnabled,
                                          boolean canManageSettings,
                                          boolean canManageExternals) {
}
