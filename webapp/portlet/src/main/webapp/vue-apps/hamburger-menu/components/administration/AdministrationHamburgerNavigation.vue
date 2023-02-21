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
  <v-container
    id="AdministrationHamburgerNavigation"
    px-0
    py-0
    class="white d-none d-sm-block"
    @mouseover="showItemActions = true"
    @mouseleave="showItemActions = false">
    <v-row class="mx-0 administrationTitle">
      <v-list-item>
        <v-list-item-icon class="mb-2 mt-3 mr-6 titleIcon">
          <v-icon class="fa fa-cog" />
        </v-list-item-icon>
        <v-list-item-content v-if="expand" class="subtitle-2">
          {{ this.$t('menu.administration.title') }}
        </v-list-item-content>
        <v-list-item-action v-if="toggleArrow" class="my-0">
          <v-btn icon @click="$root.$emit('change-administration-menu')">
            <v-icon class="me-0 pa-2 icon-default-color clickable" small>
              {{ arrowIconClass }} 
            </v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-row>
  </v-container>
</template>
<script>
export default {
  props: {
    openedMenu: {
      type: Boolean,
      default: false,
    },
    expand: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      showItemActions: false,
    };
  },
  computed: {
    arrowIconClass() {
      return this.openedMenu && this.arrowIconLeft || this.arrowIconRight;
    },
    arrowIconLeft() {
      return this.$root.ltr && 'fa-arrow-left' || 'fa-arrow-right';
    },
    arrowIconRight() {
      return this.$root.ltr && 'fa-arrow-right' || 'fa-arrow-left';
    },
    toggleArrow() {
      return this.expand && this.showItemActions;
    }
  },
};
</script>
