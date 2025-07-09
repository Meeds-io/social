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
  <v-app>
    <v-card
      flat
      :color="this.backgroundPath && 'transparent' || 'primary'"
      class="full-height d-flex justify-center align-center">
      <img v-if="backgroundPath"
           :src="backgroundPath"
           :alt="backgroundAlt || ''"
           class="full-height full-witdh object-fit-cover rounded-0">
      <v-card
        :class="backgroundPath && 'position-absolute t-0'"
        tile
        flat
        class="fill-height transparent">
        <v-card-title class="d-flex flex-column py-0 fill-height justify-center">
          <span class="text-title  mx-auto text-wrap text-break">
            {{ this.title }}
          </span>
          <span class="text-body pa-4 text-wrap mx-auto text-break">
            {{ this.subtitle }}
          </span>
        </v-card-title>
      </v-card>
    </v-card>
  </v-app>
</template>

<script>
export default {
  props: {
    backgroundPath: {
      type: String,
      default: null,
    },
    backgroundAlt: {
      type: String,
      default: null,
    },
    authenticationTitle: {
      type: String,
      default: null,
    },
    authenticationSubtitle: {
      type: String,
      default: null,
    }
  },
  computed: {
    title() {
      return this.$t(this.authenticationTitle);
    },
    subtitle() {
      return this.$t(this.authenticationSubtitle);
    },
  },
};
</script>
