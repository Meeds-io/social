/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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

(function () {

  const i18NFetchedURLsKey = `${window.location.pathname}_${eXo.env.portal.language}_${eXo.env.client.assetsVersion}_URLs`;
  const i18NFetchedURLsValue = sessionStorage && sessionStorage.getItem(i18NFetchedURLsKey);
  const i18NFetchedURLs = i18NFetchedURLsValue && JSON.parse(i18NFetchedURLsValue) || [];
  window.eXoI18N = {
    executingFetches: {},
    i18NFetchedURLs,
  };

  const preloadedMessages = {};
  window.eXoI18N.i18NFetchedURLs.forEach((url, index) => {
    const cachedMessages = sessionStorage && sessionStorage.getItem(url);
    if (cachedMessages && cachedMessages.length) {
      Object.assign(preloadedMessages, JSON.parse(cachedMessages));
    } else {
      delete window.eXoI18N.i18NFetchedURLs[index];
    }
  });
  window.eXoI18N.i18NFetchedURLs = window.eXoI18N.i18NFetchedURLs.filter(url => !!url);

  const i18nMessages = {};
  i18nMessages[eXo.env.portal.language] = preloadedMessages;
  const i18n = new VueI18n({
    locale: eXo.env.portal.language, // set locale
    fallbackLocale: 'en',
    messages: i18nMessages,
  });

  /**
   * Load translated strings from the given URLs and for the given language
   * @param {string} lang - Language to load strings for
   * @param {(string|string[])} urls - Single URL or array of URLs to load i18n files from
   * @returns {Promise} Promise giving the i18n object with loaded translated strings
   */
  function loadLanguageAsync(lang, urls) {
    if (!lang) {
      lang = i18n.locale;
    }

    if(typeof urls === 'string') {
      urls = [ urls ];
    }

    const promises = [];
    urls.forEach(url => {
      const promise = fetchLangFile(url, lang);
      if (promise) {
        promises.push(promise);
      }
    });
    return Promise.all(promises).then(() => i18n);
  }

  function fetchLangFile(url, lang) {
    if (url && url.indexOf('?') >= 0) {
      url = `${url}&v=${eXo.env.client.assetsVersion}`
    } else {
      url = `${url}?v=${eXo.env.client.assetsVersion}`
    }

    if (window.eXoI18N.executingFetches[url]) {
      return window.eXoI18N.executingFetches[url];
    } else if (window.eXoI18N.i18NFetchedURLs.indexOf(url) >= 0) {
      return Promise.resolve(i18n);
    } else {
      const cachedMessages = sessionStorage && sessionStorage.getItem(url);
      const i18NFetch = (cachedMessages && Promise.resolve(JSON.parse(cachedMessages)) || fetch(url).then(resp => resp && resp.ok && resp.json()));
      window.eXoI18N.executingFetches[url] = i18NFetch;
      return i18NFetch
        .then(data => {
          if (data) {
            i18n.mergeLocaleMessage(lang, data);
            if (!cachedMessages && !eXo.developing) {
              try {
                sessionStorage.setItem(url, JSON.stringify(data));
              } catch (e) {
                // QuotaExceededError can be thrown, thus nothing to do here
              }
            }
          }
          return i18n;
        })
        .finally(() => {
          delete window.eXoI18N.executingFetches[url];
          window.eXoI18N.i18NFetchedURLs.push(url);
          try {
            sessionStorage.setItem(i18NFetchedURLsKey, JSON.stringify(window.eXoI18N.i18NFetchedURLs));
          } catch (e) {
            // QuotaExceededError can be thrown, thus nothing to do here
          }
          return i18n;
        });
    }
  }

  return {
    'loadLanguageAsync': loadLanguageAsync,
    'i18n': i18n,
  };
})();