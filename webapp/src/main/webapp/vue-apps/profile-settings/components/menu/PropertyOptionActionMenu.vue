<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->

<template>
  <v-menu
    v-model="menu"
    transition="slide-x-reverse-transition"
    content-class="py-0 pa-0 overflow-hidden"
    min-width="169"
    offset-y
    left>
    <template #activator="{ on, attrs }">
      <v-btn
        v-bind="attrs"
        v-on="on"
        :aria-label="$t('profileSettings.dropdownList.actions.ariaLabel')"
        icon>
        <v-icon
          class="icon-default-color"
          size="20">
          fas fa-ellipsis-v
        </v-icon>
      </v-btn>
    </template>
    <v-list class="py-0 text-no-wrap">
      <v-list-item
        class="profile-property-option-menu-item px-2"
        @click="editPropertyOption">
        <div class="d-flex option-menu-icon">
          <v-icon
            size="12"
            class="clickable ma-auto icon-default-color">
            fas fa-edit
          </v-icon>
        </div>
        <span class="text-color">
          {{ $t('profileSettings.dropdownList.value.edit.label') }}
        </span>
      </v-list-item>
      <v-list-item
        class="profile-property-option-menu-item px-2"
        @click="deletePropertyOption">
        <div class="d-flex option-menu-icon">
          <v-icon
            size="12"
            class="clickable ma-auto error-color">
            fas fa-trash-alt
          </v-icon>
        </div>
        <span class="error-color">
          {{ $t('profileSettings.dropdownList.value.delete.label') }}
        </span>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script>
export default {
  data() {
    return {
      menu: false,
      waitTime: 200
    };
  },
  created() {
    document.addEventListener('mousedown', this.closeMenu);
  },
  methods: {
    closeMenu() {
      if (!this.menu) {
        return;
      }
      setTimeout(() => this.menu = false, this.waitTime);
    },
    editPropertyOption() {
      //
    },
    deletePropertyOption() {
      //
    }
  }
};
</script>
