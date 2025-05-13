class ContentLink extends HTMLElement {

  constructor() {
    super();
  }

  connectedCallback() {
    const dataObjectAttr = this.textContent?.replace?.('/', '')?.trim?.();
    if (dataObjectAttr?.length && dataObjectAttr.includes(':')) {
      const objectParts = dataObjectAttr.split(':');
      fetch(`/social/rest/contentLinks/link/${objectParts[0]}/${objectParts[1]}`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => resp?.ok && resp.json()).then(link => {
        if (link) {
          const template = document.createElement('template');
          template.innerHTML = `<a href="${link.uri}" data-object="${dataObjectAttr}" contenteditable="false" class="content-link"><i aria-hidden="true" class="v-icon notranslate ${link.icon} theme--light icon-default-color" style="font-size: 16px; margin: 0 4px;"></i>${link.title}</a>`;
          const node = template.content.firstElementChild;
          this.replaceWith(node);
        } else {
          throw new Error();
        }
      }).catch(() => {
        const template = document.createElement('template');
        template.innerHTML = `<a data-object="${dataObjectAttr}" contenteditable="false" class="content-link"><i aria-hidden="true" class="v-icon notranslate fa-times theme--light icon-default-color" style="font-size: 16px; margin: 0 4px;"></i></a>`;
        const node = template.content.firstElementChild;
        this.replaceWith(node);
      });
    }
  }

}

window.customElements.define('content-link', ContentLink);
