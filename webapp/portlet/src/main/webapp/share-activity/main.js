import './initComponents.js';
import { spacesConstants } from '../js/spacesConstants.js';

// getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

// should expose the locale ressources as REST API
const url = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_REST}/i18n/bundle/locale.social.Webui-${lang}.json`;

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ShareActivity');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
let shareActivityApp;
export function init(params) {
  const appId = `shareActivity-${params.activityId}`;

  const appElement = document.createElement('div');
  appElement.id = appId;

  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    shareActivityApp = new Vue({
      template: `<share-activity 
                  v-cacheable="{cacheId: '${appId}'}"
                  id="${appId}" />`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}

export function openShareActivityDrawer(params) {
  if (shareActivityApp) {
    shareActivityApp.$root.$emit('open-share-activity-drawer', params);
  }
}

export function destroy() {
  if (shareActivityApp) {
    shareActivityApp.$destroy();
  }
}