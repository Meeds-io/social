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

// For a dialog that moves the focus to one of its buttons when it opens. A menu entry opens a dialog on Enter's
// keydown: the dialog's button can have the focus before that keystroke's keypress, or before the repeats of a
// held Enter, whose default action would click it and close the dialog at once.
// Stateless, exposed as `$openingKeystrokeGuard`: each dialog keeps its own listener, a method that calls
// `handle(event, listener)`, passes it to `ignore` when it opens, and to `stop` when it closes and before it is
// destroyed.
const KEYSTROKE_EVENTS = ['keydown', 'keypress', 'keyup'];

export function ignore(listener) {
  KEYSTROKE_EVENTS.forEach(type => window.addEventListener(type, listener, true));
}

export function stop(listener) {
  KEYSTROKE_EVENTS.forEach(type => window.removeEventListener(type, listener, true));
}

export function handle(event, listener) {
  if (event.type === 'keydown' && !event.repeat) {
    // a new keystroke: the one that opened the dialog is over
    stop(listener);
    return;
  }
  if (event.key === 'Enter' && event.type !== 'keyup') {
    event.preventDefault();
  }
  if (event.type === 'keyup') {
    stop(listener);
  }
}
