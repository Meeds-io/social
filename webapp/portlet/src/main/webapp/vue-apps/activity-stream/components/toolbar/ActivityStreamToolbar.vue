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
    <div class="d-flex full-width pt-3">
      <div class="flex-grow-1">
        <div v-if="userCanPost" class="openLink d-flex flex-column pe-10">
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
          <div class="d-flex flex-row pt-4">
            <v-btn
              :ripple="false"
              class="d-flex flex-grow-1 flex-row justify-start py-2"
              text
              @click="openKudosDrawer">
              <v-icon
                color="primary"
                size="37">
                fa-award
              </v-icon>
              <v-span class="body-2 font-weight-bold ms-5 dark-grey-color">
                {{ $t('exoplatform.kudos.title.sendAKudos') }}
              </v-span>
            </v-btn>
            <v-btn
              :ripple="false"
              class="d-flex flex-grow-1 flex-row justify-start py-2"
              text
              @click="openPollDrawer">
              <v-icon
                color="amber darken-1"
                size="37">
                fa-poll
              </v-icon>
              <v-span class="body-2 font-weight-bold ms-5 dark-grey-color">
                {{ this.$t(`composer.poll.create.drawer.label`) }}
              </v-span>
            </v-btn>
          </div>
        </div>
        <div v-else>
          <v-card-text class="text-sub-title text-uppercase center px-0">
            {{ $t('activity.toolbar.title') }}
          </v-card-text>
        </div>
      </div>
      <div v-if="streamFilterEnabled">
        <activity-stream-filter />
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
    streamFilterEnabled() {
      return this.canFilter;
    },
    displayToolbar() {
      return (this.userCanPost || this.streamFilterEnabled) && this.user;
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
      document.dispatchEvent(new CustomEvent('activity-composer-drawer-open', {detail: {
        activityId: this.activityId,
        activityBody: this.activityBody,
        activityParams: this.activityParams,
        files: [],
        activityType: []
      }}));
    },
    openKudosDrawer() {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: eXo.env.portal.spaceId,
        type: 'USER_PROFILE',
        parentId: '',
        owner: eXo.env.portal.userName,
        spaceURL: eXo.env.portal.spaceUrl,
        readOnlySpace: true
      }}));
    },
    openPollDrawer() {
      this.openComposerDrawer();
      window.setTimeout(() => {
        document.dispatchEvent(new CustomEvent('exo-poll-open-drawer'));
      }, 200);
    }
  },
};
</script>