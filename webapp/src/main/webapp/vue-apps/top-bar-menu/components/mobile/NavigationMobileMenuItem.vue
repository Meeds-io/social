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
    offset-y
    rounded>
    <template #activator="{ attrs, on }">
      <v-tab
        v-bind="attrs"
        :aria-label="navigation.label"
        class="mx-auto pa-1 text-break navigation-mobile-menu-tab"
        :disabled="!hasPage && !hasChildren"
        :href="navigationNodeUri"
        :link="hasPage"
        :rel="navigationNodeRel"
        role="tab"
        :target="navigationNodeTarget"
        @change="updateNavigationState(navigation.uri)"
        @click="checkLink">
        <span
          class="text-truncate-3 pt-2">
          {{ navigation.label }}
        </span>
        <v-btn
          v-if="hasPage && hasChildren"
          class="mt-2"
          icon
          v-on="hasChildren && on"
          @click.stop.prevent="openDropMenu">
          <v-icon size="20">
            fa-angle-up
          </v-icon>
        </v-btn>
        <v-icon
          v-else-if="hasChildren"
          class="pa-3 mt-2"
          size="20">
          fa-angle-up
        </v-icon>
      </v-tab>
    </template>
    <navigation-mobile-menu-sub-item
      v-if="hasChildren"
      :base-site-uri="baseSiteUri"
      class="transparent"
      :navigation="navigation.children"
      :parent-navigation-uri="navigation.uri"
      :show-menu="showMenu"
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
    },
    data: () => ({
      showMenu: false,
    }),
    computed: {
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
    },
    created () {
      document.addEventListener('click', this.handleCloseMenu);
      this.$root.$on('close-sibling-drop-menus', this.handleCloseSiblingMenus);
    },
    methods: {
      updateNavigationState (value) {
        this.$emit('update-navigation-state', `${this.baseSiteUri}${value}`);
      },
      checkLink (e) {
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
      openDropMenu (persist, event) {
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