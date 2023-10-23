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
  <div>
    <v-toolbar
      v-if="displayToolbar"
      id="activityComposer"
      class="activityComposer activityComposerApp pa-0 card-border-radius"
      color="white mb-5"
      height="auto"
      flat
      dense>
      <div class="d-flex flex-column full-width">
        <div 
          :class="activityStreamToolbarStyle"
          class="d-flex full-width">
          <exo-user-avatar
            v-if="displayUserAvatar"
            :identity="user"
            :size="userAvatarSize"
            class="me-3"
            extra-class="d-flex align-center"
            avatar />
          <v-btn
            v-if="userCanPost"
            class="text-light-color openLink d-inline flex-shrink-1 px-0 my-auto subtitle-2"
            text
            @click="openComposerDrawer(true)">
            <span class="pa-2 text-truncate"> {{ composerButtonLabel }} </span>
          </v-btn>
          <span v-else class="text-sub-title text-body-1 my-auto">
            {{ $t('activity.toolbar.title') }}
          </span>
          <div class="my-auto ms-auto d-flex flex-row">
            <v-tooltip v-if="markAllReadEnabled" bottom>
              <template #activator="{ on, attrs }">
                <v-btn
                  id="toolbarMarkAllReadButton"
                  :loading="resetting"
                  class="me-sm-0 me-n2"
                  height="36px"
                  width="36px"
                  icon
                  v-bind="attrs"
                  v-on="on"
                  @click="markAllAsRead">
                  <v-icon
                    size="20px">
                    fa-envelope-open-text
                  </v-icon>
                </v-btn>
              </template>
              <span>
                {{ $t('activity.filter.button.markAllAsRead') }}
              </span>
            </v-tooltip>
            <extension-registry-components
              v-if="!spaceId"
              :params="extensionParams"
              name="ActivityToolbarAction"
              type="activity-toolbar-action"
              class="hidden-xs-only" />
            <v-tooltip bottom>
              <template #activator="{ on, attrs }">
                <v-btn
                  v-if="streamFilterEnabled"
                  id="toolbarFilterButton"
                  class="me-sm-0 me-n2"
                  height="36px"
                  width="36px"
                  icon
                  v-bind="attrs"
                  v-on="on"
                  @click="openStreamFilterDrawer">
                  <v-icon
                    :color="filterIconColor"
                    size="20px">
                    fa-sliders-h
                  </v-icon>
                </v-btn>
              </template>
              <span>
                {{ $t('activity.filter.tooltip') }}
              </span>
            </v-tooltip>
          </div>
        </div>
        <div v-if="spaceId && userCanPost" class="hidden-xs-only">
          <v-divider />
          <extension-registry-components
            :params="extensionParams"
            name="ActivityToolbarAction"
            type="activity-toolbar-action"
            class="my-auto d-flex align-center flex-wrap" />
        </div>
      </div>
    </v-toolbar>
    <activity-stream-filter-drawer
      ref="filterStreamDrawer" />
  </div>
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
    hasActivities: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      user: null,
      MESSAGE_MAX_LENGTH: 1300,
      spaceId: eXo.env.portal.spaceId,
      streamFilter: null,
      resetting: false,
    };
  },
  computed: {
    composerButtonLabel() {
      if (eXo.env.portal.spaceDisplayName){
        return this.$t('activity.composer.link.space', {0: eXo.env.portal.spaceDisplayName});
      } else {
        return this.$t('activity.composer.link');
      }
    },
    userCanPost() {
      return !this.standalone && this.canPost;
    },
    activityStreamToolbarStyle() {
      return this.userCanPost && 'py-2' || 'py-1';
    },
    streamFilterEnabled() {
      return this.canFilter;
    },
    markAllReadEnabled() {
      return this.streamFilterEnabled && this.streamFilter === 'unread_spaces_stream' && this.hasActivities;
    },
    displayToolbar() {
      return this.userCanPost || this.streamFilterEnabled;
    },
    extensionParams() {
      return {
        activityId: this.activityId,
        spaceId: this.spaceId,
        files: [],
        templateParams: this.activityParams,
        message: this.activityBody,
        maxMessageLength: this.MESSAGE_MAX_LENGTH,
        activityType: [],
      };
    },
    filterIconColor() {
      return this.streamFilter !== 'all_stream' && 'primary';
    },
    displayUserAvatar() {
      return this.user && this.userCanPost;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    userAvatarSize() {
      return this.isMobile ? '42px' : '45px';
    }
  },
  created() {
    this.streamFilter = this.$activityUtils.getStreamFilter();
    document.addEventListener('activity-stream-type-filter-applied', event => {
      this.streamFilter = event && event.detail;
    });
    this.$root.$on('activity-stream-notify-all-read', this.notifyAsRead);
    this.retrieveUserInformation();
  },
  methods: {
    retrieveUserInformation() {
      this.user = this.$currentUserIdentity && this.$currentUserIdentity.profile;
      if (!this.user) {
        return this.$identityService.getIdentityById(eXo.env.portal.userIdentityId)
          .then(data => this.user = data?.profile);
      }
    },
    openComposerDrawer() {
      this.$nextTick().then(() => {
        document.dispatchEvent(new CustomEvent('activity-composer-drawer-open', {detail: {
          activityId: this.activityId,
          activityBody: this.activityBody,
          activityParams: this.activityParams,
          files: [],
          activityType: [],
          spaceId: this.spaceId
        }}));
      });
    },
    markAllAsRead() {
      this.resetting = true;
      this.$spaceService.markAllAsRead(this.spaceId);
      window.setTimeout(() => {
        this.$root.$emit('activity-stream-reset-filter', false);
        this.notifyAsRead();
        this.resetting = false;
      }, 500);
    },
    notifyAsRead(allowReset) {
      if (allowReset) {
        window.setTimeout(() => {
          document.dispatchEvent(new CustomEvent('alert-message-html-confeti', {detail: {
            alertMessage: this.$t('activity.filter.empty_unread_spaces_stream.switchMessage'),
            alertType: 'success',
            alertLinkText: this.$t('activity.filter.button.resetFilter'),
            alertLinkCallback: () => this.$root.$emit('activity-stream-reset-filter', false),
          }}));
        }, 500);
      } else {
        window.setTimeout(() => {
          this.$root.$emit('activity-stream-reset-filter', true);
          this.$root.$emit('alert-message-html-confeti', this.$t('activity.filter.empty_unread_spaces_stream.switchMessage'), 'success');
        }, 500);
      }
    },
    openStreamFilterDrawer() {
      this.$refs.filterStreamDrawer.open();
    },
  },
};
</script>