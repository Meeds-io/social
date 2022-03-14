const DIGIT_PATTERN = /^[1-9][0-9]*$/g;

/**
 * Return Date object from string value.
 * The string value can of type:
 * - Date with format: dd/MM/yyyy
 * - Date with ISO 8601 short format : yyyy-MM-dd
 * - Date with ISO 8601 long format : YYYY-MM-DDTHH:mm:ss.sssZ
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
    if (value.length > 10) {
      //long ISO format
      return new Date(value);
    } else {
      //short ISO format
      //we need to set year, month, date, manually so that it use the current user time.

      //if we do new Date(value), if will generate a date with Time = 00:00 UTC, then when it is translated
      //in user timezone, the day can change
      const [year, month, date] = value.trim().split('-');
      const dateObj = new Date();
      dateObj.setYear(year);
      dateObj.setMonth(parseInt(month)-1);
      dateObj.setDate(date);
      return dateObj;
    }
  } else {
    const [date, month, year] = value.trim().split('/');
    const dateObj = new Date();
    dateObj.setYear(year);
    dateObj.setMonth(parseInt(month)-1);
    dateObj.setDate(date);
    return dateObj;
  }
}

/**
 * Return the ISO-8601 Date String value with format
 * YYYY-MM-DD, for example 2011-10-05
 *
 * @param {Date} dateObj Date object
 * @returns {String} ISO 8601 date string with format YYYY-MM-DD
 */
export function getISODate(dateObj) {
  if (!dateObj) {
    return null;
  }
  if (dateObj.getTimezoneOffset() < 0) {
    dateObj.setMinutes(dateObj.getMinutes() - dateObj.getTimezoneOffset());
  }
  return dateObj.toISOString().substr(0,10);
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
