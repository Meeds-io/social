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
  <div v-if="loaded">
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          :id="id"
          :href="link"
          :target="targetLink"
          :aria-label="providerButtonLabel"
          min-width="auto"
          color="primary"
          class="text-none elevation-0 btn"
          v-bind="attrs"
          outlined
          v-on="on"
          @click="clickOnProviderButton">
          <v-img
            v-if="providerImage && displayProvidersIcons"
            :src="providerImage"
            :class="displayText && 'me-2'"
            height="25"
            max-width="25"
            eager />
          <v-icon v-else :class="providerIcon" />
          <span v-if="displayText" class="text-body text-truncate">{{ providerButtonLabel }}</span>
        </v-btn>
      </template>
      <span>{{ providerButtonLabel }}</span>
    </v-tooltip>
  </div>
</template>
<script>
export default {
  props: {
    provider: {
      type: Object,
      default: null,
    },
    params: {
      type: Object,
      default: null,
    },
    displayText: {
      type: Boolean,
      default: false,
    },
    rememberme: {
      type: Boolean,
      default: false,
    },
    displayProvidersIcons: {
      type: Boolean,
      default: true,
    },
  },
  data: () => ({
    defaultProviders: ['facebook', 'openid', 'linkedin', 'twitter', 'google'],
    objectType: 'cmsPortlet',
    loaded: false,
  }),
  computed: {
    providerKeyLowerCase() {
      return this.provider?.key?.toLowerCase();
    },
    providerKeyCapitalize() {
      return `${this.providerKeyLowerCase.charAt(0).toUpperCase()}${this.providerKeyLowerCase.substring(1)}`;
    },
    id() {
      return `login-${this.providerKeyLowerCase}`;
    },
    link() {
      const link = this.provider?.url;
      if (link && link.startsWith('/')) {
        return this.rememberme && `${link}&_rememberme=true` || link;
      }
      return link;
    },
    targetLink() {
      return this.link && this.link.startsWith('/') && '_self' || '_blank';
    },
    providerButtonLabel() {
      if (this.providerTranslation) {
        return this.providerTranslation;
      }
      const providerName = this.$te(`UILoginForm.label.provider.${this.providerKeyLowerCase}`)
        ? this.$t(`UILoginForm.label.provider.${this.providerKeyLowerCase}`)
        : this.providerKeyCapitalize;
      return this.$t('UILoginForm.label.singInWith', {0: providerName});
    },
    providerIcon() {
      return this.provider?.icon;
    },
    providerImage() {
      const image = this.provider?.image;
      if (!image && this.defaultProviders.indexOf(this.providerKeyLowerCase) >= 0) {
        return `/platform-ui/skin/images/oauth/${this.providerKeyLowerCase}.png`;
      } else {
        return image;
      }
    },
  },
  created() {
    if (this.params?.oAuthProviderLabels[this.provider?.key]) {
      this.providerTranslation = this.params.oAuthProviderLabels[this.provider.key];
    }
    this.loaded = true;
    this.$root.$on('login-form-settings-updated', () => {
      if (this.params?.oAuthProviderLabels[this.provider?.key]) {
        this.providerTranslation = this.params.oAuthProviderLabels[this.provider.key];
      }
    });
  },
  methods: {
    clickOnProviderButton() {
      this.$emit('submit');
    },
  },
};
</script>
