<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association
  contact@meeds.io
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
  <v-container class="siteDetails" :class="extraClass">
    <v-card
      v-if="site"
      class="full-height"
      color="transparent"
      flat>
      <row
        v-if="!$root.displaySequentially && !displaySequentially"
        class="px-4 pt-3 position-absolute z-index-one">
        <v-btn
          class="white"
          elevation="2"
          icon
          small
          @click="$emit('close')">
          <v-icon small>
            {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
          </v-icon>
        </v-btn>
      </row>
      <img
        :src="site?.bannerUrl"
        :alt="site?.displayName"
        width="100%"
        height="auto"
        class="mt-1 card-border-radius"
        eager>
      <v-card-title :title="site?.displayName" class="text-body font-weight-bold">
        {{ site?.displayName }}
      </v-card-title>
      <v-card-subtitle
        v-sanitized-html="site?.description"
        class="py-2 rich-editor-content" />
      <site-navigation-tree
        v-if="site?.siteNavigations?.length"
        :navigations="site.siteNavigations"
        :site-name="site?.name"
        :enable-change-home="enableChangeHome"
        collapsed />
    </v-card>
    <exo-confirm-dialog
      ref="confirmDialog"
      :title="$t('menu.confirmation.title.changeHome')"
      :message="confirmMessage"
      :ok-label="$t('menu.confirmation.ok')"
      :cancel-label="$t('menu.confirmation.cancel')"
      @ok="changeHome"
      @opened="$root.$emit('dialog-opened')"
      @closed="$root.$emit('dialog-closed')" />
  </v-container>
</template>

<script>
export default {
  props: {
    site: {
      type: Object,
      default: null
    },
    enableChangeHome: {
      type: Boolean,
      default: false,
    },
    displaySequentially: {
      type: Boolean,
      default: false,
    },
    expandSelectionOnly: {
      type: Boolean,
      default: false,
    },
    extraClass: {
      type: String,
      default: '',
    },
  },
  data: () => ({
    selectedNavigation: null,
    selectedHomeUri: null,
    homeLink: eXo.env.portal.homeLink,
  }),
  computed: {
    confirmMessage() {
      return this.$t('menu.confirmation.message.changeHome', {
        0: `<b>${ this.selectedNavigation?.label || this.selectedNavigation?.name }</b>`,
      });
    },
  },
  created() {
    this.$root.$on('update-home-link', this.selectHome);
    document.addEventListener('homeLinkUpdated', () => this.homeLink = eXo.env.portal.homeLink);
  },
  beforeDestroy() {
    this.$root.$off('update-home-link', this.selectHome);
    document.removeEventListener('homeLinkUpdated', () => this.homeLink = eXo.env.portal.homeLink);
  },
  methods: {
    selectHome(nav, homeUri) {
      if (!nav?.pageKey && !nav.url || !homeUri) {
        return;
      }
      this.selectedNavigation = nav;
      this.selectedHomeUri = homeUri;
      this.$refs?.confirmDialog?.open?.();
    },
    changeHome() {
      const url = this.selectedHomeUri;
      this.$navigationService.saveUserHomePage(url)
        .then(() => {
          this.homeLink = eXo.env.portal.homeLink = url;
          if (document.querySelector('#UserHomePortalLink')) {
            document.querySelector('#UserHomePortalLink').href = this.homeLink;
          }
          document.dispatchEvent(new CustomEvent('homeLinkUpdated', {detail: this.homeLink}));
          this.selectedNavigation = null;
          this.selectedHomeUri = null;
        })
        .catch(() => document.dispatchEvent(new CustomEvent('alert-message', {detail: {
          alertMessage: this.$t('navigation.home.invalidUri'),
          alertType: 'error',
        }})));
    }
  }

};
</script>