<template>
  <card-carousel parent-class="activity-files-parent">
    <attached-item
      v-for="(attachment, index) in attachments"
      :key="index"
      :activity="activity"
      :index="index"
      :count="attachmentsCount"
      :attachment="attachment"
      :preview-width="previewWidth"
      :preview-height="previewHeight"
      class="activity-file-item" />
  </card-carousel>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    objectTypes: {
      type: String,
      default: null
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
  computed: {
    attachments() {
      const attachments = [];
      this.activity.metadatas.attachment.forEach(attachment => {
        const imageURL = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/${this.objectTypes}/${this.activity.id}/${attachment.name}` || null;
        attachments.push({
          image: imageURL,
        });
      });
      return attachments;
    },
    attachmentsCount() {
      return this.attachments.length;
    },
  }
};
</script>