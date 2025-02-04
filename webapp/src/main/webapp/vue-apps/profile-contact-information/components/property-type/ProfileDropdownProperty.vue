<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 -->

<template>
  <div :class="{'mx-4 mt-2': !multiValued}">
    <label class="font-weight-bold text-color text-capitalize-first-letter">
      {{ propertyLabel }}
    </label>
    <div class="d-flex align-center">
      <v-combobox
        v-model="selectedOption"
        :items="mappedOptions"
        :ref="`propertyOptions${property.id}`"
        :name="`propertyOptions${property.id}`"
        class="elevation-0 mt-2 pt-0 no-border dropdownPropertyInput"
        item-text="translatedValue"
        item-value="id"
        clear-icon="fas fa-times"
        clearable
        single-line
        solo
        flat
        outlined
        dense>
        <template #label>
          <span class="text-sub-title">
            {{ $t('profileContactInformation.dropdown.property.choose.label') }}
          </span>
        </template>
      </v-combobox>
      <profile-hide-property-button
        v-if="!multiValued"
        :property="propertyObject" />
      <v-btn
        v-if="multiValued"
        icon
        @click="$emit('remove')">
        <v-icon
          class="error--text"
          small>
          fa-minus
        </v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      selectedOption: null,
      propertyObject: null,
      hasInputFilterValue: false
    };
  },
  props: {
    property: {
      type: Object,
      default: null
    },
    parentProperty: {
      type: Object,
      default: null
    },
    propertyLabel: {
      type: String,
      default: null
    },
    multiValued: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    mappedOptions() {
      return this.options && this.mapOptions(this.options);
    },
    options() {
      return this.parentProperty?.propertyOptions || this.property?.propertyOptions;
    }
  },
  watch: {
    property() {
      this.clonePropertyObject();
    },
    selectedOption() {
      this.propertyObject.value = this.selectedOption?.id;
      this.$emit('property-updated', this.propertyObject);
    }
  },
  created() {
    this.clonePropertyObject();
  },
  methods: {
    mapOptions(options) {
      return options.map(option => ({
        ...option,
        translatedValue: option.translatedValue ?? option.value
      }));
    },
    clonePropertyObject() {
      this.propertyObject = this.parentProperty && this.property || structuredClone(this.property);
      this.selectedOption = this.mappedOptions?.find(
        option => `${option.id}` === `${this.propertyObject.value}`
      );
    }
  }
};
</script>
