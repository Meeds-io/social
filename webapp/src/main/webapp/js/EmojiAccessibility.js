/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
(function() {
  // Accessibility (#4246): expose emojis to screen readers by wrapping
  // them in a <span role="img" aria-label="..."> once their names are loaded.
  let emojiNameByChar = null;
  let emojiPattern = null;

  function escapeRegExp(value) {
    return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  function buildEmojiIndex(emojiBank) {
    const map = new Map();
    (emojiBank.categories || []).forEach(category => {
      (category.emojis || []).forEach(item => {
        if (item.emoji && item.name && !map.has(item.emoji)) {
          map.set(item.emoji, item.name);
        }
      });
    });
    emojiNameByChar = map;
    const chars = Array.from(map.keys()).sort((a, b) => b.length - a.length);
    emojiPattern = chars.length && new RegExp(chars.map(escapeRegExp).join('|'), 'g');
  }

  let ready = false;
  const pendingCallbacks = new Map();
  const readyPromise = fetch('/social/json/emojiBank.json?v=1')
    .then(resp => resp.ok && resp.json())
    .then(emojiBank => emojiBank && buildEmojiIndex(emojiBank))
    .catch(() => {
      // Emoji names not available: emojis will remain unlabeled.
    })
    .then(() => {
      ready = true;
      pendingCallbacks.forEach(callback => callback());
      pendingCallbacks.clear();
    });

  function onReady(key, callback) {
    if (ready) {
      callback();
    } else {
      pendingCallbacks.set(key, callback);
    }
  }

  function addAccessibleNameToEmojis(html) {
    if (!emojiPattern) {
      return html;
    }
    const container = document.createElement('div');
    container.innerHTML = html;
    const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);
    const textNodes = [];
    let node;
    while ((node = walker.nextNode())) {
      emojiPattern.lastIndex = 0;
      if (!node.parentElement?.closest('[role="img"]') && emojiPattern.test(node.nodeValue)) {
        textNodes.push(node);
      }
    }
    textNodes.forEach(textNode => {
      const fragment = document.createDocumentFragment();
      let lastIndex = 0;
      let match;
      emojiPattern.lastIndex = 0;
      while ((match = emojiPattern.exec(textNode.nodeValue))) {
        if (match.index > lastIndex) {
          fragment.appendChild(document.createTextNode(textNode.nodeValue.slice(lastIndex, match.index)));
        }
        const span = document.createElement('span');
        span.setAttribute('role', 'img');
        span.setAttribute('aria-label', emojiNameByChar.get(match[0]));
        span.textContent = match[0];
        fragment.appendChild(span);
        lastIndex = match.index + match[0].length;
      }
      fragment.appendChild(document.createTextNode(textNode.nodeValue.slice(lastIndex)));
      textNode.parentNode.replaceChild(fragment, textNode);
    });
    return container.innerHTML;
  }

  const readyMixin = {
    data() {
      return {
        emojiBankReady: ready,
      };
    },
    created() {
      onReady(this, () => {
        this.emojiBankReady = true;
      });
    },
    beforeDestroy() {
      pendingCallbacks.delete(this);
    },
  };

  window.EmojiAccessibility = {
    isReady: () => ready,
    ready: readyPromise,
    onReady,
    readyMixin,
    addAccessibleNameToEmojis,
  };
})();
