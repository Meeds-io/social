/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import './initComponents.js';
import './services.js';
import './extensions.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('HamburgerMenu');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vuetify.prototype.preset = eXo.env.portal.vuetifyPreset;

const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
const url = `/social/i18n/locale.portal.HamburgerMenu?lang=${lang}`;

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const appId = 'HamburgerNavigationMenu';

export function init(
  mode,
  defaultUserPath,
  unreadPerSpace,
  avatarUrl,
  isExternalFeatureEnabled,
  allowUserHome) {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      // init Vue app when locale ressources are ready
      Vue.createApp({
        data: {
          defaultUserPath,
          unreadPerSpace,
          avatarUrl,
          mode,
          isExternalFeatureEnabled,
          allowUserHome,
          allowClosing: true,
          hoverFirstLevel: false,
          hoverSecondLevel: false,
          hoverThirdLevel: false,
          hoverMenu: false,
          hoverButton: false,
          openedSiteName: null,
          openedSpaceId: null,
          openedSpaces: false,
          openedSpacesUrl: null,
          openedSpaceTemplateId: null,
          openedSpaceTemplateName: null,
          openedSpaceCategoryId: null,
          openedSpaceCategoryName: null,
          openedItem: null,
          openedFirstLevelType: null,
          sites: null,
          settings: null,
          spaceTemplates: null,
          openedOverlay: false,
          hoverDeferred: false,
        },
        computed: {
          isMobile() {
            return this.$vuetify.breakpoint.width < this.$vuetify.breakpoint.thresholds.md;
          },
          autoSwitchToIcon() {
            return this.mode === 'STICKY'
              && this.allowIcon
              && (this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.md)
              && (this.$vuetify.breakpoint.width < this.$vuetify.breakpoint.thresholds.lg);
          },
          stickyAllowed() {
            return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.md;
          },
          displaySequentially() {
            return this.stickyAllowed;
          },
          hidden() {
            return !this.stickyAllowed || this.mode === 'HIDDEN';
          },
          sticky() {
            return !this.hidden && (this.mode === 'STICKY' && !this.autoSwitchToIcon);
          },
          icon() {
            return !this.hidden && (this.mode === 'ICON' || this.autoSwitchToIcon);
          },
          allowedModes() {
            return this.settings?.allowedModes || [];
          },
          allowSticky() {
            return this.allowedModes.includes('STICKY');
          },
          allowIcon() {
            return this.allowedModes.includes('ICON');
          },
          allowHidden() {
            return this.allowedModes.includes('HIDDEN');
          },
          expand() {
            return !this.icon || this.hoverDeferred || !this.allowClosing;
          },
          iconExpand() {
            return this.icon && this.expand;
          },
          iconCollapse() {
            return this.icon && !this.expand;
          },
          hover() {
            return this.hoverMenu
              || this.hoverButton
              || this.hoverFirstLevel
              || this.hoverSecondLevel
              || this.hoverThirdLevel;
          },
          hoverSidebar() {
            return this.hoverMenu
              || this.hoverFirstLevel
              || this.hoverSecondLevel
              || this.hoverThirdLevel;
          },
        },
        watch: {
          expand() {
            if (this.icon) {
              window.setTimeout(() => {
                if (this.expand && !this.openedOverlay) {
                  document.dispatchEvent(new CustomEvent('drawerOpened', {detail: true}));
                  this.openedOverlay = true;
                } else if (!this.expand && this.openedOverlay) {
                  document.dispatchEvent(new CustomEvent('drawerClosed', {detail: true}));
                  this.openedOverlay = false;
                }
              }, 200);
            }
          },
          hover: {
            immediate: true,
            handler() {
              if (this.hover) {
                this.hoverDeferred = true;
              } else {
                window.setTimeout(() => {
                  if (!this.hover) {
                    this.hoverDeferred = false;
                  }
                }, 200);
              }
            },
          },
          icon: {
            immediate: true,
            handler() {
              this.updateParentStyle();
              if (this.hover) {
                this.openedOverlay = true;
              } else {
                window.setTimeout(() => {
                  document.dispatchEvent(new CustomEvent('drawerClosed'));
                }, 300);
              }
            },
          },
          sticky() {
            if (this.sticky) {
              window.setTimeout(() => {
                document.dispatchEvent(new CustomEvent('drawerClosed'));
              }, 300);
            }
          },
          hidden() {
            if (!this.hidden) {
              if (eXo.openedDrawers?.find?.(d => d?.$el?.classList?.contains('HamburgerMenuFirstLevelParent'))) {
                eXo.openedDrawers = eXo.openedDrawers.filter(d => !d?.$el?.classList?.contains('HamburgerMenuFirstLevelParent'));
              }
            }
          },
          mode() {
            document.dispatchEvent(new CustomEvent('sidebar-mode-changed', {detail: this.mode}));
          },
        },
        created() {
          this.$root.$on('dialog-opened', () => this.$root.allowClosing = false);
          this.$root.$on('dialog-closed', () => window.setTimeout(() => {
            this.$root.allowClosing = true;
            this.hoverDeferred = false;
          }, 200));
          this.$root.$on('menu-opened', () => this.$root.allowClosing = false);
          this.$root.$on('menu-closed', () => this.$root.allowClosing = true);
          document.addEventListener('homeLinkUpdated', this.updateUserHome);
          document.addEventListener('space-settings-updated', this.init);
          this.init();
        },
        mounted() {
          document.addEventListener('notification.unread.item', this.handleUpdatesFromWebSocket);
          document.addEventListener('notification.read.item', this.handleUpdatesFromWebSocket);
          document.addEventListener('notification.read.allItems', this.handleUpdatesFromWebSocket);

          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        },
        beforeDestroy() {
          document.removeEventListener('notification.unread.item', this.handleUpdatesFromWebSocket);
          document.removeEventListener('notification.read.item', this.handleUpdatesFromWebSocket);
          document.removeEventListener('notification.read.allItems', this.handleUpdatesFromWebSocket);
          document.removeEventListener('space-settings-updated', this.init);
        },
        methods: {
          async init() {
            try {
              this.settings = await this.$navigationSettingService.getSidebarConfiguration();
            } finally {
              this.$root.$applicationLoaded();
            }
          },
          updateParentStyle() {
            if (document.querySelector('#UISiteBody')?.style) {
              if (this.icon) {
                document.querySelector('#UISiteBody').style[this.$vuetify.rtl && 'marginRight' || 'marginLeft'] = '70px';
              } else {
                document.querySelector('#UISiteBody').style[this.$vuetify.rtl && 'marginRight' || 'marginLeft'] = '';
              }
            } else {
              window.setTimeout(() => this.updateParentStyle(), 50);
            }
          },
          updateUserHome() {
            this.defaultUserPath = eXo.env.portal.homeLink;
            if (document.querySelector('#UserHomePortalLinkLogo')) {
              document.querySelector('#UserHomePortalLinkLogo').href = this.defaultUserPath;
            }
            if (document.querySelector('#UserHomePortalLinkName')) {
              document.querySelector('#UserHomePortalLinkName').href = this.defaultUserPath;
            }
          },
          async handleUpdatesFromWebSocket(event) {
            const data = event?.detail;
            const wsEventName = data?.wsEventName || '';
            let spaceWebNotificationItem = data?.message?.spaceWebNotificationItem || data?.message?.spacewebnotificationitem;
            if (spaceWebNotificationItem?.length) {
              spaceWebNotificationItem = JSON.parse(spaceWebNotificationItem);
            }
            const spaceId = spaceWebNotificationItem?.spaceId;
            if (wsEventName === 'notification.unread.item') {
              if (spaceId && this.$root.unreadPerSpace[spaceId]) {
                this.$root.unreadPerSpace[spaceId]++;
              } else {
                this.$set(this.$root.unreadPerSpace, spaceId, 1);
              }
            } else if (wsEventName === 'notification.read.item') {
              if (spaceId) {
                const value = (this.$root.unreadPerSpace[spaceId] - 1) || 0;
                if (value >= 0) {
                  this.$set(this.$root.unreadPerSpace, spaceId, value);
                } else {
                  const space = await this.$spaceService.getSpaceById(spaceId);
                  this.$root.$emit('space-settings-updated', space);
                }
              }
            } else if (wsEventName === 'notification.read.allItems') {
              if (spaceId && this.$root.unreadPerSpace[spaceId] > 0) {
                this.$root.unreadPerSpace[spaceId] = 0;
              }
            }
          },
        },
        template: `<sidebar id="${appId}" />`,
        i18n,
        vuetify: Vue.prototype.vuetifyOptions,
      }, `#${appId}`, 'Hamburger Menu');
    });
}
