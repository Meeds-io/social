<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2026 Meeds Association contact@meeds.io

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
    <v-alert
      v-if="message"
      :type="usable ? 'info' : 'warning'"
      class="mx-5 mt-3 mb-0"
      dense
      outlined>
      <div class="text-color">
        {{ $t(message) }}
      </div>
    </v-alert>
    <v-alert
      v-if="usable"
      class="mx-5 mt-3 mb-0"
      type="info"
      dense
      outlined>
      <div class="text-color">
        {{ $t('loggingLevels.description') }}
        <span v-if="revertAfterMinutes">
          {{ $t('loggingLevels.revertAfter', {0: revertAfterMinutes}) }}
        </span>
      </div>
    </v-alert>
  </div>
</template>
<script>
export default {
  props: {
    enabled: {
      type: Boolean,
      default: true,
    },
    forbidden: {
      type: Boolean,
      default: false,
    },
    readOnly: {
      type: Boolean,
      default: false,
    },
    revertAfterMinutes: {
      type: Number,
      default: 0,
    },
  },
  computed: {
    usable() {
      return this.enabled && !this.forbidden;
    },
    message() {
      if (this.forbidden) {
        return 'loggingLevels.forbidden';
      } else if (!this.enabled) {
        return 'loggingLevels.disabled';
      } else if (this.readOnly) {
        return 'loggingLevels.readOnlyMode';
      }
      return null;
    },
  },
};
</script>
