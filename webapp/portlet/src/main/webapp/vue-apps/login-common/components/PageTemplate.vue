<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io

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
    <div class="d-flex flex-row full-height">
      <v-card
        color="transparent"
        :min-width="!background && '33%'"
        :max-width="background && '50%'"
        height="100%"
        class="d-none d-sm-flex flex-shrink-0 full-height position-relative"
        flat
        tile>
        <img
          v-if="background"
          :src="background"
          style="height: 100%;"
          alt="">
        <v-card
          :class="background && 'position-absolute t-0'"
          min-width="100%"
          height="100%"
          tile
          flat
          class="fill-height width-min-content flex-shrink-1 transparent">
          <nav class="fill-height flex-grow-1">
            <portal-login-introduction :color="background && 'transparent' || 'primary'">
              <template #title>
                <span :style="background && `color: ${backgroundTextColor};`">{{ title }}</span>
              </template>
              <template #subtitle>
                <span :style="background && `color: ${backgroundTextColor};`">{{ subtitle }}</span>
              </template>
            </portal-login-introduction>
          </nav>
        </v-card>
      </v-card>
      <v-main
        id="mainAppArea"
        role="main"
        class="border-box-sizing fill-height flex-grow-1 flex-shrink-1 pa-0 mb-16 mb-sm-0">
        <v-row class="overflow-x-hidden" no-gutters>
          <v-col v-if="center" class="d-none d-sm-flex" />
          <v-col :class="center && 'align-self-top align-self-sm-center' || 'd-flex flex-column'">
            <portal-login-introduction
              height="150px"
              class="d-sm-none d-block">
              <template v-if="$slots.title" #title>
                <slot name="title"></slot>
              </template>
              <template v-else #title>
                {{ companyName }}
              </template>
            </portal-login-introduction>
            <div :class="center && 'd-block' || 'd-sm-flex flex-column flex-grow-1 px-sm-8'" class="px-4 px-sm-0">
              <v-card-title v-if="$slots.title" class="display-1 primary--text px-0 d-none d-sm-flex text-wrap">
                <slot name="title"></slot>
              </v-card-title>
              <v-card-title
                v-else
                class="display-1 primary--text px-0 d-none d-sm-flex text-break text-center justify-center mx-n12">
                {{ companyName }}
              </v-card-title>
              <div :class="center && 'd-block' || 'd-sm-flex flex mx-0'">
                <slot></slot>
              </div>
            </div>
            <portal-login-branding-image
              v-if="brandingImage"
              :params="params"
              class="d-none d-md-flex" />
          </v-col>
          <v-col v-if="center" class="d-none d-sm-flex" />
        </v-row>
      </v-main>
    </div>
  </v-app>
</template>
<script>
export default {
  props: {
    params: {
      type: Object,
      default: null,
    },
    center: {
      type: Boolean,
      default: false,
    },
    brandingImage: {
      type: Boolean,
      default: false,
    },
    hideSuccessMessage: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    companyName() {
      return this.params?.companyName;
    },
    background() {
      return this.params?.authenticationBackground;
    },
    backgroundTextColor() {
      return this.params?.authenticationTextColor;
    },
    title() {
      return this.$t(this.authenticationTitleKey);
    },
    subtitle() {
      return this.$t(this.authenticationSubtitleKey);
    },
    authenticationTitleKey() {
      return this.params?.authenticationTitle;
    },
    authenticationSubtitleKey() {
      return this.params?.authenticationSubtitle;
    },
    successCode() {
      return this.params?.success || this.params?.successCode || this.params?.successMessage;
    },
    errorCode() {
      return this.params?.error || this.params?.errorCode || this.params?.errorMessage;
    },
    errorField() {
      return this.params?.errorField;
    },
    successMessage() {
      return this.successCode && this.$te(`UILoginForm.label.${this.successCode}`)
        && this.$t(`UILoginForm.label.${this.successCode}`)
        || this.successCode;
    },
    errorMessage() {
      return this.errorCode && this.$te(`UILoginForm.label.${this.errorCode}`)
        && this.$t(`UILoginForm.label.${this.errorCode}`)
        || this.errorCode;
    },
  },
  watch: {
    successMessage: {
      immediate: true,
      handler: function() {
        if (this.successMessage?.trim()?.length && !this.hideSuccessMessage) {
          this.displayAlert(this.successMessage, 'success');
        }
      },
    },
    errorMessage: {
      immediate: true,
      handler: function() {
        if (!this.errorField && this.errorMessage?.trim()?.length) {
          this.displayAlert(this.errorMessage, 'error');
        }
      },
    },
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
  methods: {
    displayAlert(message, type) {
      window.setTimeout(() => {
        this.$root.$emit('alert-message', message, type);
      }, 200);
    },
  },
};
</script>