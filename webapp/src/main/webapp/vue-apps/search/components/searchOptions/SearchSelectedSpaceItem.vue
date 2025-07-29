<!--
This file is part of the Meeds project (https://meeds.io/).

Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
    color="primary"
    class="mx-1 text-body border-color width-fit-content">
    <v-avatar
      :size="10"
      tile
      class="my-auto me-2 spaceAvatar">
      <img
        :src="spaceAvatarUrl"
        alt=""
        class="object-fit-cover ma-auto"
        loading="lazy">
    </v-avatar>
    <span>{{ spaceDisplayName }}</span>
    <v-icon
      size="10"
      class="ms-2"
      right
      @click="$emit('delete-selected-space', space)">
      fas fa-times
    </v-icon>
  </v-chip>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    }
  },
  computed: {
    spaceAvatarUrl () {
      return this.space?.avatarUrl;
    },
    spaceDisplayName () {
      return this.space?.displayName;
    }
  }
};
</script>