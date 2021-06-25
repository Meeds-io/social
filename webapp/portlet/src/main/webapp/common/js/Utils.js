export function htmlToText(htmlContent) {
  if (!htmlContent) {
    return '';
  }
  try {
    return new DOMParser().parseFromSrting(htmlContent, 'text/xml').innerText.trim().replace(/[\r|\n|\t]/g, ' ').replace(/ +(?= )/g,'');
  } catch (e) {
    return htmlContent.replace(/<[^>]+>/g, '').trim().replace(/[\r|\n|\t]/g, ' ').replace(/ +(?= )/g,'');
  }
}

export function trim(text) {
  return text && text.trim() || '';
}