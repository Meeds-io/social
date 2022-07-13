<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
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
  <v-flex class="d-flex content-box-sizing justify-end">
    <select
      v-model="streamFilter"
      class="width-auto my-auto ignore-vuetify-classes"
      @change="applyFilter">
      <option value="all">{{ $t('activity.filter.all') }}</option>
      <option value="user_stream">{{ $t('activity.filter.myActivities') }}</option>
      <option value="manage_spaces_stream">{{ $t('activity.filter.manageSpaces') }}</option>
      <option value="favorite_spaces_stream">{{ $t('activity.filter.favoriteSpaces') }}</option>
    </select>
  </v-flex>
</template>
<script>
export default {
  props: {
    streamFilter: {
      type: String,
      default: () => 'all',
    },
  },
  methods: {
    applyFilter() {
      document.dispatchEvent(new CustomEvent('activity-stream-type-filter-applied', {detail: this.streamFilter}));
    },
  },
};
</script>