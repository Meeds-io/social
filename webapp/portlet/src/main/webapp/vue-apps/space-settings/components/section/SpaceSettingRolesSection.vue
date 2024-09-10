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
  <widget-wrapper v-if="$root.space" no-margin>
    <template #title>
      <v-list-item class="px-0 mb-4" dense>
        <v-list-item-action class="my-auto me-3 ms-n2">
          <v-btn
            :title="$t('generalSettings.access.backToMain')"
            size="24"
            icon
            @click="$root.showMain">
            <v-icon size="18" class="icon-default-color">
              {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
            </v-icon>
          </v-btn>
        </v-list-item-action>
        <v-list-item-content>
          <v-list-item-title>
            <v-card
              :title="$t('SpaceSettings.backToMain')"
              class="flex-grow-0 text-title text-start py-1"
              flat
              @click="$root.showMain">
              {{ $t('SpaceSettings.roles') }}
            </v-card>
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </template>
    <template #default>
      <!-- TODO -->
    </template>
  </widget-wrapper>
</template>
<script>
export default {
  data: () => ({
    space: null,
  }),
  created() {
    this.init();
  },
  methods: {
    init() {
      this.space = this.$root.space;
    },
  },
};
</script>