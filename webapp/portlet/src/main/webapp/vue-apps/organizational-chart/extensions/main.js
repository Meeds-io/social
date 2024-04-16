import {registerExtension} from './extensions.js';

const lang = eXo?.env?.portal?.language || 'en';

const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

exoi18n.loadLanguageAsync(lang, url).then(i18n => {
  registerExtension(i18n.t('organizationalChart.header.label'));
});

