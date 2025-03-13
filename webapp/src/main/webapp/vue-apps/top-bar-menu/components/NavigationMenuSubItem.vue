<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-list
    class="pa-0"
    dense>
    <v-list-item
      v-if="hasPage || hasChildren && childrenHasPage"
      class="pt-0 pb-0 transparent"
      :href="navigationNodeUri"
      :link="!!hasPage"
      :rel="navigationNodeRel"
      :target="navigationNodeTarget"
      @click="checkLink">
      <v-menu
        v-model="showMenu"
        absolute
        :content-class="isTopBarElement && 'layout-top-bar' || ''"
        eager
        :left="$vuetify.rtl"
        offset-x
        :open-on-hover="isOpenedOnHover"
        :position-x="positionX"
        :position-y="positionY"
        rounded
        transition="slide-x-reverse-transition">
        <template #activator="{ attrs, on }">
          <v-list-item-title
            v-bind="attrs"
            class="pt-5 pb-5"
            :class="hasPage && ' ' || ' not-clickable '"
            v-on="on"
            @mouseleave="showMenu = false"
            @mouseover="showMenu = true">
            <span class="text-body">{{ navigation.label }}</span>
          </v-list-item-title>
          <v-list-item-icon
            v-if="hasChildren && childrenHasPage"
            class="ms-0 me-n2 ma-auto full-height"
            @mouseover="showMenu = true">
            <v-btn
              icon
              v-on="on"
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
          :base-site-uri="baseSiteUri"
          class="transparent"
          :navigation="children"
          :parent-navigation-uri="parentNavigationUri"
          :selected-path="selectedPath"
          @select="$emit('select')"
          @update-navigation-state="updateNavigationState" />
      </v-menu>
    </v-list-item>
  </v-list>
</template>

<script>
  export default {
    props: {
      navigation: {
        type: Object,
        default: null,
      },
      baseSiteUri: {
        type: String,
        default: null,
      },
      parentNavigationUri: {
        type: String,
        default: null,
      },
      selectedPath: {
        type: String,
        default: null,
      },
    },
    data () {
      return {
        isOpenedOnHover: true,
        showMenu: false,
        positionX: 0,
        positionY: 0,
      };
    },
    computed: {
      hasChildren () {
        return this.navigation?.children?.length;
      },
      hasPage () {
        return !!this.navigation?.pageKey;
      },
      childrenHasPage () {
        return this.checkChildrenHasPage(this.navigation);
      },
      navigationNodeUri () {
        return eXo.$navigationUtils.getNavigationNodeUri(this.baseSiteUri, this.navigation);
      },
      navigationNodeTarget () {
        return eXo.$navigationUtils.getNavigationNodeTarget(this.navigation);
      },
      navigationNodeRel () {
        return eXo.$navigationUtils.getNavigationNodeRel(this.navigation);
      },
      isSelected () {
        return this.navigationNodeUri === this.selectedPath;
      },
      isTopBarElement () {
        return this.$root.isTopBarElement;
      },
    },
    watch: {
      isSelected: {
        immediate: true,
        handler () {
          if (this.isSelected) {
            this.$emit('select');
          }
        },
      },
      showMenu () {
        this.isOpenedOnHover = !this.showMenu;
        this.positionX = window.innerWidth - (window.innerWidth - this.$el.getBoundingClientRect().right);
        this.positionY = this.$el.getBoundingClientRect().top;
        this.$root.$emit('close-sibling-drop-menus-children', this);
      },
      hasPage () {
        return !!this.navigation?.pageKey;
      },
    },
    created () {
      window.addEventListener('resize', this.updateSize);
      this.$root.$on('close-sibling-drop-menus-children', this.handleCloseSiblingMenus);
    },
    methods: {
      checkLink (e) {
        if (!this.navigationNodeUri) {
          e.stopPropagation();
          e.preventDefault();
        } else {
          this.$emit('update-navigation-state', `${this.parentNavigationUri}`);
        }
        if (this.navigationNodeUri?.includes?.('#')) {
          if (this.navigationNodeTarget === '_blank') {
            window.open(this.navigationNodeUri);
          } else {
            window.location.href = this.navigationNodeUri;
          }
        }
      },
      updateNavigationState (value) {
        this.$emit('update-navigation-state', value);
      },
      checkChildrenHasPage (navigation) {
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
      handleCloseSiblingMenus (emitter) {
        if (!emitter?.navigation?.pageLink && !emitter?.navigationNodeUri?.includes?.(this.navigationNodeUri) && this.showMenu) {
          this.showMenu = false;
        }
      },
      updateSize () {
        this.positionX = window.innerWidth - (window.innerWidth - this.$el.getBoundingClientRect().right) ;
      },
    },
  };
</script>