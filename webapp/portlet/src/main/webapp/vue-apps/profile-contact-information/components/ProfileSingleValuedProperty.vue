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
    class="d-flex">
    <div
      class="align-start text-no-wrap font-weight-bold me-3 ma-auto">
      {{ getResolvedName(property) }}
      <profile-hidden-property-info
        :property="property"
        :hover="hover"
        :is-mobile="isMobile" />
    </div>
    <div
      class="align-end flex-grow-1 text-truncate text-end">
      <v-btn
        v-if="searchable"
        v-autolinker="property.value"
        class="primary--text text-subtitle-2 pa-0 font-weight-regular"
        min-width="auto"
        text
        @click="quickSearch">
        {{ property.value }}
      </v-btn>
      <span
        v-else
        class="font-weight-regular text-subtitle-2"
        v-autolinker="property.value">
        {{ property.value }}
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
  methods: {
    quickSearch() {
      this.$emit('quick-search', this.property);
    },
    getResolvedName(item) {
      const lang = eXo?.env?.portal?.language || 'en';
      const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileContactInformation.${item.propertyName}`)!==`profileContactInformation.${item.propertyName}`?this.$t(`profileContactInformation.${item.propertyName}`):item.propertyName;
    }
  }
};
</script>
