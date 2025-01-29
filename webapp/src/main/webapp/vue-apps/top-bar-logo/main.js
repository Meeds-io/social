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
        displayMobileCompanyLogo: params.displayMobileCompanyLogo,
        displaySiteName: params.displaySiteName,
        sidebarMode: params.sidebarMode,
        siteTitle: params.siteTitle,
        siteHomePath: params.siteHomePath,
        isSitePage: params.isSitePage,
        siteIcon: params.siteIcon,
        isStandaloneSite: !document.querySelector('#HamburgerNavigationMenu'),
        isTopBarElement: false
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
          if (this.sidebarMode === 'STICKY' && !this.lgAndUp && this.mdAndUp) {
            return 'ICON';
          } else if (this.hidenIconView || this.hidenStickyView) {
            return 'HIDDEN';
          } else {
            return this.sidebarMode;
          }
        },
        displayCompanyLogo() {
          return this.isStandaloneSite || (!this.mdAndUp && this.displayMobileCompanyLogo) || (this.mdAndUp && this.sidebarModeDisplay === 'HIDDEN');
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
        displayCompany() {
          return this.displayCompanyLogo || this.displayCompanyTitle;
        },
        displaySiteTitle() {
          return this.displaySiteName
            && this.mdAndUp
            && (
              (this.siteTitle && this.siteHomePath) ||
              (this.spaceLogoPath && this.spacePortalPath)
            );
        },
        displaySiteLogo() {
          return this.siteTitle || this.spaceLogoPath;
        },
        displaySite() {
          return this.displaySiteLogo || this.displaySiteTitle;
        },
      },
      created() {
        document.addEventListener('space-settings-updated', this.refreshSpaceSettings);
        document.addEventListener('homeLinkUpdated', this.updateUserHome);
        document.addEventListener('sidebar-mode-changed', this.updateSidebarMode);
      },
      mounted() {
        this.isTopBarElement = !!this.$el.closest('.layout-top-bar');
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
