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
      class="d-flex flex-column application-border-radius border-color"
      :elevation="hoverCard && 3 || 0"
      :height="height"
      :href="spaceUrl"
      :max-height="height"
      :max-width="maxWidth"
      :min-height="minHeight"
      :min-width="minWidth"
      width="auto">
      <space-card-unread-badge :space="space" />
      <v-card
        class="d-flex mt-4 px-4"
        flat
        :max-height="avatarSize"
        max-width="100%"
        :min-height="avatarSize"
        min-width="100%">
        <v-card
          class="spaceAvatar overflow-hidden d-flex align-center justify-center z-index-two"
          flat
          :max-height="avatarSize"
          :max-width="avatarSize"
          :min-height="avatarSize"
          :min-width="avatarSize">
          <img
            :alt="$t('spaceList.spaceAvatar.alt')"
            class="overflow-hidden"
            height="100%"
            :src="space.avatarUrl"
            style="max-width: 1000%; max-height: 100%;"
            width="auto">
        </v-card>
        <div class="d-flex flex-column flex-grow-1 justify-center flex-shrink-1 overflow-hidden ps-4 ps-sm-3">
          <div
            class="flex-shrink-0 text-truncate-2 max-height-2lh font-weight-bold line-height-normal full-width"
            v-text="spaceDisplayName"></div>
        </div>
      </v-card>
      <div
        v-if="!$root.isMobile"
        class="flex-grow-1 flex-shrink-1 px-4 mt-4">
        <div
          v-sanitized-html="spaceDescriptionText"
          class="text-truncate-3 max-height-3lh full-width flex-shrink-1"></div>
      </div>
      <v-card
        class="d-flex align-center full-width flex-grow-0 flex-shrink-0 my-2 px-2 position-absolute b-0"
        flat>
        <div
          v-if="displayMembersCount"
          class="d-flex align-center ps-2">
          <div
            v-sanitized-html="spaceMembersCount"
            class="flex-shrink-0 text-subtitle"></div>
        </div>
        <v-spacer />
        <space-card-menu
          class="ms-1"
          :space="space"
          :space-action-extensions="enabledSpaceActionExtensions" />
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
      spaceDisplayName () {
        return this.space.displayName;
      },
      spaceDescription () {
        return this.space.description?.replace?.(/<p>/g, '<div>')?.replace?.(/<\/p>/g, '</div>')?.replace?.(/<ul>/g, '<ul class="ma-0 pa-0">') || '';
      },
      spaceDescriptionText () {
        return this.$utils.htmlToText(this.spaceDescription);
      },
      spaceMembersCount () {
        return this.$t('spaceList.spaceMembers', { 0: `<strong>${this.space.membersCount}</strong>` });
      },
      publicSiteName () {
        return this.space?.publicSiteName;
      },
      spacePublicSiteUrl () {
        return this.publicSiteName && `${eXo.env.portal.context}/${this.publicSiteName}`;
      },
      spaceUrl () {
        if (this.$root.anonymous) {
          return this.spacePublicSiteUrl ? this.spacePublicSiteUrl : '/portal/login';
        }
        return `${eXo.env.portal.context}/s/${this.space.id}`;
      },
      enabledSpaceActionExtensions () {
        if (!this.spaceActionExtensions || !this.space || !this.space.isMember) {
          return [];
        }
        return this.spaceActionExtensions.slice().filter(extension => extension.enabled(this.space));
      },
    },
  };
</script>