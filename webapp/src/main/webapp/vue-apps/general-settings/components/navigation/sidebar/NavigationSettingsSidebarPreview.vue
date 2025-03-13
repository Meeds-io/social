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
    class="d-none d-sm-flex flex-column overflow-hidden mb-2"
    flat
    height="fit-content"
    max-height="900"
    min-height="700"
    min-width="420">
    <v-list
      class="d-flex flex-column overflow-hidden pb-0 flex-grow-1 flex-shrink-1"
      dense>
      <v-card
        class="no-border-radius"
        flat
        min-height="57">
        <v-list-item class="d-flex">
          <v-list-item-avatar
            class="my-auto mx-0"
            height="36"
            max-width="100"
            min-width="auto"
            tile
            width="auto">
            <img
              alt=""
              class="object-fit-contain"
              height="36"
              src="/portal/rest/v1/platform/branding/logo"
              width="auto">
          </v-list-item-avatar>
          <v-list-item-content>
            <v-list-item-title class="logoTitle font-weight-bold menu-text-color text-truncate ms-4">
              {{ $root.branding?.companyName }}
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-card>
      <div class="flex-grow-1 flex-shrink-1 specific-scrollbar overflow-x-hidden overflow-y-auto">
        <portal-general-settings-navigation-settings-sidebar-preview-item
          v-for="(item, index) in menuItems"
          :key="`${item.name}_${item.icon}_${index}`"
          :home-icon="homeItemIndex === index"
          :item="item"
          :mobile-preview="mobilePreview"
          :settings="settings" />
      </div>
      <div class="flex-grow-0 flex-shrink-0">
        <v-list-item
          class="d-flex"
          dense>
          <v-list-item-action class="me-auto font-weight-bold">
            {{ productName }}
          </v-list-item-action>
          <v-list-item-action class="ms-auto d-flex flex-row">
            <v-avatar
              class="userAvatar mx-3"
              size="36">
              <v-img
                contain
                :src="avatarUrl" />
            </v-avatar>
            <v-btn
              class="userSettingsLink me-3"
              icon>
              <v-icon size="20">
                fa-sliders-h
              </v-icon>
            </v-btn>
            <v-btn
              class="logoutLink me-n3"
              icon>
              <v-icon size="20">
                fa-power-off
              </v-icon>
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
      mobilePreview: {
        type: Boolean,
        default: false,
      },
    },
    data: () => ({
      productName: eXo.env.portal.productName,
    }),
    computed: {
      menuItems () {
        return this.settings?.items || [];
      },
      homeItemIndex () {
        return this.menuItems?.findIndex?.(item => item.type === 'SITE');
      },
      avatarUrl () {
        return `/portal/rest/v1/social/users/${eXo.env.portal.userIdentityId}/avatar?byId=true`;
      },
    },
  };
</script>