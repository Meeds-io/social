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
  <div>
    <div class="d-flex align-center mb-2">
      <div class="font-weight-bold" slot="title">
        {{ $t(title) }}
      </div>
      <v-spacer />
      <div class="me-4">
        {{ $t('social.spaces.administration.manageSpaces.templateUpdateProperty') }}
      </div>
      <v-switch v-model="enabled" class="my-0 me-n2" />
    </div>
    <div class="d-flex flex-column mb-4">
      <div class="d-flex mb-2">
        <div class="col-4 pa-0">
          {{ $t('social.spaces.administration.manageSpaces.spaceCurrentCharacteristic') }}
        </div>
        <div class="col-8 pa-0 d-flex flex-wrap justify-end">
          <slot name="spaceValue"></slot>
        </div>
      </div>
      <div class="d-flex">
        <div class="col-4 pa-0">
          {{ $t('social.spaces.administration.manageSpaces.spaceNewCharacteristic') }}
        </div>
        <div class="col-8 pa-0 d-flex flex-wrap justify-end">
          <slot name="templateValue"></slot>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    title: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    enabled: false,
  }),
  watch: {
    enabled() {
      this.$emit('input', this.enabled);
    },
  },
};
</script>