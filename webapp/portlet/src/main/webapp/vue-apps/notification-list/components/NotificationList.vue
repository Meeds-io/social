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
  <v-app class="uiIntranetNotificationsPortlet">
    <v-card flat tile>
      <user-notifications />
    </v-card>
  </v-app>
</template>
<script>
export default {
  data: () =>({
    loading: 0,
    loadingInProgress: false,
  }),
  watch: {
    loadingInProgress() {
      if (this.loadingInProgress) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        this.$nextTick().then(() => this.$root.initialized = true);
      }
    },
    loading() {
      if (this.loading === 1) {
        this.loadingInProgress = true;
      } else if (this.loading === 0) {
        this.loadingInProgress = false;
      }
    },
  },
  created() {
    this.$root.$on('notification-loading-start', this.incrementLoading);
    this.$root.$on('notification-loading-end', this.decrementLoading);
  },
  methods: {
    incrementLoading() {
      this.loading++;
    },
    decrementLoading() {
      this.loading--;
    },
  },
  
};
</script>