import './initComponents.js';
import { spacesConstants } from '../js/spacesConstants.js';

// getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API
const url = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_REST}/i18n/bundle/locale.social.Webui-${lang}.json`;


// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ActivityReactions');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

export function init(params) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    new Vue({
      data: () => ({
        likers: params.likers,
        likersNumber: params.likersNum,
        commentNumber: params.commentNum
      }),
      template: `<activity-reactions :activity-id=${params.activityId} :likers="likers" :likers-number="likersNumber" :comment-number="commentNumber" app-id="activityReactions-${params.activityId}" data-id="${params.activityId}"/>`,
      i18n,
      vuetify,
    }).$mount(`#activityReactions-${params.activityId}`);
  });
  document.dispatchEvent(
    new CustomEvent('display-activity-details', {detail : {
      id : params.activityId,
      type: 'ACTIVITY',
    }}));
}

