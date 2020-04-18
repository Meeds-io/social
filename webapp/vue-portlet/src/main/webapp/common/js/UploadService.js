const MAX_RANDOM_NUMBER = 100000;

export function upload(file) {
  const random = Math.round(Math.random() * MAX_RANDOM_NUMBER);
  const now = Date.now();
  const uploadId = `${random}-${now}`;
  const uploadUrl = `${eXo.env.portal.context}/upload?uploadId=${uploadId}&action=upload`;

  const formData = new FormData();
  formData.append('file', file);

  return fetch(uploadUrl, {
    method: 'POST',
    credentials: 'include',
    body: formData,
  }).then(resp => resp && resp.ok && uploadId);
}
