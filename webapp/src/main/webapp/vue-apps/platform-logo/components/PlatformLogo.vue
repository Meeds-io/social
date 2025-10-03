<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <v-app class="full-height full-width">
    <v-hover v-slot="{ hover }">
      <v-card flat
        class="fill-height rounded-0 transparent">
        <div
          v-if="$root.canEdit && hover"
          class="position-absolute t-0 r-0"
          :class="{
            'l-0': $vuetify.rtl,
            'r-0': !$vuetify.rtl,
          }">
          <v-fab-transition hide-on-leave>
            <v-btn
              :title="$t('platformLogo.settings.editTooltip')"
              class="z-index-two me-2 mt-2"
              small
              icon
              @click="$root.$emit('platform-logo-settings')">
              <v-icon size="18">fa-cog</v-icon>
            </v-btn>
          </v-fab-transition>
        </div>

        <img
          :src="platformLogo"
          alt=""
          class="object-fit-contain full-width full-height"
          :style="align" />
      </v-card>
    </v-hover>
    <platform-logo-settings-drawer v-if="$root.canEdit" />

  </v-app>
</template>

<script>
export default {
  props: {
    platformLogo: {
      type: String,
      default: null,
    },
  },
  computed: {
    align() {
      let align = 'object-position:';
      switch (this.$root.vAlign) {
      case 'START': {
        align = `${align} top`;
        break;
      }
      case 'END': {
        align = `${align} bottom`;
        break;
      }
      default: {
        align = `${align} center`;
        break;
      }
      }
      switch (this.$root.hAlign) {
      case 'START': {
        align = `${align} left`;
        break;
      }
      case 'END': {
        align = `${align} right`;
        break;
      }
      default: {
        align = `${align} center`;
        break;
      }
      }
      return align;
    }
  },
};
</script>
