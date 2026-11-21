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

// The focus of a confirmation dialog, the one contract exo-confirm-dialog and an add-on's own confirmation dialogs
// share. When the dialog opens, the focus goes to its default button, and the keystroke that opened it does not press
// that button. When it closes, the focus goes back to the control that opened it; if the confirmed action removed
// that control, to the element that replaced it, else the next entry of its list, else the previous one, else the
// start of the page content; if the action changed the page, to the start of the page content; if it disabled the
// control while its request runs, back to it once it is enabled again.
// Exposed as `$dialogFocus` by the common bundle: an add-on reads it through `this.$dialogFocus` at call time. Each
// dialog creates its own controller, `create({content, defaultButton})`, whose two functions return the dialog's
// content element and the button that takes the focus; it calls `capture()` during the click that opens it, `open()`
// when it is shown, `close({confirmed})` when it is hidden, `confirmed` true when its action was confirmed, and
// `destroy()` before it is destroyed.
import * as openingKeystrokeGuard from './OpeningKeystrokeGuard.js';
import * as activationOrigin from './ActivationOrigin.js';

const FOCUSABLE_SELECTOR = 'a[href], button:not([disabled]), input:not([disabled]):not([type="hidden"]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';
const LIST_ITEM_SELECTOR = 'li, [role="listitem"], article, [role="article"]';
const LIST_SELECTOR = 'ul, ol, [role="list"], [role="feed"]';
const MAIN_CONTENT_ID = 'UISiteBody';
// how long the focus follows the opener after a confirmed action: longer while the opener is disabled by its request
const OPENER_FOLLOW_DELAY = 5000;
const DISABLED_OPENER_FOLLOW_DELAY = 10000;

function isShown(element) {
  // a closed side panel keeps its size but is hidden: it cannot take the focus
  return element?.isConnected
    && element !== document.body
    && element.getClientRects().length
    && window.getComputedStyle(element).visibility !== 'hidden'
    && !element.closest('[aria-hidden="true"]');
}

function siblings(element, direction) {
  const result = [];
  let sibling = element[direction];
  while (sibling) {
    result.push(sibling);
    sibling = sibling[direction];
  }
  return result;
}

function focusMainContent() {
  // the skip link's target, focusable on every portal page, made so here as well when a page lacks it
  const main = document.getElementById(MAIN_CONTENT_ID);
  if (main && !main.hasAttribute('tabindex')) {
    main.setAttribute('tabindex', '-1');
  }
  main?.focus?.();
}

export function captureOpener() {
  // the control that opened it, which a click does not always leave focused, or the button of the menu it was
  // chosen in: see ActivationOrigin.js
  return activationOrigin.menuOrigin(activationOrigin.element() || document.activeElement);
}

export function create({content, defaultButton}) {
  let shown = false;
  let opener = null;
  let pendingOpener = null;
  let openerContext = null;
  let stopFollowing = null;

  function isInDialog(element) {
    return !!content()?.contains?.(element);
  }

  function isFocusLost(active) {
    // on the body, on this closing dialog, on another dialog closing with it, or on the start of the page content
    // another dialog fell back to
    return !active
      || active === document.body
      || active.id === MAIN_CONTENT_ID
      || isInDialog(active)
      || !!active.closest?.('.v-dialog__content:not(.v-dialog__content--active)');
  }

  function shownFocusables(root) {
    return Array.from(root.querySelectorAll(FOCUSABLE_SELECTOR))
      .filter(element => isShown(element) && !isInDialog(element));
  }

  function firstShownFocusable(root) {
    return root?.isConnected && shownFocusables(root)[0];
  }

  function firstShownIn(entries, find) {
    for (const entry of entries || []) {
      const found = entry.isConnected && find(entry);
      if (found) {
        return found;
      }
    }
    return null;
  }

  function captureContext(element) {
    if (!element || element === document.body) {
      return null;
    }
    const item = element.closest(LIST_ITEM_SELECTOR);
    // the list entry: the item, or the wrapper it is the only content of
    let entry = item;
    while (entry?.parentElement
        && entry.parentElement.childElementCount === 1
        && !entry.parentElement.matches(LIST_SELECTOR)
        && entry.parentElement !== document.body) {
      entry = entry.parentElement;
    }
    return {
      href: window.location.href,
      item,
      parent: element.parentElement,
      followingEntries: entry && siblings(entry, 'nextElementSibling') || [],
      precedingEntries: entry && siblings(entry, 'previousElementSibling') || [],
    };
  }

  function focusInPlaceOf(context) {
    // replaced in place, else the next entry of its list, else the previous one, else the start of the page content
    const target = (context?.item?.isConnected || !context?.item) && firstShownFocusable(context?.parent)
      || firstShownFocusable(context?.item)
      || firstShownIn(context?.followingEntries, entry => firstShownFocusable(entry))
      || firstShownIn(context?.precedingEntries, entry => shownFocusables(entry).pop());
    if (target) {
      target.focus();
    } else {
      focusMainContent();
    }
  }

  function placeFocus(element, context) {
    if (context?.href && window.location.href !== context.href) {
      // the page changed in place (a deleted note opens its parent note): the start of the page content
      focusMainContent();
    } else if (!isShown(element)) {
      focusInPlaceOf(context);
    } else if (!element.disabled) {
      element.focus?.();
    }
    // a disabled opener (its action is running) takes the focus back once enabled again
  }

  function followOpener(element, context) {
    stopFollowing?.();
    if (!element || element === document.body) {
      return;
    }
    // only an opener disabled while its action runs takes the focus back once enabled: a click on the page leaves it
    // on the body too
    let openerWaits = !!element.disabled;
    let timeout = null;
    const observer = new MutationObserver(() => {
      const active = document.activeElement;
      // only while the focus is still on the opener, or lost: never take it from where the user moved it
      const focusLost = isFocusLost(active);
      if (!openerWaits && element.disabled) {
        openerWaits = true;
        window.clearTimeout(timeout);
        timeout = window.setTimeout(stop, DISABLED_OPENER_FOLLOW_DELAY);
      }
      if (!focusLost && active !== element) {
        stop();
      } else if (context?.href && window.location.href !== context.href) {
        placeFocus(element, context);
        stop();
      } else if (!isShown(element)) {
        placeFocus(element, context);
      } else if (openerWaits && !element.disabled) {
        if (focusLost) {
          placeFocus(element, context);
        }
        // the opener's request ended and the opener has the focus back: nothing left to follow
        stop();
      }
    });
    // the user acting on the page (a click on its text leaves the focus on the body as well) ends the following
    function stop() {
      observer.disconnect();
      window.clearTimeout(timeout);
      document.removeEventListener('pointerdown', stop, true);
      document.removeEventListener('keydown', stop, true);
      if (stopFollowing === stop) {
        stopFollowing = null;
      }
    }
    stopFollowing = stop;
    document.addEventListener('pointerdown', stop, true);
    document.addEventListener('keydown', stop, true);
    timeout = window.setTimeout(stop, openerWaits ? DISABLED_OPENER_FOLLOW_DELAY : OPENER_FOLLOW_DELAY);
    // the whole body: the confirmed action may hide or remove a far ancestor of the opener
    observer.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['disabled', 'style', 'class'],
    });
  }

  function guardOpeningKeystroke(event) {
    openingKeystrokeGuard.handle(event, guardOpeningKeystroke);
  }

  function redirectContainerFocus(event) {
    // v-dialog focuses its container, or the dialog's content itself: a screen reader would read the dialog twice
    // before its button
    const button = defaultButton();
    if (button && event.target !== button && event.target?.contains?.(button)) {
      window.removeEventListener('focusin', redirectContainerFocus, true);
      button.focus();
    }
  }

  function stopListening() {
    window.removeEventListener('focusin', redirectContainerFocus, true);
    openingKeystrokeGuard.stop(guardOpeningKeystroke);
  }

  function restoreFocus(confirmed) {
    const element = opener;
    const context = openerContext;
    opener = null;
    openerContext = null;
    window.setTimeout(() => {
      // only when the focus was not moved elsewhere meanwhile (e.g. by a side panel the dialog closed)
      if (!isFocusLost(document.activeElement)) {
        return;
      }
      placeFocus(element, context);
      if (context?.href && window.location.href !== context.href) {
        return;
      }
      // a confirmed action may remove, replace or disable the opener once its request ends; a cancelled one changes
      // nothing
      if (confirmed || element?.disabled || !isShown(element)) {
        followOpener(element, context);
      }
    }, 50);
  }

  return {
    capture() {
      // read during the click: a menu that closes on this click resets its button's aria-expanded before the
      // dialog is shown
      if (!shown) {
        pendingOpener = captureOpener();
      }
    },
    open() {
      if (shown) {
        return;
      }
      shown = true;
      stopFollowing?.();
      opener = pendingOpener || captureOpener();
      pendingOpener = null;
      openerContext = captureContext(opener);
      window.addEventListener('focusin', redirectContainerFocus, true);
      openingKeystrokeGuard.ignore(guardOpeningKeystroke);
      // fallback when the focus is outside the dialog, or still on a container of its button
      window.setTimeout(() => {
        const active = document.activeElement;
        const button = defaultButton();
        if (shown && button && active !== button && (!isInDialog(active) || active?.contains?.(button))) {
          button.focus?.();
        }
      }, 100);
    },
    close({confirmed} = {}) {
      if (!shown) {
        return;
      }
      shown = false;
      stopListening();
      restoreFocus(confirmed);
    },
    returnFocusTo(element) {
      // once closed, the dialog gives the focus to this element instead of its opener (e.g. a side panel the
      // confirmation closes gives it to the panel's own opener, its control that asked being hidden)
      opener = element;
      openerContext = captureContext(element);
    },
    destroy() {
      // destroyed while shown: nothing to give the focus back to. Destroyed once closed (a host removing the dialog on
      // its close event): the focus still follows the opener, bounded by its own delay and by the user's next action
      if (shown) {
        shown = false;
        stopListening();
        stopFollowing?.();
      }
    },
  };
}
