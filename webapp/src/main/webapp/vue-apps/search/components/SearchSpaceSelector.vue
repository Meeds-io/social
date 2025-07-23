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
  <div class="spaceFilter d-flex flex-row">
    <v-menu
      v-if="initialized"
      v-model="menu"
      :close-on-content-click="false"
      attach
      offset-y>
      <template #activator="{ on, attrs }">
        <v-chip
          outlined
          tab-index="0"
          class="text-body text-header-color mx-1"
          v-bind="attrs"
          v-on="on"
          @keydown.enter="on.click">
          <v-icon size="16" class="pe-2">
            fas fa-layer-group
          </v-icon>
          <span class="me-2">{{ $t('search.space.filter.label') }}</span>
          <i class="fas fa-chevron-down"></i>
        </v-chip>
      </template>
      <v-card class="d-flex flex-column border-box-sizing pa-4">
        <v-radio-group v-model="choice" class="mt-0 pt-0 mb-1">
          <v-radio
            value="any"
            class="mx-0">
            <template #label>
              <span>{{ $t('search.space.filter.any.option.label') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="space"
            class="mx-0">
            <template #label>
              <span>{{ $t('search.space.filter.one.option.label') }}</span>
            </template>
          </v-radio>
        </v-radio-group>
        <exo-identity-suggester
          v-if="choice === 'space'"
          v-model="space"
          ref="spacesSuggester"
          :include-users="false"
          :labels="spaceSuggesterLabels"
          autofocus
          name="spacesSuggester"
          class="user-suggester mt-n2"
          include-spaces />
        <search-selected-space-item
          v-for="space in selectedSpaceItems"
          class="mb-2"
          :key="space.id"
          :space="space"
          @delete-selected-space="deleteSpace" />
      </v-card>
    </v-menu>
    <div v-if="this.selectedSpaces.length" class="selectedSpaces">
      <search-selected-space-item
        v-for="space in selectedSpaceItems"
        :key="space.id"
        :space="space"
        @delete-selected-space="deleteSpace" />
    </div>
  </div>
</template>
<script>

export default {
  data() {
    return {
      menu: false,
      textInput: '',
      choice: 'any',
      space: [],
      selectedSpaces: [],
      initialized: false
    };
  },
  computed: {
    spaceSuggesterLabels() {
      return {
        placeholder: this.$t('activity.composer.audience.placeholder'),
        noDataLabel: this.$t('activity.composer.audience.noDataLabel'),
      };
    },
    selectedSpaceItems() {
      return this.selectedSpaces.map(item => {
        item.avatarUrl = item?.profile?.avatarUrl || item.avatarUrl;
        return item;
      });
    },
  },
  watch: {
    space() {
      if (!this.space) {
        this.$nextTick(this.$refs.spacesSuggester.$refs.selectAutoComplete.deleteCurrentItem);
        return;
      }
      const found = this.selectedSpaces?.find(item => {
        return item.spaceId === this.space.spaceId;
      });
      if (!found) {
        this.selectedSpaces.push(this.space);
        document.dispatchEvent(new CustomEvent('search-by-space', {
          detail: this.space.spaceId
        }));
      }
      this.space = null;
    },
    selectedSpaces() {
      const selectedSpaceIds = this.selectedSpaces.map(item => item.spaceId);
      this.$root.$emit('spaces-changed', selectedSpaceIds);
    },
    menu() {
      if (this.menu && this.selectedSpaces.length) {
        this.choice = 'space';
      }
    }
  },
  async created() {
    const spaceId = eXo.env?.portal?.spaceId;
    const search = window.location.search?.substring(1);
    const parameters = new URLSearchParams(search);
    const spaceIds = parameters?.getAll('spaceId') || [];
    if (!spaceIds.length && spaceId) {
      spaceIds.push(spaceId);
    }
    try {
      await Promise.all(spaceIds.map(id => this.addSpaceIfMember(id)));
    } finally {
      this.initialized = true;
    }
  },
  methods: {
    async addSpaceIfMember(spaceId) {
      const space = await this.$spaceService.getSpaceById(spaceId);
      if (space?.isMember) {
        this.selectedSpaces.push({ ...space, spaceId: space.id });
      }
    },
    deleteSpace(space) {
      const index = this.selectedSpaces.findIndex( item => space.spaceId === item.spaceId);
      this.selectedSpaces.splice(index, 1);
    },
    closeMenu() {
      this.menu = false;
    },
  }
};
</script>
