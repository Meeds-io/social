import './components/initComponents.js';
import { getActivityComposerActionExtensions } from './extension.js';

Vue.use(Vuetify);

const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

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
  if ($('.activityComposerApp').length && params && params.activityId) {
    document.dispatchEvent(new CustomEvent('activity-composer-edit-activity', {detail: params}));
  } else {
    getActivityComposerActionExtensions().forEach(extension => {
      if (extension.resourceBundle) {
        urls.push(`/portal/rest/i18n/bundle/${extension.resourceBundle}-${lang}.json`);
      }
    });
    params = params ? params : '';
    exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
      const appId = 'activityComposer';
      let appElement = null;
      let cacheAttribute = null;

      if ($(`${appId}`).length) {
        appElement = document.createElement('div');
        appElement.id = appId;
        const cacheId = `${appId}_${eXo.env.portal.spaceId}_${params && params.activityId || ''}`;
        cacheAttribute = `v-cacheable="{cacheId: '${cacheId}'}"`;
      } else {
        $(`<div id="${appId}"></div>`).appendTo($('#UIPortalApplication'));
        appElement = `#${appId}`;
        cacheAttribute = '';
      }

      new Vue({
        data: () => ({
          composerAction: params && params.composerAction || 'post',
          activityBody: params && params.activityBody || '',
          activityId: params && params.activityId || '',
          standalone: !!(params && params.activityId),
        }),
        template: `<exo-activity-composer
                     ${cacheAttribute}
                     id="${appId}"
                     :activityBody="activityBody"
                     :activity-id="activityId"
                     :composer-action="composerAction"
                     :standalone="standalone">
                   </exo-activity-composer>`,
        i18n,
        vuetify,
      }).$mount(appElement);
    });

    window.activityComposerInitialized = true;
  }
}