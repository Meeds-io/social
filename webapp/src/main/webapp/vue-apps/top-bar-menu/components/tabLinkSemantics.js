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

/**
 * A page navigation entry is a link, and the entry that only opens a menu is a button:
 * Vuetify's v-tab sets role="tab" and aria-selected on its own element at each render,
 * after the attributes given by the template, so they are corrected once rendered.
 */
export default {
  mounted() {
    this.refreshTabSemantics();
  },
  updated() {
    this.refreshTabSemantics();
  },
  methods: {
    refreshTabSemantics() {
      const tab = this.$refs.tab?.$el;
      if (!tab) {
        return;
      }
      tab.removeAttribute('aria-selected');
      if (this.hasPage || !this.hasChildren) {
        tab.removeAttribute('role');
      } else {
        tab.setAttribute('role', 'button');
        tab.setAttribute('aria-haspopup', 'menu');
        tab.setAttribute('aria-expanded', String(!!this.showMenu));
      }
    },
  },
};
