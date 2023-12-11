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
  <a
    :href="uri"
    :target="target"
    :ripple="false"
    class="d-flex px-0"
    @mouseover="showAction = true"
    @mouseleave="showAction = false">
    <v-list-item-icon size="20" class="d-flex align-center justify-center my-auto ms-0 me-3">
      <v-icon size="20" class="icon-default-color">
        {{ icon }}
      </v-icon>
    </v-list-item-icon>
    <v-list-item-title class="font-weight-normal text-font-size text-color">
      {{ navigation.label }}
    </v-list-item-title>
    <v-list-item-action
      v-if="enableChangeHome && !isNodeGroup && (isHomeLink || showAction)"
      class="my-auto">
      <v-btn
        icon
        @click="selectHome($event)">
        <v-icon
          :class="isHomeLink && 'primary--text' || 'icon-default-color'"
          small>
          fa-house-user
        </v-icon>
      </v-btn>
    </v-list-item-action>
  </a>
</template>

<script>
export default {
  props: {
    navigation: {
      type: Object,
      default: null
    },
    enableChangeHome: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    selectedNodeUri: eXo.env.portal.selectedNodeUri,
    selectedNavigation: null,
    homeLink: eXo.env.portal.homeLink,
    showAction: false,
  }),
  computed: {
    uri() {
      return this.isNodeGroup ? null : this.navigationUri(this.navigation);
    },
    target() {
      return this.navigation?.target === 'SAME_TAB' && '_self' || '_blank';
    },
    icon() {
      return this.navigation?.icon || 'fas fa-folder';
    },
    isNodeGroup() {
      return !this.navigation.pageKey;
    },
    isHomeLink() {
      return this.uri === this.homeLink;
    },
  },
  created() {
    document.addEventListener('homeLinkUpdated', () => this.homeLink = eXo.env.portal.homeLink);
  },
  methods: {
    navigationUri() {
      if (!this.navigation.pageKey) {
        return '';
      }
      let url = this.navigation.pageLink || `/portal/${this.navigation.siteKey.name}/${this.navigation.uri}`;
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url;
    },
    selectHome(event) {
      event.preventDefault();
      event.stopPropagation();
      if (this.homeLink !== this.navigationUri()) {
        this.$root.$emit('update-home-link', this.navigation);
      }
    }
  }
};
</script>