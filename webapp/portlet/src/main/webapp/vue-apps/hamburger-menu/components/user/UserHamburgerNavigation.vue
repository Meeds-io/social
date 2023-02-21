<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <v-container
    id="UserHamburgerNavigation"
    px-0
    pb-4
    pt-0
    class="white">
    <v-row class="mx-0 settingsTitle">
      <v-list-item :href="settingsUrl" class="settingsTitleWrapper">
        <v-list-item-icon class="my-auto mr-6"><i class="uiIcon uiIconToolbarNavItem settingsIcon"></i></v-list-item-icon>
        <v-list-item-content v-if="expand" class="subtitle-2 settingsTitleLabel">{{ this.$t('menu.settings.title') }}</v-list-item-content>
      </v-list-item>
    </v-row>
    <v-row class="mx-0 logoutLinks">
      <v-list-item @click="logOut()">
        <v-list-item-icon class="my-auto mr-6"><i class="uiIcon uiIconToolbarNavItem logoutIcon"></i></v-list-item-icon>
        <v-list-item-content v-if="expand" class="subtitle-2 settingsTitleLabel">{{ this.$t('menu.logout.label') }}</v-list-item-content>
      </v-list-item>
    </v-row>
  </v-container>
</template>
<script>
export default {
  props: {
    expand: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    settingsUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/settings`,
  }),
  methods: {
    logOut() {
      eXo.portal.logout();
    },
    changeLanguage() {
      if (document.getElementById('UIMaskWorkspace')) {
        window.ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ChangeLanguage', true));}
    },
    accountSettings() {
      if (document.getElementById('UIMaskWorkspace')) {
        window.ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'AccountSettings', true));
      }
    },
  }
};
</script>

