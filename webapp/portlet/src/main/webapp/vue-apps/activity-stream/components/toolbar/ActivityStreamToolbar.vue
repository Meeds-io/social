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
    height="auto"
    flat
    dense>
    <div class="d-flex flex-column full-width">
      <div class="d-flex full-width">
        <div class="flex-grow-1">
          <div v-if="userCanPost" class="openLink d-flex flex-column pe-10 pt-3 pb-3 pb-sm-0">
            <div class="d-flex flex-row">
              <exo-user-avatar
                v-if="user"
                :identity="user"
                class="d-flex align-center ms-1 me-3"
                size="40"
                avatar />
              <v-text-field
                @click="openComposerDrawer(true)"
                :placeholder="$t('activity.composer.post.placeholder')"
                class="pt-0 rounded-pill"
                height="30"
                hide-details
                outlined
                dense />
            </div>
          </div>
          <div v-else>
            <v-card-text class="text-sub-title text-body-1 px-0">
              {{ $t('activity.toolbar.title') }}
            </v-card-text>
          </div>
        </div>
        <div 
          v-if="streamFilterEnabled" 
          :class="streamFilterStyle"
          class="d-flex align-center">
          <activity-stream-filter />
        </div>
      </div>
      <div v-if="userCanPost" class="pt-1 hidden-xs-only">
        <extension-registry-components
          :params="extensionParams"
          name="ActivityToolbarAction"
          type="activity-toolbar-action"
          class="my-auto d-flex align-center flex-wrap" />
      </div>
    </div>
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
  data() {
    return {
      user: null,
      MESSAGE_MAX_LENGTH: 1300,
    };
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
    streamFilterStyle() {
      return this.userCanPost && 'align-sm-end py-3 py-sm-0';
    },
    streamFilterEnabled() {
      return this.canFilter;
    },
    displayToolbar() {
      return (this.userCanPost || this.streamFilterEnabled) && this.user;
    },
    extensionParams() {
      return {
        activityId: this.activityId,
        spaceId: eXo.env.portal.spaceId,
        files: [],
        templateParams: this.activityParams,
        message: this.activityBody,
        maxMessageLength: this.MESSAGE_MAX_LENGTH,
        activityType: [],
      };
    },
  },
  created() {
    if (!this.user) {
      this.$userService.getUser(eXo.env.portal.userName)
        .then(user => this.user = user);
    }
  },
  methods: {
    openComposerDrawer() {
      this.$nextTick().then(() => {
        document.dispatchEvent(new CustomEvent('activity-composer-drawer-open', {detail: {
          activityId: this.activityId,
          activityBody: this.activityBody,
          activityParams: this.activityParams,
          files: [],
          activityType: []
        }}));
      });
    },
  },
};
</script>