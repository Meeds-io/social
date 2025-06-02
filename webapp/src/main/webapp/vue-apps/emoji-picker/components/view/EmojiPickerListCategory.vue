<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2025 Meeds Association contact@meeds.io

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
  <v-tabs
    v-model="internalSelectedIndex"
    class="d-flex align-center justify-space-between"
    background-color="background-grey-primary"
    grow>
    <v-tab
      v-for="(category, index) in categories"
      :key="category.name"
      :disabled="isSearching"
      :ripple="false"
      class="pa-0 no-min-width">
      <v-btn
        :title="category.name"
        min-width="38"
        class="pa-0 btn full-height btn-default no-border background-grey-primary"
        @click="$emit('select', index)">
        <v-icon
          class="icon-default-color"
          size="16">
          {{ category.iconClass }}
        </v-icon>
      </v-btn>
    </v-tab>
  </v-tabs>
</template>

<script>

export default {
  data() {
    return {
      internalSelectedIndex: 10
    };
  },
  props: {
    categories: {
      type: Array,
      default: null
    },
    selectedCategory: {
      type: Object,
      default: null
    },
    isSearching: {
      type: Boolean,
      default: false
    }
  }
};
</script>
