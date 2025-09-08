import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('UsersManagement');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const appId = 'UsersManagement';

//getting language of the PLF
const lang = eXo?.env?.portal?.language || 'en';

//should expose the locale ressources as REST API 
const urls = [
  `/social/i18n/locale.portlet.Portlets?lang=${lang}`,
  `/social/i18n/locale.portlet.UsersManagement?lang=${lang}`,
];

export function init() {
  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    // init Vue app when locale ressources are ready
    Vue.createApp({
      data: {
        isDelegatedAdministrator: false,
        isSuperUser: false,
      },
      computed: {
        isMobile() {
          return this.$vuetify.breakpoint.mobile;
        },
      },
      async created() {
        const d = await this.$userService.isDelegatedAdministrator();
        this.isDelegatedAdministrator = d.result === 'true';
        const s = await this.$userService.isSuperUser();
        this.isSuperUser = s.isSuperUser === 'true';
      },
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<users-management id="${appId}" />`,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n
    }, `#${appId}`, 'Users Management');
  });
}
