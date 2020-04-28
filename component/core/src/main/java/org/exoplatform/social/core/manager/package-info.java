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
 * Provides classes that are necessary to manage all related entities on Social and all services that manage
 * each type in both separating and collaborating between them.
 *
 * Entities in Social which need to be managed are: Identity, Activity and Relationship.
 * 
 * <ul>
 * <li>The Activity contains all information regarding to people, spaces and applications activities.</li>
 * <li>The Identity contains all profile information of users and spaces.</li>
 * <li>The Relationship contains all information regarding to relationship between users and users, users and spaces.</li>
 * </ul>
 */
package org.exoplatform.social.core.manager;