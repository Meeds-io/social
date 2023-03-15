$(document).ready(installScrollControlListener);

function installScrollControlListener() {
  const siteBody = document.querySelector('#UISiteBody') || document.querySelector('#UIPageBody');
  if (!siteBody.getAttribute('scroll-control')) {
    siteBody.classList.add('overflow-y-auto');
    siteBody.style.maxHeight = '100vh';
    siteBody.setAttribute('scroll-control', 'true');
    const middleBar = document.querySelector('.MiddleToolBarTDContainer .MiddleToolBar');
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
  const siteBody = document.querySelector('#UISiteBody') || document.querySelector('#UIPageBody');
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