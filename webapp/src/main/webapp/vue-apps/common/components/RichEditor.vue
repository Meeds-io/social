<template>
  <div class="overflow-hidden">
    <div
      class="richEditor"
      :class="{
        'hide-toolbar': hideToolbar,
        'large-toolbar': largeToolbar,
      }">
      <textarea
        ref="editor"
        v-model="inputVal"
        :id="ckEditorInstanceId"
        cols="30"
        rows="10"
        class="textarea"></textarea>
      <v-progress-circular
        v-if="!editorReady"
        :width="3"
        indeterminate
        class="loadingRing position-absolute" />
      <div
        v-if="!hideCharsCount && !isMobile"
        :v-show="editorReady"
        :id="buttonId"
        :class="!validLength && 'tooManyChars' || ''"
        class="activityCharsCount line-height-normal d-flex align-center">
        {{ charsCount }}{{ maxLength > -1 && !hideCharsMaxCount ? ' / ' + maxLength : '' }}
        <i class="uiIconMessageLength ms-1 pb-2px"></i>
      </div>
    </div>
    <div v-if="containInvalidUsers" class="mt-4 text-subtitle">{{ $t('activity.composer.invalidUsers.message') }}</div>
    <attachments-image-input
      v-if="attachmentEnabled"
      v-show="displayAttachmentEditor"
      ref="attachmentsInput"
      :max-file-size="maxFileSize"
      :object-type="objectType"
      :object-id="objectId"
      :disable-paste="disableImageAttachmentPaste"
      @changed="emitChanges" />
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
    enterMode: {
      type: Number,
      default: () => 3 // div
    },
    suggesterSpaceURL: {
      type: String,
      default: eXo.env.portal.spaceUrl
    },
    suggesterSpacePrettyName: {
      type: String,
      default: eXo.env.portal.spaceName
    },
    suggesterSpaceId: {
      type: String,
      default: eXo.env.portal.spaceId
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
    focusPosition: {
      type: String,
      default: () => 'end',
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
    contentLinkEnabled: {
      type: Boolean,
      default: false
    },
    oembed: {
      type: Boolean,
      default: false
    },
    oembedOnlyVideo: {
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
    disabled: {
      type: Boolean,
      default: false
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
    },
    hideCharsCount: {
      type: Boolean,
      default: false
    },
    hideCharsMaxCount: {
      type: Boolean,
      default: false
    },
    disableAutoGrow: {
      type: Boolean,
      default: false,
    },
    autoGrowMaxHeight: {
      type: Number,
      default: () => 300,
    },
    toolbarPosition: {
      type: String,
      default: () => 'bottom',
    },
    largeToolbar: {
      type: Boolean,
      default: false,
    },
    hideToolbar: {
      type: Boolean,
      default: false,
    },
    dense: {
      type: Boolean,
      default: false,
    },
    bodyClass: {
      type: String,
      default: null,
    },
    parentClass: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    SMARTPHONE_LANDSCAPE_WIDTH: 768,
    inputVal: null,
    oembedParams: null,
    editor: null,
    baseUrl: eXo.env.server.portalBaseURL,
    containInvalidUsers: false,
    spaceId: null,
    backUpMessage: null,
    step: 0,
    initializing: false,
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
      if (this.editorReady) {
        if (this.supportsOembed) {
          this.setOembedParams({
            default_title: this.getContentToSave(this.inputVal),
            comment: this.getContentNoEmbed(this.inputVal),
          });
        } else {
          this.clearOembedParams();
        }
      }
    },
    oembedParams() {
      this.updateInput(this.inputVal);
    },
    editorReady() {
      if (this.editorReady) {
        this.$emit('ready');
        this.initOembedParams();
        this.updateInput(this.inputVal);
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
      this.updateSpaceId();
    },
    suggesterSpaceId() {
      this.updateSpaceId();
    },
    suggesterSpacePrettyName() {
      this.updateSpaceId();
    },
    spaceId() {
      if (this.editor) {
        this.editor.config.spaceId = this.spaceId;
      }
    },
    displayAttachmentEditor(newVal, oldVal) {
      if (newVal && !oldVal) {
        this.$nextTick().then(() => this.$refs?.attachmentsInput?.init());
      }
    },
    editor() {
      if (this.editor) {
        let message = this.backUpMessage || this.inputVal;
        if (message?.length) {
          message = message.replace(/@([A-Za-z0-9_'.+-]+:[0-9]+)/g, '');
          const mentionedUsers =  message.match(/@([A-Za-z0-9_'.+-]+)/g)?.map(a => a.replace('@', '')) || null;
          if (mentionedUsers?.length) {
            this.replaceSuggestedUsers(message, mentionedUsers, this.spaceId);
          }
        }
      }
    },
    disabled() {
      if (this.editor) {
        this.editor.setReadOnly(this.disabled);
        this.editor.editable().$.setAttribute('contenteditable', `${!this.disabled}`);
      }
    },
    step() {
      if (this.step === 2) {
        this.init();
      }
    },
  },
  async created() {
    try {
      await this.$utils.includeExtensions('RichEditorExtension');
      // Load CKEditor only when needed
      await new Promise(resolve => window.require(['SHARED/commons-editor', 'SHARED/suggester', 'SHARED/tagSuggester'], resolve));
      document.addEventListener('activity-composer-edited', this.handleEditorInputChange);
      this.updateSpaceId();
    } finally {
      this.step++;
    }
  },
  mounted() {
    this.step++;
  },
  beforeDestroy() {
    document.removeEventListener('activity-composer-edited', this.handleEditorInputChange);
    this.destroyCKEditor();
  },
  methods: {
    init() {
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
    initCKEditor(reset, textValue) {
      const self = this;
      window.require(['SHARED/commons-editor', 'SHARED/suggester', 'SHARED/tagSuggester'], function() {
        self.initCKEditorInstance(reset, textValue || self.value);
      });
    },
    async initCKEditorInstance(reset, textValue) {
      this.inputVal = textValue && this.getContentToEdit(textValue) || '';
      if (this.initializing || this.step < 2) {
        return;
      }
      this.initializing = true;
      const editor = CKEDITOR.instances[this.ckEditorInstanceId];
      if (editor && editor !== this.editor) {
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

      let extraPlugins = 'simpleLink,widget,editorplaceholder,formatOption,linkBalloon,vuetifyStyle';
      let removePlugins = 'image,maximize,resize';
      const toolbar = [
        ['Bold', 'Italic', 'BulletedList', 'NumberedList', 'Blockquote'],
      ];

      if (this.tagEnabled) {
        extraPlugins += ',tagSuggester';
        toolbar[0].push('tagSuggester');
      } else {
        removePlugins += ',tagSuggester';
      }

      const windowWidth = $(window).width();
      const windowHeight = $(window).height();
      if (windowWidth <= windowHeight || windowWidth >= this.SMARTPHONE_LANDSCAPE_WIDTH && !this.disableSuggester) {
        // Disable suggester on smart-phone landscape
        extraPlugins += ',suggester';
      }

      if (this.supportsOembed) {
        if (this.oembedOnlyVideo) {
          extraPlugins += ',embedsemanticOnlyVideo,embedbaseOnlyVideo';
          removePlugins += ',embedsemantic,embedbase';
        } else {
          extraPlugins += ',embedsemantic,embedbase';
          removePlugins += ',embedsemanticOnlyVideo,embedbaseOnlyVideo';
        }
      } else {
        removePlugins += ',embedsemantic,embedbase,embedsemanticOnlyVideo,embedbaseOnlyVideo';
      }

      if (!this.isMobile && !this.hideToolbar) {
        extraPlugins += ',emoji';
        toolbar[0].push('emoji');
      }
      if (this.attachmentEnabled) {
        extraPlugins += ',attachImage';
        toolbar[0].push('attachImage');
      }
      let ckEditorExtensions = [
        ...extensionRegistry.loadExtensions('RichEditor', 'ckeditor-extensions'),
        ...extensionRegistry.loadExtensions('RichEditor', `ckeditor-extensions-${this.ckEditorType || 'default'}`),
      ].filter(ext => this.useExtraPlugins || ext?.enabled?.(this.useExtraPlugins));
      let configOptions = {};
      if (ckEditorExtensions?.length) {
        ckEditorExtensions = await Promise.all(ckEditorExtensions.map(async ext => {
          if (ext.getExtension) {
            return await ext.getExtension({
              ckEditorType: this.ckEditorType,
              objectType: this.objectType,
              objectId: this.objectId,
            });
          } else {
            return ext;
          }
        }));
        ckEditorExtensions.forEach(ckEditorExtension => {
          if (ckEditorExtension?.extraPlugin) {
            extraPlugins += `,${ckEditorExtension.extraPlugin}`;
          }
          if (ckEditorExtension?.removePlugin) {
            removePlugins += `,${ckEditorExtension.removePlugin}`;
          }
          if (ckEditorExtension?.extraToolbarItem) {
            toolbar[0].push(ckEditorExtension.extraToolbarItem);
          }
          if (ckEditorExtension?.configOptions) {
            configOptions = {
              ...configOptions,
              ...ckEditorExtension.configOptions,
            };
          }
        });
      }
      if (this.contentLinkEnabled) {
        extraPlugins += ',insertContentLink';
        toolbar[0].push('InsertContentLink');
      }
      toolbar[0].unshift('formatOption');

      // this line is mandatory when a custom skin is defined
      CKEDITOR.basePath = '/commons-extension/ckeditor/';
      const self = this;
      const options = {
        customConfig: this.ckEditorConfigUrl,
        extraPlugins,
        removePlugins,
        editorplaceholder: this.placeholder,
        toolbar: this.hideToolbar ? [] : toolbar,
        toolbarLocation: this.toolbarPosition,
        toolbarCanCollapse: this.hideToolbar,
        ckEditorType: this.ckEditorType,
        objectType: this.objectType,
        objectId: this.objectId,
        allowedContent: true,
        enterMode: this.enterMode,
        typeOfRelation: this.suggestorTypeOfRelation,
        spaceURL: this.suggesterSpaceURL,
        spacePrettyName: this.suggesterSpacePrettyName,
        spaceId: this.spaceId || this.suggesterSpaceId,
        activityId: this.activityId,
        startupFocus: this.autofocus && this.focusPosition,
        pasteFilter: 'p; div; a[!href]; strong; i',
        supportsOembed: this.supportsOembed,
        bodyClass: this.dense ? `${this.bodyClass || ''} dense-margin` : this.bodyClass,
        readOnly: this.disabled,
        ...configOptions,
      };
      if (!this.disableAutoGrow) {
        options.autoGrow_onStartup = false;
        options.autoGrow_maxHeight = this.autoGrowMaxHeight;
      }
      let firstScrollableParent;
      $(this.$refs.editor).ckeditor({...options,
        on: {
          instanceReady: function (evt) {
            self.editor = evt.editor;
            this.initializing = false;
            const editable = self.editor.editable();
            editable.on('input', function () {
              self.editor.fire('change');
            });
            $(self.editor.document.$)
              .find('.atwho-inserted')
              .each(function() {
                $(this).on('click', '.remove', function() {
                  $(this).closest('[data-atwho-at-query]').remove();
                });
              });

            self.setEditorReady();
            if (self.autofocus) {
              window.setTimeout(() => self.setFocus(), 50);
            }
            firstScrollableParent = self.getScrollParent(document.querySelector('.richEditor'));
            firstScrollableParent.addEventListener('scroll', self.controlParentScroll);
            if (self.parentClass) {
              self.parentClass.split(' ')
                .map(a => a?.trim?.())
                .filter(a => a?.length)
                .forEach(a => {
                  try {
                    self.editor.container.addClass(a);
                  } catch (e) {
                    // eslint-disable-next-line no-console
                    console.warn('Error adding class ', a, ' in editor container', e);
                  }
                });
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
            if (self) {
              self.editor = null;
              firstScrollableParent?.removeEventListener?.('scroll', self.controlParentScroll);
            }
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
          this.inputVal = this.editor.getData();
        }
      } catch (e) {
        // When CKEditor not initialized or is detroying
      }
    },
    reset() {
      this.initCKEditorData('');
      if (this.$refs.attachmentsInput) {
        this.$refs.attachmentsInput.reset();
      }
    },
    unload() {
      if (this.editor) {
        this.$set(this.editor, 'status', 'not-ready');
      }
    },
    setEditorReady() {
      if (this.editor) {
        this.$set(this.editor, 'status', 'ready');
      }
    },
    setFocus(force, end) {
      if (this.editorReady && (force || this.autofocus)) {
        window.setTimeout(() => {
          this.$nextTick().then(() => {
            this.editor?.focus?.();
            if (end && this.editor?.editable?.()) {
              const range = this.editor.createRange();
              range.moveToElementEditEnd(this.editor.editable());
              this.editor.getSelection().selectRanges([range]);
            }
          });
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
        const oembedParams = {
          link: oembedUrl || '-',
          image: response.type !== 'video' && response.thumbnail_url || '-',
          html: response.type === 'video' && response.html || '-',
          title: response.title || '-',
          description: response.description || '-',
          previewHeight: response.thumbnail_height || '-',
          previewWidth: response.thumbnail_width || '-',
          default_title: this.getContent(this.inputVal, false),
          comment: this.getContentNoEmbed(this.inputVal),
        };
        if (oembedParams.image !== '-') {
          this.getAverageColor(oembedParams.image)
            .then(data => {
              oembedParams.bgColor = data?.bgColor || 'rgb(231, 231, 231)';
              oembedParams.previewHeight = data?.height || '-';
              oembedParams.previewWidth = data?.width || '-';
            })
            .finally(() => {
              this.setOembedParams(oembedParams);
            });
        } else {
          this.setOembedParams(oembedParams);
        }  
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
      content = content.replace(/@([A-Za-z0-9_'.+-]+:[0-9]+)/g, '');
      const mentionedUsers =  content.match(/@([A-Za-z0-9_'.+-]+(:[0-9]+)?)/g)?.map(a => a.replace('@', '')) || null;
      if (mentionedUsers?.length) {
        this.replaceSuggestedUsers(content, mentionedUsers, this.spaceId);
      }
      if (content.includes('<oembed>') && content.includes('</oembed>')) {
        const oembedUrl = window.decodeURIComponent(content.match(/<oembed>\n* *(.*)\n*<\/oembed>/i)[1]);
        content = content.replace(/\n* *<oembed>(.*)\n*<\/oembed>/g, '');
        if (this.supportsOembed) {
          content = `${content}<oembed>${oembedUrl}</oembed>`;
        }
      }
      content = content.replace(/]]&gt;/g, ']]>');
      content = content.replace(/&lt;!\[CDATA\[/g, '<![CDATA[');
      content = content.replace(/<div><!\[CDATA\[(.*)]]><\/div>/g, '');
      return this.replaceWithSuggesterClass(content);
    },
    getContentToCompare(content) {
      return this.getContentNoEmbed(content);
    },
    getContentToSave(content) {
      content = this.replaceSuggesterHtmlToText(content);
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
        content = content.replace(/<oembed>\n* *(.*)\n*<\/oembed>/g, '');
        const link = this.oembedParams?.url || this.oembedParams?.link;
        content = `${content}<oembed>${window.encodeURIComponent(link?.trim?.())}</oembed>`;
      } else if (content.includes('<oembed>') && content.includes('</oembed>')) {
        const oembedUrl = content.match(/<oembed>\n* *(.*)\n*<\/oembed>/i)[1];
        content = content.replace(/<oembed>\n* *(.*)\n*<\/oembed>/g, `<oembed>${oembedUrl?.trim?.()}</oembed>`);
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
        const oembedUrl = window.decodeURIComponent(content.match(/<oembed>\n* *(.*)\n*<\/oembed>/i)[1]);
        content = content.replace(/<oembed>\n* *(.*)\n*<\/oembed>/g, `<oembed>${oembedUrl}</oembed>`);
      }
      if (content.includes('<a') && content.includes('</a>')) {
        const tempdiv = document.createElement('div');
        tempdiv.innerHTML = content;
        const links = tempdiv.getElementsByTagName('a');
        for (const link of links) {
          if (link.href.indexOf(window.location.origin) === -1) {
            link.setAttribute('target', '_blank');
            link.setAttribute('rel', 'nofollow noopener noreferrer');
          }
          if (link.text.length > 75) {
            link.setAttribute('title', link.href);
            link.setAttribute('Aria-label', link.href);
            link.text = `${link.text.substring(0,75)  }...`;
          }
        }
        content = tempdiv.innerHTML;
      }
      content = content.replace(/<div><!\[CDATA\[(.*)]]><\/div>/g, '');
      return content;
    },
    updateInput(content) {
      if (this.editorReady) {
        const message = this.getContentToSave(content);
        if (!this.activityId && this.useDraftManagement && this.contextName) {
          localStorage.setItem(`activity-message-${this.contextName}`,  JSON.stringify({'url': this.baseUrl, 'text': this.inputVal}));
        }
        this.$emit('input', message);
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
      message = message.replace(/&nbsp;|&#160;/g, ' ');
      message = message.replace(/\u00A0/g, ' ');
      const tempdiv = $('<div class=\'temp\'/>').html(message || '');
      tempdiv.find('a[href*="/profile"]')
        .each(function() {
          $(this).replaceWith(function() {
            return $('<span/>', {
              class: 'atwho-inserted',
              html: `<span class="exo-mention" contenteditable="false">${$(this).text()}<a data-cke-survive href="#" class="remove"><i data-cke-survive class="uiIconClose uiIconLightGray"></i></a></span>`
            }).attr('data-atwho-at-query', '@')
              .attr('data-atwho-at-value', $(this).attr('href').substring($(this).attr('href').lastIndexOf('/')+1))
              .attr('contenteditable', 'false');
          });
        });
      tempdiv.find('a.group-role-mention')
        .each(function() {
          const role = $(this).data('role');
          const identityId = $(this).data('identity-id');
          let icon;
          if (role === 'member') {
            icon = 'fa-users';
          } else if (role === 'manager') {
            icon = 'fa-user-cog';
          } else if (role === 'redactor') {
            icon = 'fa-user-edit';
          } else if (role === 'publisher') {
            icon = 'fa-paper-plane';
          }

          $(this).replaceWith(function() {
            return $('<span/>', {
              class: 'atwho-inserted',
              html: `<span class="exo-mention"><i aria-hidden="true" class="v-icon fa ${icon}" style="font-size: 16px;"></i>${$(this).text()}<a data-cke-survive href="#" class="remove"><i data-cke-survive class="uiIconClose uiIconLightGray"></i></a></span>`
            }).attr('data-atwho-at-query', '@')
              .attr('data-atwho-at-value',`${role}:${identityId}`)
              .attr('contenteditable','false');
          });
        });
      return tempdiv.html();
    },
    replaceValidSuggestedUser(message, profile, pattern) {
      return message.replace(new RegExp(pattern, 'g'), ExtendedDomPurify.purify(`
                      <span class="atwho-inserted" data-atwho-at-query="@${profile.username}" data-atwho-at-value="${profile.username}" contenteditable="false">
                        <span class="exo-mention">${profile.fullname}${profile.isExternal === 'true' ? ` (${  this.$t('UsersManagement.type.external')  })` : ''}<a href="#" class="remove"><i class="uiIconClose uiIconLightGray"></i></a></span>
                      </span>
                    `));
    },
    replaceInvalidSuggestedUser(message, profile, pattern) {
      return message.replace(new RegExp(pattern, 'g'), ExtendedDomPurify.purify(`
                      <span class="atwho-inserted" data-atwho-at-query="@${profile.username}" data-atwho-at-value="" contenteditable="false" title="${this.$t('activity.composer.invalidUser.message')}">
                        <span class="exo-mention">
                          <i aria-hidden="true" class="v-icon notranslate fa fa-exclamation-triangle theme--light orange--text error-color" style="font-size: 16px;">
                          </i>
                          <del>${profile.fullname}${profile.isExternal === 'true' ? ` (${  this.$t('UsersManagement.type.external')  })` : ''}</del>
                          <a href="#" class="remove">
                            <i class="uiIconClose uiIconLightGray"></i>
                          </a>
                        </span>
                      </span>
                    `));
    },
    replaceSuggestedUsers(message, mentionedUsers, spaceId) {
      this.containInvalidUsers = false;
      Promise
        .all(mentionedUsers.map(username => {
          return this.$identityService.getIdentityByProviderIdAndRemoteId('organization', username)
            .then(identity => {
              if (!identity?.profile) {
                return null;
              }
              if (spaceId) {
                return this.$spaceService.isSpaceMember(spaceId, username)
                  .then(isMember => {
                    if (isMember) {
                      const profile = identity.profile;
                      profile.isMember = isMember;
                      return profile;
                    } else {
                      return null;
                    }
                  });
              } else {
                const profile = identity.profile;
                profile.isMember = true;
                return profile;
              }
            })
            .catch(() => null);
        }))
        .then(userProfiles => userProfiles.filter(p => p))
        .then(userProfiles => {
          const containsExoMentionClass = message.search('exo-mention') >= 0;
          this.containInvalidUsers = !!userProfiles.find(profile => profile.isMember !== true);
          let hasInvalidUsers = false;
          userProfiles.forEach(profile => {
            const pattern = containsExoMentionClass ? `<span [^>]* data-atwho-at-query="@${profile.username}" class="atwho-inserted">(.*?)</span> </span>` : `@${profile.username}`;
            if (profile.isMember) {
              message = this.replaceValidSuggestedUser(message, profile, pattern);
            } else {
              hasInvalidUsers = true;
              message = this.replaceInvalidSuggestedUser(message, profile, pattern);
            }
          });
          this.backUpMessage = hasInvalidUsers && message || null;
          this.editor?.setData(message);
        });
    },
    replaceSuggesterHtmlToText(message) {
      const tempdiv = $('<div class=\'temp\'/>').html(message || '');
      tempdiv.find('[data-atwho-at-value]')
        .each(function() {
          const value = $(this).attr('data-atwho-at-value');
          const pattern = new RegExp(`<span class="atwho-inserted" data-atwho-at-query="[^"]*" data-atwho-at-value="${value}"[^>]*>(.*?)</span> </span>`, 'g');
          message = message.replace(pattern, value ? `@${value}` : '');
        });
      return message;
    },
    updateSpaceId() {
      if (this.suggesterSpaceId) {
        if (!this.spaceId) {
          this.spaceId = this.suggesterSpaceId;
        }
        if (this.editorReady) {
          this.initCKEditor(true, this.backUpMessage || this.inputVal);
        }
      } else if (this.suggesterSpaceURL || this.suggesterSpacePrettyName) {
        this.getSpaceId()
          .then(() => {
            if (this.editorReady) {
              this.initCKEditor(true, this.backUpMessage || this.inputVal);
            }
          });
      } else {
        this.spaceId = null;
        if (this.editorReady) {
          this.initCKEditor(true, this.backUpMessage || this.inputVal);
        }
      }
    },
    getSpaceId() {
      if (this.suggesterSpacePrettyName) {
        return this.$spaceService.getSpaceByPrettyName(this.suggesterSpacePrettyName)
          .then(space => this.spaceId = space.id);
      } else if (this.suggesterSpaceURL) {
        return this.$spaceService.getSpaceByGroupSuffix(this.suggesterSpaceURL)
          .then(space => this.spaceId = space.id);
      }
    },
    getScrollParent(element, includeHidden) {
      let style = getComputedStyle(element);
      const excludeStaticParent = style.position === 'absolute';
      const overflowRegex = includeHidden ? /(auto|scroll|hidden)/ : /(auto|scroll)/;
      if (style.position === 'fixed') {
        return document.body;
      }
      for (let parent = element; (parent = parent.parentElement);) {
        style = getComputedStyle(parent);
        if (excludeStaticParent && style.position === 'static') {
          continue;
        }
        if (overflowRegex.test(style.overflow + style.overflowY + style.overflowX)) {
          return parent;
        }
      }
      return document.body;
    },
    controlParentScroll() {
      document.dispatchEvent(new CustomEvent ('parent-element-scrolled'));
    },
    emitChanges(attachements, changed){
      this.$emit('attachments-edited', attachements, changed);
    },
    getAverageColor(imgElem) {
      let imgDetail = {
        bgColor: 'rgb(231, 231, 231)',
        width: '-',
        height: '-'
      };
      return this.loadImage(imgElem)
        .then(data => {
          const imgData = data?.imgData || '';
          const blockSize = 5,
            rgb = { r: 0, g: 0, b: 0};
          if (Object.keys(imgData).length) {
            let i = -4, count = 0;
            const length = imgData.data.length;
            while ( (i += blockSize * 4) < length) {
              ++count;
              rgb.r += imgData.data[i];
              rgb.g += imgData.data[i+1];
              rgb.b += imgData.data[i+2];
            }
            // ~~ used to floor values
            rgb.r = ~~(rgb.r/count);
            rgb.g = ~~(rgb.g/count);
            rgb.b = ~~(rgb.b/count);
          }
          imgDetail = {
            bgColor: `rgb(${rgb.r},${rgb.g},${rgb.b})`,
            height: data?.height || '-',
            width: data?.width || '-'
          };
          return imgDetail;
        })
        .catch(e => {
          console.debug('Error while computing image background color', e); // eslint-disable-line no-console
          return imgDetail;
        });
    },
    loadImage(img) {
      return new Promise((resolve, reject) => {
        const height = 100;
        const width = 150;
        try {
          const canvas = document.createElement('canvas');
          canvas.height = height;
          canvas.width = width;
          const context = canvas.getContext && canvas.getContext('2d');
          context.imageSmoothingEnabled = true;
          const imageElement = new Image();
          imageElement.src = img;
          imageElement.crossOrigin = 1;
          imageElement.onload = () => {
            context.drawImage(imageElement, 0, 0, width, height);
            resolve({
              'imgData': context.getImageData(0, 0, width, height),
              'width': imageElement.naturalWidth,
              'height': imageElement.naturalHeight
            });
          };
          imageElement.onerror = e => reject(e);
        } catch (e) {
          reject(e);
        }
      });
    },
    handleEditorInputChange() {
      let content = this.editor.getData();
      content = this.replaceWithSuggesterClass(content);
      this.inputVal = content;
      this.updateInput(this.inputVal);
      if (this.editorReady && this.supportsOembed) {
        this.setOembedParams({
          default_title: this.getContentToSave(this.inputVal),
          comment: this.getContentNoEmbed(this.inputVal),
        });
      }
    }
  }
};
</script>
