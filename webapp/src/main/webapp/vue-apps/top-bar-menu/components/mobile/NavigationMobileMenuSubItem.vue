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
    :content-class="`topBar-navigation-bottom-drop-menu ${isTopBarElement && 'layout-top-bar' || ''}`"
    hide-overlay>
    <v-sheet
      v-if="showChildren"
      class="transparent">
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
        class="mt-n3 transparent"
        dense>
        <v-list-item-group>
          <template v-for="nav in navigationObject">
            <v-list-item
              v-if="nav.pageKey || nav.children?.length || nav.pageLink"
              :key="nav.id"
              :href="nav.nodeUri || $navigationUtils.getNavigationNodeUri(baseSiteUri, nav)"
              :target="nav.nodeTarget || $navigationUtils.getNavigationNodeTarget(nav)"
              :rel="nav.nodeRel || $navigationUtils.getNavigationNodeRel(nav)"
              :link="!!nav.pageKey"
              @click="checkLink(nav, $event)">
              <v-list-item-content>
                <v-list-item-title class="text-body" v-text="nav.label" />
              </v-list-item-content>
              <v-list-item-icon
                v-if="nav.children?.length"
                class="full-height">
                <v-btn
                  icon
                  @click.stop.prevent="next(nav)">
                  <v-icon size="18">
                    {{ $vuetify.rtl && 'fa-angle-left' || 'fa-angle-right' }}
                  </v-icon>
                </v-btn>
              </v-list-item-icon>
            </v-list-item>
          </template>
        </v-list-item-group>
      </v-list>
    </v-sheet>
  </v-bottom-sheet>
</template>

<script>
export default {
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
  data: () => ({
    navigationObject: null,
    showChildren: false,
  }),
  computed: {
    isTopBarElement() {
      return this.$root.isTopBarElement;
    }
  },
  watch: {
    showMenu(value) {
      this.showChildren = value;
      if (value) {
        this.navigationObject = Object.assign({}, this.navigation);
      }
    }
  },
  created() {
    this.navigationObject = Object.assign({}, this.navigation);
    this.showChildren = this.showMenu;
  },
  methods: {
    next(navigation) {
      const previous = this.navigationObject;
      this.navigationObject = navigation.children ;
      this.navigationObject.previous = previous;
    },
    prev() {
      this.navigationObject = this.navigationObject.previous;
    },
    checkLink(navigation, e) {
      if (!navigation.pageKey) {
        e.preventDefault();
        e.stopPropagation();
        if (navigation.children) {
          this.next(navigation);
        }
      } else {
        this.$emit('update-navigation-state', `${this.parentNavigationUri}`);
      }
      if (navigation?.nodeUri?.includes?.('#')) {
        if (navigation?.nodeTarget === '_blank') {
          window.open(navigation.nodeUri);
        } else {
          window.location.href = navigation.nodeUri;
        }
      }
    }
  }
};
</script>