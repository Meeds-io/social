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
  <v-list-item class="text-truncate pa-0">
    <exo-user-avatar
      :identity="user"
      :size="36"
      extra-class="text-truncate"
      avatar />
    <exo-space-avatar
      :space="space"
      :size="30"
      extra-class="text-truncate ms-n6 mt-6"
      avatar />
    <v-list-item-content class="py-0 accountTitleLabel text-truncate">
      <v-list-item-title class="font-weight-bold mt-2">
        <exo-user-avatar
          :identity="user"
          extra-class="text-truncate ms-2 me-1"
          fullname />
      </v-list-item-title>
      <v-list-item-subtitle class="d-flex flex-row flex-nowrap">
        <exo-space-avatar
          :space="space"
          extra-class="text-truncate"
          fullname />
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action v-if="approveButton" class="mx-0">
      <v-btn
        :title="$t('SpaceSettings.roles.acceptRequest')"
        small
        icon
        @click="$emit('approve')">
        <v-icon size="18" color="success">fa-check</v-icon>
      </v-btn>
    </v-list-item-action>
    <v-list-item-action class="ms-2">
      <v-btn
        :title="approveButton && $t('SpaceSettings.roles.rejectRequest') || $t('SpaceSettings.roles.delete')"
        small
        icon
        @click="$emit('remove')">
        <v-icon size="18" color="error">{{ approveButton && 'fa-times' || 'fa-trash' }}</v-icon>
      </v-btn>
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    membership: {
      type: Object,
      default: null,
    },
    approveButton: {
      type: Boolean,
      default: false,
    },
    displayDate: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    format: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    },
  }),
  computed: {
    user() {
      return this.membership.user;
    },
    space() {
      return this.membership.space;
    },
    avatar() {
      return this.user.avatar || this.user.profile?.avatarUrl;
    },
    fullName() {
      return this.user.fullname || this.user.profile?.fullName;
    },
    position() {
      return this.user.position || this.user.profile?.position;
    },
  }
};
</script>