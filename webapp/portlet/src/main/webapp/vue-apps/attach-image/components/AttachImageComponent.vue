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
    <attachments-multi-upload-input @update-images="upadateImage" />
    <image-items 
      :object-id="objectId"
      :object-type="objectType"
      :images="attachedFiles" />
  </div>
</template>
<script>
export default {
  props: {
    maxFileSize: {
      type: Number,
      default: 20971520,
    },
    objectId: {
      type: String,
      default: null
    },
    objectType: {
      type: String,
      default: null
    },
    attachments: {
      type: Array,
      default: null
    }
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
  computed: {
    attachedFiles() {
      if (this.attachments.length) {
        this.attachments.forEach(attachment => {
          attachment.src = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/${this.objectType}/${this.objectId}/${attachment.name}?size=120x120`;
        });
      }
      return [...this.attachments, ...this.images];
    }
  },
  created() {
    extensionRegistry.registerExtension('activity', 'saveAction', {
      key: 'attachment',
      postSave: (activity) => {
        if (!activity?.id) {
          return;
        }
        const uploadIds = this.images
          .filter((file) => file.progress === 100)
          .map((file) => file.uploadId)
          .filter((uploadId) => !!uploadId).reverse();
        if (!uploadIds?.length) {
          return;
        }
        return this.$fileAttachmentService
          .createAttachments({
            objectType: this.objectType,
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
          .finally(() => {
            this.clearFiles();
            this.$root.$emit('delete-uploaded-files');
          });
      }
    });
    extensionRegistry.registerExtension('activity', 'updateAction', {
      key: 'attachment',
      postUpdate: (activity) => {
        if (!activity?.id) {
          return;
        }
        const attachments = activity?.metadatas?.attachments || [];
        let fileIds = [];
        if (attachments.length) {
          fileIds = attachments.map((file) => file.name).filter((name) => !!name);
        }
        const objectId = activity.id;
        const objectType = this.objectType;
        return this.$fileAttachmentService
          .updateAttachments(objectType, objectId, fileIds);
      },
    });
  },

  methods: {
    upadateImage(images) {
      this.images = images;
    },
    clearFiles() {
      this.images = [];
    }
  },
};
</script>