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
  <div
    v-if="shown"
    :style="warningOnly && 'flex: 0 0 100%'"
    class="contrastRatio text-subtitle font-weight-regular">
    <div v-if="!warningOnly" :class="lowContrast && 'error--text' || 'success--text'">
      {{ ratio.toFixed(1) }}:1
    </div>
    <div v-if="lowContrast && warning" class="contrastWarning error--text text-wrap">
      {{ warning }}
    </div>
  </div>
</template>
<script>
import {READABLE_RATIO, contrastRatio} from '../js/ColorContrast.js';

export default {
  props: {
    // the text color as shown, the one an unset row inherits included
    color: {
      type: String,
      default: null,
    },
    background: {
      type: String,
      default: null,
    },
    // shown under a ratio below 4.5:1
    warning: {
      type: String,
      default: null,
    },
    // shows the warning alone, on a line of its own under the row whose label shows the ratio
    warningOnly: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    exactRatio() {
      return contrastRatio(this.color, this.background);
    },
    // truncated to one decimal, so a ratio below 4.5:1, shown red, never reads 4.5
    ratio() {
      return this.exactRatio && Math.floor(this.exactRatio * 10 + 1e-9) / 10;
    },
    lowContrast() {
      return this.exactRatio < READABLE_RATIO;
    },
    shown() {
      return !!this.ratio && (!this.warningOnly || this.lowContrast && !!this.warning);
    },
  },
};
</script>
