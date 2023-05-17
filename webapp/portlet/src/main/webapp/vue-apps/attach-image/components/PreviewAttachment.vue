<template>
  <div>
    <v-dialog
      v-model="dialog"
      width="84vw"
      :persistent="false"
      overlay-opacity="0.9"
      content-class="uiPopup overflow-y-initial "
      max-width="80vw">
      <div class="ignore-vuetify-classes ClearFix preview-attachment-action d-flex justify-end">
        <a 
          class="icon-large-size white--text"
          aria-hidden="true"
          :href="url" 
          :download="filename">
          <i class="fas fa-download"></i>
        </a>
        <a
          class="icon-large-size white--text ml-4"
          aria-hidden="true"
          @click="close">
          <i class="fas fa-times"></i>
        </a>
      </div>
      <v-card 
        flat
        max-height="80vh"
        class="transparent">
        <div class="previewContainer ">
          <v-carousel 
            v-model="currentIndex"
            :id="`previewCarousel-${activityId}`"
            class="AttachmentCarouselPreview"
            hide-delimiters
            :touchless="true"
            height="100%"
            @change="currentIndex = $event">
            <v-carousel-item
              v-for="(attachment, index) in attachments"
              :key="index"
              :src="attachment.thumbnailUrl" />
          </v-carousel>
        </div>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    }
  },
  data: () => ({
    dialog: false,
    currentIndex: 0, 
    filename: '',
    url: '#',
    objectType: 'activity'
  }),
  watch: {
    dialog() {
      if (this.dialog) {
        this.$emit('dialog-opened');
        document.dispatchEvent(new CustomEvent('modalOpened'));
      } else {
        this.$emit('dialog-closed');
        document.dispatchEvent(new CustomEvent('modalClosed'));
      }
    },
    currentIndex() {
      if (this.attachments?.length) {
        const attachmentInPreview = this.attachments[this.currentIndex].id;
        this.downloadAttachment(attachmentInPreview);
      }
      
    }
  },
  computed: {
    activityId() {
      return this.activity?.id;
    }
  },
  created() {
    document.onkeydown = this.closePreviewAttachment;
  },
  methods: {
    closePreviewAttachment(event) {
      if (event && event.key === 'Escape') {
        this.dialog = false;
      }
    },
    open(attachments, index) {
      this.attachments = attachments;
      this.currentIndex = index;
      this.dialog = true;
    },
    close() {
      this.dialog = false;
    },
    downloadAttachment(id) {
      this.$fileAttachmentService.getAttachment(this.objectType,this.activity.id,id)
        .then(resp => {
          this.filename = resp.headers.get('Content-Disposition').split(';')[0].split('=')[1].replaceAll('"', '');
          resp.blob().then(blob => {
            if (blob != null) {
              this.url = URL.createObjectURL(blob);
            }
          });
        });
    }
  }
};
</script>

