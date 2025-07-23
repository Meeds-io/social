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
  <v-app class="full-height full-width">
    <v-hover v-slot="{ hover }">
      <v-card
        flat
        :color="hasCustomBackground && 'transparent' || 'primary'"
        class="full-height d-flex"
        :class="textAlign">
        <div
          v-if="$root.canEdit && hover"
          class="position-absolute t-0 r-0"
          :class="{
            'l-0': $vuetify.rtl,
            'r-0': !$vuetify.rtl,
          }">
          <v-fab-transition hide-on-leave>
            <v-btn
              :title="$t('sidebarLogin.settings.editTooltip')"
              class="z-index-two me-2 mt-2"
              small
              icon
              @click="$root.$emit('sidebar-login-settings')">
              <v-icon size="18">fa-cog</v-icon>
            </v-btn>
          </v-fab-transition>
        </div>

        <img v-if="hasCustomBackground"
             :src="backgroundPath"
             :alt="backgroundAltText || ''"
             class="full-height full-width object-fit-cover rounded-0">
        <v-card
          :class="hasCustomBackground && 'position-absolute t-0'"
          tile
          flat
          class="fill-height transparent full-width">
          <v-card-title class="d-flex flex-column pa-0 fill-height"
            :class="[vAlign, hAlign]">
            <span class="text-title text-wrap text-break mx-4 mt-4">
              {{ title }}
            </span>
            <span class="text-body text-wrap text-break mx-4 mb-4">
              {{ subtitle }}
            </span>
          </v-card-title>
        </v-card>
      </v-card>
    </v-hover>
    <sidebar-login-settings-drawer :branding="branding" :background-path="backgroundPath" />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    branding: null,
    refreshImageIndex: Date.now(),
    loginBackgroundData: null,
  }),
  computed: {
    title() {
      return this.$t(this.branding?.loginTitle[eXo.env.portal.language] || this.branding?.loginTitle[this.defaultLanguage]);
    },
    subtitle() {
      return this.$t(this.branding?.loginSubtitle[eXo.env.portal.language] || this.branding?.loginSubtitle[this.defaultLanguage]);
    },
    defaultLanguage() {
      return this.branding?.defaultLanguage;
    },
    supportedLanguages() {
      return this.branding?.supportedLanguages;
    },
    backgroundAltText() {
      return this.branding?.loginBackgroundAltText;
    },
    backgroundPath() {
      if (this.loginBackgroundData) {
        return this.loginBackgroundData;
      }
      if (this.hasCustomBackground) {
        return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding/loginBackground?v=${this.refreshImageIndex}`;
      } else {
        return null;
      }
    },
    hasCustomBackground() {
      return this.loginBackgroundData || this.branding?.loginBackground?.fileId;
    },
    vAlign() {
      switch (this.$root.vAlign) {
      case 'START': {
        return 'justify-start';
      }
      case 'END': {
        return 'justify-end';
      }
      default: {
        return 'justify-center';

      }
      }
    },
    hAlign() {
      switch (this.$root.hAlign) {
      case 'START': {
        return 'align-start';
      }
      case 'END': {
        return 'align-end';

      }
      default: {
        return 'align-center';
      }
      }
    },
    textAlign() {
      switch (this.$root.hAlign) {
      case 'START': {
        return 'text-left';
      }
      case 'END': {
        return 'text-right';
      }
      default: {
        return 'text-center';
      }
      }
    }
  },
  created() {
    this.branding = this.$root.branding;
    this.$root.$on('sidebar-login-settings-updated', this.refresh);
  },
  methods: {
    refresh(branding, vAlign, hAlign, imageData) {
      this.branding = branding;
      this.$root.vAlign=vAlign;
      this.$root.hAlign=hAlign;
      this.loginBackgroundData = imageData;
    }
  },
};
</script>
