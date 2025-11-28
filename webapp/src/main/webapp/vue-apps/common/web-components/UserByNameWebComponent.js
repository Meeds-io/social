class UserByName extends HTMLElement {

  constructor(params) {
    super(params);
  }

  connectedCallback() {
    let username = this.textContent?.replace?.(/^@/, '')?.trim?.();
    if (username?.length) {
      if (username.includes(':')) {
        username = username.split(':').pop();
      }
      fetch(`/portal/rest/v1/social/identities/organization/${username}`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => resp?.ok && resp.json()).then(identity => {
        if (identity) {
          const template = document.createElement('template');
          let htmlContent = `<a href="/portal/profile/${username}" target="_blank" class="d-inline-flex position-relative">`;
          htmlContent += '  <div class="v-avatar ma-0 absolute-vertical-center z-index-one" style="height: 1.5em;min-width: 1.5em;width: 1.5em;">';
          htmlContent += `    <img src="${identity.profile.avatar}" class="object-fit-cover">`;
          htmlContent += '  </div>';
          htmlContent += `  <div class="overflow-hidden" style="margin-left: calc(1.6em + 4px);">${identity.profile.fullname}</div>`;
          htmlContent += '</a>';
          template.innerHTML = htmlContent;
          const node = template.content.firstElementChild;
          this.replaceWith(node);
        } else {
          throw new Error();
        }
      }).catch(e => {
        console.debug(e); // eslint-disable-line no-console
        const template = document.createElement('template');
        template.innerHTML = `<div>${username}</div>`;
        const node = template.content.firstElementChild;
        this.replaceWith(node);
      });
    }
  }

}

window.customElements.define('user-by-name', UserByName);
