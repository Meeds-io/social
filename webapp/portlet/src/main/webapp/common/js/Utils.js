export function htmlToText(htmlContent) {
  if (!htmlContent) {
    return '';
  }
  try {
    return new DOMParser().parseFromSrting(htmlContent, 'text/xml').innerText.trim();
  } catch (e) {
    return htmlContent.replace(/<[^>]+>/g, '').trim();
  }
}

export function trim(text) {
  return text && text.trim() || '';
}