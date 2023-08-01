<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association
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
  <v-fade-transition>
    <div v-if="displayDropZone" class="grey-background opacity-8 full-height full-width position-absolute t-0 d-flex align-center justify-center z-index-one pa-4">
      <div class="full-height full-width border-primary-dashed ma-4 rounded-lg d-flex align-center justify-center flex-column">
        <v-icon size="40" class="primary--text pb-4">fas fa-file-import</v-icon>
        <p class="title">{{ $t('attachment.dropZoneLabel') }}</p>
      </div>
    </div>
  </v-fade-transition>
</template>
<script>
export default {
  data: () => ({
    isDisplayed: false
  }),
  props: {
    dropEnabled: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    displayDropZone() {
      return this.isDisplayed;
    }
  },
  created() {
    document.addEventListener('attachments-show-drop-zone', this.showDropZone);
    document.addEventListener('attachments-hide-drop-zone', this.hideDropZone);
  },
  beforeDestroy() {
    document.removeEventListener('attachments-show-drop-zone', this.showDropZone);
    document.removeEventListener('attachments-hide-drop-zone', this.hideDropZone);
  },
  methods: {
    showDropZone() {
      this.isDisplayed = true;
    },
    hideDropZone() {
      this.isDisplayed = false;
    },
  }
};
</script>
