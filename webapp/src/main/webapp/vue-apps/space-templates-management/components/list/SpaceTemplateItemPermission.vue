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
  <v-chip
    :title="expression"
    color="primary"
    class="border-box-sizing">
    <v-icon
      v-if="isGroup"
      color="white"
      class="me-2 ms-n1"
      size="18">
      fa-users
    </v-icon>
    <v-avatar
      v-else
      class="spaceAvatar"
      tile
      left>
      <v-img :src="avatarUrl" />
    </v-avatar>
    <span class="text-truncate">
      {{ name }}
    </span>
  </v-chip>
</template>
<script>
export default {
  props: {
    expression: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    name: null,
    avatarUrl: null,
  }),
  computed: {
    groupId() {
      return this.expression?.includes(':') ? this.expression.split(':')[1] : this.expression;
    },
    isSpace() {
      return this.groupId?.indexOf('/spaces/') === 0;
    },
    isGroup() {
      return !this.isSpace;
    },
  },
  created() {
    this.retrieveObject();
  },
  methods: {
    async retrieveObject() {
      if (this.isSpace) {
        const space = await this.$spaceService.getSpaceByGroupId(this.groupId);
        if (space) {
          this.name = space.displayName;
          this.avatarUrl = space.avatarUrl;
        }
      } else {
        const group = await this.$identityService.getIdentityByProviderIdAndRemoteId('group', this.groupId);
        if (group) {
          this.name = group.profile?.fullname;
        }
      }
    },
  },
};
</script>