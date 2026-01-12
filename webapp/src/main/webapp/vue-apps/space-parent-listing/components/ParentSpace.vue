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
  <v-app>
    <v-hover v-model="hover">
      <widget-wrapper
        :title="headerTitle"
        :loading="this.$root.loading"
        ref="parentSpaceListing"
        key="parentSpaceListing"
        extra-class="application-body">
        <template v-if="$root.isManager" #action>
          <div class="d-flex align-center justify-center">
            <v-btn
              v-show="hover"
              height="27"
              width="27"
              class="pa-0"
              min-width="auto"
              text
              @click="openSettingsDrawer"
              icon>
              <v-icon
                size="18">
                fa-cog
              </v-icon>
            </v-btn>
          </div>
        </template>
        <template #default>
          <space-avatar
            v-if="space"
            :space="space"
            link-style
            bold-title />
        </template>
      </widget-wrapper>
    </v-hover>
    <parent-space-settings-drawer
      v-if="drawer"
      ref="parentSpaceListingSettings"
      @close="closeSettingsDrawer" />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    hover: false,
    drawer: false
  }),
  computed: {
    space() {
      return this.$root.space;
    },
    headerTitle() {
      return this.$root.headerTitle || this.$t('parentSpaceListing.header.label');
    },
  },
  methods: {
    async openSettingsDrawer() {
      this.drawer = true;
      await this.$nextTick();
      this.$refs.parentSpaceListingSettings.open();
    },
    async closeSettingsDrawer() {
      this.$refs.parentSpaceListingSettings.close();
      await this.$nextTick();
      this.drawer = false;
    }
  }
};
</script>

