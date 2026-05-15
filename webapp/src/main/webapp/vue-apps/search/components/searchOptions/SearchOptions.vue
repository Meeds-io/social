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
  <div class="searchConnectorsParent d-flex align-center mx-4 mb-2 pb-2 content-box-sizing overflow-x-auto text-no-wrap">
    <search-space-selector />
    <v-menu
      :close-on-content-click="false"
      content-class="connectors-list"
      :attach="!$root.isMobile"
      bottom
      right
      offset-y>
      <template #activator="{ on, attrs }">
        <v-chip
          outlined
          :aria-label="$t('search.filter.type')"
          v-bind="attrs"
          v-on="on"
          tabindex="0"
          class="text-body text-header-color me-1 flex-shrink-0"
          @keydown.enter="on.click">
          <v-icon size="16" class="pe-2">
            fas fa-paste
          </v-icon>
          <span class="me-8">{{ $t('search.connector.label.all') }}</span>
          <i class="fas fa-chevron-down"></i>
        </v-chip>
      </template>
      <v-list dense class="pa-0">
        <v-list-item @click="$emit('select-all-connector')">
          <v-list-item-title class="d-flex align-center">
            <v-checkbox
              :input-value="allEnabled"
              :ripple="false"
              readonly
              dense
              :aria-label="$t('search.connector.label.all')"
              class="ma-0" />
            <span>{{ $t('search.connector.label.all') }}</span>
          </v-list-item-title>
        </v-list-item>

        <v-list-item
          v-for="connector in sortedConnectors"
          :key="connector.name"
          :aria-label="getAriaLabel(connector)"
          class="clickable"
          dense
          @click="$emit('select-connector', connector)">
          <v-list-item-title class="d-flex align-center">
            <v-checkbox
              :input-value="!allEnabled && connector.enabled"
              :ripple="false"
              dense
              :aria-label="connector.label"
              class="ma-0" />
            <span>{{ connector.label }}</span>
          </v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
    <div v-if="!allEnabled" class="selected-connectors">
      <v-chip
        v-for="connector in enabledConnectors"
        :key="connector.name"
        color="primary"
        class="me-1 text-body border-color">
        <v-icon
          v-if="connector.icon"
          size="16"
          class="pe-2">
          {{ connector.icon }}
        </v-icon>
        <span>{{ connector.label }}</span>
        <v-icon
          size="10"
          class="ms-2"
          right
          @click="$emit('select-connector', connector)">
          fas fa-times
        </v-icon>
      </v-chip>
    </div>
    <search-tag-selector @tags-changed="$emit('select-tags', $event)" />
    <search-favorites-selector :favorites="favorites" />
    <search-sort-selector class="align-end ml-auto" />
  </div>
</template>

<script>
export default {
  props: {
    favorites: {
      type: Boolean,
      default: false,
    },
    allEnabled: {
      type: Boolean,
      default: false,
    },
    sortedConnectors: {
      type: Array,
      default: () => [],
    },
    enabledConnectors: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
    getAriaLabel(connector) {
      const isSelected = this.enabledConnectors.some(
        enabled => enabled?.name === connector?.name
      );
      return !this.allEnabled && isSelected && this.$t('search.connector.option.selected.type.ariaLabel', {0: connector?.label}) || this.$t('search.connector.option.type.ariaLabel', {0: connector?.label});
    }
  }
};
</script>