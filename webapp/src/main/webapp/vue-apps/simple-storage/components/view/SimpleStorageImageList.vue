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
  <v-data-table
    :headers="headers"
    :items="items"
    item-key="name"
    class="px-5"
    hide-default-footer
    dense>
    <!-- eslint-disable vue/valid-v-slot -->
    <template #item.actions="{ item }">
      {{ item }}
    </template>
  </v-data-table>
</template>

<script>
export default {
  data() {
    return {
      headers: [
        {value: 'preview', sortable: false, width: '70'},
        {text: this.$t('simpleStorage.name.label'), value: 'name'},
        {text: this.$t('simpleStorage.creationDate.label'), value: 'creationDate'},
        {text: this.$t('simpleStorage.size.label'), value: 'size'},
        {text: this.$t('simpleStorage.actions.label'), value: 'actions', sortable: false},
      ],
      items: []
    };
  }
};
</script>
