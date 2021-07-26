import './initComponents.js';
import './extensions.js';

import * as activityStreamWebSocket from './js/WebSocket.js';
if (!Vue.prototype.$activityStreamWebSocket) {
  window.Object.defineProperty(Vue.prototype, '$activityStreamWebSocket', {
    value: activityStreamWebSocket,
  });
}

import * as activityConstants from './js/ActivityConstants.js';
if (!Vue.prototype.$activityConstants) {
  window.Object.defineProperty(Vue.prototype, '$activityConstants', {
    value: activityConstants.default,
  });
}

const activityBaseLink = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity`;

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ActivityStream');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

// Disable swipe for Mobile when Stream pages are displayed
window.disableSwipeOnPage = true;

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
        activityBaseLink: activityBaseLink,
      },
      template: `<activity-stream id="${appId}" />`,
      vuetify,
      i18n,
    }).$mount(`#${appId}`);
  });
}
