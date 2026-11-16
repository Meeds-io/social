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

import org.exoplatform.social.core.model.ProfileLabel;
import org.exoplatform.social.rest.entity.ProfilePropertyOptionEntity;

/**
 * The definition of a profile attribute the registration form lists, as an
 * anonymous registrant reads it: what the form needs to draw and check the
 * field, and none of the attribute's other settings
 *
 * @param id              the attribute's id
 * @param propertyName    the attribute's name, the field's name
 * @param propertyType    the attribute's type, null for an attribute stored
 *                          without one
 * @param dropdownList    whether the value is one of the attribute's options
 * @param multiValued     whether the attribute holds several values
 * @param required        whether the registration requires a value
 * @param order           the attribute's order among the profile's
 *                          attributes, which sorts the listed ones
 * @param labels          the attribute's labels, in every language
 * @param propertyOptions the attribute's options, translated in the request's
 *                          locale
 */
public record RegistrationFieldEntity(Long id,
                                      String propertyName,
                                      String propertyType,
                                      boolean dropdownList,
                                      boolean multiValued,
                                      boolean required,
                                      Long order,
                                      List<ProfileLabel> labels,
                                      List<ProfilePropertyOptionEntity> propertyOptions) {
}
