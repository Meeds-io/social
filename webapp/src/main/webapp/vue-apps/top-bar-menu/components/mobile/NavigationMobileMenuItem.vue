<!--
   * This file is part of the Meeds project (https://meeds.io/).
   *
   * Copyright (C) 2023 Meeds Association
   * contact@meeds.io
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
-->

<template>
  <v-menu
    v-model="showMenu"
    rounded
    offset-y>
    <template #activator="{ on }">
      <v-tab
        ref="tab"
        class="mx-auto pa-1 text-break navigation-mobile-menu-tab"
        :href="navigationNodeUri"
        :target="navigationNodeTarget"
        :rel="navigationNodeRel"
        :disabled="!hasPage && !hasChildren"
        :link="hasPage"
        :aria-label="navigation.label"
        :aria-current="isSelected && 'page' || holdsSelectedEntry && 'true' || null"
        @click="checkLink"
        @keydown="onTabKeydown"
        @change="updateNavigationState(navigation.uri)">
        <span
          class="text-truncate-2 pt-2">
          {{ navigation.label }}
        </span>
        <v-btn
          v-if="hasPage && hasChildren"
          ref="chevron"
          v-on="hasChildren && on"
          class="mt-2"
          icon
          :aria-label="$t('topBar.navigation.menu.openSubMenu', [navigation.label])"
          :aria-expanded="String(!!showMenu)"
          aria-haspopup="menu"
          @click.stop.prevent="openDropMenu">
          <v-icon size="20" aria-hidden="true">
            fa-angle-up
          </v-icon>
        </v-btn>
        <v-icon
          class="pa-3 mt-2"
          v-else-if="hasChildren"
          size="20"
          aria-hidden="true">
          fa-angle-up
        </v-icon>
      </v-tab>
    </template>
    <navigation-mobile-menu-sub-item
      v-if="hasChildren"
      class="transparent"
      :navigation="navigation.children"
      :base-site-uri="baseSiteUri"
      :show-menu="showMenu"
      :parent-navigation-uri="navigation.uri"
      :selected-path="selectedPath"
      @close="closeMenu"
      @update-navigation-state="updateNavigationState" />
  </v-menu>
</template>

<script>
import tabLinkSemantics from '../tabLinkSemantics.js';

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
  data: () => ({
    showMenu: false,
  }),
  created() {
    document.addEventListener('click', this.handleCloseMenu);
    this.$root.$on('close-sibling-drop-menus', this.handleCloseSiblingMenus);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleCloseMenu);
    this.$root.$off('close-sibling-drop-menus', this.handleCloseSiblingMenus);
  },
  computed: {
    isSelected() {
      return this.selectedPath === this.navigationNodeUri;
    },
    // the entry one of whose menu pages is the current one is announced as current as well
    holdsSelectedEntry() {
      return !this.isSelected && !!this.navigation?.children?.some?.(child => this.holdsSelectedPath(child));
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
  },
  methods: {
    holdsSelectedPath(navigation) {
      return this.$navigationUtils.getNavigationNodeUri(this.baseSiteUri, navigation) === this.selectedPath
        || !!navigation.children?.some?.(child => this.holdsSelectedPath(child));
    },
    updateNavigationState(value) {
      this.$emit('update-navigation-state', `${this.baseSiteUri}${value}`);
    },
    // an entry that only opens a menu is a button: Space opens it as Enter does
    onTabKeydown(event) {
      if (event.key === ' ' && !this.hasPage && this.hasChildren && this.checkChildrenHasPage(this.navigation)) {
        event.preventDefault();
        this.openDropMenu(true, event);
      }
    },
    // Escape in the sheet closes it: the focus goes back to what opened it
    closeMenu() {
      this.showMenu = false;
      // once the sheet is closed: while open, it keeps the focus inside it
      this.$nextTick(() => (this.hasPage && this.$refs.chevron?.$el || this.$refs.tab?.$el)?.focus?.());
    },
    checkLink(e) {
      if (!this.navigationNodeUri) {
        e?.stopPropagation?.();
        e?.preventDefault?.();
      }
      if (this.navigationNodeUri?.includes?.('#')) {
        if (this.navigationNodeTarget === '_blank') {
          window.open(this.navigationNodeUri);
        } else {
          window.location.href = this.navigationNodeUri;
        }
      } else if (this.hasChildren && this.checkChildrenHasPage(this.navigation)) {
        this.openDropMenu(false, e);
      }
    },
    openDropMenu(persist, event) {
      if (!persist && this.showMenu) {
        this.showMenu = false;
      } else if (!this.showMenu) {
        event?.stopPropagation?.();
        event?.preventDefault?.();
        this.$root.$emit('close-sibling-drop-menus', this);
        this.$nextTick().then(() => {
          this.showMenu = true;
        });
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