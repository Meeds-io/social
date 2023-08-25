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
  <v-card class="pb-1" flat>
    <v-list-item class="px-0 mb-2" three-line>
      <v-list-item-content class="pt-0">
        <v-list-item-title class="my-0">
          <h4 class="font-weight-bold mt-0">
            {{ $t('generalSettings.access') }}
          </h4>
        </v-list-item-title>
        <v-list-item-subtitle class="pt-2">
          <h4 class="my-0 text-color">{{ $t('generalSettings.access.summary1') }}</h4>
        </v-list-item-subtitle>
        <v-list-item-subtitle class="py-2">
          <h5 class="my-0 subtitle-1 text-color">{{ $t('generalSettings.access.summary2') }}</h5>
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>

    <v-list-item
      class="px-0 mt-4"
      dense
      @click="accessType = 'open'">
      <v-list-item-action class="me-4">
        <v-radio-group v-model="accessType">
          <v-radio
            value="open"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle"
            @click="accessType = 'open'" />
        </v-radio-group>
      </v-list-item-action>
      <v-list-item-content class="py-0">
        <v-list-item-title>
          <h4 class="my-0 py-2">{{ $t('generalSettings.access.open') }}</h4>
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('generalSettings.access.open.subtitle') }}
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
    <v-fade-transition>
      <v-list-item
        v-if="accessType === 'open'"
        class="mt-2 mb-4"
        dense
        @click="externalUserRegistration = !externalUserRegistration">
        <v-list-item-action class="me-4">
          <v-switch v-model="externalUserRegistration" />
        </v-list-item-action>
        <v-list-item-content class="py-0">
          <v-list-item-title>
            <h4 class="my-0 py-2">{{ $t('generalSettings.access.enableExternalUsers') }}</h4>
          </v-list-item-title>
          <v-list-item-subtitle>
            {{ $t('generalSettings.access.enableExternalUsers.subtitle') }}
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-fade-transition>

    <v-list-item
      class="px-0 mt-4"
      dense
      @click="accessType = 'restricted'">
      <v-list-item-action class="me-4">
        <v-radio-group v-model="accessType">
          <v-radio
            value="restricted"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle"
            @click="accessType = 'restricted'" />
        </v-radio-group>
      </v-list-item-action>
      <v-list-item-content class="py-0">
        <v-list-item-title>
          <h4 class="my-0 py-2">{{ $t('generalSettings.access.restricted') }}</h4>
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('generalSettings.access.restricted.subtitle') }}
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
    <v-fade-transition>
      <v-list-item
        v-if="accessType === 'restricted'"
        class="mt-2 mb-4"
        dense
        @click="externalUserRegistration = !externalUserRegistration">
        <v-list-item-action class="me-4">
          <v-switch v-model="externalUserRegistration" />
        </v-list-item-action>
        <v-list-item-content class="py-0">
          <v-list-item-title>
            <h4 class="my-0 py-2">{{ $t('generalSettings.access.enableExternalUsers') }}</h4>
          </v-list-item-title>
          <v-list-item-subtitle>
            {{ $t('generalSettings.access.enableExternalUsers.subtitle') }}
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-fade-transition>

    <v-list-item dense class="px-0 mt-4 mb-2">
      <v-list-item-content class="py-0">
        <v-list-item-title>
          <h4 class="my-0 py-2">{{ $t('generalSettings.access.platformAuthentication') }}</h4>
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
    <v-list-item dense>
      <v-list-item-content>
        <v-list-item-title class="subtitle-1">
          {{ $t('generalSettings.access.passwordAuthentication') }}
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('generalSettings.access.passwordAuthentication.subtitle') }}
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <v-chip class="font-italic">
          {{ $t('generalSettings.access.default') }}
        </v-chip>
      </v-list-item-action>
    </v-list-item>
    <v-list-item dense>
      <v-list-item-content>
        <v-list-item-title class="subtitle-1">
          {{ $t('generalSettings.access.additionalAuthentication') }}
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('generalSettings.access.additionalAuthentication.subtitle') }}
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <v-chip class="font-italic">
          {{ $t('generalSettings.access.contactAdministrator') }}
        </v-chip>
      </v-list-item-action>
    </v-list-item>

    <v-list-item dense class="px-0 mt-4 mb-2">
      <v-list-item-content class="py-0">
        <v-list-item-title class="subtitle-1">
          <h4 class="my-0 py-2">{{ $t('generalSettings.access.startSettingPlatform') }}</h4>
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
    <v-list-item dense class="my-0">
      <v-list-item-content>
        <v-list-item-title class="subtitle-1">
          {{ $t('generalSettings.access.startSettingPlatform.spaces') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="d-flex flex-row align-center my-0">
        <span class="font-italic subtitle-1 dark-grey-color me-2">
          {{ $t('generalSettings.access.noDefaultSpace') }}
        </span>
        <v-btn
          icon>
          <v-icon size="24" class="icon-default-color">fa-edit</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <v-list-item dense class="my-0">
      <v-list-item-content>
        <v-list-item-title class="subtitle-1">
          {{ $t('generalSettings.access.startSettingPlatform.mandatorySpaces') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="my-0">
        <v-btn
          :href="mandatorySpacesLink"
          icon>
          <v-icon size="24" class="icon-default-color">fa-external-link-alt</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <v-list-item dense class="my-0">
      <v-list-item-content>
        <v-list-item-title class="subtitle-1">
          {{ $t('generalSettings.access.startSettingPlatform.createUsers') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="my-0">
        <v-btn
          :href="createUsersLink"
          icon>
          <v-icon size="24" class="icon-default-color">fa-external-link-alt</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <div class="d-flex my-12 mx-4 justify-end">
      <v-btn
        :aria-label="$t('generalSettings.cancel')"
        :disabled="loading"
        class="btn cancel-button me-4"
        elevation="0"
        @click="$emit('close')">
        <span class="text-none">
          {{ $t('generalSettings.cancel') }}
        </span>
      </v-btn>
      <v-btn
        :aria-label="$t('generalSettings.apply')"
        :disabled="!validForm"
        :loading="loading"
        color="primary"
        class="btn btn-primary"
        elevation="0"
        @click="save">
        <span class="text-none">
          {{ $t('generalSettings.apply') }}
        </span>
      </v-btn>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    accessSetting: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    createUsersLink: '/portal/g/:platform:administrators/usersManagement',
    mandatorySpacesLink: '/portal/g/:platform:users/spacesAdministration',
    accessType: 'open',
    externalUserRegistration: false,
  }),
  computed: {
    validForm() {
      return this.changed;
    },
    changed() {
      if (!this.branding) {
        return false;
      }
      if (this.loginBackgroundUploadId) {
        return true;
      }
      if (this.loginBackgroundData && this.loginBackgroundTextColor !== this.branding?.loginBackgroundTextColor) {
        return true;
      }
      const oldBranding = JSON.parse(JSON.stringify(this.branding));
      const newBranding = Object.assign(JSON.parse(JSON.stringify(this.branding)), {
        loginTitle: this.loginTitle,
        loginSubtitle: this.loginSubtitle,
      });
      return JSON.stringify(oldBranding) !== JSON.stringify(newBranding);
    },
  },
  watch: {
    errorMessage() {
      if (this.errorMessage) {
        this.$root.$emit('alert-message', this.$t(this.errorMessage), 'error');
      } else {
        this.$root.$emit('close-alert-message');
      }
    },
  },  
  created() {
    this.init();
  },
  methods: {
    init() {
      // TODO
    },
    save() {
      // TODO
    },
  }
};
</script>
