/*
 * This file is part of the Meeds project (https://meeds.io/).

 * Copyright (C) 2023 Meeds Association contact@meeds.io

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
<template>
  <input
    v-model="inputValueItem"
    :placeholder="itemResolvedName"
    type="text"
    class="input-block-level ignore-vuetify-classes my-3">
</template>
<script>
export default {
  name: 'AdvancedFilterInputItem',
  props: {
    settingsItem: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    inputValueItem: '',
  }),
  created() {
    this.$root.$on('reset-advanced-filter', () => this.inputValueItem = '');
    this.$root.$on('cancel-advanced-filter', () => this.inputValueItem = '');
  },
  computed: {
    itemResolvedName(){
      const item = this.settingsItem ;
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileSettings.property.name.${item.propertyName}`) !== `profileSettings.property.name.${item.propertyName}` ? this.$t(`profileSettings.property.name.${item.propertyName}`) : item.propertyName;
    },
  },
  watch: {
    inputValueItem() {
      this.$emit('input-value-changed', {'itemId': this.settingsItem.id, 'valueTosearch': this.inputValueItem});
    }
  },
};
</script>