<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <div class="d-flex flex-column">
    <v-list-item
      class="pa-0"
      dense>
      <v-list-item-icon class="me-2 my-auto">
        <v-icon size="18">{{ spaceTemplateIcon }}</v-icon>
      </v-list-item-icon>
      <v-list-item-content class="me-2 pa-0 text-truncate">
        <v-list-item-title class="text-truncate">
          {{ spaceTemplateName }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="mx-0 my-auto flex-row">
        <div class="position-relative d-flex flex-column justify-center">
          <v-card
            v-if="subspacesMaxLimit === 0"
            class="d-flex flex-row align-center justify-center"
            flat>
            <v-btn
              :title="$t('spaceTemplate.subspacesConfigurationMinLimit')"
              icon>
              <v-icon class="icon-default-color">fa-minus fa-sm</v-icon>
            </v-btn>
            <v-card-text class="pa-0">{{ $t('spaceTemplate.subspacesConfigurationStepNoLimit') }}</v-card-text>
            <v-btn
              :title="$t('spaceTemplate.subspacesConfigurationMaxLimit')"
              icon
              @click="subspacesMaxLimit++">
              <v-icon class="icon-default-color">fa-plus fa-sm</v-icon>
            </v-btn>
          </v-card>
          <number-input
            v-else
            v-model="subspacesMaxLimit"
            :label="$t('spaceTemplate.subspacesConfigurationStepMaxLimit')"
            :plus-title="$t('spaceTemplate.subspacesConfigurationMaxLimit')"
            :minus-title="$t('spaceTemplate.subspacesConfigurationMinLimit')"
            :step="1"
            :min="0"
            :max="100"
            class="ms-auto"
            editable
            @valid="invalidSubspacesMaxLimit = !$event" />
        </div>
        <v-btn
          class="ms-1 my-auto"
          icon
          @click="$emit('remove-item', spaceTemplate.id)">
          <v-icon size="18" color="error">fa-trash</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <span v-if="invalidSubspacesMaxLimit" class="error-color">{{ $t('spaceTemplate.subspacesConfigurationStepMaxLimitWarning') }} {{ globalLimit }}</span>
  </div>
</template>
<script>

export default {
  props: {
    spaceTemplate: {
      type: Object,
      default: () => null,
    },
    globalLimit: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    subspacesMaxLimit: 0,
    invalidSubspacesMaxLimit: false,
  }),
  computed: {
    spaceTemplateId() {
      return this.spaceTemplate?.id;
    },
    spaceTemplateIcon() {
      return this.spaceTemplate?.icon;
    },
    spaceTemplateName() {
      return this.spaceTemplate?.name;
    }
  },
  watch: {
    subspacesMaxLimit() {
      this.$set(this.spaceTemplate, 'subspacesMaxLimit', this.subspacesMaxLimit);
      this.invalidSubspacesMaxLimit = this.globalLimit > 0 && (this.subspacesMaxLimit > this.globalLimit || this.subspacesMaxLimit === 0);
    },
    invalidSubspacesMaxLimit() {
      if (this.invalidSubspacesMaxLimit) {
        this.$set(this.spaceTemplate, 'subspacesMaxLimit', this.globalLimit + 1 );
      }
    },
    globalLimit() {
      this.invalidSubspacesMaxLimit = this.globalLimit > 0 && (this.subspacesMaxLimit > this.globalLimit || this.subspacesMaxLimit === 0);
    },
  },
  created() {
    if (this.spaceTemplate?.subspacesMaxLimit) {
      this.subspacesMaxLimit = this.spaceTemplate?.subspacesMaxLimit;
    } else {
      this.subspacesMaxLimit = this.globalLimit;
      this.$set(this.spaceTemplate, 'subspacesMaxLimit', this.globalLimit);
    }
  }
};
</script>