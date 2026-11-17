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
    <!-- what opens the sub-menu says so itself, the entry or, when the entry is a page link, its chevron: Vuetify's
         activator attributes would land on the entry's inner block, which never has the focus. The entry is named by
         its label alone: its content would add its chevron's label to it, and to its sub-menu's name -->
    <v-list-item
      v-if="hasPage || opensSubMenu"
      :id="entryId"
      ref="entry"
      :href="navigationNodeUri"
      :target="navigationNodeTarget"
      :rel="navigationNodeRel"
      :link="!!hasPage"
      :aria-label="navigation.label"
      :aria-current="isSelected ? 'page' : null"
      :aria-haspopup="entryOpensSubMenu ? 'menu' : null"
      :aria-expanded="entryOpensSubMenu ? String(!!showMenu) : null"
      :aria-controls="entryOpensSubMenu ? subMenuId : null"
      class="py-0 px-0 transparent"
      @click="checkLink"
      @keydown="onEntryKeydown">
      <v-menu
        ref="menu"
        v-model="showMenu"
        rounded
        content-class="topBar-navigation-drop-menu topBar-navigation-drop-sub-menu"
        :position-x="positionX"
        :position-y="positionY"
        transition="slide-x-reverse-transition"
        :left="$vuetify.rtl || openLeft"
        :open-on-hover="isOpenedOnHover"
        :close-on-content-click="false"
        absolute
        eager
        offset-x>
        <template #activator="{ on }">
          <!-- the pointer's listeners only: the entry handles the keys, and the arrows pressed on the chevron inside
               this block would open the sub-menu again through Vuetify's own keydown listener -->
          <div
            ref="activator"
            class="d-flex width-full px-4"
            v-on="pointerListeners(on)"
            @mouseleave="showMenu = false">
            <v-list-item-title
              class="pt-5 pb-5 d-flex"
              :class="hasPage && ' ' || ' not-clickable '">
              <!-- the current page is shown by more than a color: underlined, as its entry in the bar -->
              <span
                :class="isSelected && 'text-decoration-underline primary--text'"
                class="text-body">{{ navigation.label }}</span>
              <v-icon
                v-if="navigation.target === 'NEW_TAB'"
                size="12"
                class="mx-1">
                fa-external-link-alt
              </v-icon>
            </v-list-item-title>
            <v-list-item-icon
              v-if="opensSubMenu"
              class="ms-0 me-n2 ma-auto full-height">
              <!-- the entry that only groups pages opens its sub-menu itself: its chevron is the pointer's way only.
                   A page link's chevron is what opens its sub-menu for a screen reader that has no arrow keys. Its Enter
                   and Space are its own: the link around it, and the menu holding the link, would make a click on the
                   link of them -->
              <v-btn
                ref="chevron"
                icon
                :tabindex="hasPage ? null : -1"
                :aria-hidden="hasPage ? null : 'true'"
                :aria-label="hasPage ? $t('topBar.navigation.menu.openSubMenu', [navigation.label]) : null"
                :aria-haspopup="hasPage ? 'menu' : null"
                :aria-expanded="hasPage ? String(!!showMenu) : null"
                :aria-controls="hasPage ? subMenuId : null"
                @keydown.native.enter.stop
                @keydown.native.space.stop
                @click.stop.prevent="toggleFromChevron">
                <v-icon
                  size="18">
                  {{ $vuetify.rtl && 'fa-angle-left' || 'fa-angle-right' }}
                </v-icon>
              </v-btn>
            </v-list-item-icon>
          </div>
        </template>
        <div @keydown="onSubMenuKeydown">
          <navigation-menu-sub-item
            v-for="children in navigation.children"
            class="transparent"
            :key="children.id"
            :navigation="children"
            :parent-navigation-uri="parentNavigationUri"
            :base-site-uri="baseSiteUri"
            :selected-path="selectedPath"
            @update-navigation-state="updateNavigationState"
            @select="$emit('select')"
            @leaving-page="$emit('leaving-page')"
            @close-menu="closeMenu" />
        </div>
      </v-menu>
    </v-list-item>
  </v-list>
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
    parentNavigationUri: {
      type: String,
      default: null
    },
    selectedPath: {
      type: String,
      default: null
    },
  },
  data() {
    return {
      isOpenedOnHover: true,
      showMenu: false,
      positionX: 0,
      positionY: 0,
      openLeft: false,
      subMenuOpener: null,
    };
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
      return this.$navigationUtils.getNavigationNodeUri(this.baseSiteUri, this.navigation);
    },
    navigationNodeTarget() {
      return this.$navigationUtils.getNavigationNodeTarget(this.navigation);
    },
    navigationNodeRel() {
      return this.$navigationUtils.getNavigationNodeRel(this.navigation);
    },
    isSelected() {
      return this.navigationNodeUri === this.selectedPath;
    },
    opensSubMenu() {
      return !!this.hasChildren && this.childrenHasPage;
    },
    entryOpensSubMenu() {
      return this.opensSubMenu && !this.hasPage;
    },
    entryId() {
      return `topBarNavigationEntry${this._uid}`;
    },
    subMenuId() {
      return `topBarNavigationSubMenu${this._uid}`;
    },
  },
  watch: {
    isSelected: {
      immediate: true,
      handler() {
        if (this.isSelected) {
          this.$emit('select');
        }
      }
    },
    showMenu() {
      this.isOpenedOnHover = !this.showMenu;
      this.updatePosition();
      this.$root.$emit('close-sibling-drop-menus-children', this);
    },
    hasPage() {
      return !!this.navigation?.pageKey;
    },
  },
  created() {
    window.addEventListener('resize', this.updateSize);
    this.$root.$on('close-sibling-drop-menus-children', this.handleCloseSiblingMenus);
  },
  mounted() {
    // the sub-menu is rendered at the end of the page (eager: from mount), so the menu holding this entry never sees
    // its clicks: they are read on the sub-menu's own content
    this.subMenuContent = this.$refs.menu?.$refs?.content;
    this.subMenuContent?.addEventListener('click', this.onSubMenuClick);
    this.linkSubMenu();
  },
  updated() {
    this.linkSubMenu();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateSize);
    this.$root.$off('close-sibling-drop-menus-children', this.handleCloseSiblingMenus);
    this.subMenuContent?.removeEventListener('click', this.onSubMenuClick);
  },
  methods: {
    // The sub-menu content is rendered at the end of the page: its entry names it, and it is named after its entry
    linkSubMenu() {
      const content = this.$refs.menu?.$refs?.content;
      if (content && this.opensSubMenu) {
        content.id = this.subMenuId;
        content.setAttribute('aria-labelledby', this.entryId);
      }
    },
    // Enter, Space and the arrow pointing to where sub-menus open (Right, Left in a right-to-left page) open the
    // sub-menu with the focus on its first entry
    onEntryKeydown(event) {
      if (!this.opensSubMenu) {
        return;
      }
      const openKey = this.$vuetify.rtl ? 'ArrowLeft' : 'ArrowRight';
      if (event.key === openKey || event.key === ' ' && !this.hasPage) {
        event.preventDefault();
        event.stopPropagation();
        // pressed on a page link's chevron, the key bubbles through the link: the focus comes back to the chevron
        const chevron = this.$refs.chevron?.$el;
        this.openSubMenu(chevron?.contains?.(event.target) ? chevron : this.$refs.entry?.$el);
      } else if (event.key === 'Enter' && !this.hasPage) {
        // Vuetify makes a click of it, which opened the sub-menu: the menu holding this entry would click it again
        event.stopPropagation();
      }
    },
    onSubMenuKeydown(event) {
      const entries = this.$navigationUtils.menuEntries(this.$refs.menu?.$refs?.content);
      const index = this.$navigationUtils.entryIndexOf(entries, document.activeElement);
      if (index < 0) {
        return;
      }
      const target = {
        ArrowDown: (index + 1) % entries.length,
        ArrowUp: (index - 1 + entries.length) % entries.length,
        Home: 0,
        End: entries.length - 1,
      }[event.key];
      const closeKey = this.$vuetify.rtl ? 'ArrowRight' : 'ArrowLeft';
      if (typeof target === 'number') {
        event.preventDefault();
        event.stopPropagation();
        this.$navigationUtils.focusMenuEntry(this.$refs.menu, entries[target]);
      } else if (event.key === 'Escape' || event.key === closeKey) {
        // Vuetify's menu would give the focus back to its activator, the entry's inner block, which cannot take it
        event.preventDefault();
        event.stopPropagation();
        this.showMenu = false;
        (this.subMenuOpener || this.$refs.entry?.$el)?.focus?.();
      } else if (event.key === 'Tab') {
        this.closeMenu();
      }
    },
    // the focus goes back to what opened the sub-menu when it closes: the entry, or the chevron of a page link, never
    // the chevron of an entry that only groups pages, hidden from the keyboard and the screen readers
    openSubMenu(opener) {
      this.subMenuOpener = opener;
      this.showMenu = true;
      // once the sub-menu content is displayed
      window.setTimeout(() => {
        this.$navigationUtils.focusMenuEntry(this.$refs.menu, this.$navigationUtils.menuEntries(this.$refs.menu?.$refs?.content)[0]);
      }, 50);
    },
    pointerListeners(listeners) {
      const { keydown, ...pointer } = listeners || {}; // eslint-disable-line no-unused-vars
      return pointer;
    },
    toggleFromChevron() {
      if (this.showMenu) {
        this.showMenu = false;
      } else {
        this.openSubMenu(this.hasPage ? this.$refs.chevron?.$el : this.$refs.entry?.$el);
      }
    },
    checkLink(e) {
      if (!this.navigationNodeUri) {
        e.stopPropagation();
        e.preventDefault();
        // activated from the keyboard or a screen reader: the pointer opens it through the inner block
        if (!this.hasPage && this.opensSubMenu && !this.$refs.activator?.contains?.(e.target)) {
          this.openSubMenu(this.$refs.entry?.$el);
        }
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
    // The chosen entry of the sub-menu keeps every menu up to the bar open, and the focus, while it replaces the page:
    // see NavigationUtils.js
    onSubMenuClick(event) {
      const entry = event.target?.closest?.('a.v-list-item[href]');
      if (!entry) {
        // an entry that only groups others opens its own sub-menu
        return;
      }
      if (this.$navigationUtils.replacesPage(entry, event)) {
        this.$emit('leaving-page');
        // Vuetify's list item blurs itself on a click that has a detail
        entry.focus();
        return;
      }
      this.closeMenu();
    },
    // an entry that leaves the page in place, and Tab, close every menu up to the bar
    closeMenu() {
      this.showMenu = false;
      this.$emit('close-menu');
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
    handleCloseSiblingMenus(emitter) {
      if (!this.showMenu || !emitter) {
        return;
      }
      if (emitter === this) {
        return;
      }
      const myUri = this.navigationNodeUri;
      const emitterUri = emitter.navigationNodeUri;
      if (myUri && emitterUri && (myUri.startsWith(emitterUri) || emitterUri.startsWith(myUri))) {
        return;
      }
      if ((!myUri || emitter && myUri && !myUri.includes(emitter.baseSiteUri)) && this.isDescendant(emitter.navigation, this.navigation)) {
        return;
      }
      this.showMenu = false;
    },
    isDescendant(childNav, parentNav) {
      if (!parentNav?.children) {
        return false;
      }
      for (const child of parentNav.children) {
        if (child === childNav) {
          return true;
        }
        if (this.isDescendant(childNav, child)) {
          return true;
        }
      }
      return false;
    },
    updateSize() {
      this.updatePosition();
    },
    // A sub-menu as wide as this list that does not fit on the right of the entry opens on its left: pushed back into
    // the window, it would cover the entry and the list the pointer comes from (a zoomed page, a menu near the edge)
    updatePosition() {
      const rect = this.$el.getBoundingClientRect();
      this.openLeft = !this.$vuetify.rtl && rect.right + rect.width > window.innerWidth;
      this.positionX = this.openLeft ? rect.left : rect.right;
      this.positionY = rect.top;
    },
  }
};
</script>