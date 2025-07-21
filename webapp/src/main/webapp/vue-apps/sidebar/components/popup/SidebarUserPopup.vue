<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    ref="userMenu"
    v-model="menu"
    :position-x="menuX"
    :position-y="menuY"
    :nudge-top="20"
    :nudge-left="80"
    content-class="z-index-modal"
    absolute
    eager
    top
    offset-y>
    <sidebar-user-popup-content
      :user="user"
      :user-status-color="statusColor"
      :profile-uri="profileUri" />
  </v-menu>
</template>

<script>

export default {
  data() {
    return {
      userName: eXo.env.portal.userName,
      profileUri: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile`,
      user: null,
      menu: false,
      position: {x: 0, y: 0},
      statusMap: {
        available: 'green',
        dnd: 'red',
        offline: 'grey',
        invisible: 'grey'
      },
      status: null,
    };
  },
  created() {
    this.getUserStatus();
    this.getUserInfo();
  },
  mounted() {
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  watch: {
    menu() {
      this.$root.allowClosing = !this.menu;
    },
    status() {
      this.$emit('user-status-updated', this.statusColor);
    }
  },
  computed: {
    statusColor() {
      return this.statusMap?.[this.status];
    },
    menuX() {
      return this.position?.x;
    },
    menuY() {
      return this.position?.y;
    }
  },
  methods: {
    open(x, y) {
      this.position.x = x;
      this.position.y = y;
      this.$nextTick(() => {
        this.menu = !this.menu;
      });
    },
    handleClickOutside(event) {
      if (!this.menu) {
        return;
      }
      const menuEl = this.$refs.userMenu?.$el;
      if (menuEl && !menuEl.contains(event.target)) {
        this.menu = false;
      }
    },
    getUserInfo() {
      this.$userService.getUser(this.userName).then(user => {
        this.user = user;
      });
    },
    getUserStatus() {
      return this.$userStateService.getUserStatus(this.userName).then(data => {
        this.status = data?.status;
      });
    },
  }
};
</script>
