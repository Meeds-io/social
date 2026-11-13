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

// Keyboard model of a menu opened from a button. Stateless: each component keeps its own state.
// The component references its list as `menuList`, its button as `menuActivator`, implements
// `closeKeyboardMenu()`, calls `focusFirstMenuEntry()` when its menu opens and binds
// `onMenuEntriesKeydown` on the list, with `.native` on a Vuetify `v-list`, which does not pass
// its key events through. Vuetify's menu only highlights an entry and keeps the focus on its
// button, so a screen reader announces none of the entries without this.
const FOCUSABLE_SELECTOR = 'button, a[href], [tabindex="0"]';
const ENTRY_SELECTOR = '[role="menuitem"], [role="menuitemcheckbox"], [role="menuitemradio"]';

function elementOf(ref) {
  return ref?.$el || ref;
}

export default {
  methods: {
    menuEntries() {
      const list = elementOf(this.$refs.menuList);
      return [...(list?.querySelectorAll?.(ENTRY_SELECTOR) || [])]
        .filter(entry => entry.getClientRects().length && !entry.hasAttribute('disabled'));
    },
    focusFirstMenuEntry() {
      // the menu's content is rendered on the next tick and shown by a transition
      this.$nextTick(() => window.setTimeout(() => {
        const entries = this.menuEntries();
        (entries.find(entry => entry.getAttribute('aria-checked') === 'true') || entries[0])?.focus();
      }, 100));
    },
    onMenuEntriesKeydown(event) {
      const entries = this.menuEntries();
      const index = entries.indexOf(document.activeElement);
      let next = null;
      if (event.key === 'ArrowDown') {
        next = entries[(index + 1) % entries.length];
      } else if (event.key === 'ArrowUp') {
        next = entries[(index - 1 + entries.length) % entries.length];
      } else if (event.key === 'Home') {
        next = entries[0];
      } else if (event.key === 'End') {
        next = entries[entries.length - 1];
      } else if (event.key === 'Escape' || event.key === 'Tab') {
        this.closeMenuToActivator(event);
        return;
      } else {
        return;
      }
      // stopped here, else Vuetify's own handler would also move its highlight
      event.preventDefault();
      event.stopPropagation();
      next?.focus();
    },
    closeMenuToActivator(event) {
      event?.preventDefault?.();
      event?.stopPropagation?.();
      this.closeKeyboardMenu();
      this.$nextTick(() => {
        // the button may be a component wrapping the focusable element
        const activator = elementOf(this.$refs.menuActivator);
        (activator?.matches?.(FOCUSABLE_SELECTOR) ? activator : activator?.querySelector?.(FOCUSABLE_SELECTOR))?.focus?.();
      });
    },
  },
};
