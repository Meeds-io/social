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
  if (identity && !isUser) {
    document.addEventListener('metadata.favorite.updated', event => {
      const metadata = event?.detail;
      if (metadata?.objectType === 'space'
        && metadata.objectId === identity.id
        && metadata.favorite !== (identity.isFavorite === 'true')) {
        identity.isFavorite = `${metadata.favorite}`;
      }
    });
  }

  el.addEventListener('mouseover', () => {
    if (identity && identity.allowAnimation && isUser) {
      el.classList.add('z-index-two', 'mt-n1');
    }
    showPopover(el, identity, isUser);
  });
  el.addEventListener('mouseleave', () => {
    if (identity && identity.allowAnimation && isUser) {
      el.classList.remove('z-index-two', 'mt-n1');
    }
  });
  el.addEventListener('focusin', () => {
    if (identity && identity.allowAnimation && isUser) {
      el.classList.add('z-index-two', 'mt-n1');
    }
    showPopover(el, identity, isUser);
  });
  el.addEventListener('focusout', () => {
    if (identity && identity.allowAnimation && isUser) {
      el.classList.remove('z-index-two', 'mt-n1');
    }
    document.dispatchEvent(new CustomEvent('popover-identity-hide'));
  });
});

export function showPopover(el, identity, isUser) {
  const rect = el.getBoundingClientRect();
  document.dispatchEvent(new CustomEvent('popover-identity-display', {
    detail: Object.assign({
      offsetX: rect.left + window.scrollX,
      offsetY: rect.top > 150 + rect.height ? rect.top : rect.bottom + window.scrollY,
      top: rect.top > 150 + rect.height ? true : false,
      identityType: isUser ? 'User' : 'Space',
      element: el,
    }, identity || {})
  }));
}

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
}).finally(() => Vue.prototype.$utils.includeExtensions('PopoverExtension'));
