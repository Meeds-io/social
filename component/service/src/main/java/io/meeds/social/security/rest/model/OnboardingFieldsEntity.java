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
package io.meeds.social.security.rest.model;

import java.util.List;
import java.util.Map;

/**
 * The additional fields of the onboarding page, as the holder of an onboarding
 * link reads them: the attributes the registration form lists, and the values
 * the link's account holds for them
 *
 * @param fields the attributes' definitions, in the settings' order; empty
 *                 when the link does not serve the account's fields
 * @param values by attribute name, the values the account holds, for the
 *                 listed attributes it holds a value for only
 */
public record OnboardingFieldsEntity(List<RegistrationFieldEntity> fields, Map<String, List<String>> values) {
}
