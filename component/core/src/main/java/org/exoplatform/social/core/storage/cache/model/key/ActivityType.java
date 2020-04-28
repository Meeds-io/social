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

package org.exoplatform.social.core.storage.cache.model.key;

/**
 * Factoring activity cache.
 * 
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public enum ActivityType {
    USER, NEWER_USER, OLDER_USER, USER_FOR_UPGRADE,
    FEED, NEWER_FEED, OLDER_FEED, FEED_FOR_UPGRADE,
    CONNECTION, NEWER_CONNECTION, OLDER_CONNECTION, CONNECTION_FOR_UPGRADE,
    SPACE, NEWER_SPACE, OLDER_SPACE, SPACE_FOR_UPGRADE,
    SPACES, NEWER_SPACES, OLDER_SPACES, SPACES_FOR_UPGRADE,
    VIEWER, POSTER, NEWER_COMMENTS, OLDER_COMMENTS,
    COMMENTS, COMMENTS_AND_SUB_COMMENTS, SUB_COMMENTS
  }