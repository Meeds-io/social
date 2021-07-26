import './initComponents.js';

// getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API
const url = `${Vue.prototype.$spacesConstants.PORTAL}/${Vue.prototype.$spacesConstants.PORTAL_REST}/i18n/bundle/locale.social.Webui-${lang}.json`;


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
        activityId: params.activityId,
        likers: params.likers,
        likersNumber: params.likersNum,
        commentNumber: params.commentNum
      }),
      template: `<activity-reactions-app
                   id="activityReactions-${params.activityId}"
                   :data-id="activityId"
                   :activity-id="activityId"
                   :likers="likers"
                   :likers-number="likersNumber"
                   :comment-number="commentNumber" />`,
      i18n,
      vuetify,
    }).$mount(`#activityReactions-${params.activityId}`);
  });
  document.dispatchEvent(
    new CustomEvent('display-activity-details', {detail: {
      id: params.activityId,
      type: 'ACTIVITY',
    }}));
}

