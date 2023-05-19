<template>
  <div v-if="attachmentsCount">
    <card-carousel parent-class="activity-files-parent">
      <attached-item
        v-for="(attachment, index) in attachments"
        :key="attachment.id"
        :activity="activity"
        :index="index"
        :attachment="attachment"
        :preview-width="previewWidth"
        :preview-height="previewHeight"
        class="activity-file-item clickable"
        @preview-attachment="openPreview(attachment.id)" />
    </card-carousel>
    <attachment-preview-dialog 
      ref="attachmentPreview"
      :activity="activity" /> 
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    previewHeight: {
      type: String,
      default: () => '152px',
    },
    previewWidth: {
      type: String,
      default: () => '250px',
    },
  },
  data: () => ({
    previewAttachments: []
  }),
  computed: {
    attachments() {
      return this.activity?.metadatas?.attachments?.map(metadata => ({
        id: metadata.name,
        thumbnailUrl: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/activity/${this.activity.id}/${metadata.name}?size=250x250`,
      })) || [];
    },
    attachmentsCount() {
      return this.attachments.length;
    },
  },
  methods: {
    openPreview(attachmentId) {
      this.previewAttachments = this.activity?.metadatas?.attachments?.map(metadata => ({
        id: metadata.name,
        filename: metadata.properties.fileName,
        thumbnailUrl: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/activity/${this.activity.id}/${metadata.name}?size=0x0&download=true`,
      })) || [];
      this.$refs.attachmentPreview.open(this.previewAttachments, attachmentId);
    },
  }
};
</script>