const DIGIT_PATTERN = /^[1-9][0-9]*$/g;

/**
 * Return Date object from string value.
 * The string value can of type:
 * - Date with format: dd/MM/yyyy
 * - Date with ISO 8601 format : yyyy-MM-dd
 * - Date timestamp
 * 
 * @param {String} value String or Integer value of date
 * @param {Boolean} isISOString if true, the date will be parsed by ISO 8601 format
 * @returns {Date} Date object
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
    lang = lang.replace(/[-_][a-z]+$/i, '');
    return new window.Intl.DateTimeFormat(lang, format).format(dateObj);
  } else {
    return getISODate(dateObj);
  }
}
