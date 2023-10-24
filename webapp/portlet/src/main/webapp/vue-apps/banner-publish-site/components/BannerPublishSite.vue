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
  <v-app class="white card-border-radius">
    <v-alert
      v-show="canView"
      type="warning"
      border="left"
      class="ma-0"
      dense
      outlined
      prominent>
      <div class="d-flex align-center">
        <div class="flex-grow-1 text-start text-color">{{ $t('publicAccess.makeSiteVisible') }}</div>
        <div class="flex-grow-0">
          <v-switch
            v-model="publicMode"
            :loading="loading"
            class="my-auto"
            hide-details
            @click="switchSiteMode" />
        </div>
      </div>
    </v-alert>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    previewMode: false,
    publicMode: false,
    loading: false,
  }),
  computed: {
    canView() {
      return !this.previewMode;
    },
  },
  watch: {
    previewMode() {
      const parentContainer = this.$el.closest('.UIContainer');
      if (parentContainer) {
        if (this.previewMode) {
          parentContainer.classList.add('hidden');
        } else {
          parentContainer.classList.remove('hidden');
        }
      }
    },
  },
  created() {
    this.publicMode = this.$root.publicMode;
    document.addEventListener('cms-preview-mode', this.switchToPreview);
    document.addEventListener('cms-edit-mode', this.switchToEdit);
  },
  beforeDestroy() {
    document.removeEventListener('cms-preview-mode', this.switchToPreview);
    document.removeEventListener('cms-edit-mode', this.switchToEdit);
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
    switchSiteMode() {
      this.loading = true;
      const formData = new FormData();
      formData.append('name', 'accessPermissions');
      formData.append('value', this.publicMode && 'Everyone' || '*:/platform/administrators,*:/platform/web-contributors');
      const params = new URLSearchParams(formData).toString();
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/sites/${this.$root.publicSiteId}`, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        method: 'PATCH',
        credentials: 'include',
        body: params,
      })
        .then(resp => {
          if (resp?.ok) {
            this.$root.$emit('alert-message', this.$t('generalSettings.publicSiteUpdatedSuccessfully'), 'success');
          } else {
            this.publicMode = !this.publicMode;
            this.$root.$emit('alert-message', this.$t('generalSettings.publicSiteUpdateError'), 'error');
          }
        })
        .finally(() => this.loading = false);
    },
  },
};
</script>