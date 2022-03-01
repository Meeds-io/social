CKEDITOR.dialog.add( 'videoDialog', function( editor ) {
    return {
        title: editor.lang.video.dialogTitle,
        minWidth: 500,
        minHeight: 60,
        resizable: CKEDITOR.DIALOG_RESIZE_NONE,
        contents: [
            {
                id: 'main',
                label: '',
                elements: [
                  {
                      type: 'hbox',
                      widths: [ '480px' ],
                      children: [
                        {
                            type: 'text',
                            id: 'url_video',
                            label: '',
                            onInput : function() {
                              if(this.getValue() === '') {
                                setValidationStatus('', editor);
                                this.getDialog().getButton('ok').disable();
                                this.getDialog().getButton('ok').getElement().setAttribute('disabled', 'disabled');
                              } else if(isValidURL(this.getValue())) {
                                setValidationStatus('ok', editor);
                                this.getDialog().getButton('ok').enable();
                                this.getDialog().getButton('ok').getElement().removeAttribute('disabled');
                              } else {
                                setValidationStatus('nok', editor);
                                this.getDialog().getButton('ok').disable();
                                this.getDialog().getButton('ok').getElement().setAttribute('disabled', 'disabled');
                              }
                            }
                        }
                      ]
                  }
                ]
            }
        ],
        onLoad: function() {
          const parentElement = this.getElement();

          parentElement.addClass('videoDialog').addClass('uiPopupNews');
          parentElement.removeClass('cke_reset_all');
          parentElement.findOne('.cke_dialog_title').$.className += ' popupHeader';
          parentElement.findOne('.cke_dialog_close_button').$.className = 'uiIconClose cke_dialog_close_button';
          parentElement.findOne('input.cke_dialog_ui_input_text').$.className = 'videoURL';
          parentElement.findOne('.cke_dialog_ui_button_ok').$.className = 'btn btn-primary';
          parentElement.findOne('.cke_dialog_ui_button_cancel').$.className = 'btn';

          parentElement.findOne('.videoURL').$.placeholder = editor.lang.video.dialogURLInputPlaceholder;

          const backgroundMask = document.querySelector('.cke_dialog_background_cover');
          backgroundMask.classList.remove('uiPopupWrapper');
          backgroundMask.classList.add('uiPopupWrapperNews');
          backgroundMask.style.backgroundColor = '';
          backgroundMask.style.opacity = '';
        },
        onShow: function(){
          const urlInputElement = this.getContentElement('main', 'url_video').getElement();
          setValidationStatus(urlInputElement, '');
          this.disableButton('ok');
          this.getButton('ok').getElement().setAttribute('disabled', 'disabled');
        },
        onFocus: function() {
          // do nothing
        },
        onOk: function(){
            var dialog = this;

            var video = detect(dialog);
            var url = '';

            if(video.provider == 'youtube'){
                url = 'https://www.youtube.com/embed/' + video.id_video + '?autohide=1&controls=1&showinfo=0';
            }
            else if(video.provider == 'vimeo'){
                url = 'https://player.vimeo.com/video/' + video.id_video + '?portrait=0';
            }
            else if(video.provider == 'dailymotion'){
                url = 'https://www.dailymotion.com/embed/video/' + video.id_video;
            }

            var p = new CKEDITOR.dom.element('div');
            p.setAttribute('class', 'video');
            p.setAttribute('contenteditable', 'false');

            var iframe = new CKEDITOR.dom.element('iframe');
            iframe.setAttribute('src', url);
            iframe.setAttribute('frameborder', '0');
            iframe.setAttribute('allow', 'fullscreen');
            p.append(iframe);

            editor.insertElement(p);
        }
    };
});


/**
 * Detect the video provider (youtube, vimeo or dailymotion)
 */
function detect(dialog){
    var url = dialog.getValueOf( 'main', 'url_video' );
    var id = '';
    var provider = '';
    var fin= '';

    if(url.indexOf('youtu.be') >= 0){
        provider = 'youtube';
        id = url.substring(url.lastIndexOf('/')+1, url.length);
    }
    if(url.indexOf('youtube') >= 0){
        provider = 'youtube';
        if(url.indexOf('</iframe>') >= 0){
            fin = url.substring(url.indexOf('embed/')+6, url.length)
            id = fin.substring(fin.indexOf('"'), 0);
        }else{
            if(url.indexOf('&') >= 0)
                id = url.substring(url.indexOf('?v=')+3, url.indexOf('&'));
            else
                id = url.substring(url.indexOf('?v=')+3, url.length);
        }
    }
    if(url.indexOf('vimeo') >= 0){
        provider = 'vimeo'
        if(url.indexOf('</iframe>') >= 0){
            fin = url.substring(url.lastIndexOf('vimeo.com/"')+6, url.indexOf('>'))
            id      = fin.substring(fin.lastIndexOf('/')+1, fin.indexOf('"',fin.lastIndexOf('/')+1))
        }else{
            id = url.substring(url.lastIndexOf('/')+1, url.length)
        }
    }
    if(url.indexOf('dai.ly') >= 0){
        provider = 'dailymotion';
        id          = url.substring(url.lastIndexOf('/')+1, url.length);
    }
    if(url.indexOf('dailymotion') >= 0){
        provider = 'dailymotion';
        if(url.indexOf('</iframe>') >= 0){
            fin = url.substring(url.indexOf('dailymotion.com/')+16, url.indexOf('></iframe>'))
            id      = fin.substring(fin.lastIndexOf('/')+1, fin.lastIndexOf('"'))
        } else {
            if(url.indexOf('_') >= 0)
                id = url.substring(url.lastIndexOf('/')+1, url.indexOf('_'))
            else
                id = url.substring(url.lastIndexOf('/')+1, url.length);
        }
    }
    return {'provider': provider, 'id_video': id};
}

function isValidURL(url) {
  var urlVideoRegex = /^http(s)?:\/\/?(youtu\.be|(www\.)?youtube\.com|(www\.)?vimeo\.com|dai\.ly|(www\.)?dailymotion\.com|dailymotion\.com)\/(.+)/;
  return urlVideoRegex.test(url);
}

function setValidationStatus(status, editor) {
  const urlInputElement = document.querySelector('.videoURL');
  var validationStatusElement = urlInputElement.parentNode.querySelector('span');
  if(!validationStatusElement) {
    validationStatusElement = document.createElement('span');
    urlInputElement.parentNode.insertBefore(validationStatusElement, urlInputElement);
  }

  if(status === 'ok') {
    validationStatusElement.classList = 'uiIconVideoURLOk';
    validationStatusElement.title = editor.lang.video.urlValidationOk;
  } else if(status === 'nok') {
    validationStatusElement.classList = 'uiIconVideoURLNok';
    validationStatusElement.title = editor.lang.video.urlValidationKo;
  } else {
    validationStatusElement.classList = '';
  }
}