<!--
   * This file is part of the Meeds project (https://meeds.io/).
   *
   * Copyright (C) 2023 Meeds Association
   * contact@meeds.io
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
-->

<template>
  <v-bottom-sheet
    v-model="showChildren"
    inset
    content-class="topBar-navigation-bottom-drop-menu"
    hide-overlay>
    <v-sheet
      v-if="showChildren">
      <div
        v-if="navigationObject.previous">
        <v-btn
          class="mt-2 ms-1"
          icon
          @click.stop.prevent="prev">
          <v-icon
            size="15">
            {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
          </v-icon>
        </v-btn>
      </div>
      <v-list
        class="mt-n3"
        dense>
        <v-list-item-group>
          <v-list-item
            v-for="children in navigationObject"
            :key="children.id"
            :href="`${baseSiteUri}${children.uri}`"
            :disabled="!children.pageKey && !children.children?.length"
            :link="!!children.pageKey"
            @click.stop="checkLink(children, $event)">
            <v-list-item-content>
              <v-list-item-title v-text="children.label" />
            </v-list-item-content>
            <v-list-item-icon
              v-if="children.children?.length"
              class="full-height">
              <v-btn
                icon
                @click.stop.prevent="next(children)">
                <v-icon
                  size="18">
                  {{ $vuetify.rtl && 'fa-angle-left' || 'fa-angle-right' }}
                </v-icon>
              </v-btn>
            </v-list-item-icon>
          </v-list-item>
        </v-list-item-group>
      </v-list>
    </v-sheet>
  </v-bottom-sheet>
</template>

<script>
export default {
  data () {
    return {
      navigationObject: null,
      showChildren: false,
    };
  },
  props: {
    navigation: {
      type: Object,
      default: null
    },
    baseSiteUri: {
      type: String,
      default: null
    },
    showMenu: {
      type: Boolean,
      default: false
    },
    parentNavigationUri: {
      type: String,
      default: null
    }
  },
  created() {
    this.navigationObject = Object.assign({}, this.navigation);
    this.showChildren = this.showMenu;
  },
  watch: {
    showMenu(value) {
      this.showChildren = value;
      if (value) {
        this.navigationObject = Object.assign({}, this.navigation);
      }
    }
  },
  methods: {
    next(children) {
      const previous = this.navigationObject;
      this.navigationObject = children.children ;
      this.navigationObject.previous = previous;
    },
    prev() {
      this.navigationObject = this.navigationObject.previous ;
    },
    checkLink(navigation, e) {
      if (!navigation.pageKey) {
        e.preventDefault();
        if (navigation.children) {
          this.next(navigation);
        }
      } else {
        this.$emit('update-navigation-state', `${this.parentNavigationUri}`);
      }
    }
  }
};
</script>