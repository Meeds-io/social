<!--
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
 -->

<template>
  <v-flex
    class="d-flex">
    <div
      class="align-start text-no-wrap font-weight-bold me-3 ma-auto">
      {{ getResolvedName(property) }}
      <profile-hidden-property-info
        :property="property"
        :hover="hover" />
    </div>
    <div
      class="align-end flex-grow-1 text-truncate text-end">
      <div
        v-for="(childProperty, i) in property.children"
        :key="i"
        :title="childProperty.value"
        class="text-no-wrap text-truncate">
        <v-hover v-slot="{hover}">
          <div
            v-if="canShowChild(childProperty)"
            :class="childProperty.hidden && 'opacity-5'">
            <div
              v-if="childProperty.hidden"
              class="d-inline-block">
              <span
                v-if="hover && !isMobile"
                class="me-2 text-caption">
                {{ $t('profileContactInformation.property.hidden.label') }}
              </span>
              <v-icon
                class="icon-default-color me-2 text-subtitle-2">
                fas fa-eye-slash
              </v-icon>
            </div>
            <span
              v-if="childProperty.propertyName"
              class="pe-1 font-weight-regular text-subtitle-2 text-capitalize">
              {{ getResolvedName(childProperty) }}:
            </span>
            <v-btn
              v-if="searchable"
              v-autolinker="childProperty.value"
              class="primary--text font-weight-regular text-subtitle-2 pa-0 ma-auto"
              min-width="auto"
              text
              @click="quickSearch(childProperty)" />
            <span
              v-else
              v-autolinker="childProperty.value"></span>
          </div>
        </v-hover>
      </div>
    </div>
  </v-flex>
</template>

<script>
export default {
  props: {
    property: {
      type: Object,
      default: () => null,
    },
    searchable: {
      type: Boolean,
      default: false,
    },
    hover: {
      type: Boolean,
      default: false,
    },
    owner: {
      type: Boolean,
      default: false,
    },
    isAdmin: {
      type: Boolean,
      default: false,
    },
    isMobile: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    quickSearch(childProperty) {
      this.$emit('quick-search', this.property, childProperty);
    },
    getResolvedName(item) {
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileContactInformation.${item.propertyName}`)!==`profileContactInformation.${item.propertyName}`?this.$t(`profileContactInformation.${item.propertyName}`):item.propertyName;
    },
    canShowHiddenChildProperty(property) {
      return !property.hidden || (property.hidden && (this.isAdmin || this.owner));
    },
    canShowChild(childProperty) {
      return (childProperty.value && childProperty.visible && childProperty.active && this.canShowHiddenChildProperty(childProperty))
               || (this.property.multiValued && this.property.active && this.property.visible && childProperty.value);
    }
  },
};
</script>
