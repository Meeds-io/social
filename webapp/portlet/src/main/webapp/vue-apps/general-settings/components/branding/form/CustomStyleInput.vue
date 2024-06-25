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
  <div>
    <v-btn class="btn" @click="open">
      <v-icon class="icon-default-color me-2" size="18">fa-edit</v-icon>
      {{ $t('generalSettings.editCustomStyle') }}
    </v-btn>
    <exo-drawer
      ref="drawer"
      allow-expand
      right>
      <template #title>
        {{ $t('generalSettings.editCustomStyle') }}
      </template>
      <template #content>
        <div class="full-height pa-5">
          <textarea
            v-model="customStyle"
            :aria-label="$t('generalSettings.customStyle')"
            class="full-width full-height"></textarea>
        </div>
      </template>
      <template #footer>
        <div class="d-flex justify-end">
          <v-btn
            :aria-label="$t('generalSettings.cancel')"
            :disabled="loading"
            class="btn cancel-button me-4"
            elevation="0"
            @click="close">
            {{ $t('generalSettings.cancel') }}
          </v-btn>
          <v-btn
            :aria-label="$t('generalSettings.apply')"
            color="primary"
            class="btn btn-primary"
            elevation="0"
            @click="apply">
            {{ $t('generalSettings.apply') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    customStyle: null,
  }),
  created() {
    this.customStyle = this.value;
  },
  methods: {
    open() {
      this.customStyle = this.value;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    apply() {
      this.$emit('input', this.customStyle);
      this.$refs.drawer.close();
    },
  },
};
</script>