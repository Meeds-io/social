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
  <v-chip
    class="ma-1 transition-2s"
    color="primary"
    :outlined="!isSelected"
    :class="isSelected && 'suggestion-selected'"
    @click="selectSuggestion">
    <span
      :class="!isSelected && 'primary--text'">
      {{ suggestion.value }}
    </span>
  </v-chip>
</template>

<script>
export default {
  props: {
    suggestion: {
      type: Object,
      default: null
    },
    isSelected: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    selectSuggestion() {
      this.$emit('select-suggestion', this.suggestion);
    }
  }
};
</script>
