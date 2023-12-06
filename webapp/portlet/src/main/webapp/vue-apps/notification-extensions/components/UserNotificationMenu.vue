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
  <v-menu
    v-if="initialized"
    v-model="menu"
    v-bind="absolute && x && y && {
      'position-x': x,
      'position-y': y,
    }"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    :attach="!absolute"
    :absolute="absolute"
    offset-y
    bottom>
    <template #activator="{ on, attrs }">
      <v-btn
        v-show="hoverMenuOrCard"
        v-bind="attrs"
        v-on="on"
        small
        icon
        @mousedown="showMenuNonAbsolute">
        <v-icon size="16" class="icon-default-color">fas fa-ellipsis-v</v-icon>
      </v-btn>
    </template>
    <v-hover v-if="menu" @input="hoverMenu = $event">
      <v-list dense class="white pa-0">
        <v-list-item
          v-if="url"
          :href="url"
          target="_blank"
          dense
          @mousedown="$emit('read-differ')">
          <v-list-item-icon class="mx-1 justify-center">
            <v-icon size="13" class="dark-grey-color">fa-external-link-alt</v-icon>
          </v-list-item-icon>
          <v-list-item-title class="pl-0">{{ $t('Notification.openInNewWindow') }}</v-list-item-title>
        </v-list-item>
        <v-list-item
          v-if="unread"
          dense
          @click="$emit('read')">
          <v-list-item-icon class="mx-1 justify-center">
            <v-icon size="13" class="dark-grey-color">fa-envelope-open-text</v-icon>
          </v-list-item-icon>
          <v-list-item-title class="pl-0">{{ $t('Notification.markRead') }}</v-list-item-title>
        </v-list-item>
        <v-list-item
          dense
          @click="$emit('remove')">
          <v-list-item-icon class="mx-1 justify-center">
            <v-icon size="13" class="dark-grey-color">fa-trash</v-icon>
          </v-list-item-icon>
          <v-list-item-title class="pl-0">{{ $t('Notification.deleteNotification') }}</v-list-item-title>
        </v-list-item>
        <v-list-item
          v-if="canMute"
          dense
          @click="$emit('mute')">
          <v-list-item-icon class="mx-1 justify-center">
            <v-icon size="13" class="dark-grey-color">fa-bell-slash</v-icon>
          </v-list-item-icon>
          <v-list-item-title class="pl-0">{{ $t('Notification.muteSpaceNotification') }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-hover>
  </v-menu>
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
    unread: {
      type: Boolean,
      default: false,
    },
    canMute: {
      type: Boolean,
      default: false,
    },
    hover: {
      type: Boolean,
      default: false,
    },
    url: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    initialized: true,
    hoverMenu: false,
    menu: false,
    absolute: false,
    x: 0,
    y: 0,
  }),
  computed: {
    hoverMenuOrCard() {
      return this.hoverMenu || this.hover;
    },
  },
  watch: {
    menu() {
      if (!this.menu) {
        this.absolute = false;
      }
    },
    absolute() {
      if (!this.absolute) {
        this.initialized = false;
        this.$nextTick(() => {
          this.x = 0;
          this.y = 0;
          this.initialized = true;
        });
      }
    },
    hoverMenuOrCard() {
      if (!this.hoverMenuOrCard && this.menu) {
        if (!this.absolute) {
          this.menu = false;
        }
        window.setTimeout(() => {
          if (!this.hoverMenuOrCard) {
            this.menu = false;
          }
        }, 200);
      }
    },
  },
  created() {
    document.addEventListener('notifications-list-scroll-activated', this.hideMenu);
  },
  beforeDestroy() {
    document.removeEventListener('notifications-list-scroll-activated', this.hideMenu);
  },
  methods: {
    showMenu(x, y) {
      this.x = x + 2;
      this.y = y + 2;
      this.absolute = true;
      this.$nextTick(() => this.menu = true);
    },
    hideMenu() {
      this.menu = false;
    },
    showMenuNonAbsolute() {
      this.absolute = false;
    },
  },
};
</script>