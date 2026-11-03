<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <v-card class="space-setting-row" flat>
    <v-list class="pa-0" dense>
      <v-list-item class="pa-0">
        <v-list-item-content>
          <v-list-item-title
            :class="danger && 'error--text'"
            class="text-title py-1">
            {{ title }}
          </v-list-item-title>
          <v-list-item-subtitle
            v-if="summary"
            class="space-setting-row-summary pt-0 pb-1 text-wrap">
            {{ summary }}
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action>
          <slot name="action"></slot>
        </v-list-item-action>
      </v-list-item>
    </v-list>
  </v-card>
</template>
<script>
/**
 * One line of the Space settings page: the setting's title, one line saying
 * its current state, and the action that opens or toggles it.
 */
export default {
  props: {
    title: {
      type: String,
      default: null,
    },
    summary: {
      type: String,
      default: null,
    },
    danger: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
