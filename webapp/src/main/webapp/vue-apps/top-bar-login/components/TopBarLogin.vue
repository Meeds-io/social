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
    <div
      v-if="publicMode"
      class="d-flex">
      <v-btn
        v-if="$root.canRegister"
        id="topBarRegisterButton"
        class="primary me-5"
        elevation="0"
        :href="authenticated && '#' || '/portal/register'">
        <span class="text-none">{{ $t('publicAccess.register') }}</span>
      </v-btn>
      <v-btn
        id="topBarLoginButton"
        class="primary me-1"
        :class="$vuetify.display.mobile.value && 'rounded'"
        :href="authenticated && '#' || loginUrl"
        :icon="$vuetify.display.mobile.value"
        outlined>
        <span
          v-if="!$vuetify.display.mobile.value"
          class="text-none">{{ $t('publicAccess.login') }}</span>
        <v-icon
          v-else
          size="16">
          fa-sign-in-alt
        </v-icon>
      </v-btn>
    </div>
    <v-btn
      v-else-if="!isSpace && authenticated"
      id="topBarAccessButton"
      class="primary"
      href="/"
      outlined
      target="_blank">
      <v-avatar
        class="me-2"
        size="20">
        <v-img
          eager
          :src="$root.avatarUrl" />
      </v-avatar>
      <span class="text-none">{{ $t('publicAccess.access') }}</span>
    </v-btn>
    <v-btn
      v-else-if="isSpace && $root.isMember"
      id="topBarAccessButton"
      class="primary"
      :href="`/portal/s/${spaceId}`"
      outlined
      :title="$t('publicAccess.enter.tooltip')">
      <v-avatar
        class="me-2"
        size="20">
        <v-img
          eager
          :src="$root.avatarUrl" />
      </v-avatar>
      <span class="text-none">{{ $t('publicAccess.enter') }}</span>
    </v-btn>
    <div
      v-else-if="isSpace && authenticated && $root.isInvitedUser"
      class="d-flex">
      <v-btn
        id="topBarAccessButtonAccept"
        class="success me-2"
        :loading="loading"
        outlined
        :title="$t('publicAccess.acceptJoin.tooltip')"
        @click="acceptToJoin">
        <v-avatar
          class="me-2"
          size="20">
          <v-img
            eager
            :src="$root.avatarUrl" />
        </v-avatar>
        <span class="text-none success--text">{{ $t('publicAccess.acceptJoin') }}</span>
      </v-btn>
      <v-btn
        id="topBarAccessButtonRefuse"
        class="error"
        :loading="loading"
        outlined
        :title="$t('publicAccess.refuseJoin.tooltip')"
        @click="refuseToJoin">
        <span class="text-none error--text">{{ $t('publicAccess.refuseJoin') }}</span>
      </v-btn>
    </div>
    <v-btn
      v-else-if="isSpace && authenticated && $root.isPendingUser"
      id="topBarAccessButton"
      class="primary"
      :loading="loading"
      outlined
      :title="$t('publicAccess.cancelRequest.tooltip')"
      @click="cancelRequest">
      <v-avatar
        class="me-2"
        size="20">
        <v-img
          eager
          :src="$root.avatarUrl" />
      </v-avatar>
      <span class="text-none">{{ $t('publicAccess.cancelRequest') }}</span>
    </v-btn>
    <v-btn
      v-else-if="isSpace && authenticated && spaceRegistration === 'open'"
      id="topBarAccessButton"
      class="primary"
      :loading="loading"
      outlined
      :title="$t('publicAccess.join.tooltip')"
      @click="join">
      <v-avatar
        class="me-2"
        size="20">
        <v-img
          eager
          :src="$root.avatarUrl" />
      </v-avatar>
      <span class="text-none">{{ $t('publicAccess.join') }}</span>
    </v-btn>
    <v-btn
      v-else-if="isSpace && authenticated && spaceRegistration === 'validation'"
      id="topBarAccessButton"
      class="primary"
      :loading="loading"
      outlined
      :title="$t('publicAccess.requestJoin.tooltip')"
      @click="requestJoin">
      <v-avatar
        class="me-2"
        size="20">
        <v-img
          eager
          :src="$root.avatarUrl" />
      </v-avatar>
      <span class="text-none">{{ $t('publicAccess.requestJoin') }}</span>
    </v-btn>
    <div
      v-else-if="isSpace && authenticated && spaceRegistration === 'closed'"
      :title="$t('publicAccess.restrictedAccess.tooltip')">
      <v-btn
        id="topBarAccessButton"
        class="primary"
        disabled
        outlined>
        <v-avatar
          class="me-2"
          size="20">
          <v-img
            eager
            :src="$root.avatarUrl" />
        </v-avatar>
        <span class="text-none">{{ $t('publicAccess.restrictedAccess') }}</span>
      </v-btn>
    </div>
  </v-app>
</template>
<script>
  export default {
    data: () => ({
      previewMode: false,
      loading: false,
    }),
    computed: {
      publicMode () {
        return this.previewMode || !this.authenticated;
      },
      spaceRegistration () {
        return this.$root.spaceRegistration;
      },
      isSpace () {
        return !!this.spaceId?.length;
      },
      spaceId () {
        return eXo?.env?.portal?.spaceId;
      },
      authenticated () {
        return eXo?.env?.portal?.userIdentityId?.length;
      },
      loginUrl () {
        return this.isSpace ? `/portal/login?initialURI=${window.location.pathname}` : '/portal/login';
      },
    },
    created () {
      document.addEventListener('cms-preview-mode', this.switchToPreview);
      document.addEventListener('cms-edit-mode', this.switchToEdit);
    },
    mounted () {
      this.$root.$applicationLoaded();
    },
    methods: {
      switchToPreview () {
        this.previewMode = true;
      },
      switchToEdit () {
        this.previewMode = false;
      },
      async acceptToJoin () {
        this.loading = true;
        try {
          await eXo.$spaceService.accept(this.spaceId);
          this.$root.isMember = true;
        } finally {
          this.loading = false;
        }
      },
      async refuseToJoin () {
        this.loading = true;
        try {
          await eXo.$spaceService.deny(this.spaceId);
          this.$root.isInvitedUser = false;
        } finally {
          this.loading = false;
        }
      },
      async join () {
        this.loading = true;
        try {
          await eXo.$spaceService.join(this.spaceId);
          this.$root.isMember = true;
        } finally {
          this.loading = false;
        }
      },
      async requestJoin () {
        this.loading = true;
        try {
          await eXo.$spaceService.requestJoin(this.spaceId);
          this.$root.isPendingUser = true;
        } finally {
          this.loading = false;
        }
      },
      async cancelRequest () {
        this.loading = true;
        try {
          await eXo.$spaceService.cancel(this.spaceId);
          this.$root.isPendingUser = false;
        } finally {
          this.loading = false;
        }
      },
    },
  };
</script>