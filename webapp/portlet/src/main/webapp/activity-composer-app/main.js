import './components/initComponents.js';
import { getActivityComposerActionExtensions } from './extension.js';

Vue.use(Vuetify);

const vuetify = new Vuetify({
  iconfont: '',
});

// getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

const urls = [
  `/portal/rest/i18n/bundle/locale.portlet.Portlets-${lang}.json`
];

// get overridden components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ActivityComposer');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

// getting locale resources
export function init() {
  getActivityComposerActionExtensions().forEach(extension => {
    if(extension.resourceBundle) {
      urls.push(`/portal/rest/i18n/bundle/${extension.resourceBundle}-${lang}.json`);
    }
  });

  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    // init Vue app when locale resources are ready
    new Vue({
      el: '#activityComposer',
      template: '<exo-activity-composer></exo-activity-composer>',
      i18n,
      vuetify
    });
  });
}