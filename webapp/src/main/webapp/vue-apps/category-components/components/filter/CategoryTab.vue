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
    ref="menu"
    v-model="menu"
    :key="`${category.id}_menu`"
    close-delay="500"
    open-on-hover
    close-on-click
    offset-y
    bottom>
    <template #activator="{on, attrs}">
      <v-tab
        v-bind="attrs"
        v-on="$listeners?.click && {
          ...on,
          click: () => $listeners.click(category),
        } || on"
        :value="category.id">
        <v-card
          :title="category.name"
          :max-width="maxWidth"
          color="transparent"
          class="text-truncate"
          flat>
          {{ category.name }}
        </v-card>
        <span class="d-flex align-center" aria-hidden="true">
          <v-icon
            class="ms-3 mr-0"
            size="16"
            right>
            fa-chevron-down
          </v-icon>
        </span>
      </v-tab>
    </template>
    <v-list class="pa-0" dense>
      <v-list-item
        v-for="subItem in category.categories"
        v-on="$listeners?.click && {
          click: () => {
            $listeners.click(subItem);
            closeMenu();
          },
        }"
        :key="subItem.id"
        :color="selectedId === subItem.id && 'var(--allPagesTertiaryColor) !important'"
        dense>
        <v-card
          :title="subItem.name"
          :max-width="maxWidth"
          color="transparent"
          class="text-truncate"
          flat>
          {{ subItem.name }}
        </v-card>
      </v-list-item>
    </v-list>
  </v-menu>
  <v-tab
    v-else
    v-on="$listeners?.click && {
      click: () => $listeners.click(category),
    }"
    :key="category.id"
    :value="category.id">
    <v-card
      :title="category.name"
      :max-width="maxWidth"
      color="transparent"
      class="text-truncate"
      flat>
      {{ category.name }}
    </v-card>
  </v-tab>
</template>
<script>
export default {
  props: {
    selectedId: {
      type: Number,
      default: () => 0,
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
  created() {
    document.addEventListener('click', this.closeMenuImmediatly);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.closeMenuImmediatly);
  },
  methods: {
    closeMenuImmediatly() {
      this.menu = false;
    },
    closeMenu() {
      window.setTimeout(() => this.menu = false, 50);
    },
  },
};
</script>