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
    content-class="topBar-navigation-drop-menu"
    :left="$vuetify.rtl"
    :open-on-hover="isOpenedOnHover"
    bottom
    offset-y>
    <template #activator="{ on, attrs }">
      <v-tab
        v-if="hasPage || hasChildren && childrenHasPage"
        :class="`mx-auto text-caption text-break ${extraClass} ${notClickable}`"
        v-on="on"
        v-bind="attrs"
        :href="navigationNodeUri"
        :target="navigationNodeTarget"
        :link="hasPage"
        :aria-label="$t('topBar.navigation.menu.openMenu')"
        role="tab"
        @click.stop="checkLink(navigation, $event)"
        @click="openUrl(navigationNodeUri, navigationNodeTarget)"
        @change="updateNavigationState(navigation.uri)">
        <span
          class="text-truncate-3">
          {{ navigation.label }}
        </span>
        <v-btn
          v-if="hasChildren && childrenHasPage"
          icon
          @click.stop.prevent="openDropMenu"
          @mouseover="showMenu = true">
          <v-icon size="20">
            fa-angle-down
          </v-icon>
        </v-btn>
      </v-tab>
    </template>
    <navigation-menu-sub-item
      v-for="children in navigation.children"
      :key="children.id"
      :navigation="children"
      :base-site-uri="baseSiteUri"
      :parent-navigation-uri="navigation.uri"
      @update-navigation-state="updateNavigationState" />
  </v-menu>
</template>

<script>
export default {
  data () {
    return {
      showMenu: false,
      isOpenedOnHover: true,
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
  watch: {
    showMenu() {
      this.isOpenedOnHover = !this.showMenu;
      this.$root.$emit('close-sibling-drop-menus', this); 
    }
  },
  created() {
    document.addEventListener('click', this.handleCloseMenu);
    this.$root.$on('close-sibling-drop-menus', this.handleCloseSiblingMenus);
  },
  computed: {
    notClickable() {
      return `${this.hasPage ? ' ' : ' not-clickable ' }`;
    },
    extraClass() {
      return `${this.showMenu ? ' light-grey-background ' : ' ' }`;
    },
    hasChildren() {
      return this.navigation?.children?.length;
    },
    hasPage() {
      return !!this.navigation?.pageKey;
    },
    navigationNodeUri() {
      return this.navigation?.pageLink || `${this.baseSiteUri}${this.navigation.uri}`;
    },
    navigationNodeTarget() {
      return this.navigation?.target === 'SAME_TAB' && '_self' || '_blank';
    },
    childrenHasPage() {
      return this.checkChildrenHasPage(this.navigation);
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
    openUrl(url, target) {
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      } else if (url.match(/^(\/portal\/)/)) {
        url = `${window.location.origin}${url}`;
      }
      window.open(url, target);
    },
  }
};
</script>