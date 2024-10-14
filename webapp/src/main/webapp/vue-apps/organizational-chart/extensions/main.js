import {registerExtension} from './extensions.js';

const lang = eXo?.env?.portal?.language || 'en';

const url = `/social/i18n/locale.portlet.Portlets?lang=${lang}`;
export function init() {
  if (!eXo?.env?.portal?.userIdentityId) {
    return;
  }
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

