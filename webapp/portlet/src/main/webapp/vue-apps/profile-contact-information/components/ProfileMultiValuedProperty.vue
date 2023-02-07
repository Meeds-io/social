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
  <div v-if="property && property.visible && hasValues">
    <v-flex class="d-flex">
      <div class="align-start text-no-wrap font-weight-bold me-3">
        {{ getResolvedName(property)}}
      </div>
      <div class="align-end flex-grow-1 text-truncate text-end">
        <div
          v-for="(childProperty, i) in property.children"
          :key="i"
          :title="childProperty.value"
          class="text-no-wrap text-truncate">
          <span v-if="childProperty.propertyName" class="pe-1 text-capitalize">
            {{ getResolvedName(childProperty)}}:
          </span>
          <span v-autolinker="childProperty.value"></span>
        </div>
      </div>
    </v-flex>
    <v-divider class="my-4" />
  </div>
</template>

<script>
export default {
  props: {
    property: {
      type: Object,
      default: () => null,
    },
  },
  computed: {
    hasValues(){
      if (this.property && this.property.children && this.property.children.length ){
        if (this.property.children.some(e => e.value)) {
          return true;
        } 
      }
      return false;
    }
  },
  methods: {
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileContactInformation.${item.propertyName}`)!==`profileContactInformation.${item.propertyName}`?this.$t(`profileContactInformation.${item.propertyName}`):item.propertyName;
    }
  },
};
</script>