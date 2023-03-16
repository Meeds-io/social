const TEXTAREA = document.createElement('textarea');

export function htmlToText(htmlContent) {
  if (!htmlContent) {
    return '';
  }
  let content = htmlContent.replace(/<[^>]+>/g, ' ').trim();
  TEXTAREA.innerHTML = content;
  content = TEXTAREA.value;
  return content.replace(/[\r|\n|\t]/g, ' ').replace(/ +(?= )/g,' ').trim();
}

export function trim(text) {
  return text && text.trim().replace(/(<p>(&nbsp;)*(\\n\\r\\t)*<\/p>)*(<div>(&nbsp;)*( \\n\\r\\t)*<\/div>)*(\\r)*(\\n)*(\\t)*/g, '') || '';
}

export function includeExtensions(suffix) {
  Object.keys(window.requirejs.s.contexts._.registry)
    .filter(definedMofule => definedMofule.includes(suffix))
    .forEach(module => {
      window.require([module], app => app.init && app.init());
    });
}

export function convertImageDataAsSrc(imageData) {
  if (Array.isArray(imageData)) {
    let binary = '';
    const bytes = new Uint8Array(imageData);
    bytes.forEach(byte => binary += String.fromCharCode(byte));
    return `data:image/png;base64,${btoa(binary)}`;
  } else {
    return imageData;
  }
}