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
  <v-chip
    :outlined="!isFavoritesFilter"
    :color="isFavoritesFilter ? 'primary' : ''"
    :aria-pressed="isFavoritesFilter"
    :aria-label="$t('search.filter.favorite')"
    class="me-1 text-body border-color flex-shrink-0"
    tabindex="0"
    @keydown.enter="emitFavoritesChange"
    @click="emitFavoritesChange">
    <v-icon size="16" class="pe-2 yellow--text text--darken-2">
      fas fa-star
    </v-icon>
    <span :class="isFavoritesFilter && 'text-white' || 'text-header-color'">{{ $t('search.connector.label.favorites') }}</span>
  </v-chip>
</template>
<script>

export default {
  props: {
    favorites: {
      type: Boolean,
      default: false,
    }
  },
  computed: {
    isFavoritesFilter() {
      return this.favorites;
    }
  },
  methods: {
    emitFavoritesChange() {
      this.$root.$emit('favorites-changed');
    }
  }
};
</script>
