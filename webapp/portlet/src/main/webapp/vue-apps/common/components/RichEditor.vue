<template>
  <div class="overflow-hidden">
    <div class="richEditor">
      <textarea
        ref="editor"
        :id="ckEditorInstanceId"
        v-model="inputVal"
        cols="30"
        rows="10"
        class="textarea"></textarea>
      <v-progress-circular
        v-if="!editorReady"
        :width="3"
        indeterminate
        class="loadingRing position-absolute" />
      <div
        :v-show="editorReady"
        :id="buttonId"
        :class="!validLength && 'tooManyChars' || ''"
        class="activityCharsCount">
        {{ charsCount }}{{ maxLength > -1 ? ' / ' + maxLength : '' }}
        <i class="uiIconMessageLength"></i>
      </div>
    </div>
    <attachments-image-input
      v-if="displayAttachmentEditor"
      ref="attachmentsInput"
      :max-file-size="maxFileSize"
      :object-type="objectType"
      :object-id="objectId"
      :disable-paste="disableImageAttachmentPaste"
      @changed="$emit('attachments-edited', $event)" />
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: ''
    },
    maxLength: {
      type: Number,
      default: -1
    },
    ckEditorId: {
      type: String,
      default: ''
    },
    ckEditorType: {
      type: String,
      default: 'activityContent'
    },
    suggestorTypeOfRelation: {
      type: String,
      default: 'mention_activity_stream'
    },
    suggesterSpaceURL: {
      type: String,
      default: eXo.env.portal.spaceUrl
    },
    activityId: {
      type: String,
      default: null,
    },
    templateParams: {
      type: Object,
      default: null,
    },
    autofocus: {
      type: Boolean,
      default: false
    },
    useExtraPlugins: {
      type: Boolean,
      default: false
    },
    useDraftManagement: {
      type: Boolean,
      default: false
    },
    contextName: {
      type: String,
      default: null
    },
    tagEnabled: {
      type: Boolean,
      default: true
    },
    oembed: {
      type: Boolean,
      default: false
    },
    oembedMinWidth: {
      type: Number,
      default: () => 300,
    },
    oembedMaxWidth: {
      type: Number,
      default: () => 500,
    },
    maxFileSize: {
      type: Number,
      default: () => 20971520,
    },
    objectType: {
      type: String,
      default: null
    },
    objectId: {
      type: String,
      default: null
    },
    disableImageAttachment: {
      type: Boolean,
      default: false
    },
    disableImageAttachmentPaste: {
      type: Boolean,
      default: false
    },
    disableSuggester: {
      type: Boolean,
      default: false
    }
  },
  data: () => ({
    SMARTPHONE_LANDSCAPE_WIDTH: 768,
    inputVal: null,
    oembedParams: null,
    editor: null,
    baseUrl: eXo.env.server.portalBaseURL,
  }),
  computed: {
    ckEditorInstanceId() {
      return this.ckEditorId ? this.ckEditorId : this.ckEditorType;
    },
    buttonId() {
      return `btn_${this.ckEditorType}`;
    },
    editorReady() {
      return this.editor && this.editor.status === 'ready';
    },
    charsCount() {
      return this.inputVal && this.$utils.htmlToText(this.inputVal).length || 0;
    },
    validLength() {
      return this.charsCount <= this.maxLength;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
    ckEditorConfigUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/richeditor/configuration?type=${this.ckEditorType || 'default'}&v=${eXo.env.client.assetsVersion}`;
    },
    supportsOembed() {
      return this.oembed || this.templateParams;
    },
    attachmentEnabled() {
      return !this.disableImageAttachment && eXo.env.portal.editorAttachImageEnabled && this.objectType?.length && eXo.env.portal.attachmentObjectTypes?.indexOf(this.objectType) >= 0;
    },
    displayAttachmentEditor() {
      return this.attachmentEnabled && this.editorReady;
    },
  },
  watch: {
    inputVal() {
      this.updateInput(this.inputVal);

      if (this.supportsOembed) {
        this.setOembedParams({
          default_title: this.getContentToSave(this.inputVal),
          comment: this.getContentNoEmbed(this.inputVal),
        });
      } else {
        this.clearOembedParams();
      }
    },
    oembedParams() {
      this.updateInput(this.inputVal);
    },
    editorReady() {
      if (this.editorReady) {
        this.$emit('ready');
        this.initOembedParams();
      } else {
        this.$emit('unloaded');
      }
    },
    validLength: {
      immediate: true,
      handler() {
        this.$emit('validity-updated', this.validLength);
      },
    },
    suggesterSpaceURL() {
      this.initCKEditor(!!this.suggesterSpaceURL, this.value);
    },
    displayAttachmentEditor(newVal, oldVal) {
      if (newVal && !oldVal) {
        this.$nextTick().then(() => this.$refs?.attachmentsInput?.init());
      }
    },
  },
  created() {
    // Load CKEditor only when needed
    window.require(['SHARED/commons-editor', 'SHARED/suggester', 'SHARED/tagSuggester']);
  },
  mounted() {
    if (!this.value?.length && this.useDraftManagement) {
      const storageMessage =  localStorage.getItem(`activity-message-${this.contextName}`);
      const storageMessageObject =  storageMessage && JSON.parse(storageMessage) || {};
      const storageMessageText = storageMessageObject?.url === eXo.env.server.portalBaseURL && storageMessageObject?.text || '';
      this.initCKEditor(true, storageMessageText);
      this.updateInput(this.inputVal);
    } else {
      this.initCKEditor(true, this.value);
    }
  },
  beforeDestroy() {
    this.destroyCKEditor();
  },
  methods: {
    initCKEditor(reset, textValue) {
      const self = this;
      this.editor = null;
      window.require(['SHARED/commons-editor', 'SHARED/suggester', 'SHARED/tagSuggester'], function() {
        self.initCKEditorInstance(reset, textValue || self.value);
      });
    },
    initCKEditorInstance(reset, textValue) {
      this.inputVal = textValue && this.getContentToEdit(textValue) || '';
      const editor = CKEDITOR.instances[this.ckEditorInstanceId];
      if (editor) {
        editor.status = 'not-ready';
      }
      this.editor = editor;
      if (this.editor && this.editor.destroy && !this.ckEditorType.includes('editActivity')) {
        if (reset) {
          editor.status = 'ready';
          this.editor.destroy(true);
        } else {
          this.initCKEditorData(textValue);
          this.setEditorReady();
          return;
        }
      }
      CKEDITOR.dtd.$removeEmpty['i'] = false;

      let extraPlugins = 'simpleLink,widget,editorplaceholder,emoji,formatOption';
      let removePlugins = 'image,maximize,resize';
      const toolbar = [
        ['Bold', 'Italic', 'BulletedList', 'NumberedList', 'Blockquote'],
      ];

      const windowWidth = $(window).width();
      const windowHeight = $(window).height();
      if (windowWidth <= windowHeight || windowWidth >= this.SMARTPHONE_LANDSCAPE_WIDTH && !this.disableSuggester) {
        // Disable suggester on smart-phone landscape
        extraPlugins = `${extraPlugins},suggester`;
      }
      if (this.supportsOembed) {
        extraPlugins = `${extraPlugins},embedsemantic,embedbase`;
      } else {
        removePlugins = `${removePlugins},embedsemantic,embedbase`;
      }
      if (this.tagEnabled) {
        extraPlugins = `${extraPlugins},tagSuggester`;
        toolbar[0].push('tagSuggester');
      } else {
        removePlugins = `${removePlugins},tagSuggester`;
      }
      if (!this.isMobile) {
        toolbar[0].push('emoji');
      }
      if (this.attachmentEnabled) {
        extraPlugins = `${extraPlugins},attachImage`;
        toolbar[0].push('attachImage');
      }
      toolbar[0].unshift('formatOption');

      const ckEditorExtensions = extensionRegistry.loadExtensions('ActivityComposer', 'ckeditor-extensions');
      if (ckEditorExtensions && ckEditorExtensions.length && this.useExtraPlugins) {
        ckEditorExtensions.forEach(ckEditorExtension => {
          if (ckEditorExtension.extraPlugin) {
            extraPlugins = `${extraPlugins},${ckEditorExtension.extraPlugin}`;
          }
          if (ckEditorExtension.removePlugin) {
            removePlugins = `${extraPlugins},${ckEditorExtension.removePlugin}`;
          }
          if (ckEditorExtension.extraToolbarItem) {
            toolbar[0].push(ckEditorExtension.extraToolbarItem);
          }
        });
      }

      // this line is mandatory when a custom skin is defined
      CKEDITOR.basePath = '/commons-extension/ckeditor/';
      const self = this;
      $(this.$refs.editor).ckeditor({
        customConfig: this.ckEditorConfigUrl,
        extraPlugins,
        removePlugins,
        editorplaceholder: this.placeholder,
        toolbar,
        allowedContent: true,
        enterMode: 3, // div
        typeOfRelation: this.suggestorTypeOfRelation,
        spaceURL: this.suggesterSpaceURL,
        activityId: this.activityId,
        autoGrow_onStartup: false,
        autoGrow_maxHeight: 300,
        startupFocus: this.autofocus && 'end',
        pasteFilter: 'p; a[!href]; strong; i', 
        on: {
          instanceReady: function () {
            self.editor = CKEDITOR.instances[self.ckEditorInstanceId];
            $(self.editor.document.$)
              .find('.atwho-inserted')
              .each(function() {
                $(this).on('click', '.remove', function() {
                  $(this).closest('[data-atwho-at-query]').remove();
                });
              });

            self.setEditorReady();
            if (this.autofocus) {
              window.setTimeout(() => self.setFocus(), 50);
            }
          },
          embedHandleResponse: function (embedResponse) {
            self.installOembed(embedResponse);
          },
          requestCanceled: function () {
            this.clearOembedParams();
          },
          change: function (evt) {
            const newData = evt.editor.getData();
            self.inputVal = newData;
            if (!self.activityId && self.useDraftManagement && self.contextName) {
              localStorage.setItem(`activity-message-${self.contextName}`,  JSON.stringify({'url': self.baseUrl, 'text': newData}));
            }
          },
          paste: function (evt) {
            if (!self.disableImageAttachmentPaste && self.$refs?.attachmentsInput && evt.data.dataTransfer.getFilesCount() > 0) {
              const files = [];
              for (let i = 0; i < evt.data.dataTransfer.getFilesCount(); i++ ) {
                files.push(evt.data.dataTransfer.getFile(i));
              }
              self.$refs.attachmentsInput.uploadFiles(files);
            }
          },
          destroy: function () {
            if (!self) {
              return;
            }
            let data = self?.value;
            if (data) {
              data = data?.replace?.(/@\w+/gm, '');
              self.inputVal = data;
            } else {
              self.inputVal = '';
            }
            self.editor = null;
          }
        }
      });
    },
    destroyCKEditor: function () {
      this.editor?.destroy?.(true);
      if (this.$refs.attachmentsInput) {
        this.$refs.attachmentsInput.reset();
      }
    },
    initCKEditorData: function(message) {
      this.inputVal = message && this.getContentToEdit(message) || '';
      try {
        if (this.editor) {
          this.editor.setData(this.inputVal);
        }
      } catch (e) {
        // When CKEditor not initialized or is detroying
      }
    },
    unload: function() {
      if (this.editor) {
        this.$set(this.editor, 'status', 'not-ready');
      }
    },
    setEditorReady: function() {
      window.setTimeout(() => {
        this.$set(this.editor, 'status', 'ready');
      }, 200);
    },
    setFocus(force) {
      if (this.editorReady && (force || this.autofocus)) {
        window.setTimeout(() => {
          this.$nextTick().then(() => this.editor.focus());
        }, 200);
      }
    },
    getMessage() {
      return this.editor?.getData() || '';
    },
    installOembed: function(embedResponse) {
      const response = embedResponse?.data?.data?.response;
      if (this.supportsOembed && response) {
        const oembedUrl = response.url;
        this.setOembedParams({
          link: oembedUrl || '-',
          image: response.type !== 'video' && response.thumbnail_url || '-',
          html: response.type === 'video' && response.html || '-',
          title: response.title || '-',
          description: response.description || '-',
          previewHeight: response.thumbnail_height || '-',
          previewWidth: response.thumbnail_width || '-',
          default_title: this.getContent(this.inputVal, false),
          comment: this.getContentNoEmbed(this.inputVal),
        });
      } else {
        this.clearOembedParams();
      }
    },
    saveAttachments() {
      if (this.$refs.attachmentsInput) {
        return this.$refs.attachmentsInput.save();
      }
    },
    getContentToEdit(content) {
      if (!content) {
        return '';
      }
      if (content.includes('<oembed>') && content.includes('</oembed>')) {
        const oembedUrl = window.decodeURIComponent(content.match(/<oembed>(.*)<\/oembed>/i)[1]);
        content = content.replace(/<oembed>(.*)<\/oembed>/g, '');
        if (this.supportsOembed) {
          content = `${content}<oembed>${oembedUrl}</oembed>`;
        }
      }
      content = content.replace(/]]&gt;/g, ']]>');
      content = content.replace(/&lt;!\[CDATA\[/g, '<![CDATA[');
      content = content.replace(/<div><!\[CDATA\[(.*)]]><\/div>/g, '');
      content = content.replace(/ {2}/g, '&nbsp;&nbsp;');
      return this.replaceWithSuggesterClass(content);
    },
    getContentToCompare(content) {
      return this.getContentNoEmbed(content);
    },
    getContentToSave(content) {
      return this.getContent(content, true);
    },
    getContent(content, cleanOembedParams) {
      if (!content) {
        content = '';
      } else {
        content = this.$utils.replaceHtmlEntites(content);
      }
      if (!content.includes('<oembed>') && cleanOembedParams && this.oembedParams) {
        this.clearOembedParams();
      } else if (!this.templateParams && (this.oembedParams?.url || this.oembedParams?.link)) {
        content = content.replace(/<oembed>(.*)<\/oembed>/g, '');
        const link = this.oembedParams?.url || this.oembedParams?.link;
        content = `${content}<oembed>${window.encodeURIComponent(link)}</oembed>`;
      } else if (content.includes('<oembed>') && content.includes('</oembed>')) {
        const oembedUrl = content.match(/<oembed>(.*)<\/oembed>/i)[1];
        content = content.replace(/<oembed>(.*)<\/oembed>/g, `<oembed>${oembedUrl}</oembed>`);
      }
      return this.getContentWithOembedHtml(content);
    },
    getContentWithOembedHtml(content) {
      content = content.replace(/<div><!\[CDATA\[(.*)]]><\/div>/g, '');
      let html = null;
      let aspectRatio = 16 / 9;
      if (!this.templateParams) {
        if (this.oembedParams?.html && this.oembedParams?.html !== '-') {
          html = this.oembedParams.html;
        }
        if (this.editor?.document?.getBody) {
          const body = this.editor.document.getBody().$;
          if (body && body.querySelector('[data-widget="embedSemantic"] div')) {
            const element = body.querySelector('[data-widget="embedSemantic"] div');
            if (element) {
              if (!html) {
                html = element.innerHTML;
              }
              aspectRatio = element.offsetWidth / element.offsetHeight;
            }
          }
        }
      }
      if (html) {
        const style = `
          position: relative;
          display: flex;
          margin: auto;
          min-height: ${parseInt(this.oembedMinWidth / aspectRatio)}px;
          min-width: ${this.oembedMinWidth}px;
          width: 100%;
          max-width: ${this.oembedMaxWidth}px;
          aspect-ratio: ${aspectRatio};
        `;
        html = html.replace(/<div>(.*)<\/div>/g, '$1');
        html = `<div style="${style}">${html}</div>`;
        content = `${content}<div><![CDATA[${window.encodeURIComponent(html)}]]></div>`;
      }
      return content;
    },
    getContentNoEmbed(content) {
      if (!content) {
        return '';
      }
      if (content.includes('<oembed>') && content.includes('</oembed>')) {
        const oembedUrl = window.decodeURIComponent(content.match(/<oembed>(.*)<\/oembed>/i)[1]);
        content = content.replace(/<oembed>(.*)<\/oembed>/g, '');
        content = content.replace(/<oembed>(.*)<\/oembed>/g, `<oembed>${oembedUrl}</oembed>`);
      }
      content = content.replace(/<div><!\[CDATA\[(.*)]]><\/div>/g, '');
      return content;
    },
    updateInput(content) {
      if (this.editorReady) {
        this.$emit('input', this.getContentToSave(content));
      }
    },
    initOembedParams() {
      this.setOembedParams({});
    },
    clearOembedParams() {
      this.setOembedParams(null);
    },
    setOembedParams(params) {
      if (this.supportsOembed) {
        if (!this.oembedParams) {
          this.oembedParams = this.templateParams || {};
        }
        if (params) {
          Object.assign(this.oembedParams, params);
        } else {
          Object.assign(this.oembedParams, {
            link: '-',
            image: '-',
            html: '-',
            title: '-',
            description: '-',
            previewHeight: '-',
            previewWidth: '-',
            default_title: '-',
            comment: '-',
          });
        }
      } else {
        this.oembedParams = null;
      }
    },
    replaceWithSuggesterClass(message) {
      const tempdiv = $('<div class=\'temp\'/>').html(message || '');
      tempdiv.find('a[href*="/profile"]')
        .each(function() {
          $(this).replaceWith(function() {
            return $('<span/>', {
              class: 'atwho-inserted',
              html: `<span class="exo-mention">${$(this).text()}<a data-cke-survive href="#" class="remove"><i data-cke-survive class="uiIconClose uiIconLightGray"></i></a></span>`
            }).attr('data-atwho-at-query',`@${$(this).attr('href').substring($(this).attr('href').lastIndexOf('/')+1)}`)
              .attr('data-atwho-at-value',$(this).attr('href').substring($(this).attr('href').lastIndexOf('/')+1))
              .attr('contenteditable','false');
          });
        });
      return tempdiv.html();
    },
  }
};
</script>
