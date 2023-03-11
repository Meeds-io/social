$(document).ready(installScrollControlListener);

function installScrollControlListener() {
  const $siteBody = $('#UISiteBody');
  if (!$siteBody.data('scroll-control')) {
    $siteBody.data('scroll-control', 'true');
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
    document.querySelector('.MiddleToolBarTDContainer .MiddleToolBar').appendChild(shadowBox);
    $siteBody[0].addEventListener('scroll', controlBodyScrollClass, false);
    controlBodyScrollClass();
  }
}

function controlBodyScrollClass() {
  const $siteBody = $('#UISiteBody');
  const topBarBoxShadow = document.querySelector('#TopBarBoxShadow');
  if($siteBody.scrollTop()) {
    if (topBarBoxShadow.style.visibility === 'hidden') {
      document.querySelector('#TopBarBoxShadow').style.visibility = '';
    }
  } else {
    if (topBarBoxShadow.style.visibility !== 'hidden') {
      document.querySelector('#TopBarBoxShadow').style.visibility = 'hidden';
    }
  }
}