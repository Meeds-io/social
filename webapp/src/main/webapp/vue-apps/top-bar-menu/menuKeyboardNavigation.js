/*
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2026 Meeds Association contact@meeds.io
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

/**
 * The keyboard walk of the top bar menu, shared by every level of it (EXO-88911).
 *
 * ONE cursor, and one only: the DOM focus. Vuetify's own menu cursor - its `listIndex` and the
 * `.v-list-item--highlighted` class it moves - is switched off by passing `disable-keys` to every
 * `v-menu` of the tree, which drops the `keydown` listener Vuetify binds on the activator.
 * Two cursors is what made the walk depth-dependent: the arrows moved Vuetify's highlight inside
 * the menu the *activator* belonged to while the focus stayed behind on that activator, so a
 * vertical arrow pressed after stepping into a submenu walked the level above instead of the one
 * on screen.
 *
 * Levels are read from the component tree, never from the DOM: menu contents are detached to
 * `[data-app]` (Vuetify's Detachable mixin, at mount here since the menus are `eager`), so DOM
 * nesting says nothing about menu depth - while `$refs.entries`, ordered against
 * `navigation.children`, is exactly the entries of the level a component opens, at any depth.
 *
 * A component using this mixin must provide:
 *  - `showMenu` (data)          : the model of the `v-menu` it opens,
 *  - `hasSubMenu` (computed)    : whether it opens one at all,
 *  - `$refs.entries`            : the `navigation-menu-sub-item` v-for of that menu,
 *  - `$refs.row` or `$refs.tab` : its own focusable element,
 * and must handle `walk` / `leave-level` emitted by its entries.
 */
export default {
  mounted() {
    // Vuetify already gives the detached content role="menu" (VMenu.genContent), but nothing
    // names it: entering a submenu is announced as a nameless "menu", so a screen reader user
    // is told nothing about which node it belongs to (EXO-88911, QA feedback). Name it after
    // the node that opens it. Set on the DOM rather than passed as a prop because the content
    // is Vuetify's own element, detached to [data-app] at mount.
    // Watched, not set once: the navigation tree is refetched on `space-settings-updated` and the
    // v-for is keyed on the node id, so a component is REUSED when a page is renamed or the
    // language changes - a name written only at mount would keep announcing the old label.
    this.$watch(() => this.navigation?.label, label => this.nameOpenedMenu(label), { immediate: true });
  },
  methods: {
    menuEntries() {
      // The walk must follow the order the menu displays, and a v-for `$refs` array is not
      // guaranteed to be in the source order (Vue 2), so the order comes from the data and the
      // components are matched to it. An entry with neither a page nor a submenu of its own
      // renders nothing (v-if), so it is not part of the walk even though its instance exists.
      const entries = this.$refs.entries || [];
      return (this.navigation?.children || [])
        .map(child => entries.find(entry => entry.navigation === child))
        .filter(entry => entry && (entry.hasPage || entry.hasSubMenu));
    },
    focusSelf() {
      // the focus goes on the row/tab itself, never on a nested child: it is the element carrying
      // the accessible name and aria-expanded, and the one the focus style is written for
      const element = this.$refs.row?.$el || this.$refs.tab?.$el;
      if (!element?.offsetParent) {
        return false;
      }
      element.focus();
      return true;
    },
    focusMenuEntry(index, attempt) {
      const entries = this.menuEntries();
      if (!entries.length) {
        return;
      }
      const entry = entries[(index % entries.length + entries.length) % entries.length];
      // only one branch stays open: entries the focus leaves behind close their own submenu,
      // whatever opened it - a previous key, or the pointer that hovered them
      entries.filter(other => other !== entry && other.showMenu)
        .forEach(other => other.closeSubMenu());
      if (!entry.focusSelf() && (attempt || 0) < 10) {
        // the menu is rendered eagerly but is only focusable once displayed, and it is displayed
        // one transition after showMenu flipped: retry briefly instead of guessing a delay
        window.setTimeout(() => this.focusMenuEntry(index, (attempt || 0) + 1), 30);
      }
    },
    closeSubMenu() {
      this.showMenu = false;
    },
    nameOpenedMenu(label) {
      const content = this.$refs.menu?.$refs?.content;
      if (content && label) {
        content.setAttribute('aria-label', label);
      }
    },
    walkFromEntry(entry, step) {
      // vertical walk inside the level THIS component opens: the entry asks its own level to move,
      // so the step is always applied to the list the focused entry belongs to, whatever its depth
      const entries = this.menuEntries();
      const current = entries.indexOf(entry);
      if (current < 0) {
        return;
      }
      this.focusMenuEntry(current + step);
    },
    walkNext() {
      this.$emit('walk', this, 1);
    },
    walkPrevious() {
      this.$emit('walk', this, -1);
    },
    // the horizontal arrows step in and out of a submenu, flipped in RTL
    onForwardKey() {
      return this.$vuetify.rtl ? this.walkBack() : this.walkDeeper();
    },
    onBackKey() {
      return this.$vuetify.rtl ? this.walkDeeper() : this.walkBack();
    },
    walkDeeper() {
      this.openMenuOn(0);
    },
    walkDeeperFromEnd() {
      this.openMenuOn(-1);
    },
    openMenuOn(index) {
      if (!this.hasSubMenu) {
        return;
      }
      this.showMenu = true;
      this.focusMenuEntry(index);
    },
    walkBack() {
      // close the submenu this entry may have opened, then let the level above take the focus back
      this.showMenu = false;
      this.$emit('leave-level');
    },
    onEnterKey(event) {
      if (this.hasPage) {
        return; // the row is a link: let the browser follow it
      }
      // a node with no page of its own has nothing to open but its submenu
      event.preventDefault();
      this.walkDeeper();
    },
    checkChildrenHasPage(navigation) {
      let childrenHasPage = false;
      navigation.children.forEach(child => {
        if (childrenHasPage === true) {
          return;
        }
        if (child.pageKey) {
          childrenHasPage = true;
        } else if (child.children.length > 0) {
          childrenHasPage = this.checkChildrenHasPage(child);
        } else {
          childrenHasPage = false;
        }
      });
      return childrenHasPage;
    },
  },
};
