<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2023 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-app>
    <v-tooltip bottom>
      <template #activator="{on, bind}">
        <v-btn
          id="topBarPreviewButton"
          v-on="on"
          v-bind="bind"
          class="ms-5"
          icon
          @click="previewMode = !previewMode">
          <v-icon size="16">{{ previewMode && 'fa-eye-slash' || 'fa-eye' }}</v-icon>
        </v-btn>
      </template>
      <span>{{ previewMode && $t('publicAccess.editModeTooltip') || $t('publicAccess.previewModeTooltip') }}</span>
    </v-tooltip>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    previewMode: false,
  }),
  mounted() {
    this.$root.$applicationLoaded();
  },
  watch: {
    previewMode() {
      if (this.previewMode) {
        document.dispatchEvent(new CustomEvent('cms-preview-mode'));
      } else {
        document.dispatchEvent(new CustomEvent('cms-edit-mode'));
      }
    },
  },
};
</script>