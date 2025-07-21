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
  <v-card class="border-radius border-box-sizing">
    <v-list class="ma-0 py-0 text-no-wrap width-fit-content">
      <v-list-item class="py-2">
        <a
          :href="profileUri"
          rel="noopener noreferrer">
          <v-badge
            :color="userStatusColor"
            :value="true"
            class="ma-0 pa-0"
            content=""
            offset-x="16"
            offset-y="16"
            width="12"
            height="12"
            bordered
            bottom
            overlap
            dot>
            <v-list-item-avatar
              width="38"
              min-width="38"
              height="38"
              class="me-1 ms-0">
              <v-img
                :src="avatarUrl"
                :alt="fullName" />
            </v-list-item-avatar>
          </v-badge>
        </a>
        <v-list-item-content class="pa-0">
          <v-list-item-title class="mb-0">
            <a
              :href="profileUri"
              rel="noopener noreferrer"
              class="text-decoration-none text-color font-weight-bold">
              {{ fullName }}
            </a>
          </v-list-item-title>
          <v-list-item-subtitle>
            <a
              class="text-caption text-color"
              :href="profileUri"
              rel="noopener noreferrer">
              {{ $t('menu.userProfilePageLink') }}
            </a>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script>

export default {
  props: {
    profileUri: {
      type: String,
      default: null
    },
    user: {
      type: Object,
      default: null
    },
    userStatusColor: {
      type: String,
      default: null
    }
  },
  computed: {
    fullName() {
      return this.user?.fullname;
    },
    avatarUrl() {
      return this.$root.avatarUrl;
    }
  }
};
</script>
