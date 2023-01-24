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
  <v-app>
    <v-main>
      <v-card
        color="white"
        class="px-4 py-6 mb-12 mb-sm-0"
        flat>
        <h3 class="font-weight-bold">{{ $t('generalSettings.title') }}</h3>
        <portal-general-settings-branding-site
          ref="brandingSettings"
          :branding="branding"
          @validity-check="validBranding = $event"
          @changed="changed = $event" />

        <portal-general-settings-branding-login
          ref="loginSettings"
          :branding="branding"
          @validity-check="validLogin = $event"
          @changed="changed = $event" />

        <div class="d-flex mt-12 justify-end">
          <v-btn
            :aria-label="$t('generalSettings.apply')"
            :disabled="!validForm"
            :loading="loading"
            color="primary"
            class="btn btn-primary register-button"
            elevation="0"
            @click="save">
            <span class="text-capitalize">
              {{ $t('generalSettings.apply') }}
            </span>
          </v-btn>
        </div>
      </v-card>
    </v-main>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    branding: null,
    errorMessage: null,
    loading: false,
    changed: false,
    validBranding: false,
    validLogin: false,
  }),
  computed: {
    validForm() {
      return this.changed && this.validBranding;
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
    loading() {
      if (this.loading) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      }
    },
  },
  mounted() {
    this.init()
      .finally(() => this.$root.$applicationLoaded());
  },
  methods: {
    init() {
      this.loading = true;
      return this.$brandingService.getBrandingInformation()
        .then(data => this.branding = data)
        .then(() => this.$nextTick())
        .then(() => this.$refs.brandingSettings.init())
        .then(() => this.$refs.loginSettings.init())
        .then(() => this.$nextTick())
        .finally(() => this.loading = false);
    },
    save() {
      this.errorMessage = null;

      const branding = Object.assign({}, this.branding);
      this.$refs.brandingSettings.preSave(branding);
      this.$refs.loginSettings.preSave(branding);

      this.loading = true;
      return this.$brandingService.updateBrandingInformation(branding)
        .then(() => this.init())
        .then(() => this.$root.$emit('alert-message', this.$t('generalSettings.savedSuccessfully'), 'success'))
        .catch(e => this.errorMessage = String(e))
        .finally(() => this.loading = false);
    },
  },
};
</script>