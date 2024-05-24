import {registerExtension} from './extensions.js';

const lang = eXo?.env?.portal?.language || 'en';

const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;
export function init() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/profile/settings/manager`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  }).then(profileSetting => {
    if (profileSetting.active) {
      exoi18n.loadLanguageAsync(lang, url).then(i18n => {
        registerExtension(i18n.t('organizationalChart.header.label'));
      });
    }
  });
}

