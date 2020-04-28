<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
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
  <v-list-item class="suggestions-list-item pa-0">
    <v-list-item-avatar
      :size="avatarSize"
      class="spaceAvatar">
      <v-img :src="!skeleton && avatarUrl || ''"
             :class="skeleton && 'skeleton-background'"></v-img>
    </v-list-item-avatar>
    <v-list-item-content class="pb-3">
      <v-list-item-title
        class="body-2 font-weight-bold suggestions-list-item-title">
        <a
          :href="url"
          :class="skeleton && 'skeleton-background skeleton-text skeleton-list-item-title skeleton-border-radius'"
          class="text-color">
          {{ space.displayName }}
        </a>
      </v-list-item-title>
      <v-list-item-subtitle
        class="caption text-sub-title suggestions-list-item-subtitle">
        <span :class="skeleton && 'skeleton-background skeleton-text skeleton-list-item-subtitle skeleton-border-radius'">
          {{ space.members }} {{ $t('spacemember.Label') }}
        </span>
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action class="suggestions-list-item-actions">
      <v-btn-toggle class="transparent">
        <a :class="skeleton && 'skeleton-background skeleton-text skeleton-border-radius'"
           text
           icon
           small
           min-width="auto"
           class="px-0 suggestions-btn-action connexion-accept-btn"
           @click="joinSpace(space)">
          <i class="uiIconPlusLight"></i>
        </a>
        <a :class="skeleton && 'skeleton-background skeleton-text skeleton-border-radius'"
           text
           small
           min-width="auto"
           class="px-0 suggestions-btn-action connexion-refuse-btn"
           @click="ignoredSuggestionSpace(space)">
          <i class="uiIconCloseCircled"></i>
        </a>
      </v-btn-toggle>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: () => null,
    },
    avatarSize: {
      type: Number,
      default: () => 37,
    },
    skeleton: {
      type: Boolean,
      default: false,
    },
    spacesSuggestionsList: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    avatarUrl() {
      return this.space && this.space.spaceAvatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.space.spaceUrl}/avatar`;
    },
    url() {
      if (!this.space || !this.space.spaceId) {
        return '#';
      }
      return `${eXo.env.portal.context}/g/:spaces:${this.space.spaceUrl}/`;
    },
  },
  methods: {
    joinSpace(item) {
      this.$spaceService.requestJoin(item.spaceId).then(
        () => {
          this.spacesSuggestionsList.splice(this.spacesSuggestionsList.indexOf(item),1);
        }
      );
    },
    ignoredSuggestionSpace(item) {
      this.$spaceService.ignoreSuggestion(item).then(
        () => {
          this.spacesSuggestionsList.splice(this.spacesSuggestionsList.indexOf(item),1);
        }
      );
    },
  },
};
</script>