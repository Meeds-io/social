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
    <v-list-item-avatar
      :class="isSpace && 'spaceAvatar' || 'userAvatar'"
      class="me-2">
      <v-avatar :size="40">
        <img
          :src="avatar"
          class="object-fit-cover ma-auto"
          loading="lazy"
          alt="">
      </v-avatar>
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title :title="fullName" class="text-body text-truncate">
        {{ fullName }}
      </v-list-item-title>
      <v-list-item-subtitle
        :title="subtitle"
        v-if="subtitle"
        class="text-truncate">
        {{ subtitle }}
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
    user: {
      type: Object,
      default: null,
    },
    approveButton: {
      type: Boolean,
      default: false,
    },
    emailSubtitle: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    avatar() {
      return this.user.avatar || this.user.profile?.avatarUrl;
    },
    fullName() {
      return this.user.fullname || this.user.profile?.fullName;
    },
    position() {
      return this.user.position || this.user.profile?.position;
    },
    email() {
      return this.user.email || this.user.profile?.email;
    },
    subtitle() {
      return this.emailSubtitle ? this.email : this.position;
    },
    isSpace() {
      return this.user.providerId === 'space';
    },
  }
};
</script>