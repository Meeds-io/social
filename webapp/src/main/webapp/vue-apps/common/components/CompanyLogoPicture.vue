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
  <!-- no box of its own, and the sizes inherited through it: a container that sizes its image with inherit (an avatar's
       height) reaches the image as if it were its direct child -->
  <picture class="companyLogoPicture" style="display: contents; height: inherit; width: inherit; border-radius: inherit;">
    <source
      v-if="darkSource"
      :srcset="darkSource"
      :media="darkMedia">
    <img
      :src="src"
      :alt="alt"
      :class="imgClass"
      :style="imgStyle"
      v-bind="$attrs">
  </picture>
</template>
<script>
// A logo shown in the version of the page: the dark source applies by the rule the stylesheet follows — always in
// dark, never in light, on a dark device in Automatic. The page's mode is the attribute of its <html>, a light scope
// around the logo showing the light one
export default {
  inheritAttrs: false,
  props: {
    src: {
      type: String,
      default: null,
    },
    darkSrc: {
      type: String,
      default: null,
    },
    alt: {
      type: String,
      default: '',
    },
    // the image's own class and style: the ones of the element would go to the <picture>, which draws no box
    imgClass: {
      type: [String, Object, Array],
      default: null,
    },
    imgStyle: {
      type: [String, Object, Array],
      default: null,
    },
  },
  data: () => ({
    pageMode: null,
  }),
  computed: {
    darkSource() {
      return this.darkSrc && this.darkSrc !== this.src && this.darkSrc;
    },
    darkMedia() {
      if (this.pageMode === 'dark') {
        return 'all';
      } else if (this.pageMode === 'auto') {
        return '(prefers-color-scheme: dark)';
      }
      return 'not all';
    },
  },
  created() {
    this.refreshMode();
    document.addEventListener('displayModeChanged', this.refreshMode);
  },
  mounted() {
    this.refreshMode();
  },
  beforeDestroy() {
    document.removeEventListener('displayModeChanged', this.refreshMode);
  },
  methods: {
    refreshMode() {
      // inside a light scope (an application that keeps its light display), the light logo
      const lightScope = this.$el?.closest?.('[data-display-mode="light"]:not(html)');
      this.pageMode = lightScope ? 'light' : this.$displayMode.getPageMode(this.$el?.ownerDocument);
    },
  },
};
</script>
