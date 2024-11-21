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
  <v-list-item
    v-else
    class="d-flex"
    dense>
    <v-list-item-avatar min-width="36">
      <v-icon size="18">{{ item.icon }}</v-icon>
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
};
</script>