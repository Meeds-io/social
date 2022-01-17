/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2022 Meeds Association
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
package org.exoplatform.social.common;

/**
 * @since 6.3.0
 * @deprecated Deprecated from 6.3.0. Replaced by a new Exception org.exoplatform.commons.ObjectAlreadyExistsException
 *             will be removed at 6.3.0
 */
@Deprecated(since="6.3.0", forRemoval=true)
public class ObjectAlreadyExistsException extends org.exoplatform.commons.ObjectAlreadyExistsException {

    public ObjectAlreadyExistsException(Object existingObject) {
        super(existingObject);
    }

    public ObjectAlreadyExistsException(Object existingObject, String message) {
        super(existingObject, message);
    }

    public ObjectAlreadyExistsException(Object existingObject, String message, Throwable e) {
        super(existingObject, message, e);
    }
}
