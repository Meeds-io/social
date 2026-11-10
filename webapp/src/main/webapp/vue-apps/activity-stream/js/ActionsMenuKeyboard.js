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

// Keyboard model of an activity or comment "…" menu: a `menu` toggle, a v-list
// referenced as `menuList` whose entries have role="menuitem", and, for a group
// entry, a `data-menu-action` attribute naming the sub-menu referenced as `group-<id>`.
export default {
  watch: {
    menu(opened) {
      if (opened) {
        // Vuetify only highlights an entry and keeps the focus on the button:
        // the focus goes to the first entry so that it is announced
        this.$nextTick(() => window.setTimeout(() => this.menuItems()[0]?.focus(), 100));
      }
    },
  },
  methods: {
    menuItems() {
      return [...(this.$refs.menuList?.$el?.querySelectorAll('[role="menuitem"]') || [])];
    },
    onMenuKeydown(event) {
      const items = this.menuItems();
      const index = items.indexOf(document.activeElement);
      let next = null;
      if (event.key === 'ArrowDown') {
        next = items[(index + 1) % items.length];
      } else if (event.key === 'ArrowUp') {
        next = items[(index - 1 + items.length) % items.length];
      } else if (event.key === 'Home') {
        next = items[0];
      } else if (event.key === 'End') {
        next = items[items.length - 1];
      } else {
        this.openGroupOnArrow(event);
        return;
      }
      // stopped here, else the menu's own handler would also move its highlight
      // and click the highlighted entry on Enter
      event.preventDefault();
      event.stopPropagation();
      next?.focus();
    },
    openGroupOnArrow(event) {
      // a group entry opens its sub-menu with the arrow pointing to the side where it unfolds
      if (!this.menu || event.key !== (this.$vuetify.rtl ? 'ArrowLeft' : 'ArrowRight')) {
        return;
      }
      const tile = event.target?.closest?.('[role="menuitem"][aria-haspopup="menu"]')
        || this.$el.querySelector('.v-list-item--highlighted[aria-haspopup="menu"]');
      const groupMenu = tile && this.$refs[`group-${tile.dataset.menuAction}`]?.[0];
      if (groupMenu) {
        event.preventDefault();
        tile.click();
        this.$nextTick(() => groupMenu.$refs?.content?.querySelector('.v-list-item')?.focus());
      }
    },
  },
};
