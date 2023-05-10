<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <div class="px-3">
    <file-multi-upload-input @update-images="upadateImage" />
    <image-items :images="images" />
  </div>
</template>
<script>
export default {
  props: {
    maxFileSize: {
      type: Number,
      default: 20971520,
    },
  },
  data: () => ({
    images: [],
  }),
  watch: {
    images: {
      deep: true,
      immediate: true,
      handler: function() {
        const uploadedImages = this.images.filter(file => file.progress === 100).length;
        if (this.images.length > 0) {
          if (uploadedImages === this.images.length) {
            this.$emit('attachments-uploading');
          }
        } else {
          this.$emit('attachments-deleted');
        }
      },
    }
  },
  created() {
    extensionRegistry.registerExtension('activity', 'saveAction', {
      key: 'attachment',
      canPostActivity: () => {
        const uploadIds = this.images
          .filter((file) => file.progress === 100)
          .map((file) => file.uploadId)
          .filter((uploadId) => !!uploadId);
        return uploadIds?.length;
      },
      postSave: (activity) => {
        if (!activity?.id) {
          return;
        }
        const uploadIds = this.images
          .filter((file) => file.progress === 100)
          .map((file) => file.uploadId)
          .filter((uploadId) => !!uploadId);
        if (!uploadIds?.length) {
          return;
        }
        return this.$fileAttachmentService
          .createAttachments({
            objectType: 'activity',
            objectId: activity?.activityId || activity?.id,
            uploadIds: uploadIds,
          })
          .then((report) => {
            if (report.errorByUploadId?.length) {
              const attachmentHtmlError = Object.keys(report.errorByUploadId)
                .map((uploadId) => {
                  const attachment = this.images.find(
                    (file) => file.uploadId === uploadId
                  );
                  return `- <strong>${attachment.name}</strong>: this.$t(report.errorByUploadId[uploadId])`;
                })
                .join('<br>');
              this.$root.$emit(
                'alert-message-html',
                attachmentHtmlError,
                'error'
              );
            }
          })
          .finally(() => (this.images = []));
      },
    });
  },
  methods: {
    upadateImage(images) {
      this.images = images;
    },
  },
};
</script>