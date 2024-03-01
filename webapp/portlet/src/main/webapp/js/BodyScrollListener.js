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
    const topBarHeight = document.querySelector('#UITopBarContainerParent')?.offsetHeight || 0;
    const bodyMaxHeight = `calc(var(--100vh, 100vh) - ${topBarHeight}px)`;

    const siteBody = document.querySelector(getScrollableSelector()) || document.querySelector('#UIPageBody');
    if (!siteBody) {
      return;
    }
    siteBody.classList.add('site-scroll-parent');
    if (!siteBody.getAttribute('scroll-control')) {
      siteBody.classList.add('overflow-y-auto');
      siteBody.classList.add('overflow-x-hidden');
      siteBody.style.maxHeight = bodyMaxHeight;
      siteBody.setAttribute('scroll-control', 'true');
      const middleBar = document.querySelector('.MiddleToolBarTDContainer .MiddleToolBar') || document.querySelector('#MiddleToolBarChildren');
      if (middleBar) {
        const shadowBox = document.createElement('div');
        shadowBox.id = 'TopBarBoxShadow';
        shadowBox.style.boxShadow = '0 6px 4px -4px rgb(0 0 0 / 30%)';
        shadowBox.style.position = 'absolute';
        shadowBox.style.width = '100vw';
        shadowBox.style.height = '56px';
        shadowBox.style.top = 0;
        shadowBox.style.left = 0;
        shadowBox.style.right = 0;
        shadowBox.style.zIndex = -1;
        shadowBox.style.visibility = 'hidden';
        middleBar.appendChild(shadowBox);
      }
      siteBody.addEventListener('scroll', controlBodyScrollClass, false);
      controlBodyScrollClass();
    }
  }

  function controlBodyScrollClass() {
    const siteBody = document.querySelector(getScrollableSelector()) || document.querySelector('#UIPageBody');
    const topBarBoxShadow = document.querySelector('#TopBarBoxShadow');
    if(siteBody.scrollTop) {
      if (topBarBoxShadow && topBarBoxShadow.style.visibility === 'hidden') {
        document.querySelector('#TopBarBoxShadow').style.visibility = '';
      }
    } else {
      if (topBarBoxShadow && topBarBoxShadow.style.visibility !== 'hidden') {
        document.querySelector('#TopBarBoxShadow').style.visibility = 'hidden';
      }
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