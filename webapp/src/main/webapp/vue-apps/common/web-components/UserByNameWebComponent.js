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
      // '.' and '..' stay dot segments once encoded: they name no user
      const identityLookup = username === '.' || username === '..' ?
        Promise.resolve(null)
        : fetch(`/portal/rest/v1/social/identities/organization/${encodeURIComponent(username)}`, {
          method: 'GET',
          credentials: 'include',
        }).then(resp => resp?.ok && resp.json());
      identityLookup.then(identity => {
        if (identity) {
          // The markup holds no value: the name, the avatar and the full name
          // are set as attributes and text, never parsed as HTML
          const template = document.createElement('template');
          let htmlContent = '<a href="" target="_blank" class="d-inline-flex position-relative">';
          htmlContent += '  <div class="v-avatar ma-0 absolute-vertical-center z-index-one" style="height: 1.5em;min-width: 1.5em;width: 1.5em;">';
          htmlContent += '    <img src="" class="object-fit-cover">';
          htmlContent += '  </div>';
          htmlContent += '  <div class="overflow-hidden" style="margin-left: calc(1.6em + 4px);"></div>';
          htmlContent += '</a>';
          template.innerHTML = htmlContent;
          const node = template.content.firstElementChild;
          node.setAttribute('href', `/portal/profile/${encodeURIComponent(username)}`);
          node.querySelector('img').setAttribute('src', identity.profile.avatar);
          node.querySelector('.overflow-hidden').textContent = identity.profile.fullname;
          this.replaceWith(node);
        } else {
          throw new Error();
        }
      }).catch(e => {
        console.debug(e); // eslint-disable-line no-console
        const node = document.createElement('div');
        node.textContent = username;
        this.replaceWith(node);
      });
    }
  }

}

window.customElements.define('user-by-name', UserByName);
