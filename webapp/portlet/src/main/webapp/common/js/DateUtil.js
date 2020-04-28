const DIGIT_PATTERN = /^[1-9][0-9]*$/g;

/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
export function getDateObjectFromString(value, isISOString) {
  value = String(value).trim();
  if (new RegExp(DIGIT_PATTERN).test(value)) {
    return new Date(parseInt(value));
  }
  if (isISOString) {
    return new Date(value);
  } else {
    const [date, month, year] = value.trim().split('/');
    return new Date(year, parseInt(month) -1, date);
  }
}

/**
 * Return the ISO Date String value with format
 * yyy-MM-dd, for example 2020-04-21.
 * 
 * This method will consider timezone offset and will avoid to
 * return next or previous date due to Date.toISOString that considers
 * timezone offset when converting to string
 * 
 * @param {Date} dateObj Date object
 * @returns {String} ISO 8601 date string with format yyyy-MM-dd
 */
export function getISODate(dateObj) {
  if (!dateObj) {
    return null;
  }
  // toISOString will modify time using timezone index
  // This operation will compensate the diff to avoid switching
  // into next or previous date
  dateObj.setMinutes(dateObj.getMinutes() - dateObj.getTimezoneOffset());
  return dateObj.toISOString().substring(0, 10);
}

/**
 * Return a locale string presenting the Date object
 * switch user locale and a custom format.
 * 
 * If language is not specified, eXo.env.portal.language will be used.
 * If format is not specified, getISODate will be used
 * 
 * @param {Date} dateObj Date object
 * @param {String} format Intl.DateTimeFormat options
 * @param {String} lang used language to format date, if null, eXo.env.portal.language
 *        will be used instead
 * @returns {String} string representation of date object
 */
export function formatDateObjectToDisplay(dateObj, format, lang) {
  if (!lang) {
    lang = eXo.env.portal.language;
  }
  if (format) {
    return new window.Intl.DateTimeFormat(lang, format).format(dateObj);
  } else {
    return getISODate(dateObj);
  }
}
