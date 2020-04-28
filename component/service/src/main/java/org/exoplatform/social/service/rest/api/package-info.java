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
/**
 * Provides the classes necessary to handle a rest call, processes request and returns results 
 * by input type (json, xml). 
 * <p>
 * The rest entities that provided are: ActivityResoure, ActivityStreamResource, IdentityResource and VersionResource
 * These classes provide rest points to handle requests to create new. get, update or delete target object
 * such as activity, activity stream, identity and version.
 */
package org.exoplatform.social.service.rest.api;