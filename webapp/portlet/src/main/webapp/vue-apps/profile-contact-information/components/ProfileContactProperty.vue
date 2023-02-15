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
  <div :id="id">
    <v-flex class="d-flex">
      <div class="align-start text-no-wrap font-weight-bold me-3">
        {{ propertyResolvedName }}
      </div>
      <div class="align-end flex-grow-1 text-truncate text-end">
        <template v-if="multivalued">
          <profile-contact-information-property-child
            v-for="childProperty in property.children"
            :key="childProperty.propertyName"
            :child-property="childProperty" />
        </template>
        <profile-contact-information-property-value
          v-else
          :value="property.value" />
      </div>
    </v-flex>
    <v-divider v-if="!last" class="my-4" />
  </div>
</template>

<script>
export default {
  props: {
    property: {
      type: Object,
      default: () => null,
    },
    last: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    id() {
      return `profileContactInformation-${this.property.propertyName.replace(/\./g, '-')}`;
    },
    multivalued() {
      return this.property?.children?.length && this.property?.children?.some(e => e.value);
    },
    language() {
      return this.$root.language;
    },
    propertyResolvedName() {
      const resolvedLabel = this.property.labels?.find(v => v.language === this.language);
      if (resolvedLabel) {
        return resolvedLabel.label;
      } else {
        return this.$t(`profileContactInformation.${this.property.propertyName}`) !== `profileContactInformation.${this.property.propertyName}`
          ? this.$t(`profileContactInformation.${this.property.propertyName}`)
          : this.property.propertyName;
      }
    },
  },
};
</script>