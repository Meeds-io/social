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
    :class="extraClass"
    class="white d-flex flex-column"
    flat>
    <div 
      :class="$slots.subtitle && 'pb-4' || 'pb-5'" 
      class="d-flex align-center">
      <slot v-if="$slots.title" name="title"></slot>
      <div v-else-if="title" class="widget-text-header text-truncate">{{ title }}</div> 
      <v-spacer />
      <slot v-if="$slots.action" name="action"></slot>
      <v-btn 
        v-else-if="actionUrl" 
        class="flex-shrink-0" 
        :href="actionUrl"
        :target="externalLink && '_blank' || '_self'"> 
        {{ $t('overview.myContributions.seeAll') }}
      </v-btn>
    </div>
    <slot v-if="$slots.subtitle" name="subtitle"></slot>
    <div v-else-if="subtitle"></div>
    <div class="d-flex flex-column flex-grow-1 pa-0">
      <slot v-id="$slots.default"></slot>
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
    extraClass: {
      type: String,
      default: () => '',
    },
    externalLink: {
      type: Boolean,
      default: () => false
    }
  },
};
</script>
