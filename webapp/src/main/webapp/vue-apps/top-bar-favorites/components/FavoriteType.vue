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
  <v-list-item
    :key="id"
    class="px-4 mx-n4 rounded-lg"
    dense
    @click="$emit('select', false)">
    <v-list-item-icon class="me-2 my-auto align-center justify-center">
      <v-icon size="18" class="icon-default-color">
        {{ icon }}
      </v-icon>
    </v-list-item-icon>
    <v-list-item-content>
      <v-list-item-title>
        {{ label }}
      </v-list-item-title>
    </v-list-item-content>
  </v-list-item>
</template>
<script>
export default {
  props: {
    group: {
      type: Object,
      default: null,
    },
    selected: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    id() {
      return this.group.id;
    },
    label() {
      return this.group.label;
    },
    icon() {
      return this.group.icon;
    },
  },
  created() {
    if (this.selected) {
      this.$emit('select', false);
    }
  },
};
</script>