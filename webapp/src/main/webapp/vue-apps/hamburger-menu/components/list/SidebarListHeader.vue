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
    :style="elevate && {
      'box-shadow' : 'rgb(0 0 0 / 30%) 0 6px 4px -4px',
    }"
    class="no-border-radius"
    min-height="57"
    flat>
    <v-list-item
      :href="$root.defaultUserPath"
      class="fill-height">
      <div class="my-auto me-2 logoContainer">
        <img
          :src="companyLogo"
          :alt="companyName"
          height="auto"
          width="36px">
      </div>
      <v-list-item-content>
        <v-list-item-title v-if="$root.expand" class="logoTitle font-weight-bold menu-text-color text-truncate">
          {{ companyName }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action v-if="$root.expand && $root.stickyAllowed" class="d-flex flex-row ms-auto my-auto">
        <v-btn
          v-if="!$root.sticky"
          :title="$t('menu.expand')"
          :aria-label="$t('menu.expand')"
          icon
          @click.stop.prevent="changeMenuStickiness('STICKY')"
          @mousedown.stop.prevent
          @mouseup.stop.prevent>
          <v-icon>{{ arrowIconLeft }}</v-icon>
        </v-btn>
        <v-btn
          v-if="!$root.hidden"
          :title="$t('menu.collapse')"
          :aria-label="$t('menu.collapse')"
          icon
          @click.stop.prevent="changeMenuStickiness('HIDDEN')"
          @mousedown.stop.prevent
          @mouseup.stop.prevent>
          <v-icon>{{ arrowIconRight }}</v-icon>
        </v-btn>
        <v-btn
          v-if="!$root.icon"
          :title="$t('menu.reduce')"
          :aria-label="$t('menu.reduce')"
          icon
          @click.stop.prevent="changeMenuStickiness('ICON')"
          @mousedown.stop.prevent
          @mouseup.stop.prevent>
          <v-icon>fa-map-pin</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
  </v-card>
</template>
<script>
export default {
  props: {
    elevate: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    companyName: eXo.env.portal.companyName,
    companyLogo: eXo.env.portal.companyLogo,
    userName: eXo.env.portal.userName,
  }),
  computed: {
    arrowIconClass() {
      return this.$root.sticky && this.arrowIconLeft || this.arrowIconRight;
    },
    arrowIconLeft() {
      return this.$vuetify.rtl && 'fa-angle-double-right' || 'fa-angle-double-left';
    },
    arrowIconRight() {
      return this.$vuetify.rtl && 'fa-angle-double-left' || 'fa-angle-double-right';
    },
  },
  methods: {
    changeMenuStickiness(mode) {
      this.$navigationSettingService.updateSidebarUserMode(mode);
      this.$root.mode = mode;
    },
  }
};
</script>