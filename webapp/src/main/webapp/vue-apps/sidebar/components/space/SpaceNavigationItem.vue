<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <v-list-item
    v-if="$root.isMobile"
    :class="homeIcon && (homeLink === spaceLink && 'UserPageLinkHome' || 'UserPageLink')"
    class="px-2 spaceItem text-truncate"
    role="button"
    @keyup.space="openOrCloseDrawer"
    @click="openOrCloseDrawer">
    <v-list-item-avatar 
      size="28"
      class="me-3 ms-3 tile my-0 spaceAvatar"
      tile>
      <img
        :src="spaceAvatar"
        alt=""
        width="28"
        height="28">
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title v-text="spaceDisplayName" class="menu-text-color" />
    </v-list-item-content>
    <v-list-item-action
      v-if="spaceUnreadCount"
      class="me-2 my-auto align-center">
      <v-chip
        color="error-color-background"
        min-width="22"
        height="22"
        dark>
        {{ spaceUnreadCount }}
      </v-chip>
    </v-list-item-action>
  </v-list-item>
  <v-hover v-else v-model="showItemActions">
    <v-list-item
      :href="spaceLink"
      :class="homeIcon && (homeLink === spaceLink && 'UserPageLinkHome' || 'UserPageLink')"
      :arial-label="$t('space.avatar.href.title',{0:space.prettyName})"
      :title="spaceDisplayName"
      class="px-2 spaceItem"
      role="link">
      <v-list-item-avatar 
        size="28"
        class="me-3 ms-2 tile my-0 spaceAvatar"
        tile>
        <img
          :src="spaceAvatar"
          alt=""
          class="rounded"
          width="28"
          height="28">
      </v-list-item-avatar>
      <v-list-item-content>
        <v-list-item-title v-text="spaceDisplayName" class="menu-text-color" />
      </v-list-item-content>
      <v-list-item-action
        v-if="toggleArrow"
        :disabled="loading"
        :loading="loading"
        class="me-2 my-auto align-center">
        <ripple-hover-button
          :active="!drawerOpened"
          icon
          @ripple-hover="openOrCloseDrawer()">
          <v-icon
            :id="space.id"
            class="me-0 pa-2 icon-default-color"
            small>
            {{ arrowIcon }} 
          </v-icon>
        </ripple-hover-button>
      </v-list-item-action>
      <v-list-item-action
        v-if="!toggleArrow && spaceUnreadCount"
        class="me-2 my-auto align-center">
        <v-chip
          v-if="spaceUnreadCount"
          color="error-color-background"
          min-width="22"
          height="22"
          dark>
          {{ spaceUnreadCount }}
        </v-chip>
      </v-list-item-action>
    </v-list-item>
  </v-hover>
</template>
<script>

export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
    spaceUrl: {
      type: String,
      default: null
    },
    homeLink: {
      type: String,
      default: null,
    },
    homeIcon: {
      type: Boolean,
      default: false,
    },
    openedSpace: {
      type: Object,
      default: null,
    },
    thirdLevel: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    showItemActions: false,
    spaceUnreadItems: null,
  }),
  computed: {
    spaceId() {
      return this.space?.id;
    },
    spaceLink() {
      return this.spaceUrl;
    },
    spaceAvatar() {
      return this.space?.avatarUrl;
    },
    spaceDisplayName() {
      return this.space?.displayName;
    },
    spaceUnreadCount() {
      return this.$root?.unreadPerSpace?.[this.space?.id];
    },
    toggleArrow() {
      return this.showItemActions || this.drawerOpened;
    },
    drawerOpened() {
      return this.openedSpace?.id === this.space?.id;
    },
    arrowIcon() {
      return this.drawerOpened && this.arrowIconLeft || this.arrowIconRight;
    },
    arrowIconLeft() {
      return this.$vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left';
    },
    arrowIconRight() {
      return this.$vuetify.rtl && 'fa-arrow-left' || 'fa-arrow-right';
    },
  },
  watch: {
    space: {
      immediate: true,
      deep: true,
      handler: function() {
        if (JSON.stringify(this.spaceUnreadItems || {}) !== JSON.stringify(this.space?.unread || {})) {
          this.spaceUnreadItems = this.space?.unread;
        }
      },
    },
  },
  methods: {
    openOrCloseDrawer(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.$root.$emit('change-space-menu', this.space, this.thirdLevel);
    },
  },
};
</script>
