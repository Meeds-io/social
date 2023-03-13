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
    <div v-for="propertylabel in propertylabels" :key="propertylabel.id">
      <property-label
        :propertylabel="propertylabel"
        :languages="filtredLanguages"
        @delete-label="deleteLabel" />
    </div>
    <div class="d-flex pt-4">
      <v-spacer />
      <div @click="addNewLabel" class="addLabelBtn primary--text">
        <v-icon color="primary">
          mdi-plus
        </v-icon>
        {{ $t('profileSettings.button.addNew') }}
      </div>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    propertylabels: {
      type: Object,
      default: null,
    },
    languages: {
      type: Object,
      default: null
    },
    labelsObjectType: {
      type: String,
      default: ''
    },
    id: {
      type: Number,
      default: 0
    },
  },

  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    filtredLanguages(){
      const result = this.propertylabels.map(a => a.language);
      return this.languages.filter(lang => !result.includes(lang.value));
    },
  },

  methods: {
    addNewLabel() {
      this.propertylabels.push( {language: eXo.env.portal.language, label: '', objectId: this.id, objectType: this.labelsObjectType});
    },
    deleteLabel(propertylabel) {
      this.propertylabels.splice(this.propertylabels.findIndex(v => v.language === propertylabel.language), 1);
    },
  }
};
</script>
