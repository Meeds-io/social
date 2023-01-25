<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
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
  <v-list-item :href="navigationUri">
    <v-list-item-icon class="me-3 py-3 my-0 d-flex">
      <i aria-hidden="true" :class="navigationIcon"></i>
    </v-list-item-icon>
    <v-list-item-content class="my-n1">
      <div class="d-flex align-center justify-space-between">
        <span>{{ navigationLabel }}</span>
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
      return `${this.applicationIcon(this.navigation.icon)} icon-default-color icon-default-size`;
    },
    navigationLabel() {
      return this.navigation?.label;
    },
    navigationUri() {
      return this.navigation?.uri || '';
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
  },
  methods: {
    applicationIcon(iconClass) {
      const navigationIcon = iconClass || '';
      if (!navigationIcon?.length) {
        return 'fas fa-sticky-note';
      }
      if (navigationIcon.includes('uiIconAppSpaceHomePage')) {
        return 'fas fa-stream';
      } else if (navigationIcon.includes('uiIconAppMembersPortlet')) {
        return 'fas fa-users';
      } else if (navigationIcon.includes('uiIconAppTasksManagement') || navigationIcon.includes('uiIconApptasks')) {
        return 'fas fa-tasks';
      } else if (navigationIcon.includes('uiIconAppNotes') || navigationIcon.includes('uiIconAppnotes') ) {
        return 'fas fa-clipboard';
      } else if (navigationIcon.includes('uiIconAppSpaceWallet')) {
        return 'fas fa-wallet';
      } else if (navigationIcon.includes('uiIconAppSpaceSettingPortlet')) {
        return 'fas fa-cog';
      } else {
        return navigationIcon;
      } 
    },
  },
};
</script>