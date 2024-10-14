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
  <v-list v-if="displayList" dense>
    <template v-if="externalInvitations">
      <space-invite-email-list-item
        v-for="u in externalInvitations"
        :key="u.id"
        :invitation="u"
        @remove="$emit('remove', u)" />
    </template>
    <space-setting-role-list-item
      v-for="u in users"
      :key="u.id"
      :user="u"
      :approve-button="approveButton"
      :display-date="displayDate"
      :role="role"
      @approve="$emit('approve', u)"
      @remove="$emit('remove', u)" />
  </v-list>
</template>
<script>
export default {
  props: {
    users: {
      type: Array,
      default: null,
    },
    externalInvitations: {
      type: Array,
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
    role: {
      type: String,
      default: null,
    },
  },
  computed: {
    displayList() {
      return this.users?.length || this.externalInvitations?.length;
    },
  },
};
</script>