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
  <v-hover v-model="hover">
    <v-card
      class="d-flex flex-column overflow-hidden transparent"
      min-height="100%"
      height="fit-content"
      max-height="100%"
      flat>
      <v-list class="list-border-active transparent d-flex flex-column overflow-hidden py-0 flex-grow-1 flex-shrink-1">
        <sidebar-list-header class="flex-grow-0 flex-shrink-0 transparent" />
        <sidebar-list-content
          :class="overflowClass"
          class="flex-grow-1 flex-shrink-1 overflow-x-hidden specific-scrollbar transparent" />
        <sidebar-list-footer class="flex-grow-0 flex-shrink-0 transparent" />
      </v-list>
      <sidebar-home-dialog />
      <sidebar-leave-space-dialog />
    </v-card>
  </v-hover>
</template>
<script>
export default {
  data: () => ({
    hover: false,
  }),
  computed: {
    overflowClass() {
      return this.$root.icon && !this.$root.expand && 'overflow-y-hidden' || 'overflow-y-auto';
    },
  },
};
</script>