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
      flat
      class="full-height">
      <row
        v-if="!displaySequentially"
        class="px-4 pt-3 position-absolute z-index-one">
        <v-btn
          class="white"
          elevation="2"
          icon
          small
          @click="$emit('close')">
          <v-icon small>
            {{ $root.ltr && 'fa-arrow-left' || 'fa-arrow-right' }}
          </v-icon>
        </v-btn>
      </row>
      <img
        :src="site?.bannerUrl"
        :alt="site?.displayName"
        width="100%"
        height="auto"
        class="mx-1 mt-1 card-border-radius"
        eager>
      <v-card-title :title="site?.displayName" class="text-capitalize font-weight-bold text-subtitle-1">
        {{ site?.displayName }}
      </v-card-title>
      <v-card-subtitle v-sanitized-html="site?.description" class="text-subtitle-2 py-2 text-color rich-editor-content" />
      <site-navigation-tree
        v-if="site?.siteNavigations?.length"
        :navigations="site.siteNavigations"
        :site-name="site?.name"
        :enable-change-home="enableChangeHome" />
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
    displaySequentially: {
      type: Boolean,
      default: false,
    },
    enableChangeHome: {
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
    homeLink: eXo.env.portal.homeLink,
  }),
  computed: {
    confirmMessage() {
      return this.$t('menu.confirmation.message.changeHome', {
        0: `<b>${ this.selectedNavigation?.label }</b>`,
      });
    },
  },
  created() {
    this.$root.$on('update-home-link', this.selectHome);
    document.addEventListener('homeLinkUpdated', () => this.homeLink = eXo.env.portal.homeLink);
  },
  methods: {
    navigationUri(navigation) {
      if (!navigation.pageKey) {
        return '';
      }
      let url = navigation.pageLink || `/portal/${navigation.siteKey.name}/${navigation.uri}`;
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url;
    },
    selectHome(nav) {
      if (!nav?.pageKey) {
        return;
      }
      this.selectedNavigation = nav;
      this.$refs.confirmDialog.open();
    },
    changeHome() {
      const selectedNavigationUri = this.navigationUri(this.selectedNavigation);
      this.$settingService.setSettingValue('USER', eXo.env.portal.userName, 'PORTAL', 'HOME', 'HOME_PAGE_URI', selectedNavigationUri)
        .then(() => {
          this.homeLink = eXo.env.portal.homeLink = selectedNavigationUri;
          document.querySelector('#UserHomePortalLink').href = this.homeLink;
          document.dispatchEvent(new CustomEvent('homeLinkUpdated', {detail: this.homeLink}));
          this.selectedNavigation = null;
        });
    }
  }

};
</script>