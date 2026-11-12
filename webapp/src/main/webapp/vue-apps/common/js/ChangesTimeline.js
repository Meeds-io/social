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
/**
 * Merges the changes of several sources into one list, newest first, each
 * source read a page at a time. Every page reads it as
 * Vue.prototype.$changesTimeline: the version history drawer, a file's
 * Changes and a task's Changes. A source is an object with:
 * - id: its id;
 * - rank: its order among sources whose changes share a date, lowest first;
 * - load({before, limit}): a promise of {entries, next}, entries being the
 *   source's changes older than the cursor before (none for its first page),
 *   newest first, each as {time, key}: time in milliseconds, key ordering the
 *   source's changes of a same time, highest first; next being the cursor of
 *   its following page, or null once it has no more.
 *
 * A change is given out only while every source not yet exhausted has one
 * read ahead, so that no older change of a source is given out before a newer
 * one of another source not read yet. A source whose read fails is dropped:
 * the changes of the others still come, and the timeline tells it failed.
 *
 * @param {Array<object>} sources the sources
 * @param {number} pageSize the most changes each read of a source asks for
 * @returns {object} the timeline: next(count) gives the following changes,
 *          each as {sourceId, entry}; hasMore() tells whether any is left;
 *          hasFailed() whether a source could not be read
 */
export function createTimeline(sources, pageSize) {
  const states = sources.map(source => ({
    source,
    buffer: [],
    cursor: null,
    exhausted: false,
    failed: false,
  }));
  return {
    next: count => emit(states, pageSize, count, []),
    hasMore: () => states.some(state => state.buffer.length || !state.exhausted),
    hasFailed: () => states.some(state => state.failed),
  };
}

/**
 * Reads a source's next page into its buffer; a source whose read fails is
 * dropped and marked failed.
 *
 * @param {object} state a source's state
 * @param {number} pageSize the most changes the read asks for
 * @returns {Promise} settled once the page is buffered, or the source dropped
 */
function fill(state, pageSize) {
  return Promise.resolve()
    .then(() => state.source.load({before: state.cursor, limit: pageSize}))
    .then(page => {
      const entries = Array.isArray(page?.entries) && page.entries || [];
      state.buffer.push(...entries);
      state.cursor = page?.next || null;
      state.exhausted = !state.cursor || !entries.length;
    })
    .catch(() => {
      state.buffer = [];
      state.exhausted = true;
      state.failed = true;
    });
}

/**
 * @param {Array<object>} states the sources' states
 * @param {number} pageSize the most changes each read asks for
 * @returns {Promise} settled once every emptied buffer of a source not yet
 *          exhausted is refilled
 */
function refill(states, pageSize) {
  return Promise.all(states.filter(state => !state.buffer.length && !state.exhausted)
    .map(state => fill(state, pageSize)));
}

/**
 * @param {object} state1 a source's state, with a change buffered
 * @param {object} state2 another source's state, with a change buffered
 * @returns {number} negative when the first's next change comes first
 */
function compareHeads(state1, state2) {
  const entry1 = state1.buffer[0];
  const entry2 = state2.buffer[0];
  if (entry1.time !== entry2.time) {
    return entry2.time - entry1.time;
  }
  const rank1 = state1.source.rank || 0;
  const rank2 = state2.source.rank || 0;
  if (rank1 !== rank2) {
    return rank1 - rank2;
  }
  return compareKeys(entry2.key, entry1.key);
}

/**
 * @param {Array<object>} states the sources' states
 * @returns {object} the state whose buffered change comes first, none when
 *          no buffer holds a change
 */
function newest(states) {
  let first = null;
  for (const state of states) {
    if (state.buffer.length && (!first || compareHeads(state, first) < 0)) {
      first = state;
    }
  }
  return first;
}

/**
 * Gives out the newest change read, after refilling every emptied buffer,
 * until count of them are given out or no source has any left.
 *
 * @param {Array<object>} states the sources' states
 * @param {number} pageSize the most changes each read asks for
 * @param {number} count the most changes to give out
 * @param {Array<object>} merged the changes given out so far
 * @returns {Promise<Array<object>>} the changes given out, newest first
 */
function emit(states, pageSize, count, merged) {
  if (merged.length >= count) {
    return Promise.resolve(merged);
  }
  return refill(states, pageSize).then(() => {
    const first = newest(states);
    if (!first) {
      return Promise.resolve(merged);
    }
    merged.push({sourceId: first.source.id, entry: first.buffer.shift()});
    return emit(states, pageSize, count, merged);
  });
}

/**
 * @param {number|string} key1 a change's key
 * @param {number|string} key2 another change's key of the same source
 * @returns {number} negative when key1 is lower
 */
function compareKeys(key1, key2) {
  if (typeof key1 === 'number' && typeof key2 === 'number') {
    return key1 - key2;
  }
  return String(key1).localeCompare(String(key2), [], {numeric: true});
}
