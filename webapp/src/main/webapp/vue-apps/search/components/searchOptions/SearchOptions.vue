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
          @keydown.enter.prevent="on.click"
          @keydown.space.prevent="on.click">
          <v-icon
            size="16"
            class="pe-2"
            aria-hidden="true">
            fas fa-paste
          </v-icon>
          <span class="me-8">{{ $t('search.connector.label.all') }}</span>
          <i class="fas fa-chevron-down"></i>
        </v-chip>
      </template>
      <v-list
        :aria-label="$t('search.filter.type.group')"
        role="group"
        dense
        class="pa-0">
        <v-list-item
          :aria-checked="String(allEnabled)"
          role="menuitemcheckbox"
          @click="$emit('select-all-connector')"
          @keydown.space.prevent="$emit('select-all-connector')">
          <v-list-item-title class="d-flex align-center">
            <v-checkbox
              :input-value="allEnabled"
              :ripple="false"
              tabindex="-1"
              aria-hidden="true"
              readonly
              dense
              class="ma-0" />
            <span>{{ $t('search.connector.label.all') }}</span>
          </v-list-item-title>
        </v-list-item>

        <v-list-item
          v-for="connector in sortedConnectors"
          :key="connector.name"
          :aria-checked="String(!allEnabled && connector.enabled)"
          role="menuitemcheckbox"
          class="clickable"
          dense
          @click="$emit('select-connector', connector)"
          @keydown.space.prevent="$emit('select-connector', connector)">
          <v-list-item-title class="d-flex align-center">
            <v-checkbox
              :input-value="!allEnabled && connector.enabled"
              :ripple="false"
              tabindex="-1"
              aria-hidden="true"
              dense
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
        <v-btn
          :aria-label="$t('search.connector.option.remove.ariaLabel', {0: connector.label})"
          class="searchConnectorRemoveButton ms-1 me-n2"
          icon
          x-small
          @click="$emit('select-connector', connector)">
          <v-icon size="10" aria-hidden="true">fas fa-times</v-icon>
        </v-btn>
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
};
</script>