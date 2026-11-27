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
  <v-tooltip
    :disabled="!disabled"
    bottom>
    <template #activator="{on}">
      <div class="flex-grow-1">
        <!-- a filter the directory already applies: greyed, read-only,
             exposed as disabled, and still focusable for its tooltip; the
             tooltip listens on the input, as focus does not bubble, and on
             a greyed input only: VTooltip opens on focus even disabled -->
        <input
          v-model="inputValueItem"
          v-on="disabled ? on : {}"
          :placeholder="itemResolvedName"
          :aria-label="itemResolvedName"
          :aria-disabled="disabled ? 'true' : null"
          :aria-describedby="disabled ? disabledHintId : null"
          :readonly="disabled"
          :class="disabled && 'people-advanced-filter-input-disabled disabled-background'"
          type="text"
          class="input-block-level ignore-vuetify-classes my-3">
        <span
          v-if="disabled"
          :id="disabledHintId"
          class="d-sr-only">
          {{ $t('peopleList.label.attributeUsedByDirectory') }}
        </span>
      </div>
    </template>
    <span>{{ $t('peopleList.label.attributeUsedByDirectory') }}</span>
  </v-tooltip>
</template>
<script>
export default {
  name: 'AdvancedFilterInputItem',
  props: {
    settingsItem: {
      type: Object,
      default: null,
    },
    // the directory already filters on this attribute
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  // a value restored from a shared URL shows in its input
  data: vm => ({
    inputValueItem: vm.settingsItem?.valueToSearch || '',
  }),
  created() {
    this.$root.$on('reset-advanced-filter', () => this.inputValueItem = '');
    this.$root.$on('cancel-advanced-filter', () => this.inputValueItem = '');
  },
  computed: {
    disabledHintId() {
      return `${this.$root.appId || 'people'}AdvancedFilterUsed-${this.settingsItem?.id}`;
    },
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
    // the directory filters on this attribute since the settings were
    // saved: the viewer's value no longer applies
    disabled() {
      if (this.disabled) {
        this.inputValueItem = '';
      }
    },
    inputValueItem() {
      this.$emit('input-value-changed', {'itemId': this.settingsItem.id, 'valueTosearch': this.inputValueItem});
    }
  },
};
</script>