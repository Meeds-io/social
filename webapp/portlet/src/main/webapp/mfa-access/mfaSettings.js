export function getSettings() {
  return fetch('/portal/rest/mfa/settings', {credentials: 'include'})
    .then((resp) => resp && resp.ok && resp.json());
}

export function generateQrCode(url) {
  window.require(['SHARED/qrcode'], () => {
    /*global QRious*/
    const qr = new QRious({
      size: 350,
      value: url
    });
    return qr.toDataURL();
  });
}



