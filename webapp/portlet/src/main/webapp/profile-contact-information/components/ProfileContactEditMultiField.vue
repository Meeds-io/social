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
  <v-card-text class="text-color pb-2">
    <div class="d-flex">
      <div class="align-start flex-grow-1 text-no-wrap text-left font-weight-bold d-flex align-center">
        <span>
          {{ title }}
        </span>
      </div>
      <div class="align-end flex-grow-1 text-no-wrap text-end">
        <v-btn
          color="primary"
          class="px-0"
          outlined
          link
          text
          @click="addNewItem">
          + {{ $t('profileContactInformation.addNew') }}
        </v-btn>
      </div>
    </div>
    <v-flex v-for="(value, i) in values" :key="i">
      <profile-contact-edit-multi-field-select
        :value="value"
        :items="items"
        :item-name="itemName"
        :item-value="itemValue"
        :type="type"
        @remove="remove(i)" />
    </v-flex>
  </v-card-text>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: () => null,
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
    items: {
      type: Array,
      default: () => [],
    },
    values: {
      type: Array,
      default: () => null,
    },
  },
  methods: {
    remove(i) {
      this.values.splice(i, 1);
    },
    addNewItem() {
      const item = {};
      if (this.itemName && this.items && this.items.length) {
        item[this.itemName] = this.items[0] || '';
      }
      if (this.itemValue) {
        item[this.itemValue] = '';
      }
      this.values.push(item);
      this.$forceUpdate();
    },
  },
};
</script>