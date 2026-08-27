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

// Shared keyboard behaviour for the submenu-toggle arrows of the sidebar
// (SidebarListItem.vue and SpaceNavigationItem.vue). Kept in one place so the
// focus logic can't drift between the two components.
//
// Components using this mixin must provide:
//   - a `drawerOpened` computed (is this arrow's panel currently open)
//   - an `activateArrow()` method (open/toggle the panel this arrow controls)
export default {
  methods: {
    // Enter/Space: toggle the panel directly rather than through the
    // ripple-hover-button's mouse-oriented click, which debounces repeat
    // activations for 500ms and would swallow a quick "Enter to close".
    // preventDefault stops the button's own click; stopPropagation stops the
    // parent v-list-item's Vuetify keydown->click from toggling a second time.
    // Once opened, focus is moved into the panel so the user lands on its first
    // control (and stays trapped there until Esc, handled in Sidebar.vue).
    // Tab is intentionally NOT hijacked: it must keep flowing to the next item
    // below the arrow in the list; entering the panel is done with Enter.
    onArrowKeydown(event) {
      if (event.key === 'Enter' || event.key === ' ' || event.key === 'Spacebar') {
        event.preventDefault();
        event.stopPropagation();
        this.activateArrow();
        this.$nextTick(() => this.focusOpenedPanel());
      }
    },
    // Moves focus to the first focusable element of the opened panel. The third
    // level is queried first so that, when both panels are open, focus prefers
    // the deeper one (a selector list would otherwise resolve in DOM order).
    // Returns true when a panel to focus exists (immediately or after the open
    // animation), false when there is nothing to focus.
    focusOpenedPanel(attempt) {
      attempt = attempt || 0;
      const panel = document.querySelector('.HamburgerMenuThirdLevelParent.v-navigation-drawer--open')
        || document.querySelector('.HamburgerMenuSecondLevelParent.v-navigation-drawer--open');
      const target = panel && (panel.querySelector('a[href], button:not([disabled]), [tabindex]:not([tabindex="-1"])') || panel);
      if (!target) {
        if (attempt < 30) {
          // The drawer may still be animating open; retry on the next frame.
          window.requestAnimationFrame(() => this.focusOpenedPanel(attempt + 1));
          return true;
        }
        return false;
      }
      target.focus();
      if (document.activeElement !== target && attempt < 30) {
        window.requestAnimationFrame(() => this.focusOpenedPanel(attempt + 1));
      }
      return true;
    },
    // Remember which element opened a panel so Esc can restore focus to it.
    // Only stores a real focus origin: on the mouse path activeElement is
    // <body>, and restoring focus to <body> would blur instead of returning to
    // the arrow.
    storeFocusOrigin(prop) {
      const active = document.activeElement;
      if (active && active !== document.body) {
        this.$root[prop] = active;
      }
    },
  },
};
