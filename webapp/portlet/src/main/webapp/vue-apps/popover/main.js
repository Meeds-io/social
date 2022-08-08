import './initComponents.js';

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

const urls = [
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.commons.Commons-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.UserPopup-${lang}.json`,
];

Vue.directive('identity-popover', (el, binding) => {
  el.addEventListener('mouseover', () => {
    const rect = el.getBoundingClientRect();
    window.dispatchEvent(new CustomEvent('popover-identity-display', {detail: Object.assign({
      offsetX: rect.left + window.scrollX,
      offsetY: rect.top + window.scrollY,
      identityType: binding?.value?.username ? 'User' : 'Space',
      element: el,
    }, binding?.value || {})}));
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
