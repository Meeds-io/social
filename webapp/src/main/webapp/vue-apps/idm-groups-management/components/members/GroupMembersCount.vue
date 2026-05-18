<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <exo-user-avatars-list
    class="justify-center align-center"
    :margin-left="totalSize > 1 && 'ml-n5' || ''"
    v-if="isManagerMembershipType"
    :default-length="totalSize"
    :icon-size="33"
    :max="3"
    compact
    clickable
    popover />
  <span v-else>
    {{ formattedTotalSize }}
  </span>
</template>
<script>
export default {
  props: {
    group: {
      type: Object,
      default: null
    },
    membershipType: {
      type: String,
      default: null
    }
  },
  data: () => ({
    groupMembers: [],
    totalSize: 0,
  }),
  computed: {
    isManagerMembershipType() {
      return this.membershipType === 'manager';
    },
    formattedTotalSize() {
      if (this.totalSize >= 1000) {
        return `${(this.totalSize / 1000).toFixed(this.totalSize >= 10000 ? 0 : 1)}k`;
      }
      return this.totalSize;
    },
  },
  created() {
    this.countGroupMembers();
  },
  methods: {
    async countGroupMembers() {
      if (!this.membershipType) {
        const data =
            await this.$groupMembersService.getGroupMembers(
              this.group.id,
              null,
              null,
              0,
              1
            ) || {};
        this.groupMembers = data?.users || data?.entities || [];
        this.totalSize = data?.size || 0;
      }
    },
  }
};
</script>
