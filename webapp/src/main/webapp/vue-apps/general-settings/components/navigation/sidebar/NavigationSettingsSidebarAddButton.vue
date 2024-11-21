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
    v-model="menu"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    content-class="application-menu z-index-modal"
    offset-y>
    <template #activator="{attrs, on}">
      <v-btn
        v-bind="attrs"
        v-on="on"
        :title="$t('generalSettings.addSideBarItem')"
        class="btn btn-primary border-radius"
        elevation="0"
        tile
        icon>
        <v-icon color="white" size="18">
          fa-plus
        </v-icon>
      </v-btn>
    </template>
    <v-list max-width="300" dense>
      <v-list-item
        link
        dense
        @click="$root.$emit('sidebar-item-add-site', settings)">
        <v-list-item-icon class="me-3">
          <v-icon size="18">fa-sitemap</v-icon>
        </v-list-item-icon>
        <v-list-item-content class="d-inline">
          <v-list-item-title>{{ $t('generalSettings.addSideBarItemSite') }}</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
      <v-list-item
        link
        dense
        @click="$root.$emit('sidebar-item-add-spaces', settings)">
        <v-list-item-icon class="me-3">
          <v-icon size="18">fa-layer-group</v-icon>
        </v-list-item-icon>
        <v-list-item-content class="d-inline">
          <v-list-item-title>{{ $t('generalSettings.addSideBarItemSpaces') }}</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
      <v-list-item
        link
        dense
        @click="$root.$emit('sidebar-item-add-link', settings)">
        <v-list-item-icon class="me-3">
          <v-icon size="18">fa-link</v-icon>
        </v-list-item-icon>
        <v-list-item-content class="d-inline">
          <v-list-item-title>{{ $t('generalSettings.addSideBarItemLink') }}</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
      <v-list-item
        link
        dense
        @click="$root.$emit('sidebar-item-add-separator', settings)">
        <v-list-item-icon class="me-3">
          <v-icon size="18">fa-grip-lines</v-icon>
        </v-list-item-icon>
        <v-list-item-content class="d-inline">
          <v-list-item-title>{{ $t('generalSettings.addSideBarItemSeparator') }}</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-menu>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    menu: false,
  }),
  watch: {
    menu() {
      // Workaround to fix closing menu when clicking outside
      if (this.menu) {
        document.addEventListener('mousedown', this.closeMenu);
      } else {
        document.removeEventListener('mousedown', this.closeMenu);
      }
    },
  },
  methods: {
    closeMenu(event) {
      if (event?.target) {
        window.setTimeout(() => {
          this.menu = false;
        }, 200);
      } else {
        this.menu = false;
      }
    },
  },
};
</script>