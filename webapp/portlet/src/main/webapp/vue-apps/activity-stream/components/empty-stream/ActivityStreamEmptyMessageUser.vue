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
  <div class="d-flex flex-column white card-border-radius">
    <div class="mx-4 my-4">
      <p v-sanitized-html="welcomeTitle"></p>
      <v-card
        max-width="500"
        flat
        class="mx-auto my-8">
        <v-row>
          <activity-stream-empty-message-space-icons-column
            :icon-size="iconSize"
            :icon-ref="userGearIcon"
            :info-message="emptyStreamForSpacesThatUserManage1" />
          <activity-stream-empty-message-space-icons-column
            :icon-size="iconSize"
            :icon-ref="shareIcon"
            :info-message="emptyStreamForSpacesThatUserManage2" 
            icon-color="secondary" />
          <activity-stream-empty-message-space-icons-column
            :icon-size="iconSize"
            :icon-ref="awardIcon"
            :info-message="emptyStreamForSpacesThatUserManage3" />
          <activity-stream-empty-message-space-icons-column
            :icon-size="iconSize"
            :icon-ref="trophyIcon"
            :info-message="emptyStreamForSpacesThatUserManage4"      
            icon-color="secondary" /> 
        </v-row>
      </v-card>
      <div v-sanitized-html="welcomeSpaceParagraph1" class="mb-4"></div>
      <div v-sanitized-html="welcomeSpaceParagraph2" class="mb-4"></div>
      <div v-sanitized-html="welcomeSpaceParagraph3" class="mb-4"></div>
      <div>
        <ol>
          <li v-html="welcomeSpaceNumberedParagraph1"></li>
          <li v-sanitized-html="welcomeSpaceNumberedParagraph2"></li>
          <li v-sanitized-html="welcomeSpaceNumberedParagraph3"></li>
        </ol>
      </div>
      <div v-sanitized-html="welcomeSpaceParagraph4" class="my-4"></div>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      iconSize: '30',
      userGearIcon: 'fa-user-cog',
      shareIcon: 'fa-share-alt',
      awardIcon: 'fa-award',
      trophyIcon: 'fa-trophy',
    };
  },
  computed: {
    profileName() {
      return this.$currentUserIdentity && this.$currentUserIdentity.profile && this.$currentUserIdentity.profile.fullname;
    },
    profileUri() {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile`;
    },
    spacesUri() {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/spaces`;
    },
    profilelink() {
      return `<a href="${this.profileUri}"><strong class="text-color">${this.profileName}</strong></a>`;
    },
    welcomeTitle() {
      return this.$t && this.$t('UIActivity.label.Welcome_Activity_Welcome_Onboard', {
        'user full name': this.profilelink,
      });
    },
    emptyStreamForSpacesThatUserManage1() {
      return this.$t('UIActivity.label.empty_space_stream_manage_community');
    },
    emptyStreamForSpacesThatUserManage2() {
      return this.$t('UIActivity.label.empty_space_stream_share_knowledge');
    },
    emptyStreamForSpacesThatUserManage3() {
      return this.$t('UIActivity.label.empty_space_stream_congrats');
    },
    emptyStreamForSpacesThatUserManage4() {
      return this.$t('UIActivity.label.empty_space_stream_value_skills');
    },
    welcomeSpaceParagraph1() {
      return this.$t('UIActivity.label.empty_space_stream_paragraph_one');
    },
    welcomeSpaceParagraph2() {
      return this.$t('UIActivity.label.empty_space_stream_paragraph_two');
    },
    welcomeSpaceParagraph3() {
      return this.$t('UIActivity.label.empty_space_stream_paragraph_three');
    },
    welcomeSpaceParagraph4() {
      return this.$t('UIActivity.label.empty_space_stream_paragraph_four');
    },
    welcomeSpaceNumberedParagraph1() {
      return this.$t('UIActivity.label.empty_space_stream_paragraph_numbered_one', {
        0: `<a class="primary--text font-weight-bold" href="${this.spacesUri}"">`,
        1: '</a>',
      });
    },
    welcomeSpaceNumberedParagraph2() {
      return this.$t('UIActivity.label.empty_space_stream_paragraph_numbered_two');
    },
    welcomeSpaceNumberedParagraph3() {
      return this.$t('UIActivity.label.empty_space_stream_paragraph_numbered_three');
    },
  },
};
</script>