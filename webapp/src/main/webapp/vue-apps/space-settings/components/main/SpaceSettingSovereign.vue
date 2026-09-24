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
  <div v-if="$root.isAllSections">
    <v-card flat>
      <v-list class="pa-0" dense>
        <v-list-item class="pa-0">
          <v-list-item-content>
            <v-list-item-title class="text-title py-1">
              {{ $t('SpaceSettings.sovereignty') }}
            </v-list-item-title>
            <v-list-item-subtitle class="pt-0 pb-1">
              {{ $t('SpaceSettings.sovereignty.description') }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-switch
              id="SpaceSettingRestrictContent"
              v-model="$root.space.sovereign"
              :loading="saving"
              class="ma-0"
              @click="switchSpaceSovereignty" />
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
  </div>
</template>
<script>
export default {
  data: () => ({
    saving: false,
  }),
  methods: {
    async switchSpaceSovereignty() {
      this.saving = true;
      try {
        await this.$spaceService.updateSpace({
          id: this.$root.spaceId,
          sovereign: this.$root.space.sovereign,
        });
      } finally {
        this.saving = false;
      }
    },
  },
};
</script>