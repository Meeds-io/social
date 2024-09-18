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
  <v-list-item class="pa-0">
    <v-list-item-avatar class="me-2">
      <v-avatar :size="40">
        <img
          :src="user.avatar"
          class="object-fit-cover ma-auto"
          loading="lazy"
          alt="">
      </v-avatar>
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title class="text-body">
        {{ user.fullname }}
      </v-list-item-title>
      <v-list-item-subtitle v-if="user.position">
        {{ user.position }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action>
      <v-btn
        small
        icon
        @click="$emit('remove')">
        <v-icon size="18" color="error">fa-trash</v-icon>
      </v-btn>
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    user: {
      type: Object,
      default: null,
    },
  },
};
</script>