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
  <v-card
    :style="cssStyle"
    class="position-sticky no-border-radius"
    flat>
    <div
      :style="parentStyle"
      class="white ms-n6">
      <v-divider
        v-if="elevate" />
      <slot></slot>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    top: {
      type: String,
      default: null,
    },
    bottom: {
      type: String,
      default: null,
    },
    scrollDiff: {
      type: Number,
      default: null,
    },
    dividerWidth: {
      type: String,
      default: () => 'calc(100% + 48px)',
    },
    parentSelector: {
      type: String,
      default: () => '.site-scroll-parent',
    },
  },
  data: () => ({
    scrolled: true,
  }),
  computed: {
    isScrollTop() {
      return this.top !== null;
    },
    isScrollBottom() {
      return this.bottom !== null;
    },
    elevate() {
      return this.scrolled;
    },
    parentStyle() {
      return `min-width: ${this.dividerWidth}`;
    },
    cssStyle() {
      if (this.isScrollTop) {
        return {
          'top': `${this.top}px`,
        };
      } else if (this.isScrollBottom) {
        return {
          'bottom': `${this.bottom}px`,
        };
      }
      return null;
    },
  },
  mounted() {
    window.setTimeout(() => {
      const parentScroll = document.querySelector(this.parentSelector);
      if (parentScroll) {
        parentScroll?.addEventListener?.('scroll', this.computeScollPosition, false);
      }
    }, 50);
  },
  methods: {
    computeScollPosition(event) {
      if (this.isScrollBottom) {
        this.scrolled = parseInt(event?.target?.scrollHeight - event?.target?.offsetHeight - event?.target?.scrollTop) > (this.scrollDiff && Number(this.scrollDiff) || 0);
      } else if (this.isScrollTop) {
        this.scrolled = event?.target?.scrollTop > (this.scrollDiff && Number(this.scrollDiff) || 0);
      }
    },
  },
};
</script>
