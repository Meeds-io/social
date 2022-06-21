<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
  <v-snackbar
    v-model="snackbar"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    class="z-index-modal"
    color="transparent"
    elevation="0"
    app>
    <exo-notification-alert
      :alert="alert"
      no-timeout
      @dismissed="clear" />
  </v-snackbar>
</template>
<script>
export default {
  data: () => ({
    snackbar: false,
    alert: null,
  }),
  watch: {
    alert() {
      this.snackbar = !this.alert;
      this.$nextTick().then(() => this.snackbar = !!this.alert);
    },
  },
  created() {
    document.addEventListener('notification-alert', event => this.alert = event?.detail);
  },
  methods: {
    clear() {
      this.alert = null;
    },
  },
};
</script>