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
  <v-list dense>
    <v-list-item class="px-2 text-left">
      <div
        class="clickable mx-2"
        @click="edit()">
        <v-icon
          size="21"
          class="pe-1">
          fas fa-edit
        </v-icon>
        <span class="ps-1">{{ $t('profileSettings.label.edit') }}</span>
      </div>
    </v-list-item>
    <v-list-item v-if="canMoveUp" class="menu-list px-2 text-left action-menu-item">
      <div
        class="clickable mx-2"
        @click="moveUp()">
        <v-icon
          size="21"
          class="pe-1">
          mdi-mouse-move-up
        </v-icon>
        <span class="ps-1">{{ $t('profileSettings.label.moveUp') }}</span>
      </div>
    </v-list-item>
    <v-list-item v-if="canMoveDown" class="menu-list px-2 text-left action-menu-item">
      <div
        class="clickable mx-2"
        @click="moveDown()">
        <v-icon
          size="21"
          class="pe-1">
          mdi-mouse-move-down
        </v-icon>
        <span class="ps-1">{{ $t('profileSettings.label.moveDown') }}</span>
      </div>
    </v-list-item>
  </v-list>
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
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    index(){
      return this.settings.findIndex(object => {
        return object.id === this.setting.id;
      });
    },
    canMoveUp()
    {
      return this.index > 0;
    },
    canMoveDown()
    {
      return this.index < this.settings.length - 1;
    }, 
  },
  methods: {
    edit(){
      this.$root.$emit('open-settings-edit-drawer', JSON.parse(JSON.stringify( this.setting)));
    },
    moveUp(){
      this.$root.$emit('move-up-setting', this.setting);
    },
    moveDown(){
      this.$root.$emit('move-down-setting', this.setting);
    }
  }
};
</script>
