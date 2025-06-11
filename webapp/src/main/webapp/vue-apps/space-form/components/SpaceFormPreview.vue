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
    height="227px"
    width="380px"
    class="d-flex flex-column"
    flat>
    <v-list class="pa-0 transparent">
      <v-list-item class="pa-0">
        <img
          v-if="previewAvatar"
          :src="previewAvatar"
          width="50px"
          height="50px"
          class="border-radius clickable">
        <v-avatar
          v-else
          class="clickable"
          color="primary"
          width="50px"
          height="50px"
          rounded>
          <span class="white--text text-h5">{{ nameInitials }}</span>
        </v-avatar>
        <v-list-item-content class="pb-0 pt-0">
          <span
            class="px-4 font-weight-bold text-truncate">
            {{ space.displayName }}
          </span>
        </v-list-item-content>
      </v-list-item>
    </v-list>
    <div
      v-sanitized-html="space.description || ''"
      class="py-5 text-truncate-3"></div>
    <div class="d-flex flex-row mt-auto align-center">
      <div class="text-subtitle"> {{ $t('spacesList.label.oneMember') }} </div>
      <v-spacer />
      <v-chip
        v-if="!closedSpace"
        color="primary"
        height="24"
        widht="78"
        small
        label
        outlined>
        <span class="primary--text font-weight-bold"> {{ spaceAccess }} </span>
      </v-chip>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
    previewAvatar: {
      type: String,
      default: null,
    }
  },

  computed: {
    nameInitials() {
      if (this.space.displayName) {
        return this.space.displayName.split(' ').filter(n => n?.length).map(n => n.at(0).toUpperCase()).slice(0, 2).join('');
      } else {
        return '';
      }
    },
    closedSpace() {
      return this.space.subscription === 'closed';
    },
    spaceAccess() {
      return this.space.subscription === 'open' ? this.$t('spacesList.button.join') : this.$t('spacesList.button.requestJoin');
    }
  }
};
</script>