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
  <v-list
    class="pa-0"
    dense>
    <v-list-item
      v-if="hasPage || hasChildren && childrenHasPage"
      class="pt-0 pb-0"
      :href="navigationNodeUri"
      :target="navigationNodeTarget"
      :class="extraClass"
      @click.stop="checkLink(navigation, $event)"
      :link="!!hasPage">
      <v-menu
        v-model="showMenu"
        rounded
        :position-x="positionX"
        :position-y="positionY"
        transition="slide-x-reverse-transition"
        absolute
        :left="$vuetify.rtl"
        :open-on-hover="isOpenedOnHover"
        offset-x>
        <template #activator="{ attrs, on }">
          <v-list-item-title
            v-on="on"
            v-bind="attrs"
            class="pt-5 pb-5 text-caption"
            :class="hasPage && ' ' || ' not-clickable '"
            v-text="navigation.label"
            @mouseleave="showMenu = false"
            @mouseover="showMenu = true" />
          <v-list-item-icon
            v-if="hasChildren && childrenHasPage"
            class="ms-0 me-n2 ma-auto full-height"
            @mouseover="showMenu = true">
            <v-btn
              v-on="on"
              icon
              @click.stop.prevent="showMenu = !showMenu">
              <v-icon
                size="18">
                {{ $vuetify.rtl && 'fa-angle-left' || 'fa-angle-right' }}
              </v-icon>
            </v-btn>
          </v-list-item-icon>
        </template>
        <navigation-menu-sub-item
          v-for="children in navigation.children"
          :key="children.id"
          :navigation="children"
          :parent-navigation-uri="parentNavigationUri"
          :base-site-uri="baseSiteUri"
          @update-navigation-state="updateNavigationState" />
      </v-menu>
    </v-list-item>
  </v-list>
</template>

<script>
export default {
  data() {
    return {
      isOpenedOnHover: true,
      showMenu: false,
      positionX: 0,
      positionY: 0,
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
    parentNavigationUri: {
      type: String,
      default: null
    }
  },
  watch: {
    showMenu() {
      this.isOpenedOnHover = !this.showMenu;
      this.positionX = window.innerWidth - (window.innerWidth - this.$el.getBoundingClientRect().right);
      this.positionY = this.$el.getBoundingClientRect().top;
      this.$root.$emit('close-sibling-drop-menus-children', this);
    }
  },
  created() {
    window.addEventListener('resize', this.updateSize);
    this.$root.$on('close-sibling-drop-menus-children', this.handleCloseSiblingMenus);
  },
  computed: {
    hasChildren() {
      return this.navigation?.children?.length;
    },
    hasPage() {
      return !!this.navigation?.pageKey;
    },
    childrenHasPage() {
      return this.checkChildrenHasPage(this.navigation);
    },
    navigationNodeUri() {
      return this.navigation?.pageLink && this.urlVerify(this.navigation?.pageLink) || `${this.baseSiteUri}${this.navigation.uri}`;
    },
    navigationNodeTarget() {
      return this.navigation?.target === 'SAME_TAB' && '_self' || '_blank';
    },
    extraClass() {
      return `${this.showMenu ? ' light-grey-background ' : ' ' }`;
    },
  },
  methods: {
    checkLink(navigation, e) {
      if (!navigation.pageKey) {
        e.preventDefault();
      } else {
        this.$emit('update-navigation-state', `${this.parentNavigationUri}`);
      }
    },
    updateNavigationState(value) {
      this.$emit('update-navigation-state', value);
    },
    checkChildrenHasPage(navigation) {
      let childrenHasPage = false;
      navigation.children.forEach(child => {
        if (childrenHasPage === true) {
          return;
        }
        if (child.pageKey) {
          childrenHasPage = true;
        } else if (child.children.length > 0) {
          childrenHasPage = this.checkChildrenHasPage(child);
        } else {
          childrenHasPage = false;
        }
      });
      return childrenHasPage;
    },
    urlVerify(url) {
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url ;
    },
    handleCloseSiblingMenus(emitter) {
      if (!emitter.navigation.pageLink && !emitter.navigationNodeUri.includes(this.navigationNodeUri) && this.showMenu) {
        this.showMenu = false;
      }
    },
    updateSize() {
      this.positionX = window.innerWidth - (window.innerWidth - this.$el.getBoundingClientRect().right) ;
    }
  }
};
</script>