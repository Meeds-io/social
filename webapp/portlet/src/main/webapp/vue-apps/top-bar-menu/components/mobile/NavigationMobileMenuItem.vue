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
    <template #activator="{ attrs, on }">
      <v-tab
        class="mx-auto text-caption pa-1 text-break navigation-mobile-menu-tab"
        v-bind="attrs"
        :href="`${baseSiteUri}${navigation.uri}`"
        :disabled="!hasPage && !hasChildren"
        :link="hasPage"
        :aria-label="$t('topBar.navigation.menu.openMenu')"
        role="tab"
        @click.stop="checkLink(navigation, $event)"
        @change="updateNavigationState(navigation.uri)">
        <span
          class="text-truncate-3 pt-2">
          {{ navigation.label }}
        </span>
        <v-btn
          v-if="hasPage && hasChildren"
          v-on="hasChildren && on"
          class="mt-2"
          icon
          @click.stop.prevent="openDropMenu">
          <v-icon size="20">
            fa-angle-up
          </v-icon>
        </v-btn>
        <v-icon
          class="pa-3 mt-2"
          v-else-if="hasChildren"
          size="20">
          fa-angle-up
        </v-icon>
      </v-tab>
    </template>
    <navigation-mobile-menu-sub-item
      v-if="hasChildren"
      :navigation="navigation.children"
      :base-site-uri="baseSiteUri"
      :show-menu="showMenu"
      :parent-navigation-uri="navigation.uri"
      @update-navigation-state="updateNavigationState" />
  </v-menu>
</template>

<script>
export default {
  data () {
    return {
      showMenu: false,
    };
  },
  props: {
    navigation: {
      type: Object,
      default: null,
    },
    baseSiteUri: {
      type: String,
      default: null
    }
  },
  created() {
    document.addEventListener('click', this.handleCloseMenu);
    this.$root.$on('close-sibling-drop-menus', this.handleCloseSiblingMenus);
  },
  computed: {
    hasChildren() {
      return this.navigation?.children?.length;
    },
    hasPage() {
      return !!this.navigation?.pageKey;
    }
  },
  methods: {
    updateNavigationState(value) {
      this.$emit('update-navigation-state', `${this.baseSiteUri}${value}`);
    },
    checkLink(navigation, e) {
      if (!navigation.pageKey) {
        e.preventDefault();
      }
      if (navigation.children) {
        this.openDropMenu();
      }
    },
    openDropMenu(persist) {
      if (!persist && this.showMenu) {
        this.showMenu = false;
      } else if (!this.showMenu) {
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
    }
  }
};
</script>