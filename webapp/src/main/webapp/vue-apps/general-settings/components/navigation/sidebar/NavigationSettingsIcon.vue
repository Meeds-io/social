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
  <v-avatar
    :height="iconSize"
    :min-width="iconSize"
    :width="iconSize + 8"
    class="d-flex justify-center align-center"
    tile>
    <v-img
      v-if="iconUrl"
      :src="iconUrl"
      :max-height="iconSize"
      :height="iconSize"
      :max-width="iconSize"
      contain
      eager />
    <v-icon
      v-else
      :size="iconSize"
      class="icon-default-color">
      {{ icon || 'fa-globe' }}
    </v-icon>
  </v-avatar>
</template>
<script>
export default {
  props: {
    iconSize: {
      type: Number,
      default: () => 34,
    },
    icon: {
      type: String,
      default: null,
    },
  },
  computed: {
    iconUrl() {
      if (this.icon?.includes?.('base64') || this.icon?.includes?.('/')) {
        return this.icon;
      } else {
        return null;
      }
    },
  },
};
</script>