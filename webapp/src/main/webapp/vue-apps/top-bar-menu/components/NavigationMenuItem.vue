<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-menu
    ref="menu"
    v-model="showMenu"
    rounded
    content-class="topBar-navigation-drop-menu"
    :left="$vuetify.rtl"
    :open-on-hover="isOpenedOnHover"
    :max-height="menuMaxHeight"
    bottom
    offset-y
    eager>
    <template #activator="{ on }">
      <v-tab
        ref="tab"
        v-if="hasPage || hasChildren && childrenHasPage"
        :href="navigationNodeUri"
        :target="navigationNodeTarget"
        :rel="navigationNodeRel"
        :link="hasPage"
        :aria-label="navigation.label"
        :class="`mx-auto text-break ${notClickable} ${navigation.tabClass || ''}`"
        :value="navigationNodeUri"
        v-on="on"
        :aria-current="isSelected ? 'page' : null"
        @click="checkLink"
        @keydown="onTabKeydown"
        @change="onTabChange">
        <span
          class="text-truncate-3">
          {{ navigation.label }}
        </span>
        <v-icon
          v-if="navigation.target === 'NEW_TAB'"
          size="12"
          aria-hidden="true"
          class="mx-1">
          fa-external-link-alt
        </v-icon>
        <v-btn
          v-if="hasChildren && childrenHasPage"
          icon
          :aria-label="$t('topBar.navigation.menu.openSubMenu', [navigation.label])"
          :aria-hidden="hasPage ? null : 'true'"
          :tabindex="hasPage ? null : -1"
          :aria-expanded="String(!!showMenu)"
          aria-haspopup="menu"
          @click.stop.prevent="openDropMenu"
          @mouseover="showMenu = true">
          <v-icon size="20" aria-hidden="true">
            fa-angle-down
          </v-icon>
        </v-btn>
      </v-tab>
    </template>
    <div @keydown="onMenuKeydown">
      <navigation-menu-sub-item
        v-for="children in navigation.children"
        class="transparent"
        :key="children.id"
        :navigation="children"
        :base-site-uri="baseSiteUri"
        :parent-navigation-uri="navigation.uri"
        :selected-path="selectedPath"
        @update-navigation-state="updateNavigationState"
        @select="updateNavigationState" />
    </div>
  </v-menu>
</template>

<script>
import tabLinkSemantics from './tabLinkSemantics.js';

export default {
  mixins: [tabLinkSemantics],
  props: {
    navigation: {
      type: Object,
      default: null,
    },
    baseSiteUri: {
      type: String,
      default: null
    },
    selectedPath: {
      type: String,
      default: null
    },
  },
  data () {
    return {
      showMenu: false,
      isOpenedOnHover: true,
      menuMaxHeight: '100vh'
    };
  },
  computed: {
    isSelected() {
      return this.selectedPath === this.navigationNodeUri;
    },
    notClickable() {
      return `${this.hasPage ? ' ' : ' not-clickable ' }`;
    },
    hasChildren() {
      return this.navigation?.children?.length;
    },
    hasPage() {
      return !!this.navigation?.pageKey;
    },
    navigationNodeUri() {
      return this.$navigationUtils.getNavigationNodeUri(this.baseSiteUri, this.navigation);
    },
    navigationNodeTarget() {
      return this.$navigationUtils.getNavigationNodeTarget(this.navigation);
    },
    navigationNodeRel() {
      return this.$navigationUtils.getNavigationNodeRel(this.navigation);
    },
    childrenHasPage() {
      return this.checkChildrenHasPage(this.navigation);
    },
  },
  watch: {
    showMenu() {
      this.isOpenedOnHover = !this.showMenu;
      this.$root.$emit('close-sibling-drop-menus', this); 
    },
    isSelected: {
      immediate: true,
      handler() {
        if (this.isSelected) {
          this.updateNavigationState();
        }
      }
    },
  },
  created() {
    document.addEventListener('click', this.handleCloseMenu);
    this.$root.$on('close-sibling-drop-menus', this.handleCloseSiblingMenus);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleCloseMenu);
    this.$root.$off('close-sibling-drop-menus', this.handleCloseSiblingMenus);
  },
  methods: {
    updateNavigationState() {
      this.$emit('update-navigation-state', this.navigationNodeUri);
    },
    // The tabs group selects the tab clicked; an entry that only opens a menu is not the current page
    onTabChange() {
      if (this.hasPage) {
        this.updateNavigationState();
      } else {
        this.$emit('restore-navigation-state');
      }
    },
    // The menu only highlights an entry on the arrow keys: the keyboard focus is moved into it
    onTabKeydown(event) {
      if (!this.hasChildren || !this.childrenHasPage || (event.key !== 'ArrowDown' && event.key !== 'ArrowUp')) {
        return;
      }
      event.preventDefault();
      this.openDropMenu(true);
      this.focusMenuEntry(event.key === 'ArrowUp' ? -1 : 0);
    },
    onMenuKeydown(event) {
      const entries = this.menuEntries();
      const index = entries.indexOf(document.activeElement);
      if (index < 0) {
        return;
      }
      const target = {
        ArrowDown: (index + 1) % entries.length,
        ArrowUp: (index - 1 + entries.length) % entries.length,
        Home: 0,
        End: entries.length - 1,
      }[event.key];
      if (typeof target === 'number') {
        event.preventDefault();
        event.stopPropagation();
        this.focusEntry(entries[target]);
      } else if (event.key === 'Escape' || event.key === 'Tab') {
        // the menu content is rendered apart from the tabs: the focus goes back to its tab, Tab moving on from there
        this.showMenu = false;
        this.$refs.tab?.$el?.focus?.();
      }
    },
    // The menu opens on Enter the entry it highlights: it highlights the entry that has the keyboard focus
    focusEntry(entry) {
      if (!entry) {
        return;
      }
      entry.focus();
      const menu = this.$refs.menu;
      if (menu?.getTiles) {
        menu.getTiles();
        menu.listIndex = menu.tiles.indexOf(entry);
      }
    },
    menuEntries() {
      const content = this.$refs.menu?.$refs?.content;
      return content ? [...content.querySelectorAll('a.v-list-item')] : [];
    },
    focusMenuEntry(index) {
      // once the menu content is displayed
      window.setTimeout(() => {
        const entries = this.menuEntries();
        this.focusEntry(entries[index < 0 ? entries.length - 1 : index]);
      }, 50);
    },
    checkLink(e) {
      if (!this.navigationNodeUri) {
        e.stopPropagation();
        e.preventDefault();
      }
      if (this.navigationNodeUri?.includes?.('#')) {
        if (this.navigationNodeTarget === '_blank') {
          window.open(this.navigationNodeUri);
        } else {
          window.location.href = this.navigationNodeUri;
        }
      } else if (this.hasChildren && this.childrenHasPage) {
        const opening = !this.showMenu;
        this.openDropMenu();
        // opened from the keyboard (Enter or Space): its first entry takes the focus
        if (opening && e?.detail === 0) {
          this.focusMenuEntry(0);
        }
      }
    },
    openDropMenu(persist) {
      if (!persist && this.showMenu) {
        this.showMenu = false;
      } else if (!this.showMenu) {
        this.showMenu = true;
        this.$root.$emit('close-sibling-drop-menus', this);
      }
    },
    handleCloseSiblingMenus(emitter) {
      if (this !== emitter && this.showMenu) {
        this.showMenu = false;
      }
    },
    handleCloseMenu() {
      if (this.showMenu) {
        setTimeout(() => {
          this.showMenu = false;
        }, 100);
      }
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
  }
};
</script>
