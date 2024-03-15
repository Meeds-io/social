/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

export function getComplementaryFilterSuggestions(objectIds, attributes, indexAlias, minDocCount) {
  const formData = new FormData();
  attributes.forEach(attribute => formData.append('attributes', attribute));
  if (minDocCount) {
    formData.append('minDocCount', minDocCount);
  }
  const params = new URLSearchParams(formData).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/complementaryfilter/suggestions/${indexAlias}?${params}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(objectIds),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error while getting complementary filter suggestions');
    } else {
      return resp.json();
    }
  });
}
