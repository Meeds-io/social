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
  <div
    v-if="menuSettings"
    class="d-flex">
    <div class="d-flex flex-column flex-grow-1 flex-shrink-1">
      <div class="d-flex align-center mb-4">
        <div class="text-header">
          {{ $t('generalSettings.sidebar') }}
        </div>
        <v-btn
          class="ms-2"
          icon
          :title="$t('generalSettings.topbar.switchDevicePreview')"
          @click="mobilePreview = !mobilePreview">
          <v-icon size="20">
            {{ mobilePreview && 'fa-desktop' || 'fa-mobile-alt' }}
          </v-icon>
        </v-btn>
      </div>
      <v-switch
        v-model="menuSettings.allowUserCustomHome"
        class="mt-0 mb-4 width-fit-content">
        <template #label>
          <help-label
            label="generalSettings.allowUserCustomHome"
            label-class="text-body"
            tooltip="generalSettings.whatIsUserCustomHome">
            <template #helpContent>
              <p>
                {{ $t('generalSettings.whatIsUserCustomHomeDescription') }}
              </p>
            </template>
          </help-label>
        </template>
      </v-switch>
      <div class="d-flex width-fit-content">
        <div class="mb-4 me-8">
          <div class="mb-2">
            <help-label
              label="generalSettings.sidebarAllowedModes"
              label-class="text-body font-weight-bold"
              tooltip="generalSettings.whatIsSidebarAllowedModes">
              <template #helpContent>
                <p>
                  {{ $t('generalSettings.whatIsSidebarAllowedModesDescription1') }}
                </p>
                <p>
                  {{ $t('generalSettings.whatIsSidebarAllowedModesDescription2') }}
                </p>
                <p>
                  {{ $t('generalSettings.whatIsSidebarAllowedModesDescription3') }}
                </p>
                <p>
                  {{ $t('generalSettings.whatIsSidebarAllowedModesDescription4') }}
                </p>
              </template>
            </help-label>
          </div>
          <v-checkbox
            v-for="mode in allModes"
            :key="mode"
            class="ma-0"
            :disabled="allowedModes.length === 1 && allowedModes.indexOf(mode) >= 0"
            hide-details
            :input-value="allowedModes.indexOf(mode) >= 0"
            @change="changeAllowedMode(mode, $event)">
            <template #label>
              <span class="text-font-size text-color">{{ $t(`generalSettings.sidebar.mode.${mode}`) }}</span>
            </template>
          </v-checkbox>
        </div>
        <div>
          <div class="mb-2">
            <help-label
              label="generalSettings.sidebarDefaultMode"
              label-class="text-body font-weight-bold"
              tooltip="generalSettings.whatIsSidebarDefaultMode">
              <template #helpContent>
                <p>
                  {{ $t('generalSettings.whatIsSidebarDefaultModeDescription1') }}
                </p>
                <p>
                  {{ $t('generalSettings.whatIsSidebarDefaultModeDescription2') }}
                </p>
              </template>
            </help-label>
          </div>
          <v-radio-group
            v-model="menuSettings.defaultMode"
            class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
            mandatory>
            <v-radio
              v-for="mode in allowedModes"
              :key="mode"
              class="mx-0 mt-0 mb-1"
              :value="mode">
              <template #label>
                <span class="text-font-size text-color">{{ $t(`generalSettings.sidebar.mode.${mode}`) }}</span>
              </template>
            </v-radio>
          </v-radio-group>
        </div>
      </div>
      <div class="d-flex">
        <div class="d-flex align-center font-weight-bold">
          {{ $t('generalSettings.sidebarItemsOrganization') }}
        </div>
        <v-spacer />
        <portal-general-settings-navigation-settings-sidebar-add-button
          :settings="settings" />
      </div>
      <v-data-table
        disable-filtering
        disable-pagination
        disable-sort
        :headers="headers"
        hide-default-footer
        :items="menuItems"
        mobile-breakpoint="0">
        <template #item.name="{item}">
          <div class="d-flex align-center ms-n4 text-truncate">
            <template v-if="item.type === 'SEPARATOR'">
              <v-card
                class="d-flex align-center justify-center me-4"
                flat
                min-width="24">
                <v-icon size="20">
                  fa-grip-lines
                </v-icon>
              </v-card>
              {{ $t('generalSettings.sidebarSeparator') }}
            </template>
            <template v-else>
              <v-card
                class="d-flex align-center justify-center me-4"
                flat
                min-width="24">
                <v-icon size="20">
                  {{ item.icon || 'fa-folder' }}
                </v-icon>
              </v-card>
              {{ $t(item.name) }}
            </template>
          </div>
        </template>
        <template #item.move="{item}">
          <div class="d-flex justify-center">
            <v-card
              class="d-flex"
              flat
              width="88">
              <v-btn
                v-if="menuItems.indexOf(item) > 0"
                class="ms-1 me-auto"
                icon
                :title="$t('generalSettings.moveUp')"
                @click="moveUp(menuItems.indexOf(item))">
                <v-icon size="20">
                  fa-arrow-up
                </v-icon>
              </v-btn>
              <v-btn
                v-if="menuItems.indexOf(item) < (menuItems.length - 1)"
                class="me-1 ms-auto"
                icon
                :title="$t('generalSettings.moveDown')"
                @click="moveDown(menuItems.indexOf(item))">
                <v-icon size="20">
                  fa-arrow-down
                </v-icon>
              </v-btn>
            </v-card>
          </div>
        </template>
        <template #item.action="{item}">
          <div class="d-flex justify-end">
            <v-btn
              v-if="item.type !== 'SEPARATOR'"
              class="me-2"
              icon
              :title="$t('generalSettings.editSideBarItem')"
              @click="edit(item)">
              <v-icon size="20">
                fa-edit
              </v-icon>
            </v-btn>
            <v-btn
              color="error"
              icon
              :title="$t('generalSettings.removeSideBarItem')"
              @click="remove(item)">
              <v-icon size="20">
                fa-trash
              </v-icon>
            </v-btn>
          </div>
        </template>
      </v-data-table>
    </div>
    <portal-general-settings-navigation-settings-sidebar-preview
      class="flex-grow-0 flex-shrink-1 elevation-3 ms-8"
      :mobile-preview="mobilePreview"
      :settings="menuSettings" />
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
      mobilePreview: false,
      allModes: ['HIDDEN', 'ICON', 'STICKY'],
    }),
    computed: {
      allowedModes () {
        return this.menuSettings?.allowedModes;
      },
      menuItems () {
        return this.menuSettings?.items;
      },
      headers () {
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
        handler () {
          if (this.settings
            && (!this.menuSettings || JSON.stringify(this.menuSettings) !== JSON.stringify(this.settings.sidebar))) {
            this.menuSettings = JSON.parse(JSON.stringify(this.settings.sidebar));
          }
        },
      },
      menuSettings: {
        deep: true,
        handler () {
          if (this.menuSettings
            && this.settings
            && JSON.stringify(this.menuSettings) !== JSON.stringify(this.settings.sidebar)) {
            this.$emit('changed', this.menuSettings);
          }
        },
      },
    },
    created () {
      this.$root.$on('sidebar-item-add-separator', this.addSeparator);
    },
    beforeUnmount () {
      this.$root.$off('sidebar-item-add-separator', this.addSeparator);
    },
    methods: {
      changeAllowedMode (mode, enable) {
        this.menuSettings.allowedModes = this.allModes.filter(m => (enable && (this.allowedModes.indexOf(m) >= 0 || m === mode)) || (!enable && this.allowedModes.indexOf(m) >= 0 && m !== mode));
      },
      remove (item) {
        this.menuSettings.items.splice(this.menuSettings.items.indexOf(item), 1);
      },
      edit (item) {
        if (item.type === 'SITE' || item.type === 'PAGE') {
          this.$root.$emit('sidebar-item-edit-site', this.settings, item);
        } else if (item.type === 'LINK') {
          this.$root.$emit('sidebar-item-edit-link', this.settings, item);
        } else if (item.type === 'SPACES' || item.type === 'SPACE_TEMPLATE' || item.type === 'SPACE_CATEGORY') {
          this.$root.$emit('sidebar-item-edit-spaces', this.settings, item);
        }
      },
      moveUp (index) {
        const item = this.menuSettings.items[index];
        this.menuSettings.items.splice(index, 1);
        this.menuSettings.items.splice(index - 1, 0, item);
      },
      addSeparator () {
        this.menuSettings.items.push({
          type: 'SEPARATOR',
        });
      },
      moveDown (index) {
        const item = this.menuSettings.items[index];
        this.menuSettings.items.splice(index, 1);
        this.menuSettings.items.splice(index + 1, 0, item);
      },
    },
  };
</script>