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
// attribute in content, so only a user's username or a space's id is used: the
// displayed identity is the one the server returns for it.
const POPOVER_JS_ESCAPES = {n: '\n', r: '\r'};
const rawPopoverUsers = new Map();
const rawPopoverSpaces = new Map();

document.addEventListener('mouseover', showRawPopover);
document.addEventListener('focusin', showRawPopover);
document.addEventListener('focusout', event => {
  if (getRawPopoverElement(event)) {
    document.dispatchEvent(new CustomEvent('popover-identity-hide'));
  }
});
// a favorite toggled in a raw space popover is kept on the space it was loaded
// from, which the next hover shows again
document.addEventListener('metadata.favorite.updated', event => {
  const metadata = event?.detail;
  const spaceId = metadata?.objectType === 'space' && `${metadata.objectId}`;
  if (spaceId && rawPopoverSpaces.has(spaceId)) {
    rawPopoverSpaces.get(spaceId).then(space => {
      if (space) {
        space.isFavorite = `${metadata.favorite}`;
      }
    });
  }
});

function showRawPopover(event) {
  const el = getRawPopoverElement(event);
  const identity = el && parseIdentityPopoverValue(el.getAttribute('v-identity-popover'));
  if (identity?.username) {
    showRawUserPopover(el, identity.username);
  } else if (identity?.identityType?.toLowerCase() === 'space' && /^\d+$/.test(identity.id || '')) {
    showRawSpacePopover(el, identity.id);
  }
}

function showRawUserPopover(el, username) {
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

function showRawSpacePopover(el, spaceId) {
  getRawPopoverSpace(spaceId).then(space => {
    // the space is loaded asynchronously: no popover once the link is left
    if (space?.id && (el.matches(':hover') || el.contains(document.activeElement))) {
      showPopover(el, space, false);
    }
  });
}

function getRawPopoverSpace(spaceId) {
  if (!rawPopoverSpaces.has(spaceId)) {
    // the same expand as the space popover's own load
    rawPopoverSpaces.set(spaceId, Vue.prototype.$spaceService.getSpaceById(spaceId, 'favorite')
      .catch(() => {
        // not kept: a transient failure is retried on the next hover
        rawPopoverSpaces.delete(spaceId);
        return null;
      }));
  }
  return rawPopoverSpaces.get(spaceId);
}

function getRawPopoverUser(username) {
  if (!rawPopoverUsers.has(username)) {
    // no expand: the same URL as the popover's own enabled/deleted check
    rawPopoverUsers.set(username, Vue.prototype.$userService.getUser(encodeURIComponent(username))
      .catch(() => {
        // not kept: a transient failure is retried on the next hover
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

// Reads the literal LinkProvider#getProfileLink builds (values escaped by
// LinkProvider#toPopoverValue) and the one kudos builds with JSON.stringify and
// quoted keys: their single-quoted values are unescaped here, and only username,
// id and identityType are used
function parseIdentityPopoverValue(value) {
  const entryPattern = /'?(\w+)'?\s*:\s*'((?:[^'\\]|\\[\s\S])*)'/g;
  const identity = {};
  let entry;
  while ((entry = entryPattern.exec(value || ''))) {
    identity[entry[1]] = entry[2].replace(/\\(u[0-9a-fA-F]{4}|[\s\S])/g, (escape, character) => (character.length === 5
      ? String.fromCharCode(parseInt(character.substring(1), 16))
      : POPOVER_JS_ESCAPES[character] || character));
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
