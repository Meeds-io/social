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
  <v-tooltip :disabled="!spaceTemplate.system" bottom>
    <template #activator="{ on, attrs }">
      <div
        v-on="on"
        v-bind="attrs">
        <v-list-item
          :disabled="spaceTemplate.system"
          dense
          @click="$root.$emit('space-templates-delete', spaceTemplate)">
          <v-icon
            :class="!spaceTemplate.system && 'error--text' || 'disabled--text'"
            size="13">
            fa-trash
          </v-icon>
          <v-list-item-title class="ps-2">
            <span :class="!spaceTemplate.system && 'error--text' || 'disabled--text'">{{ $t('spaceTemplate.label.delete') }}</span>
          </v-list-item-title>
        </v-list-item>
      </div>
    </template>
    <span>{{ $t('spaceTemplate.label.system.noDelete') }}</span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    spaceTemplate: {
      type: Object,
      default: null,
    },
  },
};
</script>