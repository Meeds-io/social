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
        :item="subItem"
        :home-icon="homeIcon && index === 0" />
    </template>
  </div>
  <div v-else-if="isSpaces || isSpaceTemplate || isSpaceCategory">
    <template v-if="displaySpacesList">
      <v-list-item class="d-flex">
        <v-list-item-avatar class="me-2 my-auto" min-width="36">
          <portal-general-settings-navigation-settings-icon
            :icon-size="20"
            :icon="item.icon" />
        </v-list-item-avatar>
        <v-list-item-content>
          <v-list-item-title class="logoTitle menu-text-color text-truncate">
            {{ $t(item.name) }}
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
      <template v-if="item?.items?.length && displayItemsInMobile">
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
    class="d-flex">
    <v-list-item-avatar class="me-2 my-auto" min-width="36">
      <portal-general-settings-navigation-settings-icon
        v-if="!item.avatar"
        :icon-size="20"
        :icon="item.icon || 'fa-folder'" />
    </v-list-item-avatar>
    <v-list-item-avatar
      v-if="item.avatar"
      class="me-2 my-auto"
      min-width="28"
      width="28"
      height="28"
      tile>
      <img
        :src="item.avatar"
        :alt="item.name"
        class="border-radius"
        width="28"
        height="auto">
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title class="logoTitle menu-text-color text-truncate">
        {{ item.name }}
      </v-list-item-title>
    </v-list-item-content>
    <v-list-item-icon v-if="homeIcon" class="my-0 ms-2 me-0">
      <v-tooltip bottom>
        <template #activator="{on, attrs}">
          <v-btn
            v-on="on"
            v-bind="attrs"
            color="primary"
            icon>
            <v-icon size="20">fa-house-user</v-icon>
          </v-btn>
        </template>
        <v-card
          color="transparent"
          max-width="50vw"
          width="300"
          flat>
          {{ $t('generalSettings.defaultUserHouse') }}
        </v-card>
      </v-tooltip>
    </v-list-item-icon>
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
    mobilePreview: {
      type: Boolean,
      default: false,
    },
    homeIcon: {
      type: Boolean,
      default: false,
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
    isSpaceCategory() {
      return this.item.type === 'SPACE_CATEGORY';
    },
    displayItemsInMobile() {
      return !this.mobilePreview || (!this.isSpaces && !this.isSpaceTemplate && !this.isSpaceCategory) || this.item?.properties?.displayItemsInMobile === 'true';
    },
    menuItems() {
      return this.item?.items;
    },
    hasItems() {
      return this.menuItems?.length;
    },
    displayOnlyWhenMember() {
      return this.item?.properties?.displayOnlyWhenMember === 'true';
    },
    notSpaceMember() {
      return this.item?.properties?.notMember === 'true';
    },
    displaySpacesList() {
      return this.hasItems || !this.displayOnlyWhenMember || !this.notSpaceMember;
    },
  },
};
</script>