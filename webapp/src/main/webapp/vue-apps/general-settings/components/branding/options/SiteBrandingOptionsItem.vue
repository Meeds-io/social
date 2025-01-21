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
  <div class="option-item mt-2">
    <v-list-item
      dense
      class="pa-0">
      <v-list-item-content>
        <v-list-item-title>
          {{ $t(`generalSettings.${option?.name}.option.label`) }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="d-flex flex-row my-auto">
        <v-btn
          icon
          @click="emitOptionEvent">
          <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
  </div>
</template>
<script>
export default {
  props: {
    option: {
      type: Object,
      required: true,
    },
  },
  methods: {
    emitOptionEvent() {
      this.$root.$emit(this.option?.event);
    }
  }
};
</script>
