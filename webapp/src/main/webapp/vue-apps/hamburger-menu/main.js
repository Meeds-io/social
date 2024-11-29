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

export function init(mode, defaultUserPath, unreadPerSpace, avatarUrl, isExternalFeatureEnabled) {
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
          hoverFirstLevel: false,
          hoverSecondLevel: false,
          hoverThirdLevel: false,
          hoverMenu: false,
          hoverButton: false,
          openedSiteName: null,
          openedSpaceId: null,
          openedSpacesUrl: null,
          openedSpaceTemplateId: null,
          openedSpaceTemplateName: null,
          sites: null,
          settings: null,
          openedOverlay: false,
          hoverDeferred: false,
          rtl: eXo.env.portal.orientation === 'rtl',
          ltr: eXo.env.portal.orientation === 'ltr',
        },
        computed: {
          stickyBreakpointWidth() {
            return this.mode === 'ICON' ? this.$vuetify.breakpoint.thresholds.md : this.$vuetify.breakpoint.thresholds.lg;
          },
          stickyAllowed() {
            return this.$vuetify.breakpoint.width >= this.stickyBreakpointWidth;
          },
          hidden() {
            return !this.stickyAllowed || this.mode === 'HIDDEN';
          },
          sticky() {
            return !this.hidden && this.mode === 'STICKY';
          },
          icon() {
            return !this.hidden && this.mode === 'ICON';
          },
          expand() {
            return !this.icon || this.hoverDeferred;
          },
          iconExpand() {
            return this.icon && this.expand;
          },
          iconCollapse() {
            return this.icon && !this.expand;
          },
          displaySequentially() {
            return this.$vuetify.breakpoint.width >= this.stickyBreakpointWidth;
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
                  document.dispatchEvent(new CustomEvent('drawerOpened'));
                  this.openedOverlay = true;
                } else if (!this.expand && this.openedOverlay) {
                  if (!eXo.openedDrawers?.length) {
                    document.dispatchEvent(new CustomEvent('drawerClosed'));
                  }
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
        },
        created() {
          document.addEventListener('homeLinkUpdated', this.updateUserHome);
          this.init();
        },
        mounted() {
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
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
            if (this.icon) {
              document.querySelector('#UISiteBody').style[this.$vuetify.rtl && 'marginRight' || 'marginLeft'] = '70px';
            } else {
              document.querySelector('#UISiteBody').style[this.$vuetify.rtl && 'marginRight' || 'marginLeft'] = '';
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
        },
        template: `<sidebar id="${appId}" />`,
        i18n,
        vuetify: Vue.prototype.vuetifyOptions,
      }, `#${appId}`, 'Hamburger Menu');
    });
}