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
  <v-list-item
    dense
    @click="$root.$emit('space-administration-manage-relationships-drawer-open', space)">
    <v-card
      class="d-flex full-height justify-center"
      color="transparent"
      min-width="20"
      flat>
      <v-icon size="16">fas fa-link</v-icon>
    </v-card>
    <v-list-item-title class="ps-2">
      {{ $t('social.spaces.administration.manageSpaces.manageRelationships') }}
    </v-list-item-title>
  </v-list-item>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
  },
};
</script>