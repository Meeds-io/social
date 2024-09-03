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
  <div class="application-background-color application-border application-border-radius">
    <div class="d-flex flex-column align-center justify-center pa-5">
      <p v-sanitized-html="welcomeTitle" class="text-title"></p>
      <p v-sanitized-html="welcomeSubTitle" class="text-body"></p>
      <div class="d-flex flex-wrap align-center justify-center my-6">
        <activity-stream-empty-message-column
          :info-message="$t('UIActivity.label.empty_stream_write_post')"
          icon-index="1"
          @apply="openComposer" />
        <activity-stream-empty-message-column
          v-if="spaceId"
          :info-message="$t('UIActivity.label.empty_stream_start_poll')"
          icon-index="2"
          @apply="writePoll" />
        <activity-stream-empty-message-column
          v-else
          :info-message="$t('UIActivity.label.empty_stream_send_kudos')"
          icon-index="2"
          @apply="sendKudos" />
        <activity-stream-empty-message-column
          v-if="spaceId"
          :info-message="$t('UIActivity.label.empty_stream_write_article')"
          icon-index="3"
          @apply="writeArticle" />
        <activity-stream-empty-message-column
          v-else
          :info-message="$t('UIActivity.label.empty_stream_join_spaces')"
          :link="spacesLink"
          icon-index="3" />
      </div>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    title: {
      type: String,
      value: null,
    },
    subtitle: {
      type: String,
      value: null,
    },
  },
  data: () => ({
    spaceId: eXo.env.portal.spaceId,
  }),
  computed: {
    spacesUri() {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/spaces`;
    },
    profileName() {
      return this.$currentUserIdentity && this.$currentUserIdentity.profile && this.$currentUserIdentity.profile.fullname;
    },
    profileUri() {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile`;
    },
    profilelink() {
      return `<a href="${this.profileUri}"><strong class="text-color">${this.profileName}</strong></a>`;
    },
    welcomeTitle() {
      return this.title || this.$t('UIActivity.label.Welcome_Activity_Welcome_Onboard', {
        'user full name': this.profilelink,
      });
    },
    welcomeSubTitle() {
      return this.subtitle || this.$t('UIActivity.label.Welcome_Activity_Placeholder');
    },
    spacesLink() {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/spaces`;
    },
  },
  methods: {
    openComposer() {
      document.dispatchEvent(new CustomEvent('activity-composer-drawer-open'));
    },
    sendKudos() {
      this.getSendKudosElement().click();
    },
    writeArticle() {
      this.getWriteArticleElement().click();
    },
    writePoll() {
      this.getPollElement().click();
    },
    getWriteArticleElement() {
      return document.querySelector('#writeNewsBtnToolbar');
    },
    getSendKudosElement() {
      return document.querySelector('#kudosBtnToolbar');
    },
    getPollElement() {
      return document.querySelector('#pollBtnToolbar');
    },
  },
};
</script>