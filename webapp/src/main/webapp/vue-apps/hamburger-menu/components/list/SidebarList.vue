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
      class="d-flex flex-column overflow-hidden"
      min-height="100%"
      height="fit-content"
      max-height="100%"
      flat>
      <v-list class="list-border-active d-flex flex-column overflow-hidden py-0 flex-grow-1 flex-shrink-1">
        <sidebar-list-header
          :elevate="elevateTop"
          class="flex-grow-0 flex-shrink-0" />
        <sidebar-list-content
          :hover="hover"
          @scroll-top="scrollTop = $event"
          @scroll-bottom="scrollBottom = $event" />
        <sidebar-list-footer
          :elevate="elevateBottom"
          class="flex-grow-0 flex-shrink-0" />
      </v-list>
      <sidebar-home-dialog />
    </v-card>
  </v-hover>
</template>
<script>
export default {
  data: () => ({
    scrollTop: false,
    scrollBottom: false,
    hover: false,
  }),
  computed: {
    elevateTop() {
      return this.hover && this.scrollTop;
    },
    elevateBottom() {
      return this.hover && this.scrollBottom;
    },
  },
};
</script>