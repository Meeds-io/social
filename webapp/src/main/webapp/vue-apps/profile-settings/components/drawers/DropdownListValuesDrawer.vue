<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
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
  <exo-drawer
    id="dropdownListDrawer"
    ref="dropdownListDrawer"
    :right="!$vuetify.rtl"
    @closed="$emit('closed')">
    <template slot="title">
      <div class="d-flex my-auto text-header font-weight-bold text-color">
        <v-btn
          icon
          :aria-label="$t('profileSettings.dropdownList.close.ariaLabel')"
          @click="close">
          <v-icon size="20">
            fas fa-arrow-left
          </v-icon>
        </v-btn>
        <span class="ms-2 my-auto">
          {{ $t('profileSettings.label.dropdownList') }}
        </span>
      </div>
    </template>
    <template slot="content">
      <div class="pa-5">
        test
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data() {
    return {
      propertyOptions: null
    };
  },
  methods: {
    open() {
      this.$refs.dropdownListDrawer.open();
    },
    close() {
      this.$refs.dropdownListDrawer.close();
    }
  }
};
</script>
