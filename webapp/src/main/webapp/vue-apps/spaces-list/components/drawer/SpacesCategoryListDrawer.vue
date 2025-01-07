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
    ref="drawer"
    v-model="drawer"
    allow-expand
    right>
    <template #title>
      {{ $t('spacesList.categories.drawer.select.title') }}
    </template>
    <template v-if="drawer && categories" #content>
      <div class="d-flex flex-column ma-5">
        <div
          v-for="category in categories"
          :key="category.id">
          <spaces-category-chip
            :category="category"
            max-width="100%"
            class="mb-4"
            @select="select(category)" />
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    categories: null,
  }),
  created() {
    this.$root.$on('spaces-list-category-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('spaces-list-category-open', this.open);
  },
  methods: {
    open(categories) {
      this.categories = categories;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    select(category) {
      this.$root.$emit('spaces-list-select-category', category);
      this.close();
    },
  },
};
</script>