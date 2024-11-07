<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    v-if="operations"
    class="d-flex flex-column"
    flat>
    <v-data-table
      :headers="headers"
      :items="operations"
      :hide-default-header="$root.isMobile"
      disable-sort>
      <template slot="item" slot-scope="props">
        <spaces-administration-binding-report-item
          :key="props.item.id"
          :operation="props.item" />
      </template>
    </v-data-table>
  </v-card>
</template>
<script>
export default {
  props: {
    operations: {
      type: Array,
      default: () => [],
    },
  },
  computed: {
    headers() {
      return [
        {
          text: this.$t('social.spaces.administration.binding.reports.table.title.group'),
          value: 'group.name',
          align: 'left',
          sortable: false,
          class: 'pe-0',
          width: '120px'
        },
        {
          text: this.$t('social.spaces.administration.binding.reports.table.title.start.date'),
          value: 'startDate',
          align: 'center',
          sortable: false,
          class: 'px-1',
          width: '120px'
        },
        {
          text: this.$t('social.spaces.administration.binding.reports.table.title.end.date'),
          value: 'endDate',
          align: 'center',
          sortable: false,
          class: 'px-1',
          width: '120px'
        },
        {
          text: this.$t('social.spaces.administration.binding.reports.table.title.operation.type'),
          value: 'operationType',
          align: 'center',
          sortable: false,
          class: 'px-1',
          width: '120px',
        },
        {
          text: this.$t('social.spaces.administration.binding.reports.table.title.added.users'),
          value: 'addedUsers',
          align: 'center',
          sortable: false,
          class: 'px-1',
          width: '90px'
        },
        {
          text: this.$t('social.spaces.administration.binding.reports.table.title.removed.users'),
          value: 'removedUsers',
          align: 'center',
          sortable: false,
          class: 'px-1',
          width: '60px'
        },
        {
          text: this.$t('social.spaces.administration.binding.reports.table.title.File'),
          value: 'id',
          align: 'center',
          sortable: false,
          class: 'px-1',
          width: '60px'
        },
      ];
    },
  },
};
</script>