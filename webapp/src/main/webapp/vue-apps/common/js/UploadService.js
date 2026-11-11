export const avatarExcceedsLimitError = 'AVATAR_EXCEEDS_LIMIT';
export const bannerExcceedsLimitError = 'BANNER_EXCEEDS_LIMIT';
export const MAX_RANDOM_NUMBER = 100000;
const SAFARI_EMPTY_BODY_VERSION = 26;
const IOS_FROZEN_VERSION = [18, 6];

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
    } else {
      throw new Error('Uploaded resource not found');
    }
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

  return toUploadableFile(file).then(uploadableFile => {
    const formData = new FormData();
    formData.append('file', uploadableFile);

    const headers = {
      method: 'POST',
      credentials: 'include',
      body: formData,
    };
    if (signal) {
      headers.signal = signal;
    }
    return fetch(uploadUrl, headers).then(resp => resp && resp.ok && uploadId);
  });
}

// WebKit 26 and later (Safari 26, and every browser on iOS 26) can send a
// file picked on the device as an empty request body, whether in a form or as
// the body itself: the server then stores 0 bytes. A copy held in memory is
// sent whole, so on that engine a picked file is copied before it is sent.
// The promise rejects when the copy does not hold the file's size, rather
// than letting an empty file be stored.
export function toUploadableFile(file) {
  if (!(file instanceof File) || !file.size || !isEmptyBodyWebKit()) {
    return Promise.resolve(file);
  }
  return file.arrayBuffer().then(content => {
    if (content?.byteLength !== file.size) {
      throw new Error('Picked file unreadable');
    }
    return new File([content], file.name, {
      type: file.type,
      lastModified: file.lastModified,
    });
  });
}

// The copy delays the request, which some upload pollers do not wait for, so
// it is limited to the engine versions that send empty bodies. Safari tells
// its version in `Version/`; from 26 on it freezes the iOS version it reports
// at 18.6 or later. The other iOS browsers, and an app added to the home
// screen, have no `Version/` and report the iOS version, frozen or real:
// 18.6 and later is taken as affected.
function isEmptyBodyWebKit() {
  if (window.navigator?.vendor !== 'Apple Computer, Inc.') {
    return false;
  }
  const userAgent = window.navigator.userAgent || '';
  const safariVersion = userAgent.match(/\bVersion\/(\d+)/);
  if (safariVersion) {
    return Number(safariVersion[1]) >= SAFARI_EMPTY_BODY_VERSION;
  }
  const iosVersion = userAgent.match(/ OS (\d+)_(\d+)/);
  if (iosVersion) {
    const major = Number(iosVersion[1]);
    return major > IOS_FROZEN_VERSION[0]
      || (major === IOS_FROZEN_VERSION[0] && Number(iosVersion[2]) >= IOS_FROZEN_VERSION[1]);
  }
  return false;
}

export function generateRandomId() {
  const random = Math.round(Math.random() * MAX_RANDOM_NUMBER);
  const now = Date.now();
  return `${random}-${now}`;
}