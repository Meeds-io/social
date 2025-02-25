<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <v-tooltip v-if="displayWarning" top>
    <template #activator="{attrs, on}">
      <v-btn
        v-on="on"
        v-bind="attrs"
        class="me-2"
        small
        icon
        @click="openSettingsDrawer">
        <v-icon
          color="warning"
          size="18">
          fa-exclamation-triangle
        </v-icon>
      </v-btn>
    </template>
    <span v-if="isAdministrator">
      {{ $t('publicWidgetHidden.tooltip.adminCase') }}
    </span>
    <span v-else>
      {{ $t('publicWidgetHidden.tooltip.pageEditorCase') }}
    </span>
  </v-tooltip>
</template>
<script>
export default {
  computed: {
    hubAccessOpen() {
      return this.$root.registrationType === 'OPEN';
    },
    isAdministrator() {
      return this.$root.isAdministrator;
    },
    displayWarning() {
      return this.$root.isPublicPage
        && this.$root.canEdit
        && !this.hubAccessOpen
        && !this.$root.settingName;
    },
  },
  methods: {
    openSettingsDrawer() {
      this.$root.$emit('spaces-list-settings-open');
    },
  },
};
</script>