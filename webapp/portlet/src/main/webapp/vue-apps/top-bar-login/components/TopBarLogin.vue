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
    <div v-if="publicMode" class="d-flex">
      <v-btn
        v-if="$root.canRegister"
        id="topBarRegisterButton"
        :href="authenticated && '#' || '/portal/register'"
        class="primary me-5"
        elevation="0">
        <span class="text-none">{{ $t('publicAccess.register') }}</span>
      </v-btn>
      <v-btn
        id="topBarLoginButton"
        :icon="$vuetify.breakpoint.mobile"
        :href="authenticated && '#' || '/portal/login'"
        :class="$vuetify.breakpoint.mobile && 'rounded'"
        class="primary me-1"
        outlined>
        <span v-if="!$vuetify.breakpoint.mobile" class="text-none">{{ $t('publicAccess.login') }}</span>
        <v-icon v-else size="16">fa-sign-in-alt</v-icon>
      </v-btn>
    </div>
    <v-btn
      v-else-if="authenticated"
      id="topBarAccessButton"
      href="/portal"
      target="_blank"
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
      return this.previewMode || !this.authenticated;
    },
    authenticated() {
      return eXo?.env?.portal?.userIdentityId?.length;
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