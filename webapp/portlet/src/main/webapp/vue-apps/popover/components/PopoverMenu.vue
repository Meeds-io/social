<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
  <v-menu
    v-if="menu"
    v-model="menu"
    rounded="rounded"
    role="tooltip"
    :close-on-content-click="false"
    :position-x="offsetX"
    :position-y="offsetY"
    transition="slide-x-transition"
    absolute
    :top="top"
    content-class="profile-popover-menu pa-1 transparent"
    elevation="0"
    max-width="350"
    min-width="350">
    <user-popover-content v-if="isUserIdentity" :identity="identity" />
    <space-popover-content v-if="isSpaceIdentity" :space="space" />
  </v-menu>
</template>

<script>

export default {
  data() {
    return {
      top: true,
      menu: false,
      element: null,
      popoverCloseDelay: 1000,
      popoverOpenDelay: 300,
      menuCloseTimer: null,
      menuOpenTimer: null,
      isMenuHovered: false,
      isActivatorHovered: false,
      offsetX: false,
      offsetY: true,
      identity: null,
      space: null,
      identityType: null,
    };
  },
  computed: {
    isUserIdentity() {
      return this.identityType && this.identityType === 'User';
    },
    isSpaceIdentity() {
      return this.identityType && this.identityType === 'Space';
    },
  },
  watch: {
    menu() {
      if (this.menu) {
        this.clearCloseTime();
        this.registerActivatorElementEvents();
        document.dispatchEvent(new CustomEvent(`${this.identityType}PopoverOpened`));
      } else {
        this.unregisterActivatorElementEvents();
      }
    },
  },
  created() {
    document.addEventListener('popover-identity-display', event => {
      const data = event?.detail;
      this.identityType = data.identityType;
      this.top = data.top;
      if (this.isUserIdentity) {
        this.identity = {
          id: data?.id,
          enabled: data?.enabled,
          deleted: data?.deleted,
          username: data?.username,
          fullname: data?.fullName,
          avatar: data?.avatar,
          position: data?.position,
          external: data?.external,
        };
        localStorage.setItem('popover-identity-type', 'USER_TIPTIP');
      } else if (this.isSpaceIdentity) {
        this.space = data;
        localStorage.setItem('popover-identity-type', 'SPACE_TIPTIP');
      }

      if (this.menu && this.element === data?.element) {
        this.clearCloseTime();
      } else {
        this.unregisterActivatorElementEvents();
        this.$nextTick().then(() => {
          this.offsetX = data?.offsetX;
          this.offsetY = data?.offsetY;
          this.element = data?.element;
          this.setActivatorHovered();
          this.registerActivatorElementEvents();
          this.setPopoverNotHovered();
          this.openPopover();
        });
      }
    });
    document.addEventListener('drawerOpened', () => this.closePopover(true));
    document.addEventListener('drawerClosed', () => this.closePopover(true));
    document.addEventListener('modalOpened', () => this.closePopover(true));
    document.addEventListener('modalClosed', () => this.closePopover(true));
    document.addEventListener('popover-identity-hide', () => this.closePopover(true));
    window.addEventListener('keydown', (event) => {
      if (event.key === 'Escape') {
        this.closePopover(true);
      }
    });
    this.$root.$on('popover-hovered', this.setPopoverHovered);
    this.$root.$on('popover-not-hovered', this.setPopoverNotHovered);
  },
  mounted() {
    // Force to close user popover when clicking outside
    $(document).on('click', (event) => {
      if (this.menu && event.target && !$(event.target).parents('#identity-popover').length) {
        this.closePopover(true);
      }
      if (this.menu) {
        // workaround for menu v-select absolute content that must be displayed inside the menu
        $('.profile-popover-menu').css('height', '100%');
      }
    });
    // Force to close user popover when scrolling
    document.addEventListener('scroll', this.onScroll, true);
  },
  methods: {
    onScroll(){
      this.closePopover(true);
    },
    registerActivatorElementEvents() {
      if (this.element) {
        $(this.element)
          .on('mouseenter', this.setActivatorHovered)
          .on('mouseleave', this.setActivatorNotHovered);
      }
    },
    unregisterActivatorElementEvents() {
      if (this.element) {
        $(this.element)
          .off('mouseenter', this.setActivatorHovered)
          .off('mouseleave', this.setActivatorNotHovered);
        this.setActivatorNotHovered();
        this.element = null;
      }
    },
    setActivatorHovered() {
      this.isActivatorHovered = true;
    },
    setActivatorNotHovered() {
      this.isActivatorHovered = false;
      this.closePopover();
    },
    setPopoverHovered() {
      this.isMenuHovered = true;
    },
    setPopoverNotHovered() {
      this.isMenuHovered = false;
      this.closePopover(true);
    },
    openPopover(immediatly) {
      if (!this.menu) {
        if (immediatly) {
          this.menu = true;
        } else {
          this.menuOpenTimer = window.setTimeout(() => {
            if (this.isActivatorHovered) {
              this.menu = true;
            }
          }, this.popoverOpenDelay);
        }
      }
    },
    closePopover(immediatly) {
      if (this.menu) {
        if (immediatly) {
          this.menu = false;
          localStorage.removeItem('popover-identity-type');
        } else {
          this.menuCloseTimer = window.setTimeout(() => {
            if (!this.isMenuHovered && !this.isActivatorHovered) {
              this.menu = false;
              localStorage.removeItem('popover-identity-type');
            }
          }, this.popoverCloseDelay);
        }
      }
    },
    clearCloseTime() {
      if (this.menuCloseTimer) {
        window.clearTimeout(this.menuCloseTimer);
        this.menuCloseTimer = null;
      }
    },
  }
};
</script>
