export const avatarExcceedsLimitError = 'AVATAR_EXCEEDS_LIMIT';
export const bannerExcceedsLimitError = 'BANNER_EXCEEDS_LIMIT';
export const MAX_RANDOM_NUMBER = 100000;

export function upload(file, uploadId) {
  if (!uploadId) {
    uploadId = generateRandomId();
  }
  const uploadUrl = `${eXo.env.portal.context}/upload?uploadId=${uploadId}&action=upload`;

  const formData = new FormData();
  formData.append('file', file);

  return fetch(uploadUrl, {
    method: 'POST',
    credentials: 'include',
    body: formData,
  }).then(resp => resp && resp.ok && uploadId);
}

export function generateRandomId() {
  const random = Math.round(Math.random() * MAX_RANDOM_NUMBER);
  const now = Date.now();
  return `${random}-${now}`;
}