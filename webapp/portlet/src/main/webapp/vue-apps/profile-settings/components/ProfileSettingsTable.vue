/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
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
 */

<template>
  <v-data-table
    ref="dataTable"
    :headers="headers"
    :items="settings"
    :items-per-page="pageSize"
    :loading="loading"
    :locale="lang"
    :sort-by.sync="sortBy"
    hide-default-footer
    disable-pagination
    disable-filtering
    class="settings-table data-table-light-border py-6 transparent">
    <template slot="item.propertyName" slot-scope="{ item }">
      {{ getResolvedName(item) }}
    </template>
    <template slot="item.actions" slot-scope="{ item }">
      <profile-settings-actions-cell :setting="item" :settings="settings" />
    </template>
    <template #no-data>{{ $t("profileSettings.noSettings") }}</template>
    <template v-if="hasMore" slot="footer">
      <v-flex class="d-flex py-2 border-box-sizing mb-1">
        <v-btn
          :loading="loading"
          :disabled="loading"
          class="white mx-auto no-border primary--text no-box-shadow"
          @click="$root.$emit('document-load-more')">
          {{ $t('profileSettings.loadMore') }}
        </v-btn>
      </v-flex>
    </template>
  </v-data-table>
</template>

<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null
    },
    pageSize: {
      type: Number,
      default: 20
    },
    offset: {
      type: Number,
      default: 20
    },
    limit: {
      type: Number,
      default: 20
    },
    loading: {
      type: Boolean,
      default: false
    },
    hasMore: {
      type: Boolean,
      default: false,
    },
    sortBy: {
      type: String,
      default: 'order',
    }
  },
  data: () => ({
    waitTimeUntilCloseMenu: 200,
    lang: eXo.env.portal.language,
  }),
  computed: {
    headers() {
      return [{
        text: this.$t && this.$t('profileSettings.propertyName'),
        value: 'propertyName',
        sortable: false,
      }, {
        text: this.$t && this.$t('profileSettings.actions'),
        value: 'actions',
        align: 'center',
        width: '60px',
        class: 'actions-column',
        sortable: false,
      }];
    },
  },
  methods: {
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileSettings.property.name.${item.propertyName}`)!==`profileSettings.property.name.${item.propertyName}`?this.$t(`profileSettings.property.name.${item.propertyName}`):item.propertyName;
    }
  }
};
</script>