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
  <component
    v-if="hasActions"
    :is="bottomMenu && 'v-bottom-sheet' || 'v-menu'"
    ref="actionMenu"
    id="peopleCompactCardBottomDrawer"
    v-model="displayActionMenu"
    :attach="bottomMenu && '#vuetify-apps' || attachMenu"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    transition="slide-x-reverse-transition"
    content-class="peopleActionMenu position-absolute z-index-modal"
    offset-y
    top>
    <template #activator="{ on }">
      <v-btn
        v-show="displayMenuButton"
        v-on="on"
        :title="$t('peopleList.label.openUserMenu')"
        :class="menuButtonClass"
        icon
        @touchstart.stop="0"
        @touchend.stop="0"
        @mousedown.stop="0"
        @mouseup.stop="0"
        @click.prevent.stop="openMenu">
        <v-icon :size="dark && 20 || 18" class="icon-default-color">fa-ellipsis-v</v-icon>
      </v-btn>
    </template>
    <v-sheet
      :min-height="bottomMenu && '30vh' || 'auto'"
      :class="bottomMenu && 'border-top-left-radius border-top-right-radius overflow-hidden white' || 'transparent'">
      <v-list
        class="pa-0 full-height white"
        dense>
        <template v-if="filteredProfileActionExtensions.length">
          <people-user-menu-item
            v-for="(extension, i) in filteredProfileActionExtensions"
            :key="i"
            :extension="extension"
            :user="user"
            :space-id="spaceId" />
        </template>
        <template v-if="filteredUserNavigationExtensions.length">
          <people-user-menu-item
            v-for="(extension, i) in filteredUserNavigationExtensions"
            :key="i"
            :extension="extension"
            :user="user"
            :space-id="spaceId" />
        </template>
        <template v-if="filteredSpaceMembersExtensions.length">
          <v-divider v-if="filteredUserNavigationExtensions.length || filteredProfileActionExtensions.length" />
          <people-user-menu-item
            v-for="(extension, i) in filteredSpaceMembersExtensions"
            :key="i"
            :extension="extension"
            :user="user"
            :space-id="spaceId" />
        </template>
      </v-list>
    </v-sheet>
  </component>
</template>
<script>
export default {
  props: {
    user: {
      type: Object,
      default: null,
    },
    spaceId: {
      type: String,
      default: null,
    },
    dark: {
      type: Boolean,
      default: false
    },
    displayMenuButton: {
      type: Boolean,
      default: false
    },
    menuButtonClass: {
      type: String,
      default: null
    },
    attachMenu: {
      type: Boolean,
      default: false,
    },
    bottomMenu: {
      type: Boolean,
      default: false,
    },
    userNavigationExtensions: {
      type: Array,
      default: () => [],
    },
    profileActionExtensions: {
      type: Array,
      default: () => [],
    },
    spaceMembersExtensions: {
      type: Array,
      default: () => [],
    },
    ignoredNavigationExtensions: {
      type: Array,
      default: () => []
    },
  },
  data: () => ({
    id: Math.random(), // NOSONAR
    displayActionMenu: false,
    waitTimeUntilCloseMenu: 200,
  }),
  computed: {
    filteredProfileActionExtensions() {
      return this.profileActionExtensions.filter(extension => (!extension.mobileOnly || this.$root.isMobile) && extension.enabled(this.user, this.spaceId))
        || [];
    },
    filteredUserNavigationExtensions() {
      return this.userNavigationExtensions.filter(extension => (!extension.mobileOnly || this.$root.isMobile) && !this.ignoredNavigationExtensions.includes(extension?.id) && extension.enabled(this.user, this.spaceId))
        || [];
    },
    filteredSpaceMembersExtensions() {
      return this.spaceId
        && this.spaceMembersExtensions?.filter?.(extension => (!extension.mobileOnly || this.$root.isMobile) && extension.enabled(this.user, this.spaceId)) || [];
    },
    hasActions() {
      return (this.filteredProfileActionExtensions.length
        + this.filteredUserNavigationExtensions.length
        + this.filteredSpaceMembersExtensions.length)
        > 0;
    },
  },
  watch: {
    displayActionMenu(newVal) {
      if (newVal) {
        document.getElementById(`peopleCardItem${this.user.id}`).style.zIndex = 3;
      } else {
        document.getElementById(`peopleCardItem${this.user.id}`).style.zIndex = 0;
      }
      // Workaround to fix closing menu when clicking outside
      if (newVal) {
        document.addEventListener('mousedown', this.closeMenu);
        document.addEventListener('drawerOpened', this.closeMenu);
        document.addEventListener('modalOpened', this.closeMenu);
        this.$root.$emit('spaces-list-menu-opened', this.id);
      } else {
        document.removeEventListener('mousedown', this.closeMenu);
        document.removeEventListener('drawerOpened', this.closeMenu);
        document.removeEventListener('modalOpened', this.closeMenu);
      }
    }
  },
  created() {
    this.$root.$on('spaces-list-menu-opened', this.closeMenu);
  },
  beforeDestroy() {
    this.$root.$off('spaces-list-menu-opened', this.closeMenu);
    document.removeEventListener('mousedown', this.closeMenu);
    document.removeEventListener('drawerOpened', this.closeMenu);
    document.removeEventListener('modalOpened', this.closeMenu);
  },
  methods: {
    openMenu() {
      window.setTimeout(() => {
        document.querySelector('.v-overlay--active').addEventListener('click', this.close);
      }, 50);
    },
    closeMenu(event) {
      if (event !== this.id && this.displayActionMenu) {
        window.setTimeout(() => {
          this.displayActionMenu = false;
        }, this.waitTimeUntilCloseMenu);
      }
    },
  },
};
</script>
