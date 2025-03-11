<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <v-card
    class="d-flex align-center justify-center grey-background"
    flat>
    <v-card
      class="d-flex flex-column flex-grow-0 align-center justify-center overflow-hidden pa-4"
      flat
      max-width="100%"
      min-height="72"
      width="340">
      <links-header
        class="full-width"
        has-links
        :settings="settings" />
      <links-list
        class="white"
        :links="linksToDisplay"
        :settings="settings" />
    </v-card>
  </v-card>
</template>
<script>
  export default {
    props: {
      settings: {
        type: Object,
        default: null,
      },
      links: {
        type: Array,
        default: null,
      },
    },
    computed: {
      isColumn () {
        return this.settings?.type === 'COLUMN';
      },
      linksToDisplay () {
        return this.links?.slice?.(0, 6) || [];
      },
    },
  };
</script>
