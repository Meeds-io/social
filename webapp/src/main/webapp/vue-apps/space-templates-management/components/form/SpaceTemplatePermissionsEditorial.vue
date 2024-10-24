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
    <div class="d-flex">
      <div class="flex-grow-1 font-weight-bold">
        {{ $t('spaceTemplate.permissionsStepIsSpaceEditorialLabel') }}
      </div>
      <div class="position-relative mx-8">
        <v-switch v-model="editorial" class="mb-0 mt-1 me-2 pa-0 r-0 absolute-vertical-center" />
      </div>
    </div>
    <div class="text-subtitle">
      {{ $t('spaceTemplate.permissionsStepIsSpaceEditorialDescription') }}
    </div>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    editorial: false,
  }),
  watch: {
    editorial() {
      this.$emit('input', this.editorial);
    },
  },
  created() {
    this.editorial = this.value;
  },
};
</script>