<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
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
  <v-col class="mx-3">
    <v-row>
      <v-icon
        class="uiIcon fa mx-auto" 
        :size="iconSize"
        :color="iconColor"
        :class="iconRef && iconRef || ''" />
    </v-row>
    <v-row class="text-center mt-1">
      <v-card
        flat
        max-width="150"
        class="mx-auto">
        {{ infoMessage }}
      </v-card>
    </v-row>
  </v-col>
</template>
<script>
export default {
  props: {
    iconRef: {
      type: String,
      default: 'fa-exclamation-circle'
    },
    iconSize: {
      type: String,
      default: '50'
    },
    iconColor: {
      type: String,
      default: 'primary'
    },
    infoMessage: {
      type: String,
      default: null
    },
  },
};
</script>