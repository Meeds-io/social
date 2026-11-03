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
  <div class="space-setting-row">
    <v-divider v-if="divider" class="mx-4" />
    <v-list-item two-line>
      <v-list-item-content>
        <v-list-item-title
          :class="danger ? 'error--text' : 'text-color'"
          class="space-setting-row-title">
          {{ title }}
        </v-list-item-title>
        <!--
          No content between the slot's tags: the webapp's template compiler
          emits it as a function the platform's Vue 2.6 runtime never renders
          (pitfall social/vue27-slot-fallback-on-vue26-runtime).
        -->
        <v-list-item-subtitle
          v-if="$scopedSlots.summary"
          class="space-setting-row-summary text-wrap">
          <slot name="summary"></slot>
        </v-list-item-subtitle>
        <v-list-item-subtitle
          v-else-if="summary"
          class="space-setting-row-summary text-wrap">
          {{ summary }}
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <slot name="action"></slot>
      </v-list-item-action>
    </v-list-item>
  </div>
</template>
<script>
/**
 * One row of the Space settings page, drawn as the rows of the user Settings
 * page: the setting's title, one line saying its current state, and the action
 * that opens or toggles it, with an optional divider above. Social's own rows
 * use it; an application that adds a row draws the same markup itself, so it
 * renders on any social version. The `summary` slot replaces the summary text
 * when the state needs markup.
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
    divider: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
