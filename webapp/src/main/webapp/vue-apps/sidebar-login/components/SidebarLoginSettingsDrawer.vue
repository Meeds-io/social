<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :right="!$vuetify.rtl"
    :loading="loading"
    eager
    @closed="reset">
    <template slot="title">
      {{ $t('sidebarLogin.drawer.settings.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card
        class="ma-4"
        flat>
        <div class="text-header">
          {{ $t('sidebarLogin.drawer.label.branding.title') }}
        </div>
        <v-card-text class="d-flex pa-0">
          <translation-text-field
            ref="titleTranslation"
            v-model="titleTranslations"
            class="width-auto flex-grow-1"
            drawer-title="sidebarLogin.drawer.label.branding.title"
            verify-i18n />
        </v-card-text>
      </v-card>

      <v-card
        class="ma-4"
        flat>
        <div class="text-header">
          {{ $t('sidebarLogin.drawer.label.branding.subtitle') }}
        </div>
        <v-card-text class="d-flex pa-0">
          <translation-text-field
            ref="subtitleTranslation"
            v-model="subtitleTranslations"
            class="width-auto flex-grow-1"
            drawer-title="sidebarLogin.drawer.label.branding.subtitle"
            verify-i18n />
        </v-card-text>
      </v-card>

      <v-card
        class="ma-4 d-flex align-center"
        flat>
        <div class="text-header">
          {{ $t('sidebarLogin.drawer.label.branding.background') }}
        </div>
        <div class="spacer" />
        <v-btn
          v-if="canDeleteBackground"
          :title="$t('sidebarLogin.drawer.label.branding.background.restore')"
          color="error"
          icon
          border
          outlined
          @click="restoreDefaultBackground">
          <v-icon size="18">fas fa-trash</v-icon>
        </v-btn>
        <div>
          <v-btn
            :title="$t('sidebarLogin.drawer.label.branding.background.tooltip')"
            outlined
            icon
            @click="$refs.imageCropDrawer.open(loginBackgroundItem)">
            <v-icon size="18">fa-camera</v-icon>
          </v-btn>
        </div>
      </v-card>

      <image-crop-drawer
        ref="imageCropDrawer"
        v-model="loginBackgroundUploadId"
        :crop-options="cropOptions"
        :max-file-size="maxFileSize"
        :src="backgroundPath"
        :use-format="true"
        :custom-format="true"
        alt
        drawer-title="generalSettings.changeLoginBackground.drawerTitle"
        @data="loginBackgroundData = $event"
        @input="uploadId = $event"
        @reset="cropperReset"
        @alt-text="loginBackgroundAltText = $event" />

      <v-card
        class="ma-4"
        flat>
          <div class="text-header">
            {{ $t('layout.alignApp') }}
          </div>
          <div class="d-flex">
            <div class="col-6 pa-0">
              <v-radio-group v-model="hAlign" class="ma-0">
                <v-radio
                  value="START"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignLeft') }}</span>
                  </template>
                </v-radio>
                <v-radio
                  value="CENTER"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignCenter') }}</span>
                  </template>
                </v-radio>
                <v-radio
                  value="END"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignRight') }}</span>
                  </template>
                </v-radio>
              </v-radio-group>
            </div>
            <div class="col-6 pa-0">
              <v-radio-group v-model="vAlign" class="ma-0">
                <v-radio
                  value="START"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignTop') }}</span>
                  </template>
                </v-radio>
                <v-radio
                  value="CENTER"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignMiddle') }}</span>
                  </template>
                </v-radio>
                <v-radio
                  value="END"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignBottom') }}</span>
                  </template>
                </v-radio>
              </v-radio-group>
            </div>
          </div>
        </v-card>
    </template>
    <template #footer>
      <div class="d-flex align-center">
        <v-btn
          :disabled="loading"
          :title="$t('sidebarLogin.drawer.settings.cancel')"
          class="btn ms-auto me-2"
          @click="close()">
          {{ $t('sidebarLogin.drawer.settings.cancel') }}
        </v-btn>
        <v-btn
          :loading="loading"
          :title="$t('sidebarLogin.drawer.settings.save')"
          color="primary"
          elevation="0"
          @click="save()">
          {{ $t('sidebarLogin.drawer.settings.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    branding: {
      type: Object,
      default: null,
    },
    backgroundPath: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    titleTranslations: {},
    subtitleTranslations: {},
    color: '#FFFFFF',
    hAlign: 'CENTER',
    vAlign: 'CENTER',
    loginBackgroundData: null,
    loginBackgroundUploadId: null,
    loginBackgroundAltText: null,
    loginBackgroundItem: {},
    removeBackground: false,
    maxFileSize: 2097152, // 2MB
    cropOptions() {
      return {
        aspectRatio: 1,
        viewMode: 1
      };
    },
  }),
  watch: {
    branding(newBranding) {
      this.titleTranslations = newBranding?.loginTitle;
      this.subtitleTranslations = newBranding?.loginSubtitle;
    },
    loginBackgroundUploadId() {
      this.removeBackground = false;
    },
  },
  computed: {
    canDeleteBackground() {
      return !this.removeBackground && (this.branding?.loginBackground?.fileId || this.loginBackgroundData);
    },
  },
  created() {
    this.titleTranslations = this.branding?.loginTitle;
    this.subtitleTranslations = this.branding?.loginSubtitle;
    this.loginBackgroundItem.altText = this.branding?.loginBackgroundAltText || '';
    if (this.$refs.imageCropDrawer) {
      this.$refs.imageCropDrawer.init();
    }
    this.$root.$on('sidebar-login-settings', this.open);
  },
  beforeDestroy() {
    this.$root.$off('sidebar-login-settings', this.open);
  },
  methods: {
    open() {
      this.reset();
      this.$refs.drawer.open();
    },
    reset() {
      this.hAlign = this.$root.hAlign || 'CENTER';
      this.vAlign = this.$root.vAlign || 'CENTER';
      this.removeBackground = false;
      this.loading = false;
    },
    close() {
      this.titleTranslations = this.branding?.loginTitle;
      this.subtitleTranslations = this.branding?.loginSubtitle;
      this.$refs.drawer.close();
    },
    savePreferences() {
      const formData = new FormData();
      formData.append('pageRef', this.$root.pageRef);
      formData.append('applicationId', this.$root.portletStorageId);
      const params = new URLSearchParams(formData).toString();
      return fetch(`/layout/rest/pages/application/preferences?${params}`, {
        method: 'PATCH',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          preferences: [{
            name: 'hAlign',
            value: this.hAlign || 'CENTER',
          },
          {
            name: 'vAlign',
            value: this.vAlign || 'CENTER',
          }],
        }),
      });
    },
    restoreDefaultBackground() {
      this.removeBackground = true;
    },
    save() {
      this.loading = true;
      const branding = Object.assign({}, this.branding);
      branding.loginTitle = this.titleTranslations;
      branding.loginSubtitle = this.subtitleTranslations;
      branding.loginBackgroundAltText = this.loginBackgroundAltText || '';

      if (this.loginBackgroundUploadId) {
        branding.loginBackground = {
          uploadId: this.loginBackgroundUploadId,
        };
      }

      if (this.removeBackground) {
        branding.loginBackground = {
          uploadId: 0,
        };
        this.loginBackgroundData = null;
      }

      return this.savePreferences().then(() => {
        this.$brandingService.updateBrandingInformation(branding)
          .then(() => this.$root.$emit('sidebar-login-settings-updated', branding, this.vAlign, this.hAlign, this.loginBackgroundData))
          .then(() => this.$root.$emit('alert-message', this.$t('sidebarLogin.drawer.savedSuccessfully'), 'success'))
          .finally(() => this.close());
      });
    },
  },
};
</script>
