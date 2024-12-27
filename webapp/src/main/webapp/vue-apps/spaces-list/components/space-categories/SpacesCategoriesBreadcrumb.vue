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
  <div class="d-flex align-center flex-grow-0 flex-shrink-0 max-width-fit overflow-x-auto specific-scrollbar">
    <v-btn
      class="hidden-xs-only"
      height="32"
      width="32"
      icon
      @click="$root.selectedCategoryId = null">
      <v-icon size="24">fa-home</v-icon>
    </v-btn>
    <div
      v-for="(category, index) in breadcrumb"
      :key="category.id">
      <div class="d-flex align-center">
        <v-icon
          :class="index === 0 && 'hidden-xs-only'"
          class="mx-2"
          size="16">
          {{ chevronIcon }}
        </v-icon>
        <spaces-category-chip
          :category="category"
          :breadcrumb="index > 1"
          class="flex-shrink-0"
          selected
          @select="$emit('select', $event)" />
      </div>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    breadcrumb: {
      type: Array,
      default: null,
    }
  },
  computed: {
    chevronIcon() {
      return this.$vuetify.rtl && 'fa-chevron-left' || 'fa-chevron-right';
    },
  },
};
</script>