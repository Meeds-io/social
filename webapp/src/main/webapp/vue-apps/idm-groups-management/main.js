import './initComponents.js';
import './services.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('GroupsManagement');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

//getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const appId = 'GroupsManagement';

//should expose the locale resources as REST API
const urls = [
  `/social/i18n/locale.portlet.Portlets?lang=${lang}`,
  `/social/i18n/locale.portlet.UsersManagement?lang=${lang}`,
];

export function init() {
  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    // init Vue app when locale resources are ready
    Vue.createApp({
      data() {
        return {
          group: null,
        };
      },
      computed: {
        selectedGroup() {
          return this.group;
        },
        isMobile() {
          return this.$vuetify.breakpoint.smAndDown;
        },
      },
      created() {
        this.$root.$on('selectGroup', this.setSelectedGroup);
      },
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      beforeDestroy() {
        this.$root.$off('selectGroup', this.setSelectedGroup);
      },
      methods: {
        setSelectedGroup(group) {
          this.group = group;
        },
      },
      template: `<group-management id="${appId}" />`,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }, `#${appId}`, 'Group Management');
  });
}
