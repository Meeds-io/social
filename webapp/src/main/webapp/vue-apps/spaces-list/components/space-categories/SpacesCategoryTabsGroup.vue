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
  <div v-if="categoriesSize" class="my-4 mx-5 position-relative">
    <v-tabs
      v-model="selectedIndex"
      class="position-relative z-index-one"
      color="text-color"
      height="32">
      <v-tab
        :value="0"
        @click="$emit('select', selectedCategory)">
        {{ $t('spacesList.categories.all') }}
      </v-tab>
      <template v-for="item in categories">
        <v-menu
          v-if="item.categories?.length"
          :key="item.id"
          close-delay="500"
          open-on-hover
          offset-y
          bottom>
          <template #activator="{on, attrs}">
            <v-tab
              v-bind="attrs"
              v-on="on"
              :value="item.id"
              @click="$emit('select', item)">
              {{ item.name }}
              <v-icon
                class="ms-2"
                size="16"
                right>
                fa-chevron-down
              </v-icon>
            </v-tab>
          </template>
          <v-list class="pa-0" dense>
            <v-list-item
              v-for="subItem in item.categories"
              :key="subItem.id"
              :color="selectedCategoryId === subItem.id && 'var(--allPagesTertiaryColor) !important'"
              dense
              @click="$emit('select', subItem)">
              {{ subItem.name }}
            </v-list-item>
          </v-list>
        </v-menu>
        <v-tab
          v-else
          :key="item.id"
          :value="item.id"
          @click="$emit('select', item)">
          {{ item.name }}
        </v-tab>
      </template>
    </v-tabs>
    <v-divider class="full-width position-absolute b-0" />
  </div>
</template>
<script>
export default {
  props: {
    selectedCategory: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    selectedIndex: 0,
  }),
  computed: {
    categories() {
      return this.selectedCategory?.categories || [];
    },
    categoriesSize() {
      return this.categories?.length || 0;
    },
    selectedCategoryId() {
      return this.$root.selectedCategoryId;
    },
  },
  watch: {
    selectedCategoryId() {
      const index = this.categories.findIndex(cat => cat.id === this.selectedCategoryId || cat?.categories?.find(subCat => subCat.id === this.selectedCategoryId)) + 1;
      if (index > 0) {
        this.selectedIndex = index;
      } else {
        this.selectedIndex = 0;
      }
    },
  },
};
</script>