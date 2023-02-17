/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
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
 */


<template>
  <v-row class="mx-0 mb-0 mt-4 max-width-fit" no-gutters>
    <v-col cols="3" class="px-2">
      <div class="flex-grow-1 text-truncate">
        <select
          v-model="propertylabel.language"
          name="documentsFilter"
          class="max-width-fit ignore-vuetify-classes my-0"
          @change="changeSettingsFilter">
          <option
            v-for="item in langArray"
            :key="item.value"
            :value="item.value">
            {{ item.value.toUpperCase().split('_')[0] }}
          </option>
        </select>
      </div>
    </v-col>
    <v-col cols="9">
      <div class="d-flex max-width-fit">
        <input
          v-model="propertylabel.label"
          :placeholder="$t('profileSettings.placeholder.label')"
          type="text"
          class="ignore-vuetify-classes flex-grow-1 pa-3 ms-1"
          maxlength="2000"
          required>

        <div class="flex-grow-0">
          <v-btn icon @click="deleteLabel">
            <v-icon size="16" class="error-color">fas fa-trash-alt</v-icon>
          </v-btn>
        </div>
      </div>
    </v-col>
  </v-row>
</template>
<script>
export default {

  props: {
    propertylabel: {
      type: Object,
      default: null,
    },
    languages: {
      type: Object,
      default: null,
    }
  },
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    langArray() {
      const langArray = Object.assign([], this.languages);
      if (this.propertylabel.language){
        langArray.unshift({value: this.propertylabel.language});
      }
      return langArray;
    }
  },

  methods: {
    deleteLabel() {
      this.$emit('delete-label', this.propertylabel);
    },
  }
};
</script>
