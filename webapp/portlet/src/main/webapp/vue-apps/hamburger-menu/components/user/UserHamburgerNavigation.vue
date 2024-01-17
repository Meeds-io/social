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
  <div
    id="UserHamburgerNavigation"
    class="white full-width">
    <div class="d-flex flex-row">
      <v-spacer />
      <div class="settingsTitle">
        <v-list-item :href="settingsUrl" class="settingsTitleWrapper px-0">
          <v-list-item-icon class="my-auto"><i class="uiIcon uiIconToolbarNavItem settingsIcon"></i></v-list-item-icon>
        </v-list-item>
      </div>
      <div class="logoutLinks">
        <v-list-item :href="logoutUrl" class="px-0">
          <v-list-item-icon class="my-auto"><i class="uiIcon uiIconToolbarNavItem logoutIcon"></i></v-list-item-icon>
        </v-list-item>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  data: () => ({
    settingsUrl: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/settings`,
    logoutUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/?portal:action=Logout&portal:componentId=UIPortal`,
  }),
};
</script>

