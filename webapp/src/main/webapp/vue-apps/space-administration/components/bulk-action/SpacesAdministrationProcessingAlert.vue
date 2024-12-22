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
  <div class="d-flex justify-center">
    <v-progress-circular
      :value="progress"
      color="primary"
      class="me-4 my-auto" />
    <div class="d-flex flex-column me-2">
      <span>{{ $t('social.spaces.administration.manageSpaces.processingSpacesBulkOperation1') }}</span>
      <span>{{ $t('social.spaces.administration.manageSpaces.processingSpacesBulkOperation2') }}</span>
      <span
        v-sanitized-html="$t('social.spaces.administration.manageSpaces.processingSpacesBulkOperation3', {
          0: `<span class='text-subtitle'>`,
          1: '</span>',
        })"></span>
    </div>
    <strong class="ms-4 my-auto text-no-wrap">{{ processedSpaces }} / {{ selectedSpacesCount }}</strong>
  </div>
</template>
<script>
export default {
  props: {
    options: {
      type: Object,
      default: null,
    },
  },
  computed: {
    processedSpaces() {
      return this.options.processedSpaces;
    },
    selectedSpacesCount() {
      return this.options.allSpacesSelected ? this.options.spacesSize : this.options.selectedSpaces.length;
    },
    progress() {
      return parseInt(this.processedSpaces / this.selectedSpacesCount * 100);
    },
  },
};
</script>