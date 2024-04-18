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
  <v-list-item
    :href="navigationUri"
    :target="target"
    dense>
    <v-list-item-icon class="my-auto d-flex">
      <v-icon class="icon-default-color icon-default-size ma-auto">
        {{ navigationIcon }}
      </v-icon>
    </v-list-item-icon>
    <v-list-item-content class="d-flex">
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
    </v-list-item-content>
  </v-list-item>
</template>
<script>
export default {
  props: {
    navigation: {
      type: Object,
      default: null
    },
    spaceUnreadItems: {
      type: Object,
      default: null
    },
  },
  computed: {
    navigationIcon() {
      return this.navigation.icon || 'fas fa-folder';
    },
    navigationLabel() {
      return this.navigation?.label;
    },
    navigationUri() {
      return this.navigation?.link && this.urlVerify(this.navigation?.link) || this.navigation?.uri;
    },
    target() {
      return this.navigation?.target === 'SAME_TAB' && '_self' || '_blank';
    },
    badgeApplicationName() {
      // TODO to know what application id to associate to each page uri
      // May be missing inside navigation REST.
      const uriParts = this.navigationUri.split('/');
      const pageUri = uriParts.length > 5 && uriParts[5] || null;
      return (!pageUri
          || pageUri.includes('stream')
          || pageUri.includes('activity')
          || pageUri.includes('home')
      ) && 'activity' || pageUri;
    },
    unreadBadge() {
      return this.spaceUnreadItems
        && (this.badgeApplicationName === 'activity' && Object.values(this.spaceUnreadItems).reduce((sum, v) => sum += v, 0) || 0)
      || 0;
    },
    navigationLabelStyle() {
      return this.unreadBadge > 0 ? { 'max-width': '140px' } : { 'max-width': '200px'};
    }
  },
  methods: {
    urlVerify(url) {
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/) && this.isValidUrl(url) ) {
        url = `//${url}`;
      }
      return url ;
    },
    isValidUrl(str) {
      const pattern = new RegExp(
        '^([a-zA-Z]+:\\/\\/)?' +
        '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|' +
        '((\\d{1,3}\\.){3}\\d{1,3}))' +
        '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*' +
        '(\\?[;&a-z\\d%_.~+=-]*)?' +
        '(\\#[-a-z\\d_]*)?$',
        'i'
      );
      return pattern.test(str);
    }
  }
};
</script>