import {registerExtension} from './extensions.js';

const lang = eXo?.env?.portal?.language || 'en';
const url = `/social/i18n/locale.portlet.Portlets?lang=${lang}`;

export function init() {
  if (!eXo?.env?.portal?.userIdentityId
    || !eXo.env.portal.isOrganizationalChartEnabled) {
    return;
  }
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => registerExtension(i18n.t('organizationalChart.header.label')));
}
