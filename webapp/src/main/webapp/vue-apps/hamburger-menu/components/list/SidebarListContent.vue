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
  <div>
    <v-list-item-group v-if="menuItems" :value="activeMenu">
      <sidebar-list-item
        v-for="(item, index) in menuItems"
        :key="`${item.name}_${item.icon}_${index}`"
        :item="item" />
    </v-list-item-group>
  </div>
</template>
<script>
export default {
  computed: {
    menuItems() {
      return this.$root.settings?.items;
    },
    activeMenu() {
      if (eXo.env.portal.spaceGroup) {
        const selectedSpaceMenu = this.menuItems.find(item => item?.properties?.groupId?.length && window.location.pathname.includes(item.properties.groupId.replaceAll('/', ':')));
        return selectedSpaceMenu?.url;
      } else {
        const selectedSiteMenu = this.menuItems.find(item => item.url && window.location.pathname.includes(item.url));
        return selectedSiteMenu?.url;
      }
    },
  },
  created() {
    this.$socialWebSocket.initCometd('/SpaceWebNotification');
  },
};
</script>