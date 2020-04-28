/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
export const spacesConstants = {
  ENV: eXo.env.portal || '',
  PORTAL: eXo.env.portal.context || '',
  PORTAL_NAME: eXo.env.portal.portalName || '',
  PORTAL_CONTEXT: eXo.env.portal.context,
  PORTAL_REST: eXo.env.portal.rest,
  PROFILE_SPACE_LINK: '/g/:spaces:',
  SOCIAL_USER_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/`,
  SOCIAL_SPACE_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/`,
  SPACES_ADMINISTRATION_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesAdministration`,
  SPACE_GROUP_BINDING_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaceGroupBindings`,
  USER_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users`,
  GROUP_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups`,
  SPACES_PER_PAGE: 30,
  DEFAULT_SPACE_AVATAR: '/eXoSkin/skin/images/system/SpaceAvtDefault.png',
  SPACES_TEMPLATES_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaceTemplates`,
  CONTAINER_NAME: eXo.env.portal.containerName || '',
  SPACE_ID: eXo.env.portal.spaceId,
  LANG: eXo.env.portal.language,
  HOST_NAME: window.location.host,
  MANAGERS_ROLE: '/users?role=manager',
  UPLOAD_API: `${eXo.env.portal.context}/upload`,
  MAX_UPLOAD_SIZE: 10,
  MAX_UPLOAD_FILES: 1,
  userName: eXo.env.portal.userName,
  format: 'json',
  typeOfRelation: 'confirmed',
};