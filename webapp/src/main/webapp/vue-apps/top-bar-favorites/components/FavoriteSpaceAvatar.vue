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
    v-if="displayName"
    :title="displayName"
    class="d-flex align-center flex-row overflow-hidden">
    <v-avatar
      class="rounded"
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
      {{ displayName }}
    </div>
  </div>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
    spaceId: {
      type: String,
      default: null,
    },
    spacePrettyName: {
      type: String,
      default: null,
    },
    spaceGroupId: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    retrievedSpace: null,
  }),
  computed: {
    displayName() {
      return this.space?.displayName || this.retrievedSpace?.displayName;
    },
    avatarUrl() {
      return this.space?.avatarUrl || this.retrievedSpace?.avatarUrl;
    },
  },
  created() {
    if (!this.space) {
      if (this.spaceId) {
        this.$spaceService.getSpaceById(this.spaceId)
          .then(space => this.retrievedSpace = space);
      } else if (this.spacePrettyName) {
        this.$spaceService.getSpaceByPrettyName(this.spacePrettyName)
          .then(space => this.retrievedSpace = space);
      } else if (this.spaceGroupId) {
        this.$spaceService.getSpaceByGroupId(this.spaceGroupId)
          .then(space => this.retrievedSpace = space);
      }
    }
  },
};
</script>
