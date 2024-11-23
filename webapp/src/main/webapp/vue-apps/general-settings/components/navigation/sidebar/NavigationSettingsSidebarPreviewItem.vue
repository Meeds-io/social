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
  <v-divider v-if="item.type === 'SEPARATOR'" class="my-1" />
  <div v-else-if="isSitePages">
    <template v-if="item?.items?.length">
      <portal-general-settings-navigation-settings-sidebar-preview-item
        v-for="(subItem, index) in item.items"
        :key="`${subItem.name}_${subItem.icon}_${index}`"
        :settings="settings"
        :item="subItem" />
    </template>
  </div>
  <div v-else-if="isSpaces || isSpaceTemplate">
    <template v-if="item?.items?.length || isSpaces">
      <v-list-item
        class="d-flex"
        dense>
        <v-list-item-avatar min-width="36">
          <v-icon size="18">{{ item.icon || 'fa-folder' }}</v-icon>
        </v-list-item-avatar>
        <v-list-item-content>
          <v-list-item-title class="logoTitle menu-text-color text-truncate">
            {{ $t(item.name) }}
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
      <template v-if="item?.items?.length">
        <portal-general-settings-navigation-settings-sidebar-preview-item
          v-for="(subItem, index) in item.items"
          :key="`${subItem.name}_${subItem.icon}_${index}`"
          :settings="settings"
          :item="subItem" />
      </template>
    </template>
  </div>
  <v-list-item
    v-else
    class="d-flex"
    dense>
    <v-list-item-avatar min-width="36">
      <v-icon v-if="!item.avatar" size="18">{{ item.icon || 'fa-folder' }}</v-icon>
    </v-list-item-avatar>
    <v-list-item-avatar
      v-if="item.avatar"
      class="me-4"
      min-width="36"
      width="36"
      height="36"
      tile>
      <img
        :src="item.avatar"
        :alt="item.name"
        class="border-radius"
        width="36"
        height="auto">
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title class="logoTitle menu-text-color text-truncate">
        {{ item.name }}
      </v-list-item-title>
    </v-list-item-content>
  </v-list-item>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
    item: {
      type: Object,
      default: null,
    },
  },
  computed: {
    isSitePages() {
      return this.item.type === 'SITE' && this.item.properties.expandPages === 'true';
    },
    isSpaces() {
      return this.item.type === 'SPACES';
    },
    isSpaceTemplate() {
      return this.item.type === 'SPACE_TEMPLATE';
    },
  },
};
</script>