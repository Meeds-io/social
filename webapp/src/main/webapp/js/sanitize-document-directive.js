/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
// v-sanitized-document renders a rich document body (a note, a page) as
// sanitized HTML, as its author wrote it: unlike v-sanitized-html, it neither
// links plain URLs, nor collapses white spaces, nor shortens link texts. It
// uses its own DOMPurify instance, so the hooks ExtendedDomPurify registers on
// the shared one do not apply, except the iframe host check, registered here.
(function() {
  // DOMPurify called with a window returns a new instance, without hooks
  const createPurifier = window.DOMPurify;
  const purifier = createPurifier(window);
  const config = {
    ADD_TAGS: ['iframe', 'content-link'],
    ADD_ATTR: ['target', 'allow', 'allowfullscreen', 'frameborder', 'v-identity-popover'],
    // A content-link chip the server writes as <a is="content-link-drawer">
    // opens its drawer: that customized built-in element is the only one a
    // document may name in its 'is' attribute
    CUSTOM_ELEMENT_HANDLING: {
      tagNameCheck: name => name === 'content-link-drawer',
      allowCustomizedBuiltInElements: true,
    },
    // What DOMPurify's defaults let through after 2.3.8 and rich content was
    // never written with: the slot tag, the attributes below (some act on
    // another element of the page by its id: popovertarget, commandfor), the
    // attribute values holding '/>', and the sms: and matrix: URI schemes
    ALLOW_SELF_CLOSE_IN_ATTR: false,
    ALLOWED_URI_REGEXP: /^(?:(?:(?:f|ht)tps?|mailto|tel|callto|cid|xmpp):|[^a-z]|[a-z+.-]+(?:[^a-z+.:-]|$))/i,
    FORBID_TAGS: ['slot'],
    FORBID_ATTR: [
      'command', 'commandfor', 'exportparts', 'inert', 'part', 'popover', 'popovertarget', 'popovertargetaction',
      'amplitude', 'dominant-baseline', 'exponent', 'intercept', 'mask-type', 'pointer-events', 'slope', 'tablevalues',
      'text-orientation', 'vector-effect', 'columnalign', 'columnspacing',
    ],
  };

  // Same rule as ExtendedDomPurify: the hosts come from the
  // io.meeds.sanitizer.iframe.allowedHosts property; a '*.example.com' entry
  // allows every sub-domain of example.com, not example.com itself. Without
  // it, no iframe is trusted.
  function isAllowedIframeSrc(src) {
    const allowedHosts = window.eXo?.env?.portal?.iframeAllowedHosts;
    if (!src?.trim() || !Array.isArray(allowedHosts)) {
      return false;
    }
    try {
      // https, or protocol-relative as iframely's own iframe is
      const url = new URL(src.trim(), window.location.href);
      return (url.protocol === 'https:' || src.trim().startsWith('//'))
        && allowedHosts.some(host => (host.startsWith('*.')
          ? url.hostname.endsWith(host.substring(1))
          : url.hostname === host));
    } catch (e) {
      return false;
    }
  }

  // DOMPurify passes the lower-cased tag name in data
  purifier.addHook('uponSanitizeElement', function(node, data) {
    if (data.tagName === 'iframe' && !isAllowedIframeSrc(node.getAttribute('src'))) {
      node.parentNode?.removeChild(node);
    }
  });

  function renderSanitizedDocument(el, binding) {
    el.innerHTML = binding.value && purifier.sanitize(binding.value, config) || '';
  }

  window.Vue.directive('sanitized-document', {
    bind: renderSanitizedDocument,
    update(el, binding) {
      if (binding.value !== binding.oldValue) {
        renderSanitizedDocument(el, binding);
      }
    },
  });
})();
