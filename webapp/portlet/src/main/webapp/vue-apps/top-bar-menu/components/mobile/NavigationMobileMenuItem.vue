<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
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
-->
<template>
  <v-menu
    v-model="showMenu"
    rounded
    offset-y>
    <template #activator="{ attrs, on }">
      <v-tab
        class="mx-auto text-caption text-break navigation-mobile-menu-tab"
        v-bind="attrs"
        :href="`${baseSiteUri}${navigation.uri}`"
        :disabled="!navigation.pageKey && !navigation.children?.length"
        :link="!!navigation.pageKey"
        @click.stop="checkLink(navigation, $event)"
        @change="updateNavigationState(navigation.uri)">
        <span
          class="text-truncate-3 pt-2">
          {{ navigation.label }}
        </span>
        <v-btn
          v-if="navigation.children?.length && navigation.pageKey"
          v-on="navigation.children?.length && on"
          class="mt-2"
          icon
          @click.stop.prevent="openDropMenu">
          <v-icon size="20">
            fa-angle-up
          </v-icon>
        </v-btn>
        <v-icon
          class="pa-3 mt-2"
          v-else-if="navigation.children?.length"
          size="20">
          fa-angle-up
        </v-icon>
      </v-tab>
    </template>
    <navigation-mobile-menu-sub-item
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
    },
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  created() {
    document.addEventListener('click', () => {
      if (this.showMenu) {
        setTimeout(() => {
          this.showMenu = false;
        },100);
      }
    });
    this.$root.$on('close-other-drop-menus', (emitter) => {
      if (this !== emitter && this.showMenu) {
        this.showMenu = false;
      }
    });
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
        this.$root.$emit('close-other-drop-menus', this);
        this.$nextTick().then(() => {
          this.showMenu = true;
        });
      }
    }
  }
};
</script>