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
      <span>{{ line1 }}</span>
      <span>{{ $t(labels.line2) }}</span>
      <span
        v-sanitized-html="$t(labels.line3, {
          0: `<span class='text-subtitle'>`,
          1: '</span>',
        })"></span>
    </div>
    <!-- The alert is a live region: an operation that states its total in
         its own text hides the counter, which would have the whole alert
         announced again at each processed space -->
    <strong :aria-hidden="counterHidden" class="ms-4 my-auto text-no-wrap">{{ processedSpaces }} / {{ selectedSpacesCount }}</strong>
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
    labels() {
      return {
        line1: 'social.spaces.administration.manageSpaces.processingSpacesBulkOperation1',
        line2: 'social.spaces.administration.manageSpaces.processingSpacesBulkOperation2',
        line3: 'social.spaces.administration.manageSpaces.processingSpacesBulkOperation3',
        ...this.options.bulkOperationLabels,
      };
    },
    selectedSpacesCount() {
      return this.options.bulkOperationTotal
        ?? (this.options.allSpacesSelected ? this.options.spacesSize : this.options.selectedSpaces.length);
    },
    line1() {
      // An operation's own line1 may be a "one | several" choice on the total
      return this.labels.line1Plural
        ? this.$tc(this.labels.line1, this.selectedSpacesCount, {0: this.selectedSpacesCount})
        : this.$t(this.labels.line1, {0: this.selectedSpacesCount});
    },
    counterHidden() {
      return this.options.bulkOperationLabels ? 'true' : null;
    },
    progress() {
      return parseInt(this.processedSpaces / this.selectedSpacesCount * 100);
    },
  },
};
</script>