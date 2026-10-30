import './initComponents.js';

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

const urls = [
  `/social/i18n/locale.portlet.Portlets?lang=${lang}`,
  `/social/i18n/locale.commons.Commons?lang=${lang}`,
  `/social/i18n/locale.portlet.social.UserPopup?lang=${lang}`,
];

Vue.directive('identity-popover', (el, binding) => {
  const identity = binding?.value;
  if (!identity) {
    // a null binding means "no popover" (e.g. <space-avatar :popover="false">),
    // not a popover on an empty identity. Evaluated on bind and on each
    // update; listeners already attached are not removed.
    return;
  }
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

// A mention rendered as raw HTML (innerHTML, v-sanitized-html,
// dynamic-html-element's html prop) is never compiled by Vue: its
// v-identity-popover stays a plain attribute that no directive binds, while a
// compiled element no longer carries it. Such elements are handled here, the
// attribute read as data and never evaluated. Any author can write that
// attribute in content, so only its username is used: the displayed identity
// is the one the server returns for that username.
const POPOVER_JS_ESCAPES = {n: '\n', r: '\r', u2028: '\u2028', u2029: '\u2029'};
const rawPopoverUsers = new Map();

document.addEventListener('mouseover', showRawPopover);
document.addEventListener('focusin', showRawPopover);
document.addEventListener('focusout', event => {
  if (getRawPopoverElement(event)) {
    document.dispatchEvent(new CustomEvent('popover-identity-hide'));
  }
});

function showRawPopover(event) {
  const el = getRawPopoverElement(event);
  const username = el && parseIdentityPopoverValue(el.getAttribute('v-identity-popover'))?.username;
  if (!username) {
    return;
  }
  getRawPopoverUser(username).then(user => {
    // the user is loaded asynchronously: no popover once the mention is left
    if (user?.username && (el.matches(':hover') || el.contains(document.activeElement))) {
      showPopover(el, {
        id: user.id,
        username: user.username,
        enabled: user.enabled,
        deleted: user.deleted,
        fullName: user.fullname,
        primaryProperty: user.primaryProperty,
        avatar: user.avatar,
        external: user.external,
        displayedEmail: user.displayedEmail,
        displayedPhone: user.displayedPhone,
      }, true);
    }
  });
}

function getRawPopoverUser(username) {
  if (!rawPopoverUsers.has(username)) {
    rawPopoverUsers.set(username, Vue.prototype.$userService.getUser(encodeURIComponent(username), 'settings')
      .catch(() => {
        rawPopoverUsers.delete(username);
        return null;
      }));
  }
  return rawPopoverUsers.get(username);
}

function getRawPopoverElement(event) {
  const el = event?.target?.closest?.('[v-identity-popover]');
  return el && !el.isContentEditable && el || null;
}

// Reads the literal built by LinkProvider#getProfileLink: single-quoted values
// whose JS escapes, added by LinkProvider#toPopoverValue, are undone here
function parseIdentityPopoverValue(value) {
  const entryPattern = /(\w+)\s*:\s*'((?:[^'\\]|\\[\s\S])*)'/g;
  const identity = {};
  let entry;
  while ((entry = entryPattern.exec(value || ''))) {
    identity[entry[1]] = entry[2].replace(/\\(u2028|u2029|[\s\S])/g, (escape, character) => POPOVER_JS_ESCAPES[character] || character);
  }
  return Object.keys(identity).length && identity || null;
}

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
    data() {
      return {
        statusMap: {
          available: '#2eb58c',
          donotdisturb: '#bc4343',
          offline: '#707070',
          invisible: '#707070'
        }
      };
    },
    template: `<popover id="${appId}" />`,
    i18n,
    vuetify: Vue.prototype.vuetifyOptions,
  }, `#${appId}`, 'identity Popover');
}).finally(() => Vue.prototype.$utils.includeExtensions('PopoverExtension'));
