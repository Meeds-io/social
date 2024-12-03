/*
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

export function getNavigationNodeUri(baseSiteUri, navigation) {
  const hasPage = !!navigation?.pageKey;
  const pageUrl = hasPage && `${baseSiteUri}${navigation.uri}`;
  const pageLink = navigation?.pageLink && Autolinker.parse(navigation?.pageLink, {
    email: true,
  })?.[0]?.getUrl?.() || navigation?.pageLink;
  navigation.nodeUri = pageLink || pageUrl;
  return navigation.nodeUri;
}

export function getNavigationNodeTarget(navigation) {
  navigation.nodeTarget = navigation?.target === 'SAME_TAB' && '_self' || '_blank';
  return navigation.nodeTarget;
}
