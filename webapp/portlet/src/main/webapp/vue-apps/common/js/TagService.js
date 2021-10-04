export function initTags(tooltipTitle) {
  const tags = document.querySelectorAll('.metadata-tag:not([title])');
  if (tags && tags.length) {
    tags.forEach(tagItem => {
      tagItem.title = tooltipTitle;
    });
  }
}