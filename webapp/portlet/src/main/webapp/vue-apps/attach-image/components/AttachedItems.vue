<template>
  <div v-if="attachmentsCount">
    <card-carousel parent-class="activity-files-parent">
      <attached-item
        v-for="attachment in attachments"
        :key="attachment.id"
        :attachment="attachment"
        :preview-width="previewWidth"
        :preview-height="previewHeight"
        class="activity-file-item"
        @preview-attachment="openPreview(attachment.id)" />
    </card-carousel>
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
    objectType: 'activity',
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
    objectId() {
      return this.activity?.id;
    },
  },
  methods: {
    openPreview(attachmentId) {
      const previewAttachments = this.activity?.metadatas?.attachments?.map(metadata => ({
        id: metadata.name,
        filename: metadata.properties.fileName,
        fileSize: metadata.properties.fileSize,
        fileMimeType: metadata.properties.fileMimeType,
        fileUpdateDate: metadata.properties.fileUpdateDate,
        thumbnailUrl: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/activity/${this.activity.id}/${metadata.name}?size=0x0&download=true`,
      })) || [];

      this.$root.$emit('open-attachments-preview', this.objectType, this.objectId, previewAttachments, attachmentId);
    },
  }
};
</script>
