
const PHONE_REGEX = /^([+*#]?\d{1,20}#?)$/;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[a-zA-Z]{2,}$/;

const TEXTAREA = document.createElement('textarea');
const HTML_ENTITIES = {
  nbsp: ' ',
  amp: '&',
  quot: '"',
  lt: '<',
  gt: '>'
};

export function htmlToText(htmlContent) {
  if (!htmlContent) {
    return '';
  }
  let content = htmlContent.replace(/<[^>]+>/g, ' ').trim();
  TEXTAREA.innerHTML = content;
  content = TEXTAREA.value;
  return content.replace(/[\r|\n|\t]/g, ' ').replace(/ +(?= )/g,' ').trim();
}

export function replaceHtmlEntites(htmlContent) {
  return htmlContent.replace(new RegExp(`&(${Object.keys(HTML_ENTITIES).join('|')});`, 'g'), (_match, entity) => HTML_ENTITIES[entity]);
}

export function trim(text) {
  return text && text.trim().replace(/(<p>(&nbsp;)*(\\n\\r\\t)*<\/p>)*(<div>(&nbsp;)*( \\n\\r\\t)*<\/div>)*(\\r)*(\\n)*(\\t)*/g, '') || '';
}

export function includeExtensions(suffix) {
  if (!window.requireJsLoadedExtension) {
    window.requireJsLoadedExtension = {};
  }
  const modules = window.requireJsModules.filter(m => m?.includes?.(suffix));
  if (modules?.length) {
    return Promise.all(modules.map(module => new Promise(resolve =>
      window.require([module], app => {
        const previousLoadingResult = window.requireJsLoadedExtension[module];
        if (!previousLoadingResult) {
          window.requireJsLoadedExtension[module] = Promise.resolve()
            .then(() => app?.init?.()) // NOSONAR
            .then(() => { // NOSONAR
              window.requireJsLoadedExtension[module] = true;
              resolve();
            });
        } else if (previousLoadingResult?.then) {
          previousLoadingResult.then(resolve);
        } else {
          resolve();
        }
      })
    )));
  } else {
    return Promise.resolve();
  }
}

export function blobToBase64(blob) {
  return new Promise(resolve => {
    const reader = new FileReader();
    reader.onload = e => resolve(e.target.result);
    reader.readAsDataURL(blob);
  });
}

export function convertImageDataAsSrc(imageData) {
  if (Array.isArray(imageData)) {
    let binary = '';
    const bytes = new Uint8Array(imageData);
    bytes.forEach(byte => binary += String.fromCharCode(byte));
    return `data:image/png;base64,${btoa(binary)}`;
  } else {
    return imageData;
  }
}

export async function importSkin(skinType, skinName) {
  const id = skinType === 'portal' ? skinName : `${skinType}_${skinName}`;
  if (!document.querySelector(`link#${id}`)) {
    await new Promise((resolve, reject) => {
      const link = document.createElement('link');
      link.id = id;
      link.type = 'text/css';
      link.rel = 'stylesheet';
      link.href = `/social/rest/skins/${skinType}/${skinName}?orientation=${eXo.env.portal.orientation === 'ltr' ? 'LT' : 'RT'}`;
      document.head.appendChild(link);
      link.onload = resolve;
      link.onerror = reject;
    });
  }
}

export function toLinkUrl(url, options) {
  if (url?.indexOf?.('./') === 0) {
    url = `${window.location.pathname.replace(/\/$/g, '')}${url.replace(/\.\//g, '/')}`;
  }
  if (url?.indexOf?.('/') === 0) {
    url = `${window.location.origin}${url}`;
  }
  const useNonSSL = url?.indexOf('http://') === 0;
  url = Autolinker.parse(url || '', options || {
    urls: true,
    email: false,
    phone: false,
    mention: false,
    hashtag: false,
  })?.[0]?.getAnchorHref?.()?.replace?.('javascript:', '');
  if (useNonSSL) {
    return url;
  } else {
    return url?.replace?.('http://', 'https://');
  }
}

export function getQueryParam(paramName) {
  const uri = window.location.search.substring(1);
  const params = new URLSearchParams(uri);
  return params.get(paramName);
}

const shortcutChars = new Set();

export function addShortcutsListener(chars, listener) {
  chars = chars.filter(c => c?.length).map(c => c.toLowerCase()).filter(c => !shortcutChars.has(c));
  chars.forEach(c => shortcutChars.add(c));
  window.addEventListener('keydown', e => {
    if (e.ctrlKey && e.shiftKey && e.key) {
      execShortcut(chars, listener, e);
    }
  });
  window.addEventListener('key-down', event => {
    const e = event?.detail;
    if (e?.key) {
      execShortcut(chars, listener, e);
    }
  });
}

export function execShortcut(chars, listener, e) {
  const c = e?.key?.toLowerCase?.();
  if (chars?.includes?.(c) && shortcutChars.has(c)) {
    e?.stopPropagation?.();
    e?.preventDefault?.();
    window.setTimeout(() => {
      listener(e.key);
    }, 10);
  }
}

export function removeShortcutsListener(chars) {
  chars.forEach(c => c && shortcutChars.delete(c));
}

export function detectOS() {
  const userAgent = window.navigator.userAgent || window.navigator.vendor || window.opera;
  if (/windows phone/i.test(userAgent)) {
    return 'Windows Phone';
  }
  if (/windows/i.test(userAgent)) {
    return 'Windows';
  }
  if (/android/i.test(userAgent)) {
    return 'Android';
  }
  if (/linux/i.test(userAgent)) {
    return 'Linux';
  }
  if (/iPad|iPhone|iPod/.test(userAgent) && !window.MSStream) {
    return 'iOS';
  }
  if (/macintosh|mac os x/i.test(userAgent)) {
    return 'Mac';
  }
  return 'Unknown';
}

export function isWindowsOs() {
  return detectOS() === 'Windows';
}

export function isLinuxOs() {
  return detectOS() === 'Linux';
}

export function isMacOs() {
  return detectOS() === 'Mac';
}

export function isValidPhone(value) {
  return !!value && PHONE_REGEX.test(value);
}

export function isValidEmail(value) {
  return !!value && EMAIL_REGEX.test(value);
}

export function isValidUrl(value) {
  return !!Autolinker.parse(String(value || ''), {
    urls: true,
    email: false,
    phone: false
  })?.[0]?.getAnchorHref?.();
}

// A Vuetify 2 v-treeview sets no ARIA role and renders each toggle as an unnamed button: the tree is
// exposed as nested lists, and each toggle is named after its node (labelOf(name)) with its expanded state.
export function describeTreeview(root, labelOf) {
  if (!root?.querySelectorAll) {
    return;
  }
  root.querySelectorAll('.v-treeview, .v-treeview-node__children').forEach(list => list.setAttribute('role', 'list'));
  root.querySelectorAll('.v-treeview-node').forEach(node => node.setAttribute('role', 'listitem'));
  root.querySelectorAll('.v-treeview-node__toggle').forEach(toggle => {
    const name = toggle.closest('.v-treeview-node__root')?.querySelector('.v-treeview-node__label')?.textContent?.trim() || '';
    toggle.setAttribute('aria-label', labelOf(name));
    toggle.setAttribute('aria-expanded', String(toggle.classList.contains('v-treeview-node__toggle--open')));
  });
  // a label that renders its own checkbox makes the tree's selection icon a duplicate, unnamed control
  root.querySelectorAll('.v-treeview-node__checkbox').forEach(checkbox => {
    if (checkbox.closest('.v-treeview-node__root')?.querySelector('.v-treeview-node__label [role="checkbox"]')) {
      checkbox.setAttribute('tabindex', '-1');
      checkbox.setAttribute('aria-hidden', 'true');
    }
  });
}

// Keeps the v-treeview elements under root described across the renders that rebuild their nodes
// (children loaded, a node opened, a search). Returns the function that stops it.
export function watchTreeview(root, labelOf) {
  if (!root || !window.MutationObserver) {
    return () => null;
  }
  let frame = 0;
  const observer = new window.MutationObserver(() => {
    if (!frame) {
      frame = window.requestAnimationFrame(() => {
        frame = 0;
        describeTreeview(root, labelOf);
      });
    }
  });
  // the toggle's open state is a class change: the attributes this function sets are not observed
  observer.observe(root, { childList: true, subtree: true, attributes: true, attributeFilter: ['class'] });
  describeTreeview(root, labelOf);
  return () => observer.disconnect();
}
