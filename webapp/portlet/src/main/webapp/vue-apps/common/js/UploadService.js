export const avatarExcceedsLimitError = 'AVATAR_EXCEEDS_LIMIT';
export const bannerExcceedsLimitError = 'BANNER_EXCEEDS_LIMIT';
export const MAX_RANDOM_NUMBER = 100000;

export function getUploadProgress(uploadId) {
  return fetch(`${eXo.env.portal.context}/upload?uploadId=${uploadId}&action=progress`, {
    method: 'GET',
    credentials: 'include'
  }).then(resp => {
    return resp && resp.ok && resp.text();
  }).then(data => {
    data = JSON.parse(data.replace('upload', '"upload"'));
    data = data && data['upload'] || data;
    if (data[uploadId]) {
      if (data[uploadId] && data[uploadId].status === 'failed') {
        throw new Error('Upload error');
      }
      return data[uploadId].percent;
    }
    return 0;
  });
}

export function deleteUpload(uploadId) {
  return fetch(`${eXo.env.portal.context}/upload?uploadId=${uploadId}&action=delete`, {
    method: 'POST',
    credentials: 'include'
  });
}

export function abortUpload(uploadId) {
  return fetch(`${eXo.env.portal.context}/upload?uploadId=${uploadId}&action=abort`, {
    method: 'POST',
    credentials: 'include'
  });
}

export function upload(file, uploadId, signal) {
  if (!uploadId) {
    uploadId = generateRandomId();
  }
  const uploadUrl = `${eXo.env.portal.context}/upload?uploadId=${uploadId}&action=upload`;

  const formData = new FormData();
  formData.append('file', file);

  const headers = {
    method: 'POST',
    credentials: 'include',
    body: formData,
  };
  if (signal) {
    headers.signal = signal;
  }
  return fetch(uploadUrl, headers).then(resp => resp && resp.ok && uploadId);
}

export function generateRandomId() {
  const random = Math.round(Math.random() * MAX_RANDOM_NUMBER);
  const now = Date.now();
  return `${random}-${now}`;
}