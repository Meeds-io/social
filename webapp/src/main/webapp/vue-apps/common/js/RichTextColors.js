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

// Rich text keeps the colours its author chose, except black and white text, which would vanish on the other
// version's background: such an element is marked rt-color-black or rt-color-white, and the skin gives it the Auto
// text colour in dark (platform-ui core/displayModes.less). The browser normalises element.style.color to rgb(), so
// one comparison covers every spelling. An element that sets a background, or lies inside one within the content,
// keeps its colours as written: white text on a black cell stays white.

const BLACK = 'rgb(0, 0, 0)';

const WHITE = 'rgb(255, 255, 255)';

const BLACK_CLASS = 'rt-color-black';

const WHITE_CLASS = 'rt-color-white';

function hasBackground(element, root) {
  for (let current = element; current && current !== root.parentElement; current = current.parentElement) {
    const background = current.style?.backgroundColor;
    if ((background && background !== 'transparent' && background !== 'rgba(0, 0, 0, 0)')
        || current.style?.backgroundImage && current.style.backgroundImage !== 'none'
        || current.hasAttribute?.('bgcolor')) {
      return true;
    }
    if (current === root) {
      break;
    }
  }
  return false;
}

/**
 * Marks the black and white text of a rich content, and unmarks an element whose colour or background changed.
 *
 * @param {Element} root the element holding the rich content
 * @returns {void}
 */
export function markColors(root) {
  if (!root?.querySelectorAll) {
    return;
  }
  root.querySelectorAll(`[style], .${BLACK_CLASS}, .${WHITE_CLASS}`).forEach(element => {
    const color = element.style?.color;
    const black = color === BLACK && !hasBackground(element, root);
    const white = color === WHITE && !hasBackground(element, root);
    element.classList.toggle(BLACK_CLASS, black);
    element.classList.toggle(WHITE_CLASS, white);
    if (!element.classList.length) {
      element.removeAttribute('class');
    }
  });
}
