<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    <div class="font-weight-bold">
      {{ $t('spaceTemplate.defaultSpaceConfigurationStepAccess') }}
    </div>
    <v-radio-group
      v-model="registration"
      class="mt-2 ms-n1"
      mandatory
      inset>
      <v-radio
        value="OPEN"
        class="mt-0 mb-8">
        <template #label>
          <div class="d-flex flex-column">
            <div class="text-body">
              {{ $t('spaceTemplate.defaultSpaceConfigurationStepAccessOpen') }}
            </div>
            <div class="text-subtitle position-absolute line-height-normal mt-5">
              {{ $t('spaceTemplate.defaultSpaceConfigurationStepAccessOpenDescription') }}
            </div>
          </div>
        </template>
      </v-radio>
      <v-radio
        value="VALIDATION"
        class="mt-0 mb-8">
        <template #label>
          <div class="d-flex flex-column">
            <div class="text-body">
              {{ $t('spaceTemplate.defaultSpaceConfigurationStepAccessValidation') }}
            </div>
            <div class="text-subtitle position-absolute line-height-normal mt-5">
              {{ $t('spaceTemplate.defaultSpaceConfigurationStepAccessValidationDescription') }}
            </div>
          </div>
        </template>
      </v-radio>
      <v-radio
        value="CLOSED"
        class="mt-0 mb-6">
        <template #label>
          <div class="d-flex flex-column">
            <div class="text-body">
              {{ $t('spaceTemplate.defaultSpaceConfigurationStepAccessClosed') }}
            </div>
            <div class="text-subtitle position-absolute line-height-normal mt-5">
              {{ $t('spaceTemplate.defaultSpaceConfigurationStepAccessClosedDescription') }}
            </div>
          </div>
        </template>
      </v-radio>
    </v-radio-group>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    registration: null,
  }),
  watch: {
    registration() {
      this.$emit('input', this.registration);
    },
  },
  created() {
    this.registration = this.value;
  },
};
</script>