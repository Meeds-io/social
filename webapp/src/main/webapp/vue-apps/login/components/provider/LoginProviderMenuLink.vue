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
  <v-list-item
    :id="id"
    :href="link"
    :target="targetLink"
    min-width="auto"
    rel="nofollow noreferrer noopener"
    color="primary"
    outlined
    dense
    @click="clickOnProviderButton">
    <v-list-item-icon class="me-2">
      <v-img
        v-if="providerImage"
        :src="providerImage"
        height="16"
        max-width="16"
        class="my-auto me-2"
        eager />
      <v-icon v-else :class="providerIcon" />
    </v-list-item-icon>
    <v-list-item-content>
      <v-list-item-title class="text-truncate">{{ providerButtonLabel }}</v-list-item-title>
    </v-list-item-content>
  </v-list-item>
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
    rememberme: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    defaultProviders: ['facebook', 'openid', 'linkedin', 'twitter', 'google'],
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
  methods: {
    clickOnProviderButton() {
      this.$emit('submit');
    },
  },
};
</script>