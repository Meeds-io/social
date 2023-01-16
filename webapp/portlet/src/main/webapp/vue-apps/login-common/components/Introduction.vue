<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
    :width="width"
    :max-width="maxWidth"
    :height="height"
    :max-height="maxHeight"
    :color="color"
    class="border-box-sizing d-flex"
    tile
    flat>
    <v-row
      align-content="center"
      justify="center"
      no-gutters>
      <v-col>
        <v-card
          class="text-center"
          color="transparent"
          flat
          dark>
          <v-card-title class="d-flex flex-column">
            <span
              v-if="$slots.title"
              class="display-1 font-weight-bold mx-auto text-wrap text-break">
              <slot name="title"></slot>
            </span>
            <span
              v-if="$slots.subtitle"
              class="text-h6 pa-4 text-wrap mx-auto text-break">
              <slot name="subtitle"></slot>
            </span>
          </v-card-title>
        </v-card>
      </v-col>
    </v-row>
  </v-card>
</template>
<script>
export default {
  props: {
    width: {
      type: String,
      default: () => '100%',
    },
    height: {
      type: String,
      default: () => '100%',
    },
    maxWidth: {
      type: String,
      default: () => '100%',
    },
    maxHeight: {
      type: String,
      default: () => '100%',
    },
    color: {
      type: String,
      default: () => 'primary',
    },
  },
  computed: {
    mobile() {
      return this.$vuetify.breakpoint.mobile;
    },
  },
};
</script>
