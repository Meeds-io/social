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
  <v-app
    color="transaprent"
    class="HamburgerNavigationMenu"
    flat>
    <a
      class="HamburgerNavigationMenuLink flex border-box-sizing"
      @click="$emit('open-drawer')">
      <div class="px-5 py-3">
        <v-icon size="24">fa-bars</v-icon>
      </div>
      <v-btn
        v-show="showBadge"
        class="hamburger-unread-badge"
        absolute
        icon
        height="16"
        width="16"
        text>
        <div class="hamburger-unread-badge error-color-background"></div>
      </v-btn>
    </a>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    showHamburgerBadge: false
  }),
  props: {
    unreadBySpace: {
      type: Object,
      default: () => ({})
    },
    displayBadge: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    showBadge() {
      return this.displayBadge || this.showHamburgerBadge;
    }
  },
  mounted() {
    document.addEventListener('notification.unread.item', this.handleUpdatesFromWebSocket);
  },
  methods: {
    handleUpdatesFromWebSocket(event) {
      const data = event?.detail;
      const wsEventName = data?.wsEventName || '';
      if (wsEventName === 'notification.unread.item') {
        if (!this.showHamburgerBadge) {
          this.showHamburgerBadge = true;
        }
      }
    },
  }
};
</script>
