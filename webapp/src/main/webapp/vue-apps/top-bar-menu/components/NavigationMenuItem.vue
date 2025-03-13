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
    v-model="showMenu"
    bottom
    :content-class="`topBar-navigation-drop-menu ${isTopBarElement && 'layout-top-bar' || ''}`"
    eager
    :left="$vuetify.rtl"
    offset-y
    :open-on-hover="isOpenedOnHover"
    rounded>
    <template #activator="{ on, attrs }">
      <v-tab
        v-if="hasPage || hasChildren && childrenHasPage"
        :aria-label="navigation.label"
        :class="`mx-auto text-break ${notClickable}`"
        :href="navigationNodeUri"
        :link="hasPage"
        :rel="navigationNodeRel"
        v-bind="attrs"
        role="tab"
        :target="navigationNodeTarget"
        :value="navigationNodeUri"
        v-on="on"
        @change="updateNavigationState"
        @click="checkLink">
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
      :base-site-uri="baseSiteUri"
      class="transparent"
      :navigation="children"
      :parent-navigation-uri="navigation.uri"
      :selected-path="selectedPath"
      @select="updateNavigationState"
      @update-navigation-state="updateNavigationState" />
  </v-menu>
</template>

<script>
  export default {
    props: {
      navigation: {
        type: Object,
        default: null,
      },
      baseSiteUri: {
        type: String,
        default: null,
      },
      selectedPath: {
        type: String,
        default: null,
      },
    },
    data () {
      return {
        showMenu: false,
        isOpenedOnHover: true,
      };
    },
    computed: {
      isSelected () {
        return this.selectedPath === this.navigationNodeUri;
      },
      notClickable () {
        return `${this.hasPage ? ' ' : ' not-clickable ' }`;
      },
      hasChildren () {
        return this.navigation?.children?.length;
      },
      hasPage () {
        return !!this.navigation?.pageKey;
      },
      navigationNodeUri () {
        return eXo.$navigationUtils.getNavigationNodeUri(this.baseSiteUri, this.navigation);
      },
      navigationNodeTarget () {
        return eXo.$navigationUtils.getNavigationNodeTarget(this.navigation);
      },
      navigationNodeRel () {
        return eXo.$navigationUtils.getNavigationNodeRel(this.navigation);
      },
      childrenHasPage () {
        return this.checkChildrenHasPage(this.navigation);
      },
      isTopBarElement () {
        return this.$root.isTopBarElement;
      },
    },
    watch: {
      showMenu () {
        this.isOpenedOnHover = !this.showMenu;
        this.$root.$emit('close-sibling-drop-menus', this); 
      },
      isSelected: {
        immediate: true,
        handler () {
          if (this.isSelected) {
            this.updateNavigationState();
          }
        },
      },
    },
    created () {
      document.addEventListener('click', this.handleCloseMenu);
      this.$root.$on('close-sibling-drop-menus', this.handleCloseSiblingMenus);
    },
    methods: {
      updateNavigationState () {
        this.$emit('update-navigation-state', this.navigationNodeUri);
      },
      checkLink (e) {
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
          this.openDropMenu();
        }
      },
      openDropMenu (persist) {
        if (!persist && this.showMenu) {
          this.showMenu = false;
        } else if (!this.showMenu) {
          this.showMenu = true;
          this.$root.$emit('close-sibling-drop-menus', this);
        }
      },
      handleCloseSiblingMenus (emitter) {
        if (this !== emitter && this.showMenu) {
          this.showMenu = false;
        }
      },
      handleCloseMenu () {
        if (this.showMenu) {
          setTimeout(() => {
            this.showMenu = false;
          }, 100);
        }
      },
      checkChildrenHasPage (navigation) {
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
</script>