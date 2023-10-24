<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2023 Meeds Association contact@meeds.io

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
  <v-app>
    <v-btn
      v-if="publicMode"
      id="topBarLoginButton"
      href="/portal/login"
      class="primary"
      outlined>
      <span class="text-none">{{ $t('publicAccess.login') }}</span>
    </v-btn>
    <v-btn
      v-else
      id="topBarAccessButton"
      :href="$root.userHomeUrl"
      class="primary"
      outlined>
      <v-avatar size="20" class="me-2">
        <v-img :src="$root.avatarUrl" eager />
      </v-avatar>
      <span class="text-none">{{ $t('publicAccess.access') }}</span>
    </v-btn>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    previewMode: false,
  }),
  computed: {
    publicMode() {
      return this.previewMode || !this.$root.avatarUrl;
    },
  },
  created() {
    document.addEventListener('cms-preview-mode', this.switchToPreview);
    document.addEventListener('cms-edit-mode', this.switchToEdit);
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
  methods: {
    switchToPreview() {
      this.previewMode = true;
    },
    switchToEdit() {
      this.previewMode = false;
    },
  },
};
</script>