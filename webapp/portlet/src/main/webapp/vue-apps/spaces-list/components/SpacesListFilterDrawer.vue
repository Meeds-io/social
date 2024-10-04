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
  <exo-drawer
    id="SpacesListFilterDrawer"
    ref="drawer"
    v-model="drawer"
    allow-expand
    right>
    <template #title>
      {{ $t('spaceList.advanced.filter.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <div class="d-flex flex-column">
        <div class="text-header mx-4 mt-4">
          {{ $t('spaceList.advanced.filter.spaceType.label') }}
        </div>
        <v-radio-group
          v-model="filter"
          class="mt-2 ms-3"
          mandatory
          inset>
          <v-radio
            v-for="spaceFilter in spaceFilters"
            :key="spaceFilter.value"
            :value="spaceFilter.value"
            class="mt-0">
            <template #label>
              <div class="text-body">
                {{ spaceFilter.text }}
              </div>
            </template>
          </v-radio>
        </v-radio-group>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-btn
          class="btn ms-auto me-2"
          @click="close">
          {{ $t('spacesList.label.cancel') }}
        </v-btn>
        <v-btn
          color="primary"
          elevation="0"
          @click="apply">
          {{ $t('spacesList.label.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    filter: null,
  }),
  computed: {
    spaceFilters() {
      return [{
        text: this.$t('spacesList.filter.all'),
        value: 'all',
      },{
        text: this.$t('spacesList.filter.userSpaces'),
        value: 'member',
      },{
        text: this.$t('spacesList.filter.managingSpaces'),
        value: 'manager',
      },{
        text: this.$t('spacesList.filter.favoriteSpaces'),
        value: 'favorite',
      }];
    },
  },
  created() {
    this.$root.$on('spaces-list-filter-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('spaces-list-filter-open', this.open);
  },
  methods: {
    open(filter) {
      this.filter = filter;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    apply() {
      this.$root.$emit('spaces-list-filter-update', this.filter);
      this.close();
    },
  },
};
</script>