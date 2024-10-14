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
    <v-list-item-title class="menu-text-color">
      <div class="d-flex align-center justify-space-between my-auto">
        <span class="text-truncate" :style="navigationLabelStyle">{{ navigationLabel }}</span>
        <v-chip
          v-if="unreadBadge"
          color="error-color-background"
          min-width="22"
          height="22"
          dark>
          {{ unreadBadge }}
        </v-chip>
      </div>
    </v-list-item-title>
    <v-list-item-action
      v-if="!spaceUnreadItems && enableChangeHome && !isNodeGroup && (isHomeLink || showAction)"
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
    spaceUnreadItems: {
      type: Object,
      default: null
    },
  },
  data: () => ({
    selectedNodeUri: eXo.env.portal.selectedNodeUri,
    selectedNavigation: null,
    homeLink: eXo.env.portal.homeLink,
    showAction: false,
  }),
  computed: {
    navigationUri() {
      if (!this.navigation.pageKey || !this.navigation.siteKey) {
        return '';
      }
      let url = null;
      if (this.navigation.pageLink) {
        url = this.navigation.pageLink;
      } else if (this.navigation.siteKey.type === 'GROUP') {
        url = `${eXo.env.portal.context}/g/${this.navigation.siteKey.name.replace?.(/\//g, ':')}/${this.navigation.uri}`;
      } else {
        url = `${eXo.env.portal.context}/${this.navigation.siteKey.name}/${this.navigation.uri}`;
      }
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url;
    },
    uri() {
      return this.isNodeGroup ? null : this.navigationUri;
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
    unreadBadge() {
      return this.spaceUnreadItems
        && Object.values(this.spaceUnreadItems).reduce((sum, v) => sum += v, 0)
        || 0;
    },
    navigationLabel() {
      return this.navigation?.label;
    },
    navigationLabelStyle() {
      return this.unreadBadge > 0 ? { 'max-width': '140px' } : { 'max-width': '200px'};
    },
  },
  created() {
    document.addEventListener('homeLinkUpdated', () => this.homeLink = eXo.env.portal.homeLink);
  },
  methods: {
    selectHome(event) {
      event.preventDefault();
      event.stopPropagation();
      if (this.homeLink !== this.navigationUri) {
        this.$root.$emit('update-home-link', this.navigation);
      }
    }
  }
};
</script>