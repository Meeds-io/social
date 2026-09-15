<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
-->
<template>
  <div class="ms-2">
    <v-checkbox
      v-for="category in categories"
      :key="category.id"
      :input-value="value.includes(category.id)"
      :label="category.label"
      class="mt-2 pt-0"
      dense
      hide-details
      @change="checkCategory(category.id, $event)" />
  </div>
</template>

<script>
export default {
  props: {
    /** Identifiers of the categories the user checked */
    value: {
      type: Array,
      default: () => [],
    },
    /** Categories the installed addons offer, in display order */
    categories: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
    checkCategory(categoryId, checked) {
      const categories = this.value.filter(id => id !== categoryId);
      if (checked) {
        categories.push(categoryId);
      }
      this.$emit('input', categories);
    },
  },
};
</script>
