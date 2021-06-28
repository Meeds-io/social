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

export function initTipTip(selectorOrElement, labels) {
  const userLinks = $(selectorOrElement).find('a[href*="/profile/"]');
  $.each(userLinks, (idx, el) => {
    $(el).userPopup({
      restURL: `${eXo.env.portal.context}/${eXo.env.portal.rest}/social/people/getPeopleInfo/{0}.json`,
      labels: labels,
      content: false,
      defaultPosition: 'left',
      keepAlive: true,
      maxWidth: '240px',
    });
  });
}
