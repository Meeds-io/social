<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
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
  <div class="d-flex flex-no-wrap pb-2 multiField">
    <select
      v-if="items && items.length"
      v-model="value[itemName]"
      class="ignore-vuetify-classes align-start flex-grow-0 half-width text-capitalize">
      <option
        v-for="item in items"
        :key="item"
        :value="item"
        class="text-capitalize">
        {{ getLabel(item) }}
      </option>
    </select>
    <input
      v-model="value[itemValue]"
      :title="value[itemValue]"
      type="text"
      class="ignore-vuetify-classes align-end flex-grow-1"
      maxlength="2000" />
    <v-icon small class="removeMultiFieldValue error--text" @click="$emit('remove')">fa-minus</v-icon>
  </div>
</template>

<script>
export default {
  props: {
    items: {
      type: Array,
      default: () => [],
    },
    type: {
      type: String,
      default: () => null,
    },
    itemName: {
      type: String,
      default: () => null,
    },
    itemValue: {
      type: String,
      default: () => null,
    },
    value: {
      type: Object,
      default: () => ({}),
    },
  },
  methods: {
    getLabel(key) {
      const label = `profileContactInformation.${this.type}.${key}`;
      const translation = this.$t(label);
      return translation === label && key || translation;
    },
  }
};
</script>