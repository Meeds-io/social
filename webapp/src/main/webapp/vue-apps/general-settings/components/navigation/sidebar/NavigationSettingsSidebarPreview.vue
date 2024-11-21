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
  <v-card
    class="d-none d-sm-flex flex-column overflow-hidden"
    min-width="420"
    min-height="700"
    height="fit-content"
    max-height="900"
    flat>
    <v-list class="d-flex flex-column overflow-hidden pb-0 flex-grow-1 flex-shrink-1" dense>
      <div class="flex-grow-0 flex-shrink-0">
        <v-list-item class="d-flex" dense>
          <v-list-item-avatar min-width="36">
            <img
              src="/portal/rest/v1/platform/branding/logo"
              height="36px"
              width="auto"
              alt="">
          </v-list-item-avatar>
          <v-list-item-content>
            <v-list-item-title class="logoTitle font-weight-bold menu-text-color text-truncate">
              {{ $root.branding?.companyName }}
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </div>
      <div class="flex-grow-1 flex-shrink-1 overflow-x-hidden overflow-y-auto">
        <portal-general-settings-navigation-settings-sidebar-preview-item
          v-for="(item, index) in menuItems"
          :key="index"
          :settings="settings"
          :item="item" />
      </div>
      <div class="flex-grow-0 flex-shrink-0">
        <v-list-item class="d-flex" dense>
          <v-list-item-action class="me-auto font-weight-bold">
            {{ productName }}
          </v-list-item-action>
          <v-list-item-action class="ms-auto d-flex flex-row">
            <v-avatar
              class="userAvatar mx-3"
              size="36">
              <v-img :src="avatarUrl" />
            </v-avatar>
            <v-btn
              class="userSettingsLink me-3"
              icon>
              <v-icon size="20">fa-sliders-h</v-icon>
            </v-btn>
            <v-btn
              class="logoutLink me-n3"
              icon>
              <v-icon size="20">fa-power-off</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </div>
    </v-list>
  </v-card>
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
    productName: eXo.env.portal.productName,
  }),
  computed: {
    menuItems() {
      return this.settings?.items || [];
    },
    avatarUrl() {
      return `/portal/rest/v1/social/users/${eXo.env.portal.userIdentityId}/avatar?byId=true`;
    },
  },
};
</script>