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
  <v-hover v-slot="{ hover }">
    <v-app>
      <!-- the settings cog is a button of its own beside the create button, drawn over its end -->
      <div class="d-flex justify-center align-center">
        <v-btn
          :class="$root.isAdministrator && (hover || cogFocused) && 'pe-10'"
          color="primary"
          class="d-block"
          elevation="0"
          max-width="92%"
          @click="addNewSpace">
          <span class="full-width text-truncate"> {{ $root.label }} </span>
        </v-btn>
        <v-btn
          v-if="$root.isAdministrator"
          :aria-label="$t('space.creation.instantiation.settingsDrawer.title')"
          :class="{ 'd-sr-only-focusable': !hover }"
          class="ms-n9"
          icon
          small
          dark
          @focus="cogFocused = true"
          @blur="cogFocused = false"
          @click="openDrawerSettings">
          <v-icon size="16">fa-cog</v-icon>
        </v-btn>
      </div>
      <space-creation-settings-drawer
        @updated="spaceCreationSettingsUpdated"
        ref="spaceCreationSettingsDrawer" />
    </v-app>
  </v-hover>
</template>

<script>
export default {
  data: () => ({
    // the cog holds the focus: the create button keeps the room the cog is drawn over
    cogFocused: false,
  }),
  methods: {
    addNewSpace() {
      window.require(['SHARED/spaceForm'], drawer => drawer.open(null, this.$root.spaceTemplates));
    },
    openDrawerSettings() {
      this.$root.$emit('space-creation-settings-open');
    },
    spaceCreationSettingsUpdated(settings) {
      this.$root.settings = settings;
    }
  }
};
</script>
