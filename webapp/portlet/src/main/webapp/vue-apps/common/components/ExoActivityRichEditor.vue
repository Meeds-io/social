<template>
  <div class="activityRichEditor">
    <textarea
      ref="editor"
      :id="ckEditorType"
      v-model="inputVal"
      :placeholder="placeholder"
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
      default: () => ({}),
    },
    autofocus: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      SMARTPHONE_LANDSCAPE_WIDTH: 768,
      inputVal: null,
      editor: null,
    };
  },
  computed: {
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
  },
  watch: {
    inputVal(val) {
      if (this.editorReady) {
        this.$emit('input', val);
      }
    },
    validLength() {
      this.$emit('validity-updated', this.validLength);
    },
    editorReady() {
      if (this.editorReady) {
        this.$emit('ready');
      } else {
        this.$emit('unloaded');
      }
    },
    value(val) {
      if (!this.editor) {
        this.initCKEditor();
      }
      let editorData = null;
      try {
        editorData = this.editor.getData();
      } catch (e) {
        // When CKEditor not initialized yet
      }
      this.installOembedOriginalMessage(editorData);
      if (val !== editorData) {
        // Knowing that using CKEDITOR.setData will rewrite a new CKEditor Body,
        // the suggester (which writes its settings in body attribute) doesn't
        // find its settings anymore when using '.setData' after initializing.
        // Thus, we destroy the ckEditor instance before setting new data.
        this.initCKEditorData(val || '');
        this.installOembedOriginalMessage(val);
      }
    }
  },
  mounted() {
    this.initCKEditor(true);
  },
  methods: {
    initCKEditor: function (reset) {
      this.inputVal = this.replaceWithSuggesterClass(this.value);

      this.editor = CKEDITOR.instances[this.ckEditorType];
      if (this.editor && this.editor.destroy && !this.ckEditorType.includes('editActivity')) {
        if (reset) {
          this.editor.destroy(true);
        } else {
          this.initCKEditorData(this.value);
          this.setEditorReady();
          return;
        }
      }
      CKEDITOR.dtd.$removeEmpty['i'] = false;

      let extraPlugins = 'simpleLink,suggester,widget';
      let removePlugins = 'image,maximize,resize';
      const toolbar = [
        ['Bold', 'Italic', 'BulletedList', 'NumberedList', 'Blockquote'],
      ];

      const windowWidth = $(window).width();
      const windowHeight = $(window).height();
      if (windowWidth > windowHeight && windowWidth < this.SMARTPHONE_LANDSCAPE_WIDTH) {
        // Disable suggester on smart-phone landscape
        extraPlugins = 'simpleLink';
      }
      if (this.templateParams) {
        extraPlugins = `${extraPlugins},embedsemantic,tagSuggester`;
      } else {
        removePlugins = `${removePlugins},embedsemantic,embedbase`;
      }
      if (eXo.env.portal.activityTagsEnabled) {
        extraPlugins = `${extraPlugins},tagSuggester`;
      } else {
        removePlugins = `${removePlugins},tagSuggester`;
      }

      const ckEditorExtensions = extensionRegistry.loadExtensions('ActivityComposer', 'ckeditor-extensions');
      if (ckEditorExtensions && ckEditorExtensions.length && !['activityShare', 'kudosContent'].includes(this.ckEditorType)) {
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
        customConfig: '/commons-extension/ckeditorCustom/config.js',
        extraPlugins,
        removePlugins,
        toolbar,
        allowedContent: true,
        enterMode: 3, // div
        typeOfRelation: this.suggestorTypeOfRelation,
        spaceURL: this.suggesterSpaceURL,
        activityId: this.activityId,
        autoGrow_onStartup: false,
        autoGrow_maxHeight: 300,
        on: {
          instanceReady: function () {
            self.editor = CKEDITOR.instances[self.ckEditorType];
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
          },
          embedHandleResponse: function (embedResponse) {
            self.installOembed(embedResponse);
          },
          requestCanceled: function () {
            self.cleanupOembed();
          },
          change: function (evt) {
            const newData = evt.editor.getData();

            self.inputVal = newData;
          },
          destroy: function () {
            self.inputVal = '';
            self.editor = null;
          }
        }
      });
    },
    destroyCKEditor: function () {
      if (this.editor) {
        this.editor.destroy(true);
      }
    },
    replaceWithSuggesterClass: function(message) {
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
    initCKEditorData: function(message) {
      this.inputVal = message && this.replaceWithSuggesterClass(message) || '';
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
    setFocus: function() {
      if (this.editorReady) {
        window.setTimeout(() => {
          this.$nextTick().then(() => this.editor.focus());
        }, 200);
      }
    },
    getMessage: function() {
      const newData = this.editor && this.editor.getData();
      return newData ? newData : '';
    },
    installOembedOriginalMessage: function(message, noClean) {
      if (!noClean && (!message || !message.includes('<oembed'))) {
        this.cleanupOembed();
      } else if (message
          && message.includes('<oembed')
          && this.templateParams
          && this.templateParams.link
          && this.templateParams.default_title !== message) {
        this.templateParams.default_title = message;
        const url = window.decodeURIComponent(this.templateParams.link);
        this.templateParams.comment = window.decodeURIComponent(message)
          .replace(`<oembed>${url}</oembed>`, '');
      }
    },
    installOembed: function(embedResponse) {
      if (this.templateParams && embedResponse) {
        const data = embedResponse.data && embedResponse.data.data;
        const response = data && data.response;
        if (response) {
          this.templateParams.link = response.url || '-';
          this.templateParams.image = response.type !== 'video' && response.thumbnail_url || '-';
          this.templateParams.html = response.type === 'video' && response.html || '-';
          this.templateParams.title = response.title || '-';
          this.templateParams.description = response.description || '-';
          this.templateParams.previewHeight = response.thumbnail_height || '-';
          this.templateParams.previewWidth = response.thumbnail_width || '-';
          window.setTimeout(() => {
            this.installOembedOriginalMessage(this.message, !this.message || !this.message.includes('<oembed'));
          }, 200);
        } else {
          this.cleanupOembed();
        }
      } else if (!embedResponse) {
        this.cleanupOembed();
      }
    },
    cleanupOembed: function() {
      if (this.templateParams
          && ((this.templateParams.image && this.templateParams.image !== '-')
              || (this.templateParams.html && this.templateParams.html !== '-'))) {
        this.templateParams.link = '-';
        this.templateParams.html = '-';
        this.templateParams.comment = '-';
        this.templateParams.default_title = '-';
        this.templateParams.image = '-';
        this.templateParams.description = '-';
        this.templateParams.title = '-';
        this.templateParams.registeredKeysForProcessor = '-';
      }
    },
  }
};
</script>
