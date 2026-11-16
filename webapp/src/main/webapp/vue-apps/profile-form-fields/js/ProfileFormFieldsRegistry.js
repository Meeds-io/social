/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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

// The fields component a host renders for each context it hands it: the
// extension's validate() reaches the component through that context, which
// the host keeps, while the component is rebuilt at each opening of the form
const components = new WeakMap();

// The outcome of the list's load for each context, which the extension's
// execute() reads once the account exists, the component being gone when the
// form has closed meanwhile
const loadStates = new WeakMap();

export function register(context, component) {
  if (context) {
    components.set(context, component);
  }
}

export function unregister(context, component) {
  if (context && components.get(context) === component) {
    components.delete(context);
  }
}

export function get(context) {
  return context && components.get(context) || null;
}

export function setLoadState(context, state) {
  if (context && state) {
    loadStates.set(context, state);
  }
}

export function getLoadState(context) {
  return context && loadStates.get(context) || null;
}
