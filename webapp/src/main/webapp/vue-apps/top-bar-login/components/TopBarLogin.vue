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
        :href="authenticated && '#' || loginUrl"
        :class="$vuetify.breakpoint.mobile && 'rounded'"
        class="primary me-1"
        outlined>
        <span v-if="!$vuetify.breakpoint.mobile" class="text-none">{{ $t('publicAccess.login') }}</span>
        <v-icon v-else size="16">fa-sign-in-alt</v-icon>
      </v-btn>
    </div>
    <v-btn
      v-else-if="!isSpace && authenticated"
      id="topBarAccessButton"
      href="/"
      target="_blank"
      class="primary"
      outlined>
      <v-avatar size="20" class="me-2">
        <v-img :src="$root.avatarUrl" eager />
      </v-avatar>
      <span class="text-none">{{ $t('publicAccess.access') }}</span>
    </v-btn>
    <v-btn
      v-else-if="isSpace && $root.isMember"
      id="topBarAccessButton"
      :href="`/portal/s/${spaceId}`"
      :title="$t('publicAccess.enter.tooltip')"
      class="primary"
      outlined>
      <v-avatar size="20" class="me-2">
        <v-img :src="$root.avatarUrl" eager />
      </v-avatar>
      <span class="text-none">{{ $t('publicAccess.enter') }}</span>
    </v-btn>
    <div v-else-if="isSpace && authenticated && $root.isInvitedUser" class="d-flex">
      <v-btn
        id="topBarAccessButtonAccept"
        :title="$t('publicAccess.acceptJoin.tooltip')"
        :loading="loading"
        class="success me-2"
        outlined
        @click="acceptToJoin">
        <v-avatar size="20" class="me-2">
          <v-img :src="$root.avatarUrl" eager />
        </v-avatar>
        <span class="text-none success--text">{{ $t('publicAccess.acceptJoin') }}</span>
      </v-btn>
      <v-btn
        id="topBarAccessButtonRefuse"
        :title="$t('publicAccess.refuseJoin.tooltip')"
        :loading="loading"
        class="error"
        outlined
        @click="refuseToJoin">
        <span class="text-none error--text">{{ $t('publicAccess.refuseJoin') }}</span>
      </v-btn>
    </div>
    <v-btn
      v-else-if="isSpace && authenticated && $root.isPendingUser"
      id="topBarAccessButton"
      :title="$t('publicAccess.cancelRequest.tooltip')"
      :loading="loading"
      class="primary"
      outlined
      @click="cancelRequest">
      <v-avatar size="20" class="me-2">
        <v-img :src="$root.avatarUrl" eager />
      </v-avatar>
      <span class="text-none">{{ $t('publicAccess.cancelRequest') }}</span>
    </v-btn>
    <v-btn
      v-else-if="isSpace && authenticated && spaceRegistration === 'open'"
      id="topBarAccessButton"
      :loading="loading"
      :title="$t('publicAccess.join.tooltip')"
      class="primary"
      outlined
      @click="join">
      <v-avatar size="20" class="me-2">
        <v-img :src="$root.avatarUrl" eager />
      </v-avatar>
      <span class="text-none">{{ $t('publicAccess.join') }}</span>
    </v-btn>
    <v-btn
      v-else-if="isSpace && authenticated && spaceRegistration === 'validation'"
      id="topBarAccessButton"
      :title="$t('publicAccess.requestJoin.tooltip')"
      :loading="loading"
      class="primary"
      outlined
      @click="requestJoin">
      <v-avatar size="20" class="me-2">
        <v-img :src="$root.avatarUrl" eager />
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
        <v-avatar size="20" class="me-2">
          <v-img :src="$root.avatarUrl" eager />
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
    publicMode() {
      return this.previewMode || !this.authenticated;
    },
    spaceRegistration() {
      return this.$root.spaceRegistration;
    },
    isSpace() {
      return !!this.spaceId?.length;
    },
    spaceId() {
      return eXo?.env?.portal?.spaceId;
    },
    authenticated() {
      return eXo?.env?.portal?.userIdentityId?.length;
    },
    loginUrl() {
      return this.isSpace ? `/portal/login?initialURI=${window.location.pathname}` : '/portal/login';
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
    async acceptToJoin() {
      this.loading = true;
      try {
        await this.$spaceService.accept(this.spaceId);
        this.$root.isMember = true;
      } finally {
        this.loading = false;
      }
    },
    async refuseToJoin() {
      this.loading = true;
      try {
        await this.$spaceService.deny(this.spaceId);
        this.$root.isInvitedUser = false;
      } finally {
        this.loading = false;
      }
    },
    async join() {
      this.loading = true;
      try {
        await this.$spaceService.join(this.spaceId);
        this.$root.isMember = true;
      } finally {
        this.loading = false;
      }
    },
    async requestJoin() {
      this.loading = true;
      try {
        await this.$spaceService.requestJoin(this.spaceId);
        this.$root.isPendingUser = true;
      } finally {
        this.loading = false;
      }
    },
    async cancelRequest() {
      this.loading = true;
      try {
        await this.$spaceService.cancel(this.spaceId);
        this.$root.isPendingUser = false;
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>