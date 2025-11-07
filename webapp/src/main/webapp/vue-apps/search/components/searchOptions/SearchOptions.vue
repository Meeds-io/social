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
      offset-y
      @input="onMenuToggle">
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
      <v-list 
        dense 
        class="pa-0"
        role="group"
        :aria-label="$t('search.connector.label.all.menu')">
        <v-list-item
          ref="firstItemList"
          tag="div"
          class="d-flex align-center px-3"
          tabindex="0"
          @click="$emit('select-all-connector')"
          @keydown.space.prevent="$emit('select-all-connector')"
          @keydown.tab.stop="focusNextItem"
          @keydown.down.prevent="focusNextItem"
          @keydown.up.prevent="focusPrevItem">
          <input
            type="checkbox"
            class="mt-0 mx-2 primary--text"
            :checked="allEnabled"
            @click="handleClick"
            tabindex="-1"
            aria-hidden="true">
          <label class="mb-0 mx-2">{{ $t('search.connector.label.all') }}</label>
        </v-list-item>
        <v-list-item
          v-for="connector in sortedConnectors"
          :key="connector.name"
          tag="div"
          class="d-flex align-center px-3 clickable"
          :aria-label="getAriaLabel(connector)"
          tabindex="0"
          @click="$emit('select-connector', connector)"
          @keydown.space.prevent="$emit('select-connector', connector)"
          @keydown.tab.stop="onTabPress"
          @keydown.down.prevent="focusNextItem"
          @keydown.up.prevent="focusPrevItem">
          <input
            type="checkbox"
            class="mt-0 mx-2 primary--text"
            :checked="!allEnabled && connector.enabled"
            tabindex="-1"
            aria-hidden="true">
          <label class="mb-0 mx-2">{{ connector.label }}</label>
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
      return (
        (!this.allEnabled &&
          isSelected &&
          this.$t('search.connector.option.selected.type.ariaLabel', {
            0: connector?.label,
          })) ||
        this.$t('search.connector.option.type.ariaLabel', { 0: connector?.label })
      );
    },
    onMenuToggle(isOpen) {
      if (isOpen) {
        setTimeout(() => {
          this.$nextTick(() => {
            this.$refs.firstItemList?.$el?.focus();
            const menuContent = this.$el.querySelector('.connectors-list');
            if (menuContent?.getAttribute('role') === 'menu') {
              menuContent.removeAttribute('role');
            }
          });
        }, 100);
      }
    },
    focusNextItem(event) {
      event.stopPropagation();
      event.preventDefault();
      const items = Array.from(this.$el.querySelectorAll('.connectors-list [tabindex="0"]'));
      const active = document.activeElement;
      const index = items.indexOf(active);
      if (index >= 0) {
        items[index].blur();
      }
      let next = '';
      if (event.key === 'Tab' && !event.shiftKey && index === items.length - 1) {
        next = items[0];
      } else if (event.shiftKey && index === 0) {
        next = items[items.length - 1];
      } else {
        next = index < items.length - 1 ? items[index + 1] : items[items.length - 1];
      }
      next.focus();
    },
    focusPrevItem(event) {
      event.stopPropagation();
      event.preventDefault();
      const items = Array.from(this.$el.querySelectorAll('.connectors-list [tabindex="0"]'));
      const active = document.activeElement;
      const index = items.indexOf(active);
      if (index >= 0) {
        items[index].blur();
      }
      const prev = index > 0 ? items[index - 1] : items[0];
      prev.focus();
    },
    handleClick(event) {
      if (this.allEnabled) {
        event.stopPropagation();
        event.preventDefault();
      }
    },
    onTabPress(event) {
      event.stopPropagation();
      event.preventDefault();
      if (event.shiftKey) {
        this.focusPrevItem(event);
      } else {
        this.focusNextItem(event);
      }
    }
  }
};
</script>