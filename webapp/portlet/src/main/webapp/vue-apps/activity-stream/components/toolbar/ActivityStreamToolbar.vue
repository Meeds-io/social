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
  <v-toolbar
    v-if="displayToolbar"
    id="activityComposer"
    class="activityComposer activityComposerApp pa-0"
    color="white mb-5"
    height="52"
    flat
    dense>
    <v-flex class="d-flex">
      <div v-if="userCanPost" class="openLink my-auto ps-0 text-truncate">
        <a @click="openComposerDrawer(true)" class="primary--text">
          <i class="uiIconEdit"></i>
          {{ composerButtonLabel }}
        </a>
      </div>
      <div v-else>
        <v-card-text class="text-sub-title text-uppercase center px-0">
          {{ $t('activity.toolbar.title') }}
        </v-card-text>
      </div>
      <div
        v-if="streamFilterEnabled"
        class="ms-auto my-auto">
        <activity-stream-filter />
      </div>
    </v-flex>
  </v-toolbar>
</template>

<script>
export default {
  props: {
    activityBody: {
      type: String,
      default: ''
    },
    activityId: {
      type: String,
      default: ''
    },
    activityParams: {
      type: Object,
      default: null
    },
    standalone: {
      type: Boolean,
      default: false
    },
    canPost: {
      type: Boolean,
      default: false
    },
    canFilter: {
      type: Boolean,
      default: false
    },
  },
  computed: {
    composerButtonLabel() {
      if (eXo.env.portal.spaceDisplayName){
        return this.$t('activity.composer.link', {0: eXo.env.portal.spaceDisplayName});
      } else {
        return this.$t('activity.composer.post');
      }
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    userCanPost() {
      return !this.standalone && this.canPost;
    },
    streamFilterEnabled() {
      return eXo.env.portal.StreamFilterEnabled && this.canFilter;
    },
    displayToolbar() {
      return this.userCanPost || this.streamFilterEnabled;
    },
  },
  methods: {
    openComposerDrawer() {
      document.dispatchEvent(new CustomEvent('activity-composer-drawer-open', {detail: {
        activityId: this.activityId,
        activityBody: this.activityBody,
        activityParams: this.activityParams,
        files: [],
        activityType: []
      }}));
    },
  },
};
</script>