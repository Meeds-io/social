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
  <div v-if="validProperty" class="text-no-wrap text-truncate">
    <span class="pe-1 profile-contact-information-label text-capitalize">
      {{ propertyResolvedName }}:
    </span>
    <profile-contact-information-property-value
      :value="childProperty.value"
      class="profile-contact-information-value" />
  </div>
</template>

<script>
export default {
  props: {
    childProperty: {
      type: Object,
      default: () => null,
    },
  },
  computed: {
    language() {
      return this.$root.language;
    },
    validProperty() {
      return this.childProperty?.propertyName && this.childProperty?.value;
    },
    propertyResolvedName() {
      if (!this.childProperty) {
        return;
      }
      const resolvedLabel = this.childProperty.labels?.find(v => v.language === this.language);
      if (resolvedLabel) {
        return resolvedLabel.label;
      } else {
        return this.$t(`profileContactInformation.${this.childProperty.propertyName}`) !== `profileContactInformation.${this.childProperty.propertyName}`
          ? this.$t(`profileContactInformation.${this.childProperty.propertyName}`)
          : this.childProperty.propertyName;
      }
    },
  },
};
</script>