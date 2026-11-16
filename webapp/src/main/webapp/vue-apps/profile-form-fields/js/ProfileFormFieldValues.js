/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import {hasValue} from './ProfileFormFieldRules.js';

// How a stored list held by a single-valued attribute is shown, and compared
const JOIN_SEPARATOR = ', ';

// The fields of an account's edition: one per listed attribute the user's
// answer serves, holding the stored value as the server's rules read it,
// with the list's definition of the attribute (an attribute the answer
// lacks, which this node's settings do not hold yet, gets no field); and by
// attribute, the stored value as it is compared, the values its format
// rules accept unchanged, and the dropdown items for stored values no
// option is
export function toEditionFields(settings, properties) {
  const served = {};
  properties
    .filter(property => property?.propertyName)
    .forEach(property => served[property.propertyName] = property);
  const fields = {
    settings: settings.filter(setting => served[setting.propertyName]),
    values: {},
    snapshot: {},
    unchangedValues: {},
    legacyOptions: {},
  };
  fields.settings.forEach(setting => {
    const name = setting.propertyName;
    const entries = readStoredEntries(served[name], setting);
    fields.values[name] = toFieldValue(entries, setting);
    fields.snapshot[name] = toComparable(fields.values[name], setting);
    fields.unchangedValues[name] = fields.snapshot[name];
    fields.legacyOptions[name] = toLegacyOptions(entries, setting);
  });
  return fields;
}

// The values to write: the filled attributes on creation; on an account's
// edition, given the stored values, the changed ones, an emptied one as its
// clear
export function toData(settings, values, snapshot) {
  return settings
    .filter(setting => {
      const name = setting.propertyName;
      return snapshot ? isChanged(values[name], snapshot[name], setting) : hasValue(values[name]);
    })
    .map(setting => {
      const sent = toSentValue(values[setting.propertyName], setting);
      return setting.multiValued
        ? {propertyName: setting.propertyName, values: sent}
        : {propertyName: setting.propertyName, value: sent.length ? sent[0] : ''};
    });
}

// The values the user's answer serves for an attribute, read as the server's
// rules read the stored value, with the stored definition of the attribute:
// the served value, split on its commas under a multi-valued attribute, else
// the values of its children
export function readStoredEntries(property, setting) {
  const value = property?.value;
  if (typeof value === 'string' && value.length) {
    return setting.multiValued ? value.split(',').filter(entry => entry.length) : [value];
  }
  return (property?.children || [])
    .map(child => child?.value)
    .filter(entry => typeof entry === 'string' && entry.length);
}

export function findOption(setting, entry) {
  return (setting.propertyOptions || []).find(option => String(option?.id) === entry) || null;
}

export function optionLabel(option) {
  return option?.translatedValue || option?.value || '';
}

// The value the attribute's field shows: the option itself for a stored
// option id, the stored text otherwise, and a stored list under a
// single-valued attribute joined in one text
export function toFieldValue(entries, setting) {
  const toValue = entry => {
    const option = setting.dropdownList ? findOption(setting, entry) : null;
    return option ? option.id : entry;
  };
  if (setting.multiValued) {
    return entries.map(toValue);
  } else if (!entries.length) {
    return setting.dropdownList || setting.propertyType === 'user' ? null : '';
  } else if (entries.length === 1) {
    return toValue(entries[0]);
  }
  return entries.join(JOIN_SEPARATOR);
}

// The items a dropdown adds to its options for what it holds and no option
// is: a legacy text, or a stored list under a single-valued attribute, shown
// with each option id by its option's label
export function toLegacyOptions(entries, setting) {
  if (!setting.dropdownList) {
    return [];
  } else if (!setting.multiValued && entries.length > 1) {
    const label = entry => {
      const option = findOption(setting, entry);
      return option ? optionLabel(option) : entry;
    };
    return [{
      id: entries.join(JOIN_SEPARATOR),
      value: entries.map(label).join(JOIN_SEPARATOR),
    }];
  }
  return entries
    .filter(entry => !findOption(setting, entry))
    .map(entry => ({id: entry, value: entry}));
}

// A field's value in the form it is compared with the stored one: the list
// of its non-empty texts in order, one at most for a single-valued
// attribute. A blank text is a value, as the server reads a stored one, so
// that a stored blank value emptied in its field is a change
export function toComparable(value, setting) {
  let entries = [value];
  if (setting.multiValued) {
    entries = Array.isArray(value) ? value : [];
  }
  return entries
    .map(entry => (entry == null ? '' : String(entry)))
    .filter(entry => entry.length);
}

// A field's value in the form it is sent: the list of its non-blank texts in
// order, one at most for a single-valued attribute; an empty list is the
// clear
export function toSentValue(value, setting) {
  return toComparable(value, setting).filter(hasValue);
}

// Whether a field holds a change to write, against the stored value as
// toComparable gives it: its values, blanks dropped, differ from the stored
// ones, or they are the same and the administrator removed the blanks the
// stored value held. Blanks added alone are no change
export function isChanged(value, stored, setting) {
  const sent = toSentValue(value, setting);
  const storedSent = stored.filter(hasValue);
  if (!isSameList(sent, storedSent)) {
    return true;
  }
  return isSameList(toComparable(value, setting), sent) && !isSameList(stored, storedSent);
}

// Whether two lists of texts hold the same texts in the same order
export function isSameList(first, second) {
  return first.length === second.length && first.every((entry, index) => entry === second[index]);
}
