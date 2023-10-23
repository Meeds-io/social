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
    <img
      :height="height"
      :style="{maxHeight: height}"
      src="/social-portlet/images/emptySpaceStream.png"
      class="mx-auto"
      loading="lazy">
    <div class="mx-4 mb-4">
      <p v-sanitized-html="welcomeTitle"></p>
      <div v-sanitized-html="welcomeSpaceParagraph1" class="mb-4"></div>
      <div v-sanitized-html="welcomeSpaceParagraph2" class="mb-4"></div>
      <div v-sanitized-html="conclusionParagraph"></div>
    </div>
  </div>
</template>
<script>
export default {
  data: function() {
    return {
      height: '380px',
    };
  },
  computed: {
    welcomeTitle() {
      return this.$t('UIActivity.label.Welcome_Space', {
        'space name': `<strong>${eXo.env.portal.spaceDisplayName}</strong>`,
      });
    },
    welcomeSpaceParagraph1() {
      return this.$t('UIActivity.label.empty_space_stream_paragraph_one');
    },
    welcomeSpaceParagraph2() {
      return this.$t('UIActivity.label.empty_space_stream_paragraph_two');
    },
    conclusionParagraph() {
      return this.$t('UIActivity.label.empty_stream_lets_begin');
    },
  },
};
</script>