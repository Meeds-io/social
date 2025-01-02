<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
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
  <exo-drawer
    id="dropdownListDrawer"
    ref="dropdownListDrawer"
    :right="!$vuetify.rtl"
    @closed="$emit('closed')">
    <template slot="title">
      <div class="d-flex my-auto text-header font-weight-bold text-color">
        <v-btn
          icon
          :aria-label="$t('profileSettings.dropdownList.close.ariaLabel')"
          @click="close">
          <v-icon size="20">
            fas fa-arrow-left
          </v-icon>
        </v-btn>
        <span class="ms-2 my-auto">
          {{ $t('profileSettings.label.dropdownList') }}
        </span>
      </div>
    </template>
    <template slot="content">
      <div class="py-5 px-4">
        <div>
          <v-btn
            v-if="!enableWriteValue"
            class="btn btn-primary"
            @click="enableWriteValue = true">
            <v-icon
              size="14"
              class="me-2">
              fas fa-plus
            </v-icon>
            {{ $t('profileSettings.dropdownList.addValue.label') }}
          </v-btn>
          <div v-if="enableWriteValue">
            <v-text-field
              v-model="propertyValue"
              :placeholder="$t('profileSettings.dropdownList.input.placeholder')"
              class="pt-0"
              autofocus
              outlined
              dense
              @keydown.enter="addPropertyValue" />
            <div class="d-flex">
              <p class="caption mb-0 text-sub-title">
                {{ $t('profileSettings.dropdownList.input.info') }}
              </p>
              <div class="d-flex ms-auto">
                <v-tooltip
                  bottom>
                  <template #activator="{ on, attrs }">
                    <v-btn
                      v-bind="attrs"
                      v-on="on"
                      :disabled="!propertyValue?.length"
                      :aria-label="$t('profileSettings.dropdownList.value.validate.label')"
                      width="28"
                      min-width="28"
                      height="28"
                      class="success-color"
                      icon
                      @click="addPropertyValue">
                      <v-icon size="20">
                        fas fa-check
                      </v-icon>
                    </v-btn>
                  </template>
                  {{ $t('profileSettings.dropdownList.value.validate.label') }}
                </v-tooltip>
                <v-tooltip
                  bottom>
                  <template #activator="{ on, attrs }">
                    <v-btn
                      v-bind="attrs"
                      v-on="on"
                      :disabled="!enableWriteValue"
                      :aria-label="$t('profileSettings.dropdownList.value.cancel.label')"
                      width="28"
                      min-width="28"
                      height="28"
                      class="error-color"
                      icon
                      @click="resetValues">
                      <v-icon size="20">
                        fas fa-times
                      </v-icon>
                    </v-btn>
                  </template>
                  {{ $t('profileSettings.dropdownList.value.cancel.label') }}
                </v-tooltip>
              </div>
            </div>
          </div>
        </div>
        <v-container
          :class="!enableWriteValue && 'mt-6' || 'mt-1'"
          class="pt-0"
          no-gutters>
          <v-row class="text-sub-title border-bottom-color">
            <v-col
              cols="8"
              class="font-weight-bold py-4 px-0">
              {{ $t('profileSettings.dropdownList.name.label') }}
            </v-col>
            <v-col
              cols="4"
              class="text-end font-weight-bold py-4 px-0">
              {{ $t('profileSettings.dropdownList.actions.label') }}
            </v-col>
          </v-row>
          <v-row v-if="!propertyOptions.length">
            <v-col
              class="text-center pt-8"
              cols="12">
              <v-icon
                class="icon-default-color"
                size="60">
                far fa-address-card
              </v-icon>
              <p class="mt-4">
                {{ $t('profileSettings.dropdownList.no.values.label') }}
              </p>
            </v-col>
          </v-row>
          <div v-else>
            <v-row
              v-for="option in sortedOptions"
              :key="option.id"
              class="border-bottom-color">
              <v-col
                cols="8"
                class="d-flex py-1 px-0">
                <span class="my-auto">
                  {{ option.value }}
                </span>
              </v-col>
              <v-col
                cols="4"
                class="text-end py-1 px-0">
                <v-btn
                  class="me-1"
                  icon>
                  <v-icon
                    class="icon-default-color"
                    size="20">
                    fas fa-language
                  </v-icon>
                </v-btn>
                <property-option-action-menu />
              </v-col>
            </v-row>
          </div>
        </v-container>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data() {
    return {
      propertyOptions: [],
      propertyValue: null,
      enableWriteValue: false,
      setting: null
    };
  },
  watch: {
    propertyOptions() {
      this.$root.$emit('property-setting-options-updated', this.propertyOptions);
    }
  },
  computed: {
    sortedOptions() {
      return [...this.propertyOptions].sort((a, b) => a.value.localeCompare(b.value, {numeric: true}));
    }
  },
  methods: {
    addPropertyValue() {
      if (!this.propertyValue) {
        return;
      }
      const values = this.propertyValue.split(',').map(value => value.trim()).filter(Boolean);
      values.forEach(value => {
        this.propertyOptions.push({
          value,
          propertySettingId: this.setting?.id || null,
        });
      });
      this.setting.propertyOptions = this.propertyOptions;
      this.resetValues();
    },
    open(setting) {
      this.setting = setting;
      this.propertyOptions = setting.propertyOptions || [];
      this.resetValues();
      this.$refs.dropdownListDrawer.open();
    },
    resetValues() {
      this.propertyValue = '';
      this.enableWriteValue = false;
    },
    close() {
      this.$refs.dropdownListDrawer.close();
    }
  }
};
</script>
