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
  <v-list-item-group
    v-if="menuItems?.length"
    v-model="selectedValue"
    @change="resetSelectedValue">
    <sidebar-list-item
      v-for="(subItem, index) in menuItems"
      :key="`${subItem.name}_${subItem.url}_${index}`"
      :item="subItem" />
  </v-list-item-group>
</template>
<script>
export default {
  props: {
    item: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    selectedValue: null,
  }),
  computed: {
    menuItems() {
      return this.item?.items;
    },
    activeMenu() {
      if (this.menuItems?.length) {
        if (eXo.env.portal.spaceGroup) {
          const selectedSpaceMenu = this.menuItems.find(item =>
            item?.properties?.groupId?.length
            && window.location.pathname.includes(item.properties.groupId.replaceAll('/', ':')));
          return selectedSpaceMenu?.url;
        } else {
          const selectedSiteMenu = this.menuItems.find(item => item.url && window.location.pathname.includes(item.url));
          return selectedSiteMenu?.url;
        }
      } else {
        return null;
      }
    },
  },
  watch: {
    selectedValue() {
      this.resetSelectedValue();
    },
    activeMenu: {
      immediate: true,
      handler() {
        this.resetSelectedValue();
      },
    },
  },
  methods: {
    async resetSelectedValue() {
      if (this.selectedValue !== this.activeMenu) {
        this.selectedValue = null;
        await this.$nextTick();
        this.selectedValue = this.activeMenu;
      }
    },
  },
};
</script>