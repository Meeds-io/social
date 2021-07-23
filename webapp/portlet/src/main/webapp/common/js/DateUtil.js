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

export function getRelativeTimeLabelKey(dateObj) {
  if (!dateObj || !dateObj.getTime) {
    return '';
  }
  const periodInSeconds = (Date.now() - dateObj.getTime()) / 1000;
  if (periodInSeconds < 60) {
    return 'TimeConvert.label.Less_Than_A_Minute';
  } else if (periodInSeconds < 120) {
    return 'TimeConvert.label.About_A_Minute';
  } else if (periodInSeconds < 3600) {
    return 'TimeConvert.label.About_?_Minutes';
  } else if (periodInSeconds < 7200) {
    return 'TimeConvert.label.About_An_Hour';
  } else if (periodInSeconds < 86400) {
    return 'TimeConvert.label.About_?_Hours';
  } else if (periodInSeconds < 172800) {
    return 'TimeConvert.label.About_A_Day';
  } else if (periodInSeconds < 2592000) {
    return 'TimeConvert.label.About_?_Days';
  } else if (periodInSeconds < 5184000) {
    return 'TimeConvert.label.About_A_Month';
  } else {
    return 'TimeConvert.label.About_?_Months';
  }
}

export function getRelativeTimeValue(dateObj) {
  if (!dateObj || !dateObj.getTime) {
    return '';
  }
  const periodInSeconds = (Date.now() - dateObj.getTime()) / 1000;
  if (periodInSeconds >= 120 && periodInSeconds < 3600) {
    return Math.round(periodInSeconds / 60);
  } else if (periodInSeconds >= 7200 && periodInSeconds < 86400) {
    return Math.round(periodInSeconds / 3600);
  } else if (periodInSeconds >= 172800 && periodInSeconds < 2592000) {
    return Math.round(periodInSeconds / 86400);
  } else if (periodInSeconds >= 5184000) {
    return Math.round(periodInSeconds / 2592000);
  }
}

export function getShortRelativeTimeLabelKey(dateObj) {
  if (!dateObj || !dateObj.getTime) {
    return '';
  }
  const periodInMinutes = parseInt((Date.now() - dateObj.getTime()) / 60000);
  if (periodInMinutes < 1) {
    return 'TimeConvert.label.Short.Now';
  } else {
    const periodInHours = parseInt(periodInMinutes / 60);
    if (periodInHours < 1) {
      return 'TimeConvert.label.Short.Minutes';
    } else {
      const periodInDays = parseInt(periodInHours / 24);
      if (periodInDays < 1) {
        return 'TimeConvert.label.Short.Hours';
      } else {
        const periodInMonths = parseInt(periodInDays / 30);
        if (periodInMonths < 1) {
          return 'TimeConvert.label.Short.Days';
        } else  if (periodInMonths < 12) {
          return 'TimeConvert.label.Short.Months';
        } else {
          return 'TimeConvert.label.Short.Years';
        }
      }
    }
  }
}

export function getShortRelativeTimeValue(dateObj) {
  if (!dateObj || !dateObj.getTime) {
    return '';
  }
  const periodInMinutes = parseInt((Date.now() - dateObj.getTime()) / 60000);
  if (periodInMinutes > 1) {
    const periodInHours = parseInt(periodInMinutes / 60);
    if (periodInHours < 1) {
      return periodInMinutes;
    } else {
      const periodInDays = parseInt(periodInHours / 24);
      if (periodInDays < 1) {
        return periodInHours;
      } else {
        const periodInMonths = parseInt(periodInDays / 30);
        if (periodInMonths < 1) {
          return periodInDays;
        } else  if (periodInMonths < 12) {
          return periodInMonths;
        } else {
          return parseInt(periodInMonths / 12);
        }
      }
    }
  }
}