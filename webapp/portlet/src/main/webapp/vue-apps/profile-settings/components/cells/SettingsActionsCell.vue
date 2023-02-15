/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

<template>
  <div
    class="d-flex flex-nowrap">
    <div
      :id="`setting-action-menu-cel-${setting.id}`">
      <v-tooltip bottom>
        <template #activator="{ on, attrs }">
          <v-btn
            icon
            small
            v-bind="attrs"
            v-on="on">
            <v-icon
              :size="isMobile ? 14 : 18"
              class="clickable text-sub-title button-settings-action"
              @click="displayActionMenu()">
              mdi-dots-vertical
            </v-icon>
          </v-btn>
          <v-menu
            v-model="menuDisplayed"
            :attach="`#setting-action-menu-cel-${setting.id}`"
            transition="slide-x-reverse-transition"
            :content-class="isMobile ? 'settingsActionMenuMobile' : 'settingsActionMenu'"
            offset-y
            offset-x
            close-on-click
            absolute>
            <profile-settings-action-menu
              :setting="setting"
              :settings="settings" />
          </v-menu>
        </template>
        <span>
          {{ menuActionTooltip }}
        </span>
      </v-tooltip>
    </div>
  </div>
</template>
<script>
export default {

  props: {
    setting: {
      type: Object,
      default: null,
    },
    settings: {
      type: Object,
      default: null,
    }
  },
  data: () => ({
    loading: false,
    menuDisplayed: false,
    waitTimeUntilCloseMenu: 200,
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    menuActionTooltip() {
      return this.$t('profileSettings.label.menu.action.tooltip');
    },
  },
  created() {
    $(document).on('mousedown', () => {
      if (this.menuDisplayed) {
        window.setTimeout(() => {
          $(`#setting-action-menu-cel-${this.setting.id}`).parent().parent().parent().parent().css('background', '#fff');
          this.menuDisplayed = false;
        }, this.waitTimeUntilCloseMenu);
      }
    });
  },
  methods: {
    displayActionMenu() {
      if (this.isMobile){
        this.$root.$emit('open-setting-action-menu', this.setting);
      } else {
        this.menuDisplayed = true;
        $(`#setting-action-menu-cel-${this.setting.id}`).parent().parent().parent().parent().css('background', '#eee');
      }
    },
  }
};
</script>
