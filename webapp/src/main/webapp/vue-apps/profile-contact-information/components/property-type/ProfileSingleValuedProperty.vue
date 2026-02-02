<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
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
  <v-flex
    class="d-flex flex-wrap">
    <div
      class="align-start text-no-wrap font-weight-bold me-3 mt-2 text-truncate">
      {{ propertyLabel }}
      <profile-hidden-property-info
        :property="property"
        :hover="hover"
        :is-mobile="isMobile" />
    </div>
    <div
      class="align-end flex-grow-1 text-truncate text-end mt-2">
      <div
        v-if="userProperty"
        class="ms-auto width-fit-content">
        <div
          class="my-1"
          :class="property.hidden && 'opacity-5'">
          <exo-user-avatar
            :profile-id="property.value"
            :show-disabled-user="false"
            :size="28"
            class="my-auto"
            popover-left-position
            align-top />
        </div>
      </div>
      <a
        v-else-if="searchable"
        v-autolinker="propertyDisplayValue"
        class="primary--text pa-0 font-weight-regular"
        @click.prevent="quickSearch"
        href="javascript:void(0);">
        {{ propertyDisplayValue }}
      </a>
      <span
        v-else
        class="font-weight-regular"
        v-autolinker:[propertyType]="property.value">
        {{ propertyDisplayValue }}
      </span>
    </div>
  </v-flex>
</template>

<script>

export default {
  props: {
    property: {
      type: Object,
      default: () => null,
    },
    propertyLabel: {
      type: String,
      default: null
    },
    searchable: {
      type: Boolean,
      default: false,
    },
    hover: {
      type: Boolean,
      default: false,
    },
    isMobile: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    userProperty() {
      return this.property.propertyType === 'user';
    },
    propertyOption() {
      return this.property.dropdownList
        ? this.property.propertyOptions?.find(option => `${option.id}` === `${this.property.value}`)
        : null;
    },
    propertyDisplayValue() {
      return this.propertyOption?.translatedValue ?? this.propertyOption?.value ?? this.property.value;
    },
    propertyType() {
      return this.property?.propertyType;
    }
  },
  methods: {
    quickSearch() {
      const property = { ...this.property, value: this.property.dropDownList ?? this.propertyDisplayValue };
      this.$emit('quick-search', property);
    }
  }
};
</script>
