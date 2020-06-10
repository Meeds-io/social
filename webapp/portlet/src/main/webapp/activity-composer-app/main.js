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
export function init(params) {
  getActivityComposerActionExtensions().forEach(extension => {
    if(extension.resourceBundle) {
      urls.push(`/portal/rest/i18n/bundle/${extension.resourceBundle}-${lang}.json`);
    }
  });
  params = params ? params : '';
  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    // init Vue app when locale resources are ready
    new Vue({
      el: '#activityComposer',
      data: function() {
        return {
          composerAction: params.composerAction || 'post',
          activityBody: params.activityBody || '',
          ckEditorType: params.ckEditorType || 'activityContent',
          activityId: params.activityId || '',
        };
      },
      template: '<exo-activity-composer :message="activityBody" :activity-id="activityId" :composer-action="composerAction" :ck-editor-type="ckEditorType"></exo-activity-composer>',
      i18n,
      vuetify
    });
  });
}