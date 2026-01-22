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
      <div class="d-flex justify-center">
        <v-btn
          color="primary"
          class="d-block"
          elevation="0"
          max-width="92%"
          @click="addNewSpace">
          <span class="full-width text-truncate"> {{ $root.label }} </span>
          <v-icon
            v-if="$root.isAdministrator && hover"
            color="white"
            size="16"
            @click.stop="openDrawerSettings">
            fa-cog
          </v-icon>
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
