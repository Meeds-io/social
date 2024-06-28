<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-list-item class="px-0" two-line>
    <v-list-item-content>
      <v-list-item-title class="text-title">
        {{ optionLabel }}
      </v-list-item-title>
      <v-list-item-subtitle class="text-subtitle">
        {{ description }}
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action>
      <v-btn
        icon
        @click="$emit('action')">
        <v-icon
          :size="iconSize"
          class="icon-default-color">
          {{ actionIcon }}
        </v-icon>
      </v-btn>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
export default {
  props: {
    optionLabel: {
      type: String,
      default: null
    },
    description: {
      type: String,
      default: null
    },
    actionIcon: {
      type: String,
      default: null
    },
    iconSize: {
      type: Number,
      default: 18
    }
  }
};
</script>
