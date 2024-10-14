<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
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
  <v-dialog
    ref="dialog"
    v-model="modal"
    color="white"
    width="290px">
    <template #activator="{ on }">
      <v-list-item
        two-line
        class="px-0"
        dense>
        <v-list-item-action class="me-2 my-0">
          <v-card
            :color="value"
            height="50px"
            width="50px"
            v-on="on" />
        </v-list-item-action>
        <v-list-item-content class="d-flex flex-column align-start me-2">
          <template v-if="label">
            <v-list-item-title class="me-auto">
              {{ label }} {{ $t('generalSettings.color.label') }}
            </v-list-item-title>
            <v-spacer class="my-1" />
            <v-list-item-subtitle>
              {{ value }}
            </v-list-item-subtitle>
          </template>
          <v-list-item-title v-else>
            {{ value }}
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </template>
    <v-color-picker
      v-model="color"
      :swatches="swatches"
      mode="hexa"
      show-swatches />
    <v-row class="mx-0 white">
      <v-col class="center">
        <v-btn
          text
          color="primary"
          @click="cancel">
          {{ $t('generalSettings.cancel') }}
        </v-btn>
      </v-col>
      <v-col class="center">
        <v-btn
          text
          color="primary"
          @click="save">
          {{ $t('generalSettings.ok') }}
        </v-btn>
      </v-col>
    </v-row>
  </v-dialog>
</template>
<script>
export default {
  props: {
    label: {
      type: String,
      default: null,
    },
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    modal: false,
    color: null,
    originalValue: null,
    swatches: [
      ['#FF0000', '#319ab3', '#f97575'],
      ['#98cc81', '#4273c8', '#cea6ac'],
      ['#bc99e7', '#9ee4f5', '#774ea9'],
      ['#ffa500', '#bed67e', '#0E100F'],
      ['#ffaacc', '#0000AA', '#000055'],
    ],
  }),
  watch: {
    modal() {
      if (this.modal) {
        this.originalValue = this.value;
      }
    },
    value() {
      this.color = this.value;
    },
  },
  created() {
    this.color = this.value;
  },
  methods: {
    cancel() {
      this.$emit('input', this.originalValue);
      this.modal = false;
    },
    save() {
      this.$emit('input', this.color);
      this.modal = false;
    }
  }
};
</script>
