<template>
  <card-carousel v-if="attachmentsCount" parent-class="activity-files-parent">
    <attached-item
      v-for="(attachment, index) in attachments"
      :key="attachment.id"
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
      return this.activity?.metadatas?.attachments?.map(metadata => ({
        id: metadata.name,
        thumbnailUrl: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/activity/${this.activity.id}/${metadata.name}?size=250x250`,
      })) || [];
    },
    attachmentsCount() {
      return this.attachments.length;
    },
  }
};
</script>