<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 -->

<template>
  <v-hover
    v-slot="{ hover }">
    <v-card
      :class="hover && 'elevation-2'"
      class="mx-auto d-flex flex-column border-box-sizing card-border-radius chartCompactUserCard"
      width="268"
      height="85"
      outlined
      @click="$emit('click')">
      <div>
        <v-list-item class="px-2">
          <v-list-item-avatar
            class="me-2 mb-auto"
            size="35">
            <v-img
              :src="`${avatarUrl}&size=35x35`"
              :lazy-src="avatarUrl" />
          </v-list-item-avatar>
          <v-list-item-content class="pb-0 pt-1">
            <v-list-item-title
              class="text-wrap userCardTitle mb-0">
              <p class="text-truncate-2 text-break font-weight-bold mb-0">
                {{ fullName }}
                <span
                  v-if="externalUser"
                  class="grey--text">
                  {{ $t('peopleList.label.external') }}
                </span>
              </p>
            </v-list-item-title>
            <v-list-item-subtitle>
              <p class="mb-0 caption text-truncate">
                {{ primaryProperty }}
              </p>
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </div>
      <div
        v-if="managedUsersCount !== '0'"
        class="d-flex mt-auto align-self-end pe-2 pb-1">
        <p class="mb-0">
          <v-icon
            size="16">
            fas fa-user-friends
          </v-icon>
          {{ managedUsersCount }}
        </p>
      </div>
    </v-card>
  </v-hover>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: null
    }
  },
  computed: {
    enabled() {
      return this.user?.enabled;
    },
    primaryProperty() {
      return this.user?.primaryProperty;
    },
    bannerUrl() {
      return this.user?.banner;
    },
    avatarUrl() {
      return this.user?.avatar;
    },
    fullName() {
      return this.user?.fullname;
    },
    managedUsersCount() {
      return !this.user?.managedUsersCount && '0' || this.user?.managedUsersCount < 99
          && this.user?.managedUsersCount || '+99';
    },
    externalUser() {
      return this.user?.external;
    },
  }
};
</script>
