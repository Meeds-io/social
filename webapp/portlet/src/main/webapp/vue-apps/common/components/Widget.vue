<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association contact@meeds.io
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
    :loading="loading"
    :class="extraClass"
    :height="height"
    :min-width="minWidth"
    class="d-flex flex-column white card-border-radius"
    flat>
    <div class="d-flex flex-column flex-grow-1 pa-5">
      <div 
        v-if="hasHeader"
        :class="headerPadding" 
        class="d-flex align-center">
        <slot v-if="$slots.title" name="title"></slot>
        <div v-else-if="title" class="widget-text-header text-truncate">{{ title }}</div> 
        <v-spacer />
        <slot v-if="$slots.action" name="action"></slot>
        <v-btn 
          v-else-if="actionUrl" 
          class="flex-shrink-0 px-0" 
          :href="actionUrl"
          :target="externalLink && '_blank' || '_self'"
          text
          small> 
          <span class="text-font-size primary--text text-capitalize-first-letter"> 
            {{ actionText }} 
          </span>
        </v-btn>
      </div>
      <div 
        v-if="$slots.subtitle"
        class="pb-4">
        <slot name="subtitle"></slot>
      </div>
      <div 
        v-else-if="subtitle"
        class="pb-4"> 
        {{ subtitle }}
      </div>
      <div 
        v-if="$slots.default"
        class="d-flex flex-column flex-grow-1">
        <slot></slot>
      </div>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    title: {
      type: String,
      default: () => '',
    },
    subtitle: {
      type: String,
      default: () => '',
    },
    actionUrl: {
      type: String,
      default: () => '',
    },
    actionLabel: {
      type: String,
      default: () => '',
    },
    extraClass: {
      type: String,
      default: () => '',
    },
    externalLink: {
      type: Boolean,
      default: () => false
    },
    loading: {
      type: Boolean,
      default: () => false,
    },
    height: {
      type: String,
      default: () => '',
    },
    minWidth: {
      type: String,
      default: () => '',
    },
  },
  computed: {
    hasHeader() {
      return this.$slots.title || this.title || this.$slots.actions || this.actionUrl;
    },
    headerPadding() {
      if (!this.$slots.default) {
        if (this.subtitle || this.$slots.subtitle) {
          return 'mb-4';
        } else {
          return '';
        }
      } else {
        return 'mb-5';
      }
    },
    actionText() {
      return this.actionLabel || this.$t('Widget.label.seeAll');
    }
  }
};
</script>
