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
  <v-list class="px-4 pt-0 pb-4" dense>
    <div class="text-header my-4">{{ $t('UITopBarFavoritesPortlet.types') }}</div>
    <v-list-item-group
      v-model="selectedGroupIndex"
      color="primary"
      dense>
      <favorite-type
        v-for="(group, index) in groups"
        :key="group.name"
        :group="group"
        :selected="index === selectedGroupIndex"
        :unread-only="index === unreadIndex"
        @select="selectType(index, group)" />
    </v-list-item-group>
  </v-list>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    loading: false,
    extensions: [],
    extensionApp: 'favorite',
    extensionType: 'favorite-type',
    selectedGroupIndex: 0,
  }),
  computed: {
    groups() {
      const groups = [];
      this.extensions
        .forEach(group => {
          groups.push({
            ...group,
            label: this.$te(`UITopBarFavoritesPortlet.types.${group.id}`)
              ? this.$t(`UITopBarFavoritesPortlet.types.${group.id}`)
              : group.name
          });
        });
      groups.splice(0, 0, {
        rank: -1,
        name: 'all',
        label: this.$t('UITopBarFavoritesPortlet.types.all'),
        icon: 'fa-star',
      });
      groups.sort((g1, g2) => (g1.rank || 100) - (g2.rank || 100));
      return groups;
    },
  },
  created() {
    document.addEventListener(`extension-${this.extensionApp}-${this.extensionType}-updated`, this.refreshExtensions);
    this.refreshExtensions();
  },
  beforeDestroy() {
    document.removeEventListener(`extension-${this.extensionApp}-${this.extensionType}-updated`, this.refreshExtensions);
  },
  methods: {
    selectType(index, group) {
      this.selectedGroupIndex = index;
      this.$emit('change', group?.id, group?.label);
    },
    refreshExtensions() {
      this.extensions = extensionRegistry.loadExtensions(this.extensionApp, this.extensionType);
    },
  },
};
</script>