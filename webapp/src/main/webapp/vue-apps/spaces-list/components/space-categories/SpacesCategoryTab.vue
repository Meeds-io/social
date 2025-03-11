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
  <v-menu
    v-if="category.categories?.length"
    :key="`${category.id}_menu`"
    ref="menu"
    v-model="menu"
    bottom
    close-delay="500"
    close-on-click
    offset-y
    open-on-hover>
    <template #activator="{on, attrs}">
      <v-tab
        v-bind="attrs"
        :value="category.id"
        v-on="$attrs?.click && {
          ...on,
          click: () => $attrs.click(category),
        } || on">
        <v-card
          class="text-truncate"
          color="transparent"
          flat
          :max-width="maxWidth"
          :title="category.name">
          {{ category.name }}
        </v-card>
        <v-icon
          class="ms-2"
          right
          size="16"
          @click.stop.prevent="menu = true">
          fa-chevron-down
        </v-icon>
      </v-tab>
    </template>
    <v-list
      class="pa-0"
      dense>
      <v-list-item
        v-for="subItem in category.categories"
        :key="subItem.id"
        :color="$root.selectedCategoryId === subItem.id && 'var(--allPagesTertiaryColor) !important'"
        dense
        v-on="$attrs?.click && {
          click: () => {
            $attrs.click(subItem);
            closeMenu();
          },
        }">
        <v-card
          class="text-truncate"
          color="transparent"
          flat
          :max-width="maxWidth"
          :title="subItem.name">
          {{ subItem.name }}
        </v-card>
      </v-list-item>
    </v-list>
  </v-menu>
  <v-tab
    v-else
    :key="category.id"
    :value="category.id"
    v-on="$attrs?.click && {
      click: () => $attrs.click(category),
    }">
    <v-card
      class="text-truncate"
      color="transparent"
      flat
      :max-width="maxWidth"
      :title="category.name">
      {{ category.name }}
    </v-card>
  </v-tab>
</template>
<script>
  export default {
    props: {
      selectedCategory: {
        type: Object,
        default: null,
      },
      category: {
        type: Object,
        default: null,
      },
      maxWidth: {
        type: Number,
        default: () => 150,
      },
    },
    data: () => ({
      menu: false,
    }),
    created () {
      document.addEventListener('click', this.closeMenuImmediatly);
    },
    beforeUnmount () {
      document.removeEventListener('click', this.closeMenuImmediatly);
    },
    methods: {
      closeMenuImmediatly () {
        this.menu = false;
      },
      closeMenu () {
        window.setTimeout(() => this.menu = false, 50);
      },
    },
  };
</script>