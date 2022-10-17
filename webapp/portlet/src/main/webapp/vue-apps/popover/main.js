import './initComponents.js';

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

const urls = [
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.commons.Commons-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.UserPopup-${lang}.json`,
];

Vue.directive('identity-popover', (el, binding) => {
  const identity = binding?.value;
  const isUser = identity?.username;
  if (!isUser) {
    document.addEventListener('space-favorite-added', event => {
      const spaceId = event?.detail;
      if (spaceId === identity.id) {
        identity.isFavorite = 'true';
      }
    });
    document.addEventListener('space-favorite-removed', event => {
      const spaceId = event?.detail;
      if (spaceId === identity.id) {
        identity.isFavorite = 'false';
      }
    });
  }

  el.addEventListener('mouseover', () => {
    const rect = el.getBoundingClientRect();
    document.dispatchEvent(new CustomEvent('popover-identity-display', {
      detail: Object.assign({
        offsetX: rect.left + window.scrollX,
        offsetY: rect.top + window.scrollY,
        identityType: isUser ? 'User' : 'Space',
        element: el,
      }, identity || {})
    }));
  });
});

const appId = 'Popovers';

const popoverAppElement = document.createElement('div');
popoverAppElement.setAttribute('id', appId);
document.querySelector('#vuetify-apps').append(popoverAppElement);

exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
  Vue.createApp({
    template: `<popover id="${appId}" />`,
    i18n,
    vuetify: Vue.prototype.vuetifyOptions,
  }, `#${appId}`, 'identity Popover');
});
