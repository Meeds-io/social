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
      <v-btn
        color="primary"
        @click="addNewSpace">
        {{ $t('space.creation.instantiation.create.button') }}
        <v-icon
          v-if="$root.isAdministrator && hover"
          color="white"
          size="16"
          class="mx-1"
          @click.stop="openDrawerSettings">
          fa-cog
        </v-icon>
      </v-btn>
      <space-creation-settings-drawer 
        :save-settings-url="$root.saveSettingsUrl"
        :saved-settings="{
          spaceTemplates: $root.settings.spaceTemplates,
          spaceCreationTemplateChoice: $root.settings.spaceCreationTemplateChoice
        }"
        @updated="spaceCreationSettingsUpdated"
        ref="spaceCreationSettingsDrawer" />
    </v-app>
  </v-hover>
</template>

<script>
export default {
  methods: {
    addNewSpace() {
      window.require(['SHARED/spaceForm'], drawer => drawer.open(false, true, this.$root.settings.spaceTemplates));
    },
    openDrawerSettings() {
      this.$root.$emit('space-creation-settings-open');
    },
    spaceCreationSettingsUpdated(settings) {
      this.$root.settings.spaceTemplates = settings.spaceTemplates;
      this.$root.settings.spaceCreationTemplateChoice = settings.spaceCreationTemplateChoice;
      this.$refs.spaceCreationSettingsDrawer.close();
    }
  }
};
</script>
