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
          // reinitialize CKEditor instance
          CKEDITOR.instances[this.ckEditorType].destroy(true);
          this.initCKEditor();
          CKEDITOR.instances[this.ckEditorType].setData(val);
        }
      }
    }
  },
  mounted() {
    this.initCKEditor();
  },
  methods: {
    initCKEditor: function () {
      CKEDITOR.plugins.addExternal('embedsemantic', '/commons-extension/eXoPlugins/embedsemantic/', 'plugin.js');
      if (typeof CKEDITOR.instances[this.ckEditorType] !== 'undefined' && !this.ckEditorType.includes('editActivity')) {
        CKEDITOR.instances[this.ckEditorType].destroy(true);
      }
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
    setFocus: function() {
      CKEDITOR.instances[this.ckEditorType].focus();
    },
    getMessage: function() {
      const newData = CKEDITOR.instances[this.ckEditorType].getData();
      return newData ? newData : '';
    }
  }
};
</script>
