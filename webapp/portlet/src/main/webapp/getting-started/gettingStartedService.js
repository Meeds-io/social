import { spacesConstants } from '../js/spacesConstants.js';

export function getGettingStartedSteps(){
  return fetch(`${spacesConstants.PORTAL_CONTEXT}/${spacesConstants.PORTAL_REST}/getting-started`, {credentials: 'include'}).then(resp => resp.json());
}