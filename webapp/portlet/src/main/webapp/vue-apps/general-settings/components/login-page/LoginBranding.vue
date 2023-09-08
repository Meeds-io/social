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
  <v-row class="ma-0">
    <v-col cols="12" class="pa-0">
      <h4 class="font-weight-bold mt-0">
        {{ $t('generalSettings.loginCharacteristics') }}
      </h4>
    </v-col>
    <v-col cols="12" class="pa-0">
      <div class="d-flex flex-wrap-reverse">
        <v-card
          color="transparent"
          :min-width="hasImage && 'auto' || width"
          :width="hasImage && 'auto' || width"
          :min-height="height"
          :height="height"
          class="position-relative mx-auto me-md-4 ms-md-0 mb-4"
          flat
          tile>
          <v-btn
            v-if="hasImage && loginBackgroundUploadId"
            :title="$t('generalSettings.loginBackgroundCancelImageChoice')"
            :loading="deleting"
            color="primary"
            class="position-absolute r-3 mt-2 z-index-two white"
            icon
            border
            outlined
            @click="deleteBackground">
            <v-icon size="18">fas fa-undo</v-icon>
          </v-btn>
          <v-btn
            v-else-if="canDeleteDefaultBackground"
            :title="$t('generalSettings.loginBackgroundRestoreDefaultBackground')"
            :loading="deleting"
            color="error"
            class="position-absolute r-3 mt-2 z-index-two white"
            icon
            border
            outlined
            @click="deleteDefaultBackground">
            <v-icon size="22">fas fa-trash</v-icon>
          </v-btn>
          <img
            v-if="hasImage"
            :src="loginBackgroundSrc"
            style="height: 100%;"
            alt=""
            lazy>
          <v-card
            :class="hasImage && 'position-absolute t-0'"
            min-width="100%"
            height="100%"
            tile
            flat
            class="fill-height width-min-content flex-shrink-1 transparent">
            <nav class="fill-height flex-grow-1">
              <portal-login-introduction :color="hasImage && 'transparent' || 'primary'">
                <template #title>
                  <span :style="hasImage && `color: ${loginBackgroundTextColor};`" class="subtitle-3">{{ $t(title) }}</span>
                </template>
                <template #subtitle>
                  <span :style="hasImage && `color: ${loginBackgroundTextColor};`" class="subtitle-1">{{ $t(subtitle) }}</span>
                </template>
              </portal-login-introduction>
            </nav>
          </v-card>
        </v-card>
        <v-card
          class="flex-grow-1 flex-shrink-0"
          flat>
          <h4 class="my-0">
            {{ $t('generalSettings.loginTitle.title') }}
          </h4>
          <h6 class="text-subtitle grey--text me-2">
            {{ $t('generalSettings.loginTitle.subtitle') }}
          </h6>
          <v-card max-width="350px" flat>
            <translation-text-field
              v-model="loginTitle"
              id="loginTitle"
              :placeholder="$t('generalSettings.loginTitle.placeholder')"
              :default-language="defaultLanguage"
              :supported-languages="supportedLanguages"
              drawer-title="generalSettings.translateTitle" />
          </v-card>
          <h4 class="mb-0 mt-4">
            {{ $t('generalSettings.loginSubtitle.title') }}
          </h4>
          <h6 class="text-subtitle grey--text me-2">
            {{ $t('generalSettings.loginSubtitle.subtitle') }}
          </h6>
          <v-card max-width="350px" flat>
            <translation-text-field
              v-model="loginSubtitle"
              id="loginSubtitle"
              :placeholder="$t('generalSettings.loginSubtitle.placeholder')"
              :default-language="defaultLanguage"
              :supported-languages="supportedLanguages"
              drawer-title="generalSettings.translateSubtitle" />
          </v-card>
          <h4 class="mb-0 mt-4">
            {{ $t('generalSettings.loginBackground.title') }}
          </h4>
          <h6 class="text-subtitle grey--text me-2">
            {{ $t('generalSettings.loginBackground.subtitle') }}
          </h6>
          <portal-general-settings-login-background-selector
            ref="loginBackground"
            v-model="loginBackgroundUploadId"
            :default-data="defaultLoginBackgroundSrc"
            :aspect-ratio="aspectRatio"
            @data-updated="updateLoginBackgroundData"
            @text-color-updated="loginBackgroundTextColor = $event" />
        </v-card>
      </div>
    </v-col>
    <v-col
      cols="12"
      class="pa-0">
      <div class="d-flex my-12 justify-end">
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
    </v-col>
  </v-row>
