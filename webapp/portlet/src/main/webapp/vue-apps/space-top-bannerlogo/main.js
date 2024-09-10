import './initComponents.js';
const appId = 'SpaceTopBannerLogo';
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
const url = `/social-portlet/i18n/locale.portlet.Portlets?lang=${lang}`;

let popover;
export function init(params) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale resources are ready
    popover = Vue.createApp({
      data: () => ({
        spaceId: params.id,
        isFavorite: params.isFavorite,
        muted: params.muted === 'true',
        isMember: params.isMember,
        logoPath: params.logoPath,
        portalPath: params.portalPath,
        imageClass: params.imageClass,
        logoTitle: decodeURIComponent(params.logoTitle || ''),
        titleClass: params.titleClass,
        membersNumber: params.membersNumber,
        spaceDescription: decodeURIComponent(params.spaceDescription || ''),
        managers: params.managers,
        homePath: params.homePath,
      }),
      template: `<exo-space-logo-banner
                    id="SpaceTopBannerLogo"
                    :space-id="spaceId"
                    :muted="muted"
                    :is-member="isMember"
                    :logo-path="logoPath" 
                    :portal-path="portalPath"
                    :logo-title="logoTitle" 
                    :members-number="membersNumber"
                    :managers="managers"
                    :home-path="homePath"
                    :space-description="spaceDescription" />`,
      created() {
        document.addEventListener('space-settings-updated', this.refreshSpaceSettings);
      },
      methods: {
        refreshSpaceSettings(event) {
          const space = event?.detail;
          if (space) {
            params.logoTitle = space.displayName;
            params.spaceDescription = space.description;
            params.logoPath = space.avatarUrl;
            params.portalPath = `${eXo.env.portal.context}/g/${space.groupId.replaceAll('/', ':')}/${space.prettyName}`;
            init(params);
          }
        }
      },
      i18n,
      vuetify,
    }, `#${appId}`, 'social-portlet');
  });
}
export function destroy() {
  if (popover) {
    popover.$destroy();
  }
}
