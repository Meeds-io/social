import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ActivityStream');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }

  // Register predefined activity types
  extensionRegistry.registerExtension('activity', 'types', {
    type: 'default',
    options: {
      getBody: activity => activity && activity.title || '',
    },
  });
  extensionRegistry.registerExtension('activity', 'types', {
    type: 'LINK_ACTIVITY',
    options: {
      init: null,
      getActivityLink: null,
      getBody: activity => activity && activity.templateParams && activity.templateParams.comment || '',
      getTitle: activity => activity && activity.templateParams && activity.templateParams.title || activity.templateParams.defaultTitle || activity.templateParams.link || '',
      getSummary: activity => activity && activity.templateParams && activity.templateParams.description || '',
      getThumbnail: activity => activity && activity.templateParams && activity.templateParams.image || '',
      supportsThumbnail: true,
      getSourceLink: activity => activity && activity.templateParams && activity.templateParams.link,
      getSourceIcon: () => 'fa-link',
    },
  });
}

//getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

const appId = 'ActivityStream';

//should expose the locale ressources as REST API 
const urls = [
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.commons.Commons-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.social.Webui-${lang}.json`,
];

export function init() {
  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    new Vue({
      data: {
        activityBaseLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity`,
      },
      template: `<activity-stream id="${appId}" />`,
      vuetify,
      i18n,
    }).$mount(`#${appId}`);
  });
}
