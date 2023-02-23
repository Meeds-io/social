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
  <v-list
    v-if="menuItem && menuItem.label && menuItem.key && menuItem.children && menuItem.children.length"
    dense
    rounded
    class="adminMenu contentMenuWrapper py-0">
    <v-subheader class="text-uppercase subtitle-2 font-weight-bold">{{ menuItem.label }}</v-subheader>
    <v-treeview
      :items="menuItem.children"
      item-children="children"
      item-key="name"
      item-text="label"
      dense
      rounded
      hoverable
      open-all
      class="subItemTitle">
      <template slot="label" slot-scope="{ item }">
        <a v-if="item && item.link" :href="item.link">{{ item.label }}</a>
        <span v-else-if="item">{{ item.label }}</span>
        <span v-else>{{ item }}</span>
      </template>
    </v-treeview>
  </v-list>
</template>

<script>
export default {
  name: 'AdministrationMenuItemApp',
  props: {
    menuItem: {
      type: Object,
      default: null
    },
  },
};
</script>