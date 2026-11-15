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
  <v-app
    color="transaprent"
    class="HamburgerNavigationMenu"
    flat>
    <v-hover v-model="$root.hoverButton">
      <sidebar-button
        :unread-per-space="unreadPerSpace"
        :expanded="firstLevelDrawer"
        @open-drawer="openFirstLevel" />
    </v-hover>
    <div
      class="layout-side-bar"
      @keydown.esc="closeOnEscape"
      @keydown.tab="followLevelsFocusOrder"
      @focusin="$root.focusMenu = hasKeyboardFocus()"
      @focusout="closeOnFocusLeave">
      <template v-if="$root.displaySequentially">
        <sidebar-third-level
          v-if="allowDisplayLevels"
          v-model="thirdLevelDrawer"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth" />
        <sidebar-second-level
          v-if="allowDisplayLevels"
          v-model="secondLevelDrawer"
          :second-level="secondLevel"
          :third-level-drawer="thirdLevelDrawer"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth"
          :site="site" />
        <sidebar-first-level
          :first-level-drawer="firstLevelDrawer"
          :second-level-drawer="secondLevelDrawer"
          :third-level-drawer="thirdLevelDrawer"
          :second-level="secondLevel"
          :opened-site="site"
          :opened-space="space"
          :drawer-width="drawerWidth"
          @firstLevelDrawer="updateFirstLevelDrawer($event)"
          @sticky-tab="followLevelsFocusOrder"
          @sticky-escape="closeOnEscape"
          @sticky-focusin="$root.focusMenu = hasKeyboardFocus()"
          @sticky-focusout="closeOnFocusLeave" />
        <v-overlay
          v-if="showInnerOverlay"
          absolute />
      </template>
      <template v-else>
        <sidebar-first-level
          :first-level-drawer="firstLevelDrawer"
          :second-level-drawer="secondLevelDrawer"
          :third-level-drawer="thirdLevelDrawer"
          :second-level="secondLevel"
          :opened-site="site"
          :opened-space="space"
          :drawer-width="drawerWidth"
          @firstLevelDrawer="updateFirstLevelDrawer($event)"
          @sticky-tab="followLevelsFocusOrder"
          @sticky-escape="closeOnEscape"
          @sticky-focusin="$root.focusMenu = hasKeyboardFocus()"
          @sticky-focusout="closeOnFocusLeave" />
        <sidebar-second-level
          v-if="allowDisplayLevels"
          v-model="secondLevelDrawer"
          :second-level="secondLevel"
          :third-level-drawer="thirdLevelDrawer"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth"
          :site="site" />
        <sidebar-third-level
          v-if="allowDisplayLevels"
          v-model="thirdLevelDrawer"
          :opened-space="space"
          :home-link="homeLink"
          :drawer-width="drawerWidth" />
      </template>
    </div>
  </v-app>
</template>
<script>
// the first level's own card: in sticky display its menu content, which holds the card, is moved out of this
// component to the page's #ParentSiteStickyMenu, away from the element carrying the first level's class
const FIRST_LEVEL = '.HamburgerMenuFirstLevel';
const SECOND_LEVEL = '.HamburgerMenuSecondLevelParent';
const THIRD_LEVEL = '.HamburgerMenuThirdLevelParent';
const LEVELS = `.HamburgerMenuFirstLevelParent, ${FIRST_LEVEL}, ${SECOND_LEVEL}, ${THIRD_LEVEL}`;
const FOCUSABLE_SELECTOR = 'a[href]:not([tabindex="-1"]), button:not([disabled]):not([tabindex="-1"]), input:not([disabled]):not([type="hidden"]):not([tabindex="-1"]), [tabindex="0"]';

/** The side menu's levels and the space menu shown over the page from its space panel, which belongs to it. */
const MENU_PARTS = `${LEVELS}, .space-hamburger-action-menu`;

export default {
  data: () => ({
    secondLevelOpener: null,
    thirdLevelOpener: null,
    firstLevelDrawer: false,
    secondLevelDrawer: false,
    thirdLevelDrawer: false,
    secondLevel: null,
    drawerWidth: 310,
    space: null,
    site: null,
    initStep: 0,
    limit: 7,
    offset: 0,
    unreadPerSpace: null,
    interval: null,
    mouseEvent: false,
    pointerUse: false,
    closeTimeout: null,
    visibility: ['displayed', 'temporal']
  }),
  computed: {
    allowDisplayLevels() {
      return this.stickyDisplay || this.firstLevelDrawer;
    },
    levelsOpened() {
      return this.secondLevelDrawer || this.thirdLevelDrawer;
    },
    showOverlay() {
      return this.stickyDisplay && this.levelsOpened;
    },
    stickyDisplay() {
      return this.$root.sticky;
    },
    iconDisplay() {
      return this.$root.icon;
    },
    hiddenDisplay() {
      return this.$root.hidden;
    },
    hover() {
      return this.$root.hover;
    },
    mode() {
      return this.$root.mode;
    },
    showInnerOverlay() {
      return this.$root.sticky && (this.secondLevelDrawer || this.thirdLevelDrawer);
    },
  },
  watch: {
    showOverlay() {
      if (this.showOverlay) {
        document.dispatchEvent(new CustomEvent('drawerOpened', {detail: true}));
      } else {
        document.dispatchEvent(new CustomEvent('drawerClosed', {detail: true}));
      }
    },
    iconDisplay: {
      immediate: true,
      handler() {
        if (this.$root.icon) {
          this.firstLevelDrawer = true;
        }
      },
    },
    secondLevelDrawer() {
      if (!this.$root.displaySequentially && document.querySelector('.v-overlay--active')) {
        if (this.secondLevelDrawer) {
          document.querySelector('.v-overlay--active').addEventListener('click', this.closeLevelsDrawer);
        } else {
          document.querySelector('.v-overlay--active').removeEventListener('click', this.closeLevelsDrawer);
        }
      }
      if (!this.secondLevelDrawer) {
        this.secondLevelOpener = null;
        this.thirdLevelOpener = null;
        this.thirdLevelDrawer = false;
        this.space = null;
        this.site = null;
        this.secondLevel = null;
        this.resetOpenedSpacesLevel();
      }
    },
    // a panel opened, or showing another entry, from the keyboard: the focus follows into it. The kind of
    // content, the site or the space it shows, and the third level each change on their own
    secondLevel() {
      if (this.secondLevel) {
        this.focusOpenedLevelFromKeyboard();
      }
    },
    thirdLevelDrawer() {
      if (this.thirdLevelDrawer) {
        this.focusOpenedLevelFromKeyboard();
      } else {
        this.thirdLevelOpener = null;
      }
    },
    firstLevelDrawer() {
      if (!this.firstLevelDrawer) {
        this.restoreFocusToButton();
      }
      if (!this.firstLevelDrawer && !this.stickyDisplay) {
        this.thirdLevelDrawer = false;
        this.secondLevelDrawer = false;
        this.space = null;
        this.site = null;
        this.secondLevel = null;
      } else if (this.firstLevelDrawer && this.$root.displaySequentially && this.mouseEvent) {
        // Close if mouse is not entered to menu
        this.closeTimeout = window.setTimeout(() => {
          if (!this.hover) {
            this.closeMenu();
          }
        }, 500);
      }
    },
    stickyDisplay() {
      this.closeMenu();
      if (this.stickyDisplay) {
        document.body.className = `${document.body.className.replace('HamburgerMenuSticky', '')  } HamburgerMenuSticky`;
      } else {
        document.body.className = document.body.className.replace('HamburgerMenuSticky', '');
      }
    },
    hover() {
      if (this.hover) {
        if (this.interval) {
          window.clearInterval(this.interval);
          this.interval = null;
        }
      } else if (!this.interval && this.$root.displaySequentially && !this.$root.hidden) {
        this.interval = window.setTimeout(() => this.closeMenu(), 500);
      }
    },
    site() {
      this.$root.openedSiteName = this.site?.name;
      if (this.site) {
        this.focusOpenedLevelFromKeyboard();
      }
    },
    space() {
      this.$root.openedSpaceId = this.space?.id;
      if (this.space) {
        this.focusOpenedLevelFromKeyboard();
      }
    },
    mode(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.closeMenuEffectively(true);
      }
    },
  },
  created() {
    this.$root.$on('change-space-menu', this.changeSpaceMenu);
    this.$root.$on('change-spaces-menu', this.changeSpacesMenu);
    this.$root.$on('change-site-menu', this.changeSiteMenu);
    document.addEventListener('closeDisplayedDrawer', this.closeDisplayedDrawer);
    document.addEventListener('drawerOpened', this.closeDisplayedDrawerIfNotSelf);
    document.addEventListener('pointerdown', this.trackPointerUse, true);
    document.addEventListener('mousedown', this.trackPointerUse, true);
  },
  beforeDestroy() {
    document.removeEventListener('pointerdown', this.trackPointerUse, true);
    document.removeEventListener('mousedown', this.trackPointerUse, true);
    this.$root.$off('change-space-menu', this.changeSpaceMenu);
    this.$root.$off('change-spaces-menu', this.changeSpacesMenu);
    this.$root.$off('change-site-menu', this.changeSiteMenu);
    document.removeEventListener('closeDisplayedDrawer', this.closeDisplayedDrawer);
    document.removeEventListener('drawerOpened', this.closeDisplayedDrawerIfNotSelf);
  },
  methods: {
    async openFirstLevel(mouseEvent) {
      this.mouseEvent = !!mouseEvent && this.pointerUse;
      this.firstLevelDrawer = false;
      await this.$nextTick();

      window.clearTimeout(this.closeTimeout);
      window.clearInterval(this.interval);
      this.firstLevelDrawer = true;
      this.moveFocusToMenu();
      if (this.$root.allowClosing) {
        this.$root.allowClosing = false;
        this.closeTimeout = window.setTimeout(() => this.$root.allowClosing = true, 2000);
      }
    },
    moveFocusToMenu() {
      if (this.$root.sticky || this.$root.icon) {
        return;
      }
      this.focusReturnPending = true;
      window.setTimeout(() => {
        const menu = document.querySelector('#HamburgerMenuNavigation');
        const target = menu?.querySelector?.('a[href], button:not([disabled]), [tabindex="0"]');
        if (target) {
          target.focus();
        }
      }, 400);
    },
    restoreFocusToButton() {
      if (!this.focusReturnPending) {
        return;
      }
      this.focusReturnPending = false;
      const active = document.activeElement;
      if (!active || active === document.body
          || active.closest?.('.HamburgerMenuFirstLevelParent, .HamburgerMenuSecondLevelParent, .HamburgerMenuThirdLevelParent')) {
        this.$el?.querySelector?.('.HamburgerNavigationMenuLink')?.focus?.();
      }
    },
    async changeSpacesMenu(spaceTemplateId, spaceCategoryId, spacesUrl, sortBy, name, type) {
      this.site = null;
      if (this.secondLevel === 'spaces'
          && ((spaceTemplateId && this.$root.openedSpaceTemplateId === spaceTemplateId)
              || (spaceCategoryId && this.$root.openedSpaceCategoryId === spaceCategoryId)
              || (spacesUrl && this.$root.openedSpaces))) {
        this.space = null;
        this.secondLevel = null;
        this.secondLevelDrawer = false;
        this.thirdLevelDrawer = false;
        window.setTimeout(() => {
          this.$root.openedFirstLevelType = null;
          this.$root.openedSpaceTemplateId = null;
          this.$root.openedSpaceCategoryId = null;
          this.$root.openedSpaceTemplateName = null;
          this.$root.openedSpaceCategoryName = null;
          this.$root.openedItem = null;
          this.$root.openedSpaces = false;
          this.$root.spacesSortBy = null;
        }, 50);
      } else {
        if (this.secondLevel) {
          this.secondLevel = null;
          await this.$nextTick();
        }
        this.space = null;
        this.$root.openedFirstLevelType = type;
        this.$root.openedSpaceTemplateId = spaceTemplateId;
        this.$root.openedSpaceCategoryId = spaceCategoryId;
        this.$root.openedSpaceTemplateName = spaceTemplateId && name || null;
        this.$root.openedSpaceCategoryName = spaceCategoryId && name || null;
        this.$root.openedSpaces = !spaceTemplateId && !spaceCategoryId;
        this.$root.openedSpacesUrl = spacesUrl;
        this.$root.spacesSortBy = sortBy;
        this.secondLevel = 'spaces';
        this.secondLevelDrawer = true;
        this.thirdLevelDrawer = false;
      }
    },
    changeSpaceMenu(space, thirdLevel) {
      this.site = null;
      if (!thirdLevel && this.secondLevel === 'spaces') {
        this.space = space;
        this.secondLevel = 'spaceMenu';
        this.collapseSpacesEntries();
        this.secondLevelDrawer = true;
        this.thirdLevelDrawer = false;
      } else if (this.space?.id === space?.id) {
        this.space = null;
        if (this.thirdLevelDrawer) {
          this.thirdLevelDrawer = false;
        } else {
          this.secondLevelDrawer = false;
        }
      } else {
        this.space = space;
        if (this.secondLevel === 'spaces') {
          this.thirdLevelDrawer = true;
        } else {
          this.secondLevel = 'spaceMenu';
          this.collapseSpacesEntries();
          this.secondLevelDrawer = true;
          this.thirdLevelDrawer = false;
        }
      }
      if (!thirdLevel) {
        this.$root.openedFirstLevelType = 'SPACE';
      }
    },
    changeSiteMenu(site) {
      this.space = null;
      if (this.site?.name === site.name) {
        this.secondLevel = null;
        this.secondLevelDrawer = false;
        this.thirdLevelDrawer = false;
        this.site= null;
      } else {
        this.site = site;
        this.secondLevel = 'site';
        this.$root.openedFirstLevelType = 'SITE';
        this.collapseSpacesEntries();
        this.secondLevelDrawer = true;
        this.thirdLevelDrawer = false;
      }
    },
    // the second level shows a site or a space instead of a spaces list: the spaces, template or category entry
    // that showed the list before is no longer expanded
    collapseSpacesEntries() {
      this.$root.openedSpaceTemplateId = null;
      this.$root.openedSpaceCategoryId = null;
      this.$root.openedSpaceTemplateName = null;
      this.$root.openedSpaceCategoryName = null;
      this.$root.openedSpaces = false;
    },
    closeDisplayedDrawerIfNotSelf(event) {
      if (!event?.detail) {
        this.closeMenuEffectively(true);
      }
    },
    closeDisplayedDrawer() {
      if (this.firstLevelDrawer || this.secondLevelDrawer) {
        this.closeMenu();
      }
    },
    updateFirstLevelDrawer(drawer) {
      if (this.$root.icon && this.$root.stickyAllowed) {
        this.firstLevelDrawer = true;
      } else {
        this.firstLevelDrawer = drawer;
      }
    },
    closeMenu() {
      if (!this.$root.allowClosing) {
        this.interval = window.setTimeout(() => this.closeMenu(), 500);
        return;
      }
      // the menu stays open while it is used, with the mouse or with the keyboard
      if (!this.hover && !this.hasKeyboardFocus()) {
        this.closeMenuEffectively();
      }
    },
    // the deepest level open once the panels have moved in
    focusOpenedLevelFromKeyboard() {
      if (!this.hasKeyboardFocus()) {
        // opened, or switched to another entry, with the mouse: the entry recorded earlier no longer opens it
        if (this.thirdLevelDrawer) {
          this.thirdLevelOpener = null;
        } else {
          this.secondLevelOpener = null;
        }
      } else {
        this.recordLevelOpener(document.activeElement);
        window.setTimeout(() => {
          const level = (this.thirdLevelDrawer && THIRD_LEVEL)
            || (this.secondLevelDrawer && SECOND_LEVEL);
          if (level) {
            // the first control shown: a hidden one (v-show) is still matched, and does not take the focus
            Array.from(document.querySelector(level)?.querySelectorAll?.('a[href], button:not([disabled]), [tabindex="0"]') || [])
              .find(control => control.getClientRects().length)
              ?.focus();
          }
        }, 400);
      }
    },
    // a mouse press whose target is not under the pointer comes from a screen reader's activation, which focuses
    // the control and then presses it where it is (seen with Chrome's accessibility default action: a mouse
    // pointerdown and mousedown, the target not hovered): only a press under the pointer, or a touch, is the
    // user's pointer. Both events are tracked, for an activation that sends no pointer event; the mouse press a
    // tap is followed by, when the browser marks it so, leaves the touch's decision as it is
    trackPointerUse(event) {
      if (event.type === 'mousedown' && event.sourceCapabilities?.firesTouchEvents) {
        return;
      }
      this.pointerUse = (!!event.pointerType && event.pointerType !== 'mouse') || !!event.target?.matches?.(':hover');
      this.$root.focusMenu = this.hasKeyboardFocus();
    },
    // focus inside the menu not left there by the mouse: a clicked entry keeps the focus too, without :focus-visible,
    // and a screen reader's activation leaves it there without :focus-visible as well
    hasKeyboardFocus() {
      const active = document.activeElement;
      return !!active?.closest?.(LEVELS)
        && (!this.pointerUse || !!active.matches?.(':focus-visible'));
    },
    // A second level closed by any way (Escape, the overlay, the menu closing) leaves its spaces, template or
    // category entry collapsed, as a second click on its arrow does: the entries build their expanded state
    // from these fields. Delayed as that click does, while the level slides out; kept when a level reopened
    resetOpenedSpacesLevel() {
      window.setTimeout(() => {
        if (this.secondLevelDrawer) {
          return;
        }
        this.$root.openedFirstLevelType = null;
        this.$root.openedSpaceTemplateId = null;
        this.$root.openedSpaceCategoryId = null;
        this.$root.openedSpaceTemplateName = null;
        this.$root.openedSpaceCategoryName = null;
        this.$root.openedItem = null;
        this.$root.openedSpaces = false;
        this.$root.spacesSortBy = null;
      }, 50);
    },
    // the entry that opened a level from the keyboard: the focus goes back to it when the level closes, and the
    // level comes right after it in the focus order
    recordLevelOpener(active) {
      if (this.thirdLevelDrawer && active?.closest?.(SECOND_LEVEL)) {
        this.thirdLevelOpener = active;
      } else if (this.secondLevelDrawer && active?.closest?.(FIRST_LEVEL)) {
        this.secondLevelOpener = active;
      }
    },
    // the recorded opener while it still shows its level open, else the level's expanded button (a level opened,
    // or switched to another entry, with the mouse)
    levelOpener(recorded, parentLevel) {
      if (recorded?.isConnected && parentLevel?.contains?.(recorded) && recorded.getAttribute('aria-expanded') !== 'false') {
        return recorded;
      }
      return parentLevel?.querySelector?.('button[aria-expanded="true"]') || null;
    },
    // the live first level: in sticky display the page first shows a copy of the menu cached from the previous
    // page in the same container, removed once the menu is mounted, and the live content is appended after it
    firstLevelElement() {
      return Array.from(document.querySelectorAll(FIRST_LEVEL)).pop() || null;
    },
    levelFocusables(level) {
      return Array.from(level?.querySelectorAll?.(FOCUSABLE_SELECTOR) || [])
        .filter(element => element.getClientRects().length && window.getComputedStyle(element).visibility !== 'hidden');
    },
    // Escape closes the level holding the focus, and the focus goes back to the entry that opened it; on the
    // first level it closes the menu
    closeOnEscape(event) {
      const target = event?.target;
      if (target?.closest?.('[aria-haspopup="true"][aria-expanded="true"]')) {
        // the button of a menu open in a level (a space's actions): Vuetify closes that menu alone
        return;
      }
      if (this.thirdLevelDrawer && target?.closest?.(THIRD_LEVEL)) {
        this.closeLevelToOpener(event, true);
      } else if (this.secondLevelDrawer && target?.closest?.(SECOND_LEVEL)) {
        this.closeLevelToOpener(event, false);
      } else if (this.$root.sticky) {
        // the sticky first level stays: Escape on it closes the levels open beside it, the focus kept
        if (this.secondLevelDrawer) {
          event.preventDefault();
          event.stopPropagation();
          this.secondLevelDrawer = false;
        }
      } else if (this.firstLevelDrawer || this.secondLevelDrawer || this.thirdLevelDrawer) {
        this.closeMenuEffectively();
      }
    },
    closeLevelToOpener(event, thirdLevel) {
      // stopped here: the drawers' overlay closes the displayed drawer on an Escape reaching the document
      event.preventDefault();
      event.stopPropagation();
      const parentLevel = thirdLevel ? document.querySelector(SECOND_LEVEL) : this.firstLevelElement();
      const opener = this.levelOpener(thirdLevel ? this.thirdLevelOpener : this.secondLevelOpener, parentLevel)
        || this.levelFocusables(parentLevel)[0];
      if (thirdLevel) {
        // as a second click on the space's arrow does
        this.space = null;
        this.thirdLevelDrawer = false;
      } else {
        this.secondLevelDrawer = false;
      }
      this.$nextTick(() => opener?.focus?.());
    },
    // While a second level is open, Tab follows the levels as a tree: an open level comes right after the entry
    // that opened it, so the end of the second level leads to the entry after its opener in the first level,
    // and the focus cycles within the menu instead of reaching the page behind it. The levels are not in that
    // order in the page: shown side by side, the second level comes before the first one, and in sticky display
    // the first level is elsewhere in the page. In the narrow display the levels are stacked at the same place,
    // the deepest one covering the others: the focus then cycles within that one
    followLevelsFocusOrder(event) {
      if (!this.secondLevelDrawer) {
        return;
      }
      const order = this.$root.displaySequentially ? this.levelsFocusOrder() : this.levelFocusables(
        document.querySelector(this.thirdLevelDrawer ? THIRD_LEVEL : SECOND_LEVEL));
      const index = order.indexOf(event.target);
      if (index < 0 || order.length < 2) {
        return;
      }
      event.preventDefault();
      order[(index + (event.shiftKey ? -1 : 1) + order.length) % order.length].focus();
    },
    levelsFocusOrder() {
      const firstLevel = this.firstLevelElement();
      const secondLevel = document.querySelector(SECOND_LEVEL);
      const thirdLevel = this.thirdLevelDrawer && document.querySelector(THIRD_LEVEL);
      const secondLevelOrder = this.insertAfterOpener(this.levelFocusables(secondLevel),
        thirdLevel ? this.levelFocusables(thirdLevel) : [],
        this.levelOpener(this.thirdLevelOpener, secondLevel));
      return this.insertAfterOpener(this.levelFocusables(firstLevel),
        secondLevelOrder,
        this.levelOpener(this.secondLevelOpener, firstLevel));
    },
    insertAfterOpener(level, subLevel, opener) {
      if (!subLevel.length) {
        return level;
      }
      const index = level.indexOf(opener);
      return index < 0 ? [...level, ...subLevel] : [...level.slice(0, index + 1), ...subLevel, ...level.slice(index + 1)];
    },
    // a menu shown over the page closes once the keyboard focus leaves all its levels; the space menu its space panel opens counts as one of them
    closeOnFocusLeave(event) {
      const target = event.relatedTarget;
      if (!target?.closest?.(MENU_PARTS)) {
        this.$root.focusMenu = false;
      }
      if (this.$root.sticky || this.$root.icon || !target || target.closest?.(MENU_PARTS)) {
        return;
      }
      if (this.firstLevelDrawer || this.secondLevelDrawer || this.thirdLevelDrawer) {
        this.focusReturnPending = false;
        this.closeMenuEffectively();
      }
    },
    closeMenuEffectively(force) {
      if (force && this.$root.icon) {
        this.$root.hoverMenu = false;
        this.$root.hoverThirdLevel = false;
        this.$root.hoverSecondLevel = false;
        this.$root.hoverFirstLevel = false;
        this.$root.hoverDeferred = false;
      } else {
        this.updateFirstLevelDrawer(false);
      }
      this.secondLevelDrawer = false;
      this.thirdLevelDrawer = false;
      this.space = null;
      this.site = null;
      this.secondLevel = null;
    },
    closeLevelsDrawer(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (this.thirdLevelDrawer) {
        this.thirdLevelDrawer = false;
      } else {
        this.secondLevelDrawer = false;
      }
    },
  },
};
</script>
