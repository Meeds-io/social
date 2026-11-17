/*
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

// Light scopes: an element carrying data-display-mode="light" keeps the light display inside a dark page (an
// application not adapted to dark). Vuetify 2 moves the content of a menu, a dialog, a tooltip or a bottom sheet out
// of the application, into [data-app], at its first opening (the Detachable mixin's initDetach): the moved content
// carries the scope with it. To re-port at the next Vuetify upgrade.

// the component names Vuetify gives the detachable components
const DETACHABLE_COMPONENTS = ['v-menu', 'v-dialog', 'v-tooltip', 'v-bottom-sheet'];

const LIGHT_SCOPE = '[data-display-mode="light"]:not(html)';

/**
 * @param {object} vm a Vue component instance
 * @returns {boolean} true when the component lies in a light scope, or belongs to a light-only application
 */
export function isInLightScope(vm) {
  return !!(vm?.$root?.lightOnly || vm?.$el?.closest?.(LIGHT_SCOPE));
}

/**
 * Wraps the detach of Vuetify's detachable components so that a content detached from a light scope keeps it. Outside
 * a light scope nothing changes. Called once, after Vue.use(Vuetify), which registers the detachables: each detachable's
 * `methods` object is replaced by a copy holding the wrapper. Vue re-resolves a component constructor's options when a
 * global mixin is added after the constructor was built, and keeps what differs by reference from the options it
 * sealed: a replaced `methods` object survives, an in-place patch of initDetach does not.
 *
 * @param {object} Vue the Vue constructor
 * @returns {void}
 */
export function wrapDetachables(Vue) {
  // found by the name Vuetify gives the component: it registers them under their PascalCase key (VMenu…), and a
  // global mixin added after the registration moves the registered components to the prototype of
  // Vue.options.components, hence the for...in
  for (const key in Vue.options.components) { // eslint-disable-line guard-for-in
    const options = Vue.options.components[key]?.options;
    const initDetach = DETACHABLE_COMPONENTS.includes(options?.name) && options.methods?.initDetach;
    if (initDetach && !initDetach.lightScopeWrapper) {
      options.methods = Object.assign({}, options.methods, {
        initDetach: wrapInitDetach(initDetach),
      });
    }
  }
}

function wrapInitDetach(initDetach) {
  const wrapper = function(...args) {
    // the closest test at each detach: light scopes appear at mount and at run time
    const lightScope = isInLightScope(this);
    const result = initDetach.apply(this, args);
    if (lightScope && this.hasDetached && this.$refs.content?.setAttribute) {
      this.$refs.content.setAttribute('data-display-mode', 'light');
    }
    return result;
  };
  wrapper.lightScopeWrapper = true;
  return wrapper;
}