</template>
<script>
export default {
  props: {
    branding: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    loginTitle: {},
    loginSubtitle: {},
    loginBackgroundUploadId: null,
    loginBackgroundData: null,
    loginBackgroundTextColor: null,
    errorMessage: null,
    aspectRatio: 16 / 9 / 2.5,
    height: 400,
    deleting: false,
    refreshImageIndex: Date.now(),
  }),
  computed: {
    isDefaultBackgroundDeleted() {
      return this.loginBackgroundUploadId === '0';
    },
    hasCustomBackground() {
      return this.loginBackgroundUploadId && !this.isDefaultBackgroundDeleted;
    },
    hasDefaultBackground() {
      return this.branding?.loginBackground?.size && !this.isDefaultBackgroundDeleted;
    },
    hasImage() {
      return this.hasCustomBackground || this.hasDefaultBackground;
    },
    width() {
      return this.height * this.aspectRatio;
    },
    defaultLanguage() {
      return this.branding?.defaultLanguage;
    },
    supportedLanguages() {
      return this.branding?.supportedLanguages;
    },
    defaultLoginTitle() {
      return this.branding?.loginTitle || {};
    },
    defaultLoginSubtitle() {
      return this.branding?.loginSubtitle || {};
    },
    defaultLoginBackgroundSrc() {
      return this.hasDefaultBackground && `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding/loginBackground?v=${this.refreshImageIndex}` || null;
    },
    canDeleteDefaultBackground() {
      return this.branding?.loginBackground?.fileId && !this.isDefaultBackgroundDeleted;
    },
    loginBackgroundSrc() {
      if (this.hasCustomBackground) {
        return this.$utils.convertImageDataAsSrc(this.loginBackgroundData);
      } else if (this.hasDefaultBackground) {
        return this.defaultLoginBackgroundSrc;
      } else {
        return null;
      }
    },
    title() {
      return this.loginTitle[this.defaultLanguage];
    },
    subtitle() {
      return this.loginSubtitle[this.defaultLanguage];
    },
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
    changed() {
      this.$emit('changed', this.changed);
    },
  },  
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.loginBackgroundUploadId = null;
      this.refreshImageIndex = Date.now();
      this.loginTitle = this.defaultLoginTitle && JSON.parse(JSON.stringify(this.defaultLoginTitle)) || {};
      this.loginSubtitle = this.defaultLoginSubtitle && JSON.parse(JSON.stringify(this.defaultLoginSubtitle)) || {};
      this.loginBackgroundTextColor = this.branding?.loginBackgroundTextColor;
      this.loginBackgroundData = this.defaultLoginBackgroundSrc;
      this.$refs.loginBackground.init(this.loginBackgroundData, this.loginBackgroundTextColor);
    },
    save() {
      this.errorMessage = null;

      const branding = Object.assign({}, this.branding);
      this.$refs.loginBackground.preSave(branding);
      branding.loginTitle = this.loginTitle;
      branding.loginSubtitle = this.loginSubtitle;

      this.$root.loading = true;
      return this.$brandingService.updateBrandingInformation(branding)
        .then(() => this.$emit('saved'))
        .then(() => this.$root.$emit('alert-message', this.$t('generalSettings.savedSuccessfully'), 'success'))
        .catch(e => this.errorMessage = String(e))
        .finally(() => this.$root.loading = false);
    },
    deleteDefaultBackground() {
      this.deleting = true;
      window.setTimeout(() => {
        this.loginBackgroundData = null;
        this.loginBackgroundUploadId = '0';
        this.loginBackgroundTextColor = null;
        this.$refs.loginBackground.init(this.loginBackgroundData, this.loginBackgroundTextColor, this.loginBackgroundUploadId);
        this.$nextTick(() => this.deleting = false);
      }, 50);
    },
    deleteBackground() {
      this.deleting = true;
      window.setTimeout(() => {
        this.loginBackgroundData = this.defaultLoginBackgroundSrc;
        this.loginBackgroundUploadId = null;
        this.$refs.loginBackground.init(this.loginBackgroundData, this.loginBackgroundTextColor);
        this.$nextTick(() => this.deleting = false);
      }, 50);
    },
    updateLoginBackgroundData(imageData) {
      if (imageData) {
        this.loginBackgroundData = imageData;
      } else if (this.loginBackgroundUploadId !== '0') {
        this.loginBackgroundData = this.defaultLoginBackgroundSrc;
      }
    },
  }
};
</script>
