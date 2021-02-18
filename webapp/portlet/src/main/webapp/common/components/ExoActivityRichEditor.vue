<template>
  <div class="activityRichEditor">
    <textarea ref="editor" :id="ckEditorType" v-model="inputVal" :placeholder="placeholder" cols="30" rows="10" class="textarea"></textarea>
    <v-progress-circular
      v-if="!editorReady"
      :width="3"
      indeterminate
      class="loadingRing"/>
    <div v-show="editorReady" :class="charsCount > maxLength ? 'tooManyChars' : ''" class="activityCharsCount">
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
    }
  },
  data() {
    return {
      SMARTPHONE_LANDSCAPE_WIDTH: 768,
      inputVal: this.value,
      charsCount: 0,
      editorReady: false
    };
  },
  watch: {
    inputVal(val) {
      this.$emit('input', val);
    },
    value(val) {
      // watch value to reset the editor value if the value has been updated by the component parent
      const editorData = CKEDITOR.instances[this.ckEditorType].getData();
      if (editorData != null && val !== editorData) {
        if (val === '') {
          this.initCKEditor();
        } else {
          //Knowing that using CKEDITOR.setData will rewrite a new CKEditor Body,
          // the suggester (which writes its settings in body attribute) doesn't
          // find its settings anymore when using '.setData' after initializing.
          // Thus, we destroy the ckEditor instance before setting new data.
          CKEDITOR.instances[this.ckEditorType].destroy(true);
          this.initCKEditor();
          this.initCKEditorData(val);
        }
      }
    }
  },
  mounted() {
    this.initCKEditor(true);
  },
  methods: {
    initCKEditor: function () {
      CKEDITOR.plugins.addExternal('embedsemantic', '/commons-extension/eXoPlugins/embedsemantic/', 'plugin.js');
      if (CKEDITOR.instances[this.ckEditorType] && CKEDITOR.instances[this.ckEditorType].destroy && !this.ckEditorType.includes('editActivity')) {
        CKEDITOR.instances[this.ckEditorType].destroy(true);
      }
      CKEDITOR.dtd.$removeEmpty['i'] = false;
      let extraPlugins = 'simpleLink,suggester,widget,embedsemantic';
      const windowWidth = $(window).width();
      const windowHeight = $(window).height();
      if (windowWidth > windowHeight && windowWidth < this.SMARTPHONE_LANDSCAPE_WIDTH) {
        // Disable suggester on smart-phone landscape
        extraPlugins = 'simpleLink,embedsemantic';
      }
      // this line is mandatory when a custom skin is defined
      CKEDITOR.basePath = '/commons-extension/ckeditor/';
      const self = this;
      $(this.$refs.editor).ckeditor({
        customConfig: '/commons-extension/ckeditorCustom/config.js',
        extraPlugins: extraPlugins,
        allowedContent: true,
        removePlugins: 'image,maximize,resize',
        toolbar: [
          ['Bold', 'Italic', 'BulletedList', 'NumberedList', 'Blockquote'],
        ],
        typeOfRelation: 'mention_activity_stream',
        spaceURL: eXo.env.portal.spaceUrl,
        autoGrow_onStartup: false,
        autoGrow_maxHeight: 300,
        on: {
          instanceReady: function () {
            self.editorReady = true;
            $(CKEDITOR.instances[self.ckEditorType].document.$)
              .find('.atwho-inserted')
              .each(function() {
                $(this).on('click', '.remove', function() {
                  $(this).closest('[data-atwho-at-query]').remove();
                });
              });
          },
          change: function (evt) {
            const newData = evt.editor.getData();

            self.inputVal = newData;

            const pureText = newData ? newData.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
            self.charsCount = pureText.length;
          },
          destroy: function () {
            self.inputVal = '';
            self.charsCount = 0;
          }
        }
      });
    },
    initCKEditorData: function(message) {
      if (message) {
        const tempdiv = $('<div class=\'temp\'/>').html(message);
        tempdiv.find('a[href*="/profile"]')
          .each(function() {
            $(this).replaceWith(function() {
              return $('<span/>', {
                class:'atwho-inserted',
                html: `<span class="exo-mention">${$(this).text()}<a data-cke-survive href="#" class="remove"><i data-cke-survive class="uiIconClose uiIconLightGray"></i></a></span>`
              }).attr('data-atwho-at-query',`@${  $(this).attr('href').substring($(this).attr('href').lastIndexOf('/')+1)}`)
                .attr('data-atwho-at-value',$(this).attr('href').substring($(this).attr('href').lastIndexOf('/')+1))
                .attr('contenteditable','false');
            });
          });
        message = `${tempdiv.html()  }&nbsp;`;
      }
      CKEDITOR.instances[this.ckEditorType].setData(message);
    },
    unload: function() {
      if (CKEDITOR.instances[this.ckEditorType]) {
        CKEDITOR.instances[this.ckEditorType].status = 'not-ready';
      }
    },
    setFocus: function() {
      if (CKEDITOR.instances[this.ckEditorType]) {
        CKEDITOR.instances[this.ckEditorType].status = 'ready';
        CKEDITOR.instances[this.ckEditorType].focus();
      }
    },
    getMessage: function() {
      const newData = CKEDITOR.instances[this.ckEditorType].getData();
      return newData ? newData : '';
    }
  }
};
</script>
