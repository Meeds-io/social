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
  <div>
<select
      v-model="propertylabel.language"
      name="documentsFilter"
      class="selectLanguageFilter input-block-level ignore-vuetify-classes  pa-2"
      @change="changeSettingsFilter">
      <option
        v-for="item in langArray"
        :key="item.value"
        :value="item.value">
        {{ item.value.toUpperCase().split('_')[0] }}
      </option>
    </select>
    <input
      v-model="propertylabel.label"
      :placeholder="$t('profileSettings.placeholder.label')"
      type="text"
      class="ignore-vuetify-classes flex-grow-1 pa-3 ms-1"
      maxlength="2000"
      required
    >
      <v-icon
          size="16" class="removeIcon pa-1" @click="deleteLabel">
          fas fa-trash-alt
          
        </v-icon>
    
    </div>
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
