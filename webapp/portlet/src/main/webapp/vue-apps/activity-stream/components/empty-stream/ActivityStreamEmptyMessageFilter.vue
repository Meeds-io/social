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
  <div class="d-flex flex-column white card-border-radius">
    <v-flex class="d-flex my-auto border-box-sizing">
      <div class="d-flex flex-column ma-auto py-10 text-center text-sub-title">
        <v-icon
          size="70"
          color="primary"
          class="mx-auto mt-8 fa"
          :class="emptyStreamIcon" />
        <span class="text-sub-title my-7">{{ emptyStreamLabel }}</span>
        <div v-if="streamFilter !== 'all_stream'">
          <v-btn
            class="primary"
            outlined
            border
            @click="$root.$emit('activity-stream-reset-filter', false)">
            {{ $t('activity.filter.button.resetFilter') }}
          </v-btn>
        </div>
      </div>
    </v-flex>
  </div>
</template>
<script>
export default {
  props: {
    streamFilter: {
      type: String,
      default: null,
    },
  },
  computed: {
    emptyStreamLabel() {
      return this.$t(`activity.filter.empty_${this.streamFilter}.msg`);
    },
    emptyStreamIcon() { 
      switch (this.streamFilter) {
      case 'favorite_spaces_stream':
        return 'fa-star-half-alt';
      case 'unread_spaces_stream':
        return 'fa-envelope-open';
      case 'manage_spaces_stream':
        return 'fa-user-cog';
      case 'pin_stream':
        return 'fa-thumbtack';
      case 'user_stream':
        return 'fa-feather-alt';
      case 'user_favorite_stream':
        return 'fa-star-half-alt';
      default:
        return '';
      }
    },
  }
};
</script>