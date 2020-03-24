import { spacesConstants } from '../js/spacesConstants.js';

export function getGettingStartedSteps(){
  // getting language of the PLF
  const lang = window && window.eXo && window.eXo.env && window.eXo.env.portal && window.eXo.env.portal.language || 'en';
  return fetch(`${spacesConstants.PORTAL_CONTEXT}/${spacesConstants.PORTAL_REST}/getting-started?lang=${lang}`, {credentials: 'include'}).then(resp => resp.json());
}