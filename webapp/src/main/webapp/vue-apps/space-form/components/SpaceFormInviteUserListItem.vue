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
      class="me-2"
      :class="isSpace && 'spaceAvatar' || 'userAvatar'">
      <v-avatar :size="40">
        <img
          alt=""
          class="object-fit-cover ma-auto"
          loading="lazy"
          :src="avatar">
      </v-avatar>
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title
        class="text-body text-truncate"
        :title="fullName">
        {{ fullName }}
      </v-list-item-title>
      <v-list-item-subtitle
        v-if="subtitle"
        class="text-truncate"
        :title="subtitle">
        {{ subtitle }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action class="ms-2">
      <v-btn
        icon
        small
        :title="$t('SpaceSettings.roles.delete')"
        @click="$emit('remove')">
        <v-icon
          color="error"
          size="18">
          fa-trash
        </v-icon>
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
      emailSubtitle: {
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
      avatar () {
        return this.user.avatar || this.user.profile?.avatarUrl;
      },
      fullName () {
        return this.user.fullname || this.user.profile?.fullName;
      },
      position () {
        return this.user.position || this.user.profile?.position;
      },
      email () {
        return this.user.email || this.user.profile?.email;
      },
      subtitle () {
        return this.emailSubtitle ? this.email : this.position;
      },
      isSpace () {
        return this.user.providerId === 'space';
      },
    },
  };
</script>