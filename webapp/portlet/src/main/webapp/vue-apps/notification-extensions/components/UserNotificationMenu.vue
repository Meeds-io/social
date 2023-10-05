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
    v-model="menu"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    bottom
    offset-y
    attach>
    <template #activator="{ on, attrs }">
      <v-btn
        small
        icon
        v-bind="attrs"
        v-on="on">
        <v-icon size="16" class="icon-default-color">fas fa-ellipsis-v</v-icon>
      </v-btn>
    </template>
    <v-list dense class="white pa-0">
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
  },
  data: () => ({
    menu: false,
  }),
  created() {
    // Workaround to fix closing menu when clicking outside
    $(document).mousedown(() => {
      if (this.menu) {
        window.setTimeout(() => {
          this.menu = false;
        }, 200);
      }
    });
  },
};
</script>