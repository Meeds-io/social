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
  <v-btn
    :loading="loading"
    :disabled="disabled"
    :outlined="outlined"
    :icon="iconButton"
    :title="iconButton && label"
    :class="{extraButtonClass, 'ms-2': lgAndUp, 'ms-0': !lgAndUp}"
    class="no-border transparent"
    @click="$emit('click')">
    <v-icon
      :class="extraIconClass"
      :size="iconSize"
      class="ma-1">
      {{ icon }}
    </v-icon>
    <span
      v-if="!iconButton"
      :class="extraTextClass"
      class="ms-1">
      {{ label }}
    </span>
  </v-btn>
</template>

<script>
export default {
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    label: {
      type: String,
      default: ''
    },
    icon: {
      type: String,
      default: ''
    },
    iconSize: {
      type: Number,
      default: 16
    },
    outlined: {
      type: Boolean,
      default: false
    },
    extraButtonClass: {
      type: String,
      default: ''
    },
    extraTextClass: {
      type: String,
      default: ''
    },
    extraIconClass: {
      type: String,
      default: ''
    },
    iconButton: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    lgAndUp () {
      return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.lg;
    }
  }
};
</script>