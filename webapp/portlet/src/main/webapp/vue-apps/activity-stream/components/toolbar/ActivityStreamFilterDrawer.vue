<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association
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
  <exo-drawer
    id="filterStreamDrawer"
    ref="filterStreamDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('activity.filter.title') }}
    </template>
    <template slot="content">
      <div class="pa-5">
        <v-radio-group
          v-model="filter"
          mandatory>
          <v-radio
            v-for="streamFilter in streamFilters"
            :key="streamFilter.value"
            :label="streamFilter.text"
            :value="streamFilter.value" />
        </v-radio-group>
      </div>
    </template>
    <template slot="footer">
      <div class="VuetifyApp flex d-flex">
        <v-btn
          class="dark-grey-color px-1 hidden-xs-only"
          text
          @click="resetFilter()">
          <template>
            <i class="fas fa-redo me-3"></i>
            {{ $t('activity.filter.resetFilter') }}
          </template>
        </v-btn>
        <v-spacer />
        <div class="d-btn">
          <v-btn
            class="btn me-2"
            @click="cancel">
            <template>
              {{ $t('activity.filter.button.cancel') }}
            </template>
          </v-btn>
          <v-btn
            class="btn btn-primary"
            @click="applyFilter">
            <template>
              {{ $t('activity.filter.button.apply') }}
            </template>
          </v-btn>
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    filterToChange: null,
    spaceId: eXo.env.portal.spaceId,
    filter: 'all_stream',
  }),
  computed: {
    streamFilters() {
      return [{
        text: this.spaceId && this.$t('activity.filter.anyActivity') || this.$t('activity.filter.all'),
        value: 'all_stream',
      },{
        text: this.$t('activity.filter.unreadSpacesStream'),
        value: 'unread_spaces_stream',
      },{
        text: this.$t('activity.filter.pinnedActivities'),
        value: 'pin_stream',
        enable: !this.spaceId,
      },{
        text: this.$t('activity.filter.myActivities'),
        value: 'user_stream',
      },{
        text: this.$t('activity.filter.favoriteActivities'),
        value: 'user_favorite_stream',
      },{
        text: this.$t('activity.filter.manageSpaces'),
        value: 'manage_spaces_stream',
        enable: !this.spaceId,
      },{
        text: this.$t('activity.filter.favoriteSpaces'),
        value: 'favorite_spaces_stream',
        enable: !this.spaceId,
      }].filter(filter => filter.enable == null || filter.enable === true);
    },
  },
  created() {
    this.$root.$on('activity-stream-reset-filter', this.resetFilter);
    this.filter = this.$activityUtils.getStreamFilter();
  },
  beforeDestroy() {
    this.$root.$off('activity-stream-reset-filter', this.resetFilter);
  },
  methods: {
    applyFilter() {
      this.$root.$emit('close-alert-message');
      document.dispatchEvent(new CustomEvent('activity-stream-type-filter-applied', {detail: this.filter}));
      this.$activityUtils.setStreamFilter(this.filter);
      this.$refs.filterStreamDrawer.close();
    },
    open() {
      this.$refs.filterStreamDrawer.open();
    },
    cancel() {
      this.filter = this.$activityUtils.getStreamFilter();
      if (this.$refs.filterStreamDrawer) {
        this.$refs.filterStreamDrawer.close();
      }
    },
    resetFilter(silent) {
      if (silent) {
        this.$activityUtils.setStreamFilter('all_stream');
      } else {
        this.filter = 'all_stream';
        this.applyFilter();
      }
    }
  },
};
</script>