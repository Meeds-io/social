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
  <div>
    <v-tooltip
      :disabled="!optionDisabled"
      bottom>
      <template #activator="{ on, attrs }">
        <label
          v-bind="attrs"
          :for="`${name}-toggle`"
          class="d-flex mt-7 align-center justify-space-between"
          v-on="on">
          <span class="font-weight-bold">
            {{ toggleLabel }}
          </span>
          <v-switch
            :input-value="enabled"
            :disabled="optionDisabled"
            :name="`${name}-toggle`"
            :ripple="false"
            color="primary"
            class="ma-0 pt-0"
            hide-details
            @change="$emit('update:enabled', $event)" />
        </label>
      </template>
      {{ tooltipLabel }}
    </v-tooltip>
    <label
      v-if="enabled"
      :for="name"
      class="mt-2">
      {{ selectLabel }}
      <v-select
        :value="value"
        :items="items"
        :placeholder="placeholder"
        :name="name"
        item-text="label"
        item-value="value"
        class="pt-1"
        dense
        outlined
        @input="$emit('input', $event)" />
    </label>
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: null
    },
    enabled: {
      type: Boolean,
      default: false
    },
    items: {
      type: Array,
      default: () => []
    },
    disabled: {
      type: Boolean,
      default: false
    },
    name: {
      type: String,
      default: null
    },
    toggleLabel: {
      type: String,
      default: null
    },
    selectLabel: {
      type: String,
      default: null
    },
    placeholder: {
      type: String,
      default: null
    },
    tooltipLabel: {
      type: String,
      default: null
    },
    isSelectedActive: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    optionDisabled() {
      return this.disabled || !this.items?.length || !this.isSelectedActive;
    }
  }
};
</script>
