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
      <h4 class="font-weight-bold mb-4 mt-8">
        {{ $t('generalSettings.loginCharacteristics') }}
      </h4>
    </v-col>
    <v-col
      cols="12"
      lg="6"
      class="pa-0">
      <div class="d-flex">
        <v-card
          class="flex-grow-0 flex-shrink-0 me-4 position-relative"
          width="33%"
          flat
          tile>
          <v-responsive :aspect-ratio="aspectRatio">
            <v-img
              :src="loginBackgroundSrc" 
              height="100%"
              width="100%"
              role="presentation"
              eager>
              <v-btn
                v-if="defaultLoginBackgroundData"
                color="error"
                class="position-absolute r-3 mt-2 z-index-two white error"
                icon
                border
                outlined
                @click="deleteDefaultBackground">
                <v-icon>fas fa-trash</v-icon>
              </v-btn>
              <portal-login-introduction :branding="branding" :color="loginBackgroundSrc && 'transparent' || 'primary'">
                <template #title>
                  <span :style="loginBackgroundSrc && `color: ${loginBackgroundTextColor};`" class="title font-weight-bold">{{ $t(title) }}</span>
                </template>
                <template #subtitle>
                  <span :style="loginBackgroundSrc && `color: ${loginBackgroundTextColor};`" class="subtitle-1">{{ $t(subtitle) }}</span>
                </template>
              </portal-login-introduction>
            </v-img>
          </v-responsive>
        </v-card>
        <v-card
          class="flex-grow-1 flex-shrink-0"
          width="calc(67% - 16px)"
          flat>
          <h4 class="my-0">
            {{ $t('generalSettings.loginTitle.title') }}
          </h4>
          <h6 class="text-subtitle grey--text">
            {{ $t('generalSettings.loginTitle.subtitle') }}
          </h6>
          <v-card max-width="350px" flat>
            <translation-text-field
              v-model="loginTitle"
              id="loginTitle"
              :placeholder="$t('generalSettings.loginTitle.placeholder')"
              :default-language="defaultLanguage"
              :supported-languages="supportedLanguages"
              drawer-title="generalSettings.translateTitle"
              required />
          </v-card>
          <h4 class="mb-0 mt-4">
            {{ $t('generalSettings.loginSubtitle.title') }}
          </h4>
          <h6 class="text-subtitle grey--text">
            {{ $t('generalSettings.loginSubtitle.subtitle') }}
          </h6>
          <v-card max-width="350px" flat>
            <translation-text-field
              v-model="loginSubtitle"
              id="loginSubtitle"
              :placeholder="$t('generalSettings.loginSubtitle.placeholder')"
              :default-language="defaultLanguage"
              :supported-languages="supportedLanguages"
              drawer-title="generalSettings.translateSubtitle"
              required />
          </v-card>
        </v-card>
      </div>
    </v-col>
    <v-col
      cols="12"
      lg="6"
      class="pa-0">
      <h4 class="my-0">
        {{ $t('generalSettings.loginBackground.title') }}
      </h4>
      <h6 class="text-subtitle grey--text">
        {{ $t('generalSettings.loginBackground.subtitle') }}
      </h6>
      <portal-general-settings-login-background-selector
        ref="loginBackground"
        v-model="loginBackgroundUploadId"
        :default-data="defaultLoginBackgroundData"
        :aspect-ratio="aspectRatio"
        @data-updated="updateLoginBackgroundData"
        @text-color-updated="loginBackgroundTextColor = $event" />
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
  }),
  computed: {
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
    defaultLoginBackgroundData() {
      return this.loginBackgroundData && this.branding?.loginBackground?.data || null;
    },
    loginBackgroundSrc() {
      if (this.loginBackgroundData) {
        return this.$utils.convertImageDataAsSrc(this.loginBackgroundData);
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
    isValidForm() {
      return this.title?.length
          && this.subtitle?.length;
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
    isValidForm() {
      this.$emit('validity-check', this.isValidForm);
    },
    changed() {
      this.$emit('changed', this.changed);
    },
    errorMessage() {
      if (this.errorMessage) {
        this.$root.$emit('alert-message', this.$t(this.errorMessage), 'error');
      } else {
        this.$root.$emit('close-alert-message');
      }
    },
  },
  methods: {
    init() {
      this.loginTitle = this.defaultLoginTitle && JSON.parse(JSON.stringify(this.defaultLoginTitle)) || {};
      this.loginSubtitle = this.defaultLoginSubtitle && JSON.parse(JSON.stringify(this.defaultLoginSubtitle)) || {};
      this.loginBackgroundTextColor = this.branding?.loginBackgroundTextColor;
      this.loginBackgroundData = this.branding?.loginBackground?.data;
      this.loginBackgroundUploadId = null;
      this.$refs.loginBackground.init(this.loginBackgroundData, this.loginBackgroundTextColor);
    },
    preSave(branding) {
      this.$refs.loginBackground.preSave(branding);
      branding.loginTitle = this.loginTitle;
      branding.loginSubtitle = this.loginSubtitle;
    },
    deleteDefaultBackground() {
      this.loginBackgroundData = null;
      this.loginBackgroundUploadId = '0';
      this.loginBackgroundTextColor = null;
      this.$refs.loginBackground.init(this.loginBackgroundData, this.loginBackgroundTextColor, this.loginBackgroundUploadId);
    },
    updateLoginBackgroundData(imageData) {
      if (imageData) {
        this.loginBackgroundData = imageData;
      } else if (this.loginBackgroundUploadId !== '0') {
        this.loginBackgroundData = this.branding?.loginBackground?.data;
      }
    },
  }
};
</script>
