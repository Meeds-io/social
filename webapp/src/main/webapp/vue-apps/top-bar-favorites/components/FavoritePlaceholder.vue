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
  <v-card
    class="d-flex full-height align-center justify-center no-border-radius"
    min-height="260"
    flat>
    <div class="noFavoritesContent">
      <v-icon class="mx-auto mb-3" size="68">fas fa-star </v-icon>
      <p>{{ label }}</p>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    type: {
      type: String,
      default: null,
    },
  },
  computed: {
    label() {
      return !this.type || this.type === 'all' ? this.$t('UITopBarFavoritesPortlet.NoFavorites') : this.$t('UITopBarFavoritesPortlet.NoTypedFavorites', {
        0: this.$t(`UITopBarFavoritesPortlet.types.${this.type}`)?.toLowerCase?.(),
      });
    },
  }
};
</script>