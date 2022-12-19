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
  <div class="d-flex flex-column white border-radius">
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
            :info-message="firstFeatureMessage" />
          <activity-stream-empty-message-space-icons-column
            icon-color="secondary"
            :icon-size="iconSize"
            :icon-ref="shareIcon"
            :info-message="secondFeatureMessage" />
          <activity-stream-empty-message-space-icons-column
            :icon-size="iconSize"
            :icon-ref="awardIcon"
            :info-message="thirdFeatureMessage" />
          <activity-stream-empty-message-space-icons-column
            icon-color="secondary"
            :icon-size="iconSize"
            :icon-ref="trophyIcon"
            :info-message="fourthFeatureMessage" />      
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
      emptyActivityStreamActionName: 'empty-activity-stream-spaces',
      iconSize: '30',
      userGearIcon: 'fa-user-cog',
      shareIcon: 'fa-share-alt',
      awardIcon: 'fa-award',
      trophyIcon: 'fa-trophy',
    };
  },
  created() {
    document.addEventListener(this.emptyActivityStreamActionName, this.clickOnSpacesActivityStreamEmptyActionLink);
  },
  beforeDestroy() {
    document.removeEventListener(this.emptyActivityStreamActionName, this.clickOnSpacesActivityStreamEmptyActionLink);
  },
  computed: {
    welcomeTitle() {
      return this.$t && this.$t('UIActivity.label.Welcome_Space', {
        'space name': `<strong>${eXo.env.portal.spaceDisplayName}</strong>`,
      });
    },
    firstFeatureMessage() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_manage_community');
    },
    secondFeatureMessage() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_share_knowledge');
    },
    thirdFeatureMessage() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_congrats');
    },
    fourthFeatureMessage() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_value_skills');
    },
    welcomeSpaceParagraph1() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_paragraph_one');
    },
    welcomeSpaceParagraph2() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_paragraph_two');
    },
    welcomeSpaceParagraph3() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_paragraph_three');
    },
    welcomeSpaceParagraph4() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_paragraph_four');
    },
    welcomeSpaceNumberedParagraph1() {
      return this.$t('UIActivity.label.empty_space_stream_paragraph_numbered_one', {
        0: `<a class="primary--text font-weight-bold" href="javascript:void(0)" onclick="document.dispatchEvent(new CustomEvent('${this.emptyActivityStreamActionName}'))">`,
        1: '</a>',
      });
    },
    welcomeSpaceNumberedParagraph2() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_paragraph_numbered_two');
    },
    welcomeSpaceNumberedParagraph3() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_paragraph_numbered_three');
    },
  },
  methods: {
    clickOnSpacesActivityStreamEmptyActionLink() {
      window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/spaces`;
    },
  },
};
</script>