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
      {{ $t('spaceTemplate.defaultSpaceConfigurationStepVisibility') }}
    </div>
    <v-radio-group
      v-model="visibility"
      class="mt-2 ms-n1"
      mandatory
      inset>
      <v-radio
        value="PRIVATE"
        class="mb-6">
        <template #label>
          <div class="d-flex flex-column">
            <div class="text-body">
              {{ $t('spaceTemplate.defaultSpaceConfigurationStepVisibilityListed') }}
            </div>
            <div class="text-subtitle position-absolute line-height-normal mt-5">
              {{ $t(`spaceTemplate.defaultSpaceConfigurationStepVisibilityListedDescription`) }}
            </div>
          </div>
        </template>
      </v-radio>
      <v-radio
        value="HIDDEN"
        class="mt-0 mb-8">
        <template #label>
          <div class="d-flex flex-column">
            <div class="text-body">
              {{ $t('spaceTemplate.defaultSpaceConfigurationStepVisibilityUnlisted') }}
            </div>
            <div class="text-subtitle position-absolute line-height-normal mt-5">
              {{ $t(`spaceTemplate.defaultSpaceConfigurationStepVisibilityUnlistedDescription`) }}
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
    visibility: null,
  }),
  watch: {
    visibility() {
      this.$emit('input', this.visibility);
    },
  },
  created() {
    this.visibility = this.value;
  },
};
</script>