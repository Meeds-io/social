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
  <div v-if="menuSettings" class="d-flex">
    <div class="d-flex flex-column flex-grow-1 flex-shrink-1">
      <div class="text-header mb-4">
        {{ $t('generalSettings.sidebar') }}
      </div>
      <v-switch
        v-model="menuSettings.allowUserCustomHome"
        :label="$t('generalSettings.allowUserCustomHome')"
        class="mt-0 mb-4 width-fit-content" />
      <div class="d-flex width-fit-content">
        <div class="mb-4 me-8">
          <div class="font-weight-bold mb-2">
            {{ $t('generalSettings.sidebarAllowedModes') }}
          </div>
          <v-checkbox
            v-for="mode in allModes"
            :key="mode"
            :input-value="allowedModes.indexOf(mode) >= 0"
            :disabled="allowedModes.length === 1 && allowedModes.indexOf(mode) >= 0"
            hide-details
            class="ma-0"
            @change="changeAllowedMode(mode, $event)">
            <template #label>
              <span class="text-font-size text-color">{{ $t(`generalSettings.sidebar.mode.${mode}`) }}</span>
            </template>
          </v-checkbox>
        </div>
        <div>
          <div class="font-weight-bold mb-2">
            {{ $t('generalSettings.sidebarDefaultMode') }}
          </div>
          <v-radio-group
            v-model="menuSettings.defaultMode"
            class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
            mandatory>
            <v-radio
              v-for="mode in allowedModes"
              :key="mode"
              :value="mode"
              class="mx-0 mt-0 mb-1">
              <template #label>
                <span class="text-font-size text-color">{{ $t(`generalSettings.sidebar.mode.${mode}`) }}</span>
              </template>
            </v-radio>
          </v-radio-group>
        </div>
      </div>
      <div class="font-weight-bold mb-2">
        {{ $t('generalSettings.sidebarItemsOrganization') }}
      </div>
      <v-data-table
        :headers="headers"
        :items="menuItems"
        mobile-breakpoint="0"
        hide-default-footer
        disable-sort
        disable-pagination
        disable-filtering>
        <template slot="item.name" slot-scope="{item}">
          <div class="d-flex align-center ms-n4 text-truncate">
            <template v-if="item.type === 'SEPARATOR'">
              <v-icon size="20" class="me-4">fa-grip-lines</v-icon>
              {{ $t('generalSettings.sidebarSeparator') }}
            </template>
            <template v-else>
              <v-icon size="20" class="me-4">{{ item.icon }}</v-icon>
              {{ $t(item.name) }}
            </template>
          </div>
        </template>
        <template slot="item.move" slot-scope="{item}">
          <div class="d-flex justify-center">
            <v-card
              class="d-flex"
              width="88"
              flat>
              <v-btn
                v-if="menuItems.indexOf(item) > 0"
                class="ms-1 me-auto"
                icon
                @click="moveUp(menuItems.indexOf(item))">
                <v-icon size="20">fa-arrow-up</v-icon>
              </v-btn>
              <v-btn
                v-if="menuItems.indexOf(item) < (menuItems.length - 1)"
                class="me-1 ms-auto"
                icon
                @click="moveDown(menuItems.indexOf(item))">
                <v-icon size="20">fa-arrow-down</v-icon>
              </v-btn>
            </v-card>
          </div>
        </template>
        <template slot="item.enabled" slot-scope="{item}">
          <div class="d-flex justify-center">
            <v-switch v-model="item.enabled" class="ma-auto" />
          </div>
        </template>
        <template slot="item.mobile" slot-scope="{item}">
          <div class="d-flex justify-center">
            <v-switch v-model="item.mobile" class="ma-auto" />
          </div>
        </template>
      </v-data-table>
    </div>
    <portal-general-settings-navigation-settings-sidebar-preview
      class="flex-grow-0 flex-shrink-0 elevation-3" />
  </div>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    menuSettings: null,
    allModes: ['HIDDEN', 'ICON', 'STICKY'],
  }),
  computed: {
    allowedModes() {
      return this.menuSettings?.allowedModes;
    },
    menuItems() {
      return this.menuSettings?.items;
    },
    headers() {
      return [{
        text: this.$t('generalSettings.header.sidebarItem'),
        value: 'name',
        align: 'left',
        class: 'ps-0',
        width: '50%',
      }, {
        text: this.$t('generalSettings.header.sidebarMove'),
        value: 'move',
        align: 'center',
        width: '25%',
      }, {
        text: this.$t('generalSettings.header.sidebarActions'),
        value: 'action',
        align: 'center',
        width: '25%',
      }];
    },
  },
  watch: {
    settings: {
      deep: true,
      immediate: true,
      handler() {
        if (this.settings
            && (!this.menuSettings || JSON.stringify(this.menuSettings) !== JSON.stringify(this.settings.sidebar))) {
          this.menuSettings = JSON.parse(JSON.stringify(this.settings.sidebar));
        }
      },
    },
    menuSettings: {
      deep: true,
      handler() {
        if (this.menuSettings
            && this.settings
            && JSON.stringify(this.menuSettings) !== JSON.stringify(this.settings.sidebar)) {
          this.$emit('changed', this.menuSettings);
        }
      },
    },
  },
  methods: {
    changeAllowedMode(mode, enable) {
      this.menuSettings.allowedModes = this.allModes.filter(m => (enable && this.allowedModes.indexOf(m) >= 0 || m === mode) || (!enable && this.allowedModes.indexOf(m) >= 0 && m !== mode));
    },
    moveUp(index) {
      const item = this.menuSettings.items[index];
      this.menuSettings.items.splice(index, 1);
      this.menuSettings.items.splice(index - 1, 0, item);
    },
    moveDown(index) {
      const item = this.menuSettings.items[index];
      this.menuSettings.items.splice(index, 1);
      this.menuSettings.items.splice(index + 1, 0, item);
    },
  },
};
</script>