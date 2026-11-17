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

// The element whose activation opens a dialog, for the dialog to give the focus back to it once closed.
// A click does not always focus what it clicks: Safari does not focus a button it clicks, so the document's
// active element is then the body or whatever had the focus before. So the target of the last click is kept
// until the focus moves to another element, a menu's item standing for the menu's button: the item is hidden
// once the menu closes, and the menu's keyboard navigation clicks it while the focus stays on the button.
// Stateful: exposed as `$activationOrigin` by the common bundle, which installs it once; read it through the
// prototype, never through an import, which would run a second instance with no click recorded.
const ACTIVABLE_SELECTOR = 'a[href], button, input, select, textarea, summary, [role="button"], [role="link"], [role="menuitem"], [role="tab"], [role="option"], [tabindex]';
const EXPANDED_MENU_BUTTON_SELECTOR = ':scope > [aria-haspopup][aria-expanded="true"], :scope > * > [aria-haspopup][aria-expanded="true"]';

let lastActivated = null;
let installed = false;

// The focusable control a click landed in: a matched element that cannot take the focus (a role without a
// tabindex) gives way to its closest focusable ancestor
function activatedControl(target) {
  let control = target?.closest?.(ACTIVABLE_SELECTOR);
  while (control && control.tabIndex < 0 && !control.hasAttribute('tabindex')) {
    control = control.parentElement?.closest(ACTIVABLE_SELECTOR);
  }
  return control || null;
}

function recordClick(event) {
  // mapped while the click runs: the menu closing on it resets its button's aria-expanded right after
  lastActivated = menuOrigin(activatedControl(event.target));
}

function forgetOnFocusMove(event) {
  if (lastActivated && event.target !== lastActivated) {
    lastActivated = null;
  }
}

export function install() {
  if (installed) {
    return;
  }
  installed = true;
  document.addEventListener('click', recordClick, true);
  document.addEventListener('focusin', forgetOnFocusMove, true);
}

// An item of a menu opened the dialog: the menu is hidden once the dialog closes, its button stays.
// A menu's keyboard navigation clicks the highlighted item while the focus stays on the button.
// Only a menu button is taken: a tooltip activator also has an expanded popup while it is hovered or focused
export function menuOrigin(element) {
  const menu = element?.closest?.('.v-menu__content');
  if (!menu) {
    return element;
  }
  // an attached menu sits next to its button
  let ancestor = menu.parentElement;
  for (let level = 0; ancestor && ancestor !== document.body && level < 4; level++) {
    const button = ancestor.querySelector(EXPANDED_MENU_BUTTON_SELECTOR);
    if (button) {
      return button;
    }
    ancestor = ancestor.parentElement;
  }
  return document.querySelector('[aria-haspopup="menu"][aria-expanded="true"]') || element;
}

// The element that opened what is opening now, never one inside `container`: the clicked control while the
// focus has not moved since the click, the active element otherwise, null when that is the body.
export function element(container) {
  const outside = candidate => !!candidate
    && candidate !== document.body
    && candidate.isConnected
    && candidate.getClientRects().length > 0
    && !container?.contains?.(candidate);
  if (outside(lastActivated)) {
    return lastActivated;
  }
  const active = menuOrigin(document.activeElement);
  return outside(active) ? active : null;
}
