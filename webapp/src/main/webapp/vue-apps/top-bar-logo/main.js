import './initComponents.js';
const appId = 'brandingTopBar';
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ExoPopover');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
const vuetify = Vue.prototype.vuetifyOptions;

const lang = eXo && eXo.env.portal.language || 'en';
const url = `/social/i18n/locale.portlet.Portlets?lang=${lang}`;

let popover;
export function init(params) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale resources are ready
    popover = Vue.createApp({
      template: `<top-bar-logo id="${appId}" />`,
      data: {
        spaceId: params.id,
        isFavorite: params.isFavorite,
        muted: params.muted === 'true',
        isMember: params.isMember,
        portalPath: params.portalPath,
        logoPath: params.logoPath,
        logoTitle: decodeURIComponent(params.logoTitle || ''),
        spacePortalPath: params.spacePortalPath,
        spaceLogoPath: params.spaceLogoPath,
        spaceLogoTitle: decodeURIComponent(params.spaceLogoTitle || ''),
        imageClass: params.imageClass,
        titleClass: params.titleClass,
        membersNumber: params.membersNumber,
        spaceDescription: decodeURIComponent(params.spaceDescription || ''),
        managers: params.managers,
        canRedactOnSpace: params.canRedactOnSpace,
        displayCompanyName: params.displayCompanyName,
        displaySiteName: params.displaySiteName,
        sidebarMode: params.sidebarMode,
        siteTitle: params.siteTitle,
        siteHomePath: params.siteHomePath,
        siteIcon: params.siteIcon,
        isStandaloneSite: !document.querySelector('#HamburgerNavigationMenu'),
      },
      computed: {
        xl() {
          return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.xl;
        },
        lgAndUp() {
          return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.lg;
        },
        mdAndUp() {
          return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.md;
        },
        smAndUp() {
          return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.sm;
        },
        hidenIconView() {
          return this.sidebarMode === 'ICON' && !this.mdAndUp;
        },
        hidenStickyView() {
          return this.sidebarMode === 'STICKY' && !this.lgAndUp;
        },
        sidebarModeDisplay() {
          if (this.hidenIconView || this.hidenStickyView) {
            return 'HIDDEN';
          } else {
            return this.sidebarMode;
          }
        },
        displayCompany() {
          return this.displayCompanyLogo || this.displayCompanyTitle;
        },
        displayCompanyLogo() {
          return this.isStandaloneSite
            || (
              (this.mdAndUp || !this.displaySite)
              && (
                (this.displayCompanyName && this.sidebarModeDisplay === 'HIDDEN') ||
                (this.sidebarModeDisplay === 'HIDDEN' && !this.displaySite)
              )
            );
        },
        displayCompanyTitle() {
          return this.isStandaloneSite
            || (this.displayCompanyName
                && this.logoTitle
                && this.portalPath
                && (this.xl || (this.mdAndUp && !this.displaySite))
                && (this.sidebarModeDisplay === 'HIDDEN' || this.sidebarModeDisplay === 'ICON' || !this.displaySite)
            );
        },
        displaySite() {
          return this.displaySiteLogo || this.displaySiteTitle;
        },
        displaySiteLogo() {
          return this.displaySiteName && (this.siteIcon || this.spaceLogoPath);
        },
        displaySiteTitle() {
          return this.displaySiteName
            && this.mdAndUp
            && (
              (this.siteTitle && this.siteHomePath) ||
              (this.spaceLogoPath && this.spacePortalPath)
            );
        },
      },
      created() {
        document.addEventListener('space-settings-updated', this.refreshSpaceSettings);
        document.addEventListener('homeLinkUpdated', this.updateUserHome);
        document.addEventListener('sidebar-mode-changed', this.updateSidebarMode);
      },
      methods: {
        updateUserHome(event) {
          this.portalPath = event?.detail;
        },
        updateSidebarMode(event) {
          this.sidebarMode = event?.detail;
        },
        refreshSpaceSettings(event) {
          const space = event?.detail;
          if (space) {
            this.spaceLogoTitle = space.displayName;
            this.spaceDescription = space.description;
            this.spaceLogoPath = space.avatarUrl;
            this.membersNumber = space.membersCount;
            this.$forceUpdate();
          }
        }
      },
      i18n,
      vuetify,
    }, `#${appId}`, 'Topbar Logo');
  });
}
export function destroy() {
  if (popover) {
    popover.$destroy();
  }
}
