function() {

  $(document).ready(installScrollControlListener);
  window.onresize = computeViewPort;

  function computeViewPort() {
    if (window.innerWidth < (eXo.env.portal?.vuetifyPreset?.breakpoint?.thresholds?.xs || 768)) {
      document.documentElement.style.setProperty('--100vh', `${window.innerHeight}px`);
    } else {
      document.documentElement.style.removeProperty('--100vh');
    }
  }

  function installScrollControlListener() {
    computeViewPort();
    const siteBody = document.querySelector(getScrollableSelector()) || document.querySelector('#UIPageBody');
    if (!siteBody) {
      return;
    }
    siteBody.classList.add('site-scroll-parent');
    if (!siteBody.getAttribute('scroll-control')) {
      siteBody.classList.add('overflow-y-auto');
      siteBody.classList.add('overflow-x-hidden');
      siteBody.style.maxHeight = `calc(var(--100vh, 100vh) - ${siteBody.getBoundingClientRect().y}px)`;
      siteBody.setAttribute('scroll-control', 'true');
      siteBody.addEventListener('scroll', controlBodyScrollClass, false);
      controlBodyScrollClass();
    }
  }

  function controlBodyScrollClass() {
    const siteBody = document.querySelector('.site-scroll-parent')
      || document.querySelector(getScrollableSelector())
      || document.querySelector('#UIPageBody');
    if(siteBody.scrollTop) {
      if (!siteBody.classList?.contains?.('site-scroll-top')) {
        siteBody.classList.add('site-scroll-top');
      }
      if (!document.body.classList?.contains?.('scroll-top')) {
        document.body.classList.add('scroll-top');
        siteBody.style.maxHeight = `calc(var(--100vh, 100vh) - ${siteBody.getBoundingClientRect().y}px)`;
      }
    } else {
      siteBody.classList.remove('site-scroll-top');
      document.body.classList.remove('scroll-top');
    }
  }

  function getScrollableSelector() {
    return document.querySelector('.page-scroll-content')
      && '.page-scroll-content'
      || (document.querySelector('#UISiteBody .UITopBarContainer')
          && '#UIPageBody'
          || '#UISiteBody');
  }
}();