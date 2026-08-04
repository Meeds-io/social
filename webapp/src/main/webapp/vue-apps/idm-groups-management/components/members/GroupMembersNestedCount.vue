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
  <v-btn
    v-if="initialized"
    class="px-2"
    text
    icon
    @click="openGroupMembershipDrawer">
    {{ nestedCount || '-' }}
  </v-btn>
</template>
<script>
export default {
  props: {
    member: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    nestedCount: 0,
    initialized: false,
  }),
  computed: {
    parentGroupId() {
      return this.$root.selectedGroup?.id;
    },
  },
  created() {
    this.retrieveNestedGroupsCount();
  },
  methods: {
    retrieveNestedGroupsCount() {
      return this.$groupMembersService.getUserNestedGroupsCount(this.member?.userName, this.parentGroupId)
        .then(counts => {
          this.nestedCount = counts?.nestedCount || 0;
          this.initialized = true;
        })
        .catch(error => console.error('Error while counting user nested groups:', error));
    },
    openGroupMembershipDrawer() {
      this.$root.$emit('open-group-membership-drawer', this.member);
    },
  },
};
</script>
