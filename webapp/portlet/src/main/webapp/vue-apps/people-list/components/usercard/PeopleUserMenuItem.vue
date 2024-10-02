<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-list-item
    v-bind="extension?.link && {
      href: extension.link(user, spaceId)
    }"
    v-on="extension?.click && {
      click: () => extension.click(user, spaceId)
    }">
    <v-list-item-title class="align-center d-flex">
      <v-card
        class="d-flex align-center justify-center transparent"
        height="25"
        width="25"
        flat>
        <v-icon :class="extension.iconColor" size="20">
          {{ extension.class || extension.icon }}
        </v-icon>
      </v-card>
      <span :class="extension.textColor" class="ms-3">
        {{ extension.title || $t(extension.titleKey) }}
      </span>
    </v-list-item-title>
  </v-list-item>
</template>
<script>
export default {
  props: {
    extension: {
      type: Object,
      default: null,
    },
    user: {
      type: Object,
      default: null,
    },
    spaceId: {
      type: Object,
      default: null,
    },
  },
};
</script>
