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
  <div class="d-flex position-relative align-end">
    <v-spacer />
    <v-fade-transition hide-on-leave>
      <div
        v-if="hover"
        :class="{
          'r-0': !$vuetify.rtl,
          'l-0': $vuetify.rtl,
        }"
        class="position-absolute z-index-two t-0 ma-2">
        <v-tooltip v-if="$root.hasImages" bottom>
          <template #activator="{on, bind}">
            <v-btn
              v-on="on"
              v-bind="bind"
              class="me-2 white elevation-1"
              small
              icon
              @click="$emit('remove')">
              <v-icon size="18">fa-trash</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('image.label.removeImage') }}</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template #activator="{on, bind}">
            <v-btn
              v-on="on"
              v-bind="bind"
              :loading="loading"
              class="white elevation-1"
              small
              icon
              @click="$emit('edit')">
              <v-icon size="18">fa-camera</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('image.label.editSettings') }}</span>
        </v-tooltip>
      </div>
    </v-fade-transition>
  </div>
</template>
<script>
export default {
  props: {
    hover: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
};
</script>