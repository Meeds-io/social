<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <v-hover v-model="hoverCard">
    <v-card
      :href="spaceUrl"
      :min-height="minHeight"
      :height="height"
      :max-height="height"
      :elevation="hoverCard && 3 || 0"
      :min-width="minWidth"
      :max-width="maxWidth"
      width="auto"
      class="d-flex flex-column application-border-radius border-color">
      <space-card-unread-badge :space="space" />
      <v-card
        :min-height="avatarSize"
        :max-height="avatarSize"
        min-width="100%"
        max-width="100%"
        class="d-flex mt-4 px-4"
        flat>
        <v-card
          :min-width="avatarSize"
          :max-width="avatarSize"
          :min-height="avatarSize"
          :max-height="avatarSize"
          class="spaceAvatar overflow-hidden d-flex align-center justify-center z-index-two"
          flat>
          <img
            :src="space.avatarUrl"
            :alt="$t('spaceList.spaceAvatar.alt')"
            style="max-width: 1000%; max-height: 100%;"
            height="100%"
            width="auto"
            class="overflow-hidden">
        </v-card>
        <div class="d-flex flex-column flex-grow-1 justify-center flex-shrink-1 overflow-hidden ps-4 ps-sm-3">
          <div
            v-text="spaceDisplayName"
            class="flex-shrink-0 text-truncate-2 max-height-2lh font-weight-bold line-height-normal full-width"></div>
        </div>
      </v-card>
      <div v-if="!$root.isMobile" class="flex-grow-1 flex-shrink-1 px-4 mt-4">
        <div
          v-sanitized-html="spaceDescription"
          class="text-truncate-3 max-height-3lh full-width flex-shrink-1"></div>
      </div>
      <v-card
        class="d-flex align-center full-width flex-grow-0 flex-shrink-0 my-2 px-2 position-absolute b-0"
        flat>
        <div v-if="displayMembersCount" class="d-flex align-center ps-2">
          <div
            v-sanitized-html="spaceMembersCount"
            class="flex-shrink-0 text-subtitle"></div>
        </div>
        <v-spacer />
        <space-card-menu
          :space="space"
          :space-action-extensions="enabledSpaceActionExtensions"
          class="ms-1" />
      </v-card>
    </v-card>
  </v-hover>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: () => ({}),
    },
    height: {
      type: Number,
      default: () => 227,
    },
    minHeight: {
      type: Number,
      default: () => 227,
    },
    minWidth: {
      type: Number,
      default: () => 220,
    },
    maxWidth: {
      type: Number,
      default: () => (366 - 18),
    },
    displayMembersCount: {
      type: Boolean,
      default: false,
    },
    spaceActionExtensions: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    avatarSize: 65,
    hoverCard: false,
  }),
  computed: {
    spaceDisplayName() {
      return this.space.displayName;
    },
    spaceDescription() {
      return this.space.description?.replace?.(/<p>/g, '<div>')?.replace?.(/<\/p>/g, '</div>')?.replace?.(/<ul>/g, '<ul class="ma-0 pa-0">') || '';
    },
    spaceMembersCount() {
      return this.$t('spaceList.spaceMembers', {0: `<strong>${this.space.membersCount}</strong>`});
    },
    spaceUrl() {
      return `${eXo.env.portal.context}/s/${this.space.id}`;
    },
    enabledSpaceActionExtensions() {
      if (!this.spaceActionExtensions || !this.space || !this.space.isMember) {
        return [];
      }
      return this.spaceActionExtensions.slice().filter(extension => extension.enabled(this.space));
    },
  },
};
</script>