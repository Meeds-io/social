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

