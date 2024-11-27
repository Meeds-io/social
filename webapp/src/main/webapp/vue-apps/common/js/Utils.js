const TEXTAREA = document.createElement('textarea');
const HTML_ENTITIES = {
  nbsp: ' ',
  amp: '&',
  quot: '"',
  lt: '<',
  gt: '>'
};

export function htmlToText(htmlContent) {
  if (!htmlContent) {
    return '';
  }
  let content = htmlContent.replace(/<[^>]+>/g, ' ').trim();
  TEXTAREA.innerHTML = content;
  content = TEXTAREA.value;
  return content.replace(/[\r|\n|\t]/g, ' ').replace(/ +(?= )/g,' ').trim();
}

export function replaceHtmlEntites(htmlContent) {
  return htmlContent.replace(new RegExp(`&(${Object.keys(HTML_ENTITIES).join('|')});`, 'g'), (_match, entity) => HTML_ENTITIES[entity]);
}

export function trim(text) {
  return text && text.trim().replace(/(<p>(&nbsp;)*(\\n\\r\\t)*<\/p>)*(<div>(&nbsp;)*( \\n\\r\\t)*<\/div>)*(\\r)*(\\n)*(\\t)*/g, '') || '';
}

export function includeExtensions(suffix) {
  const modules = Object.keys(window.requirejs.s.contexts._.registry)
    .filter(definedMofule => definedMofule.includes(suffix));
  if (modules?.length) {
    return Promise.all(modules.map(module => new Promise(resolve =>
      window.require([module], app => Promise.resolve(app?.init?.())
        .then(resolve))
    )));
  } else {
    return Promise.resolve();
  }
}

export function blobToBase64(blob) {
  return new Promise(resolve => {
    const reader = new FileReader();
    reader.onload = e => resolve(e.target.result);
    reader.readAsDataURL(blob);
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

export function toLinkUrl(url) {
  if (url?.indexOf?.('./') === 0) {
    url = `${window.location.pathname.replace(/\/$/g, '')}${url.replace(/\.\//g, '/')}`;
  }
  if (url?.indexOf?.('/') === 0) {
    url = `${window.location.origin}${url}`;
  }
  const useNonSSL = url?.indexOf('http://') === 0;
  url = Autolinker.parse(url || '', {
    urls: true,
    email: false,
    phone: false,
    mention: false,
    hashtag: false,
  })?.[0]?.getUrl?.()?.replace?.('javascript:', '');
  if (useNonSSL) {
    return url;
  } else {
    return url?.replace?.('http://', 'https://');
  }
}