export function isFeatureEnabled(featureName) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/features/${featureName}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.text();
    }
  }).then(featureEnabled => featureEnabled === 'true');
}