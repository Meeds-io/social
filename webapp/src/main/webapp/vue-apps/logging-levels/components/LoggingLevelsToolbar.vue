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
  <application-toolbar
    :right-text-filter="{
      minCharacters: 1,
      placeholder: $t('loggingLevels.filter.placeholder'),
      tooltip: $t('loggingLevels.filter.placeholder'),
    }"
    class="px-1"
    @filter-text-input="$emit('filter-changed', $event)"
    @filter-text-input-end-typing="$emit('filter-changed-end-typing', $event)">
    <template #left>
      <v-btn
        id="applicationToolbarLeftButton"
        :aria-label="$t('loggingLevels.add')"
        :disabled="readOnly"
        :class="$vuetify.breakpoint.mobile && 'px-0'"
        class="btn btn-primary text-truncate"
        @click="$emit('add')">
        <v-icon size="18">fa-plus</v-icon>
        <span class="text-truncate text-none hidden-xs-only ms-2">
          {{ $t('loggingLevels.add') }}
        </span>
      </v-btn>
    </template>
    <template #right>
      <v-btn
        :aria-label="$t('loggingLevels.resetAll.button')"
        :disabled="readOnly"
        class="btn"
        @click="$emit('reset-all')">
        <v-icon size="18">fa-undo</v-icon>
        <span class="text-truncate text-none hidden-xs-only ms-2">
          {{ $t('loggingLevels.resetAll.button') }}
        </span>
      </v-btn>
    </template>
  </application-toolbar>
</template>
<script>
export default {
  props: {
    readOnly: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
