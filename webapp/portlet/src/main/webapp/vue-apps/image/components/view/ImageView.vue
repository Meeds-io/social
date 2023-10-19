<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <img
    v-if="$root.imageUrl"
    id="spaceAvatarImg"
    :src="$root.imageUrl"
    :alt="$root.imageAltText"
    :width="$root.fixedHeight && `${width}px` || '100%'"
    :height="$root.fixedHeight && `${$root.fixedHeight}px` || '100%'"
    :class="cssClass"
    :style="cssStyle"
    class="border-box-sizing">
</template>
<script>
export default {
  computed: {
    appWidth() {
      return this.$root.imageAspectRatio * this.$root.imageHeight;
    },
    imgWidth() {
      return this.$root.formatAspectRatio * this.$root.imageHeight;
    },
    width() {
      return Math.max(this.appWidth, this.imgWidth);
    },
    appHeight() {
      return this.$root.fixedHeight;
    },
    imgHeight() {
      return this.width / this.$root.formatAspectRatio;
    },
    height() {
      return Math.max(this.appHeight, this.imgHeight);
    },
    cssClass() {
      return this.$root.fixedHeight
        && ((this.imgWidth < this.appWidth) && 'absolute-vertical-center' || 'absolute-horizontal-center t-0')
        || 'fill-height fill-width';
    },
    cssStyle() {
      return this.$root.fixedHeight && {
        height: `${this.height}px`,
        'width': `${this.width}px`,
        'min-width': `${this.width}px`,
      };
    },
  },
};
</script>