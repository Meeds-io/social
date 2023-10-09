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
    :class="[extraClass, expanded && 'pa-5' || 'pa-0']"
    class="white d-flex flex-column border-radius"
    flat>
    <v-card-title v-if="hasTitle" class="text-header text-sub-title justify-space-between flex-nowrap px-0 pt-0 pb-4">
      <slot name="title"></slot>
      <div>
        <slot name="action"></slot>
        <a 
          v-if="seeAllUrl" 
          class="flex-shrink-0" 
          :href="seeAllUrl"> <h5 class="subtitle-1 primary--text my-0"> {{ $t('overview.myContributions.seeAll') }} </h5> </a>
      </div>
    </v-card-title>
    <v-card-subtitle class="text-color pa-0">
      <slot name="subtitle"></slot>
    </v-card-subtitle>

    <v-card-text class="d-flex flex-column flex-grow-1 pa-0">
      <slot name="content"></slot>
    </v-card-text>
  </v-card>
</template>
<script>
export default {
  props: {
    seeAllUrl: {
      type: String,
      default: () => '',
    },
    extraClass: {
      type: String,
      default: () => '',
    },
    hasTitle: {
      type: Boolean,
      default: () => true,
    },
    expanded: {
      type: Boolean,
      default: () => true
    }
  },
};
</script>
