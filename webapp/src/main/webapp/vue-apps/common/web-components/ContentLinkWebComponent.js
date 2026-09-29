const CONTENT_LINK_TYPES_BUNDLE = 'locale.portlet.ContentLink';

let contentLinkExtensions = null;

let contentLinkLabels = null;

/**
 * The content-link types, read once per page.
 *
 * @returns {Promise<Array>} the registered extensions, empty when unavailable
 */
function getContentLinkExtensions() {
  if (!contentLinkExtensions) {
    contentLinkExtensions = fetch('/social/rest/contentLinks', {
      method: 'GET',
      credentials: 'include',
    }).then(resp => resp?.ok && resp.json() || [])
      .catch(() => []);
  }
  return contentLinkExtensions;
}

/**
 * The labels of the content-link types, in the reader's language, read once per
 * page from the bundle every type contributes its keys to.
 *
 * @returns {Promise<Object>} the labels by key, empty when unavailable
 */
function getContentLinkLabels() {
  if (!contentLinkLabels) {
    const lang = window.eXo?.env?.portal?.language || 'en';
    contentLinkLabels = fetch(`/social/i18n/${CONTENT_LINK_TYPES_BUNDLE}?lang=${lang}`, {
      method: 'GET',
      credentials: 'include',
    }).then(resp => resp?.ok && resp.json() || {})
      .catch(() => ({}));
  }
  return contentLinkLabels;
}

/**
 * Escapes a text for an HTML attribute or element content.
 *
 * @param {string} text the text
 * @returns {string} the escaped text
 */
function escapeHtml(text) {
  return String(text || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/**
 * The chip of a link its reader cannot follow, for a type declaring a private
 * label: the type's icon and that label, with no link. It is the same whether
 * the object is missing or the reader may not view it, so it tells nothing of
 * the object. A drawer type keeps its drawer, which answers for the object.
 *
 * @param {string} objectType the linked object's type
 * @param {string} dataObjectAttr the chip's `type:id` reference
 * @returns {Promise<string>} the chip's HTML, or null when the type declares no
 *          private label
 */
async function getPrivateLinkHtml(objectType, dataObjectAttr) {
  const extensions = await getContentLinkExtensions();
  const extension = extensions?.find?.(ext => ext.objectType === objectType);
  if (!extension?.privateTitleKey) {
    return null;
  }
  const labels = await getContentLinkLabels();
  const label = labels?.[extension.privateTitleKey] || extension.privateTitleKey;
  return `<a${extension.drawer ? ' is="content-link-drawer"' : ''} data-object="${escapeHtml(dataObjectAttr)}" contenteditable="false" class="content-link"><i aria-hidden="true" class="v-icon notranslate ${escapeHtml(extension.icon)} theme--light icon-default-color" style="font-size: 16px; margin: 0 4px;"></i>${escapeHtml(label)}</a>`;
}

class ContentLink extends HTMLElement {

  /**
   * @param {Object} params the element's construction parameters
   */
  constructor(params) {
    super(params);
  }

  /**
   * Replaces the element with the chip of the object it designates: its icon and
   * title, linking to it, when the reader may view it; else, for a type declaring
   * a private label, the type's icon and that label; else a cross.
   *
   * @returns {void}
   */
  connectedCallback() {
    const dataObjectAttr = this.textContent?.replace?.('/', '')?.trim?.();
    if (dataObjectAttr?.length && dataObjectAttr.includes(':')) {
      const objectParts = dataObjectAttr.split(':');
      fetch(`/social/rest/contentLinks/link/${objectParts[0]}/${objectParts[1]}`, {
        method: 'GET',
        credentials: 'include',
      }).then(async resp => {
        if (resp?.status === 403 || resp?.status === 404) {
          const privateLinkHtml = await getPrivateLinkHtml(objectParts[0], dataObjectAttr);
          if (privateLinkHtml) {
            return {privateLinkHtml};
          }
        }
        return resp?.ok && resp.json();
      }).then(link => {
        if (link?.privateLinkHtml) {
          const template = document.createElement('template');
          template.innerHTML = link.privateLinkHtml;
          this.replaceWith(template.content.firstElementChild);
        } else if (link) {
          const template = document.createElement('template');
          template.innerHTML = `<a href="${link.uri}"${link.drawer ? ' is="content-link-drawer"' : ''} data-object="${dataObjectAttr}"  contenteditable="false" class="content-link"><i aria-hidden="true" class="v-icon notranslate ${link.icon} theme--light icon-default-color" style="font-size: 16px; margin: 0 4px;"></i>${link.title}</a>`;
          const node = template.content.firstElementChild;
          this.replaceWith(node);
        } else {
          throw new Error();
        }
      }).catch(() => {
        const template = document.createElement('template');
        template.innerHTML = `<a data-object="${dataObjectAttr}" contenteditable="false" class="content-link"><i aria-hidden="true" class="v-icon notranslate fa-times theme--light icon-default-color" style="font-size: 16px; margin: 0 4px;"></i></a>`;
        const node = template.content.firstElementChild;
        this.replaceWith(node);
      });
    }
  }

}

class ContentLinkDrawer extends HTMLAnchorElement {

  /**
   * @param {Object} params the element's construction parameters
   */
  constructor(params) {
    super(params);
  }

  /**
   * Opens the type's drawer on a click, in place of following the link.
   *
   * @returns {void}
   */
  connectedCallback() {
    this.onclick = event => {
      event.preventDefault();
      event.stopPropagation();
      const objectParts = this.getAttribute('data-object')?.split?.(':');
      if (objectParts?.length === 2) {
        self.require(['SHARED/ContentLink'], app => app.openPluginDrawer(objectParts[0], objectParts[1]));
      }
    };
  }
}

window.customElements.define('content-link', ContentLink);
window.customElements.define('content-link-drawer', ContentLinkDrawer, { extends: 'a' });
