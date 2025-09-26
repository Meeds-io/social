<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2025 Meeds Association contact@meeds.io

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
    :class="{'t-0': topPosition, 'b-0': !topPosition}"
    class="position-fixed z-index-modal r-0 width-fit-content d-flex flex-column">
    <v-badge
      color="#bc4343"
      :value="showBadge"
      class="ma-0 pa-0"
      content=""
      offset-x="16"
      offset-y="12"
      width="12"
      height="12"
      bordered
      top
      overlap
      dot>
      <v-btn
        :title="buttonTooltip"
        class="white elevation-2"
        icon
        @click="scrollToTarget">
        <v-icon
          class="icon-default-color"
          size="20">
          {{ upArrow ? 'fas fa-arrow-up' : 'fas fa-arrow-down' }}
        </v-icon>
      </v-btn>
    </v-badge>
    <v-btn
      v-if="closeable"
      :title="closeableTooltip"
      class="white elevation-2 mx-auto mt-1 align-center"
      width="16"
      min-width="20"
      height="20"
      icon
      @click="$emit('closed')">
      <v-icon size="14">
        fa-times
      </v-icon>
    </v-btn>
  </div>
</template>

<script>

export default {
  props: {
    showBadge: {
      type: Boolean,
      default: false
    },
    scrollTarget: {
      type: String,
      default: null
    },
    scrollOptions: {
      type: Object,
      default: () => ({behavior: 'smooth', block: 'center'})
    },
    topPosition: {
      type: Boolean,
      default: false
    },
    upArrow: {
      type: Boolean,
      default: false
    },
    closeable: {
      type: Boolean,
      default: false
    },
    closeableTooltip: {
      type: String,
      default: null
    },
    buttonTooltip: {
      type: String,
      default: null
    }
  },
  methods: {
    scrollToTarget() {
      if (this.scrollTarget) {
        const targetElement = document.getElementById(this.scrollTarget);
        if (targetElement) {
          targetElement.scrollIntoView(this.scrollOptions);
        }
      }
      this.$emit('click');
    }
  }
};
</script>
