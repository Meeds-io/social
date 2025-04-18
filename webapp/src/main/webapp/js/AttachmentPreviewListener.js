function() {
    document.addEventListener('open-attachments-preview', event => {
      window.require(["SHARED/AttachmentPreview"], preview => {
          preview.init(event);
      });
    });
}();