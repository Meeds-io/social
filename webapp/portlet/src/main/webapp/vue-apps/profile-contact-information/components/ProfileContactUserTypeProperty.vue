<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
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
  <div class="mx-4 mt-2">
    <label
      class="font-weight-bold text-color text-capitalize-first-letter">
      {{ getResolvedName(property) }}
    </label>
    <div class="d-flex">
      <exo-identity-suggester
        :ref="`suggester${property.propertyName}`"
        v-model="users"
        :labels="{
          searchPlaceholder: this.$t('profileSettings.search.placeholder'),
          placeholder: this.$t(`profileSettings.search.${property.propertyName}.label`),
          noDataLabel: this.$t('Search.noResults'),
        }"
        :ignore-items="listIgnoredItems"
        :disabled="!property.editable"
        include-users
        :search-options="{}"
        @input="addSelected" />
      <div class="ma-auto">
        <profile-hide-property-button
          :property="property" />
      </div>
    </div>
    <profile-user-type-property-values
      :user-type-properties="propertyObject?.children || propertyObject"
      :disabled="!propertyObject.editable"
      @remove-value="removeUser" />
  </div>
</template>

<script>
export default {
  data() {
    return {
      users: [],
      ignoredItems: [],
      propertyObject: null
    };
  },
  props: {
    property: {
      type: Object,
      default: null
    }
  },
  computed: {
    listIgnoredItems() {
      return this.ignoredItems;
    }
  },
  watch: {
    'property.children': function() {
      this.clonePropertyObject();
    }
  },
  created() {
    this.clonePropertyObject();
  },
  methods: {
    clonePropertyObject() {
      this.propertyObject = structuredClone(this.property);
    },
    removeUser(value) {
      if (this.propertyObject.multiValued) {
        const index = this.propertyObject?.children?.findIndex(property => property.value === value);
        if (index !== -1) {
          this.propertyObject.children.splice(index, 1);
          this.ignoredItems.splice(index, 1);
        }
      } else {
        this.propertyObject.value = null;
      }
      this.$emit('property-updated', this.propertyObject);
    },
    addSelected(user) {
      if (!user) {
        return;
      }
      if (this.propertyObject.multiValued) {
        const index = this.propertyObject?.children?.findIndex(property => property.value === user?.remoteId);
        if (index === -1) {
          this.propertyObject?.children.push({isNew: true, value: user.remoteId});
        }
      } else {
        this.propertyObject.value = user.remoteId;
      }
      this.ignoredItems.push(user.id);
      this.$nextTick(() => {
        this.users = null;
      });
      this.$emit('property-updated', this.propertyObject);
    },
    getResolvedName(property){
      const lang = eXo?.env.portal.language || 'en';
      const resolvedLabel = !property.labels ? null : property.labels.find(v => v.language === lang);
      if (resolvedLabel) {
        return resolvedLabel.label;
      }
      return this.$t && this.$te(`profileContactInformation.${property.propertyName}`)
                     && this.$t(`profileContactInformation.${property.propertyName}`)
                     || property.propertyName;
    }
  }
};
</script>
