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

import * as activityUtils from './js/ActivityUtils.js';
if (!Vue.prototype.$activityUtils) {
  window.Object.defineProperty(Vue.prototype, '$activityUtils', {
    value: activityUtils,
  });
}

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const activityBaseLink = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity`;

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

const appId = 'ActivityStream';

// Attention!!! when changing this, the list of preloaded
// URLs has to change in JSP as well
const urls = [
  `/social-portlet/i18n/locale.portlet.Portlets?lang=${lang}`,
  `/social-portlet/i18n/locale.commons.Commons?lang=${lang}`,
  `/social-portlet/i18n/locale.social.Webui?lang=${lang}`,
];

export function init(maxFileSize) {
  exoi18n.loadLanguageAsync(lang, urls)
    .then(i18n => {
      Vue.createApp({
        data: {
          maxFileSize,
          activityBaseLink: activityBaseLink,
          selectedActivityId: null,
          selectedCommentId: null,
          canPost: null,
          replyToComment: false,
          displayCommentActionTypes: []
        },
        computed: {
          isMobile() {
            return this.$vuetify?.breakpoint?.mobile;
          },
        },
        created() {
          this.replyToComment = window.location.hash.includes('#comment-reply');
        },
        template: `<activity-stream id="${appId}" />`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n,
      }, `#${appId}`, 'Stream');
    })
    .finally(() => Vue.prototype.$utils.includeExtensions('ActivityStreamExtension'));
}
