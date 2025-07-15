<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2025 Meeds Association contact@meeds.io

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
  <div
    v-if="fullname"
    :title="fullname"
    class="d-flex align-center flex-row overflow-hidden">
    <v-avatar
      min-width="16"
      height="16"
      width="16">
      <img
        :src="avatarUrl"
        class="object-fit-cover ma-auto"
        loading="lazy"
        alt="">
    </v-avatar>
    <div class="ms-1 text-truncate">
      {{ fullname }}
    </div>
  </div>
</template>
<script>
export default {
  props: {
    identity: {
      type: Object,
      default: null,
    },
    username: {
      type: String,
      default: null,
    },
  },
  data() {
    return {
      retrievedIdentity: null,
    };
  },
  computed: {
    userIdentity() {
      return this.identity || this.retrievedIdentity;
    },
    fullname() {
      return this.userIdentity?.fullname;
    },
    avatarUrl() {
      return this.userIdentity?.avatar;
    },
  },
  created() {
    if (!this.identity) {
      this.$userService.getUser(this.username)
        .then(user => this.retrievedIdentity = user);
    }
  },
};
</script>