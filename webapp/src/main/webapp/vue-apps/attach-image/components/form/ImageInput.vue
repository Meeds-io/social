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
  <div>
    <attachments-image-input-items 
      :object-id="attachmentObjectId"
      :object-type="objectType"
      :images="attachedFiles"
      @delete="deleteImage" />
    <attachments-image-input-multi-upload
      ref="uploadInput"
      :images.sync="images"
      :disable-paste="disablePaste"
      @delete="deleteImage" />
  </div>
</template>
<script>
export default {
  props: {
    maxFileSize: {
      type: Number,
      default: () => 20971520,
    },
    objectId: {
      type: String,
      default: null
    },
    objectType: {
      type: String,
      default: null
    },
    disablePaste: {
      type: Boolean,
      default: false
    },
  },
  data: () => ({
    images: [],
    attachments: [],
    attachmentUpdated: true,
    metadatasObjectId: null,
    haveChanges: false,
  }),
  computed: {
    attachedFiles() {
      if (this.attachments.length && this.attachmentUpdated) {
        this.attachments.forEach(attachment => {
          const baseAttchementSrc = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/${this.objectType}/${this.attachmentObjectId}/${attachment.id}?lastModified=${attachment.updated}`;
          attachment.src = ( this.objectType === 'activity' ) ? baseAttchementSrc : `${baseAttchementSrc}&size=120x120`;
        });
      }
      return [...this.attachments, ...this.images];
    },
    attachmentObjectId() {
      return this.objectId || this.metadatasObjectId;
    }
  },
  watch: {
    attachedFiles() {
      this.$emit('changed', this.attachedFiles, this.haveChanges);
      if (!this.haveChanges) {
        this.haveChanges = true;
      }
    },
  },
  created() {
    document.addEventListener('attachment-save', this.triggerAttachmentsSave);
    document.addEventListener('attachment-update', this.updateImage);
    extensionRegistry.registerExtension(this.objectType, 'saveAction', {
      key: 'attachment',
      postSave: object => {
        if (!object?.id || this.objectType !== object.type) {
          return;
        }
        this.metadatasObjectId = object.id;
        this.save();
      },
    });
  },
  beforeDestroy() {
    document.removeEventListener('attachment-save', this.triggerAttachmentsSave);
    document.removeEventListener('attachment-update', this.updateImage);
  },
  methods: {
    init() {
      if (this.objectType && this.attachmentObjectId) {
        return this.$fileAttachmentService.getAttachments(this.objectType, this.attachmentObjectId)
          .then(data => {
            this.attachments = data?.attachments || [];
          })
          .catch(() => this.reset());
      } else {
        this.reset();
      }
    },
    reset() {
      this.attachments = [];
    },
    triggerAttachmentsSave(event) {
      const objectType = event?.detail?.objectType;
      const objectId = event?.detail?.objectId;

      if (objectType !== this.objectType || objectId !== this.attachmentObjectId) {
        return;
      }
      return this.save();
    },
    uploadFiles(files) {
      this.$refs.uploadInput.uploadFiles(files);
    },
    save() {
      const uploadedFiles = this.images
        .filter((file) => file.progress >= 100)
        .map((file) => ({
          uploadId: file.uploadId,
          altText: file?.altText || ''
        }));
      const attachedFiles = this.attachments
        .filter(file => file.id)
        .map(file => ({
          id: file.id,
          uploadId: file?.uploadId || '' ,
          altText: file?.altText || ''
        }));
      attachedFiles.sort((a1, a2) => Number(a1.id) - Number(a2.id));
      return this.$fileAttachmentService.saveAttachments({
        objectType: this.objectType,
        objectId: this.attachmentObjectId,
        uploadedFiles,
        attachedFiles,
      }).then((report) => {
        if (report?.errorByUploadId?.length) {
          const attachmentHtmlError = Object.keys(report.errorByUploadId)
            .map((uploadId) => {
              const attachment = this.images.find(
                (file) => file.uploadId === uploadId
              );
              return `- <strong>${attachment.name}</strong>: this.$t(report.errorByUploadId[uploadId])`;
            }).join('<br>');
          this.$root.$emit('alert-message-html', attachmentHtmlError, 'error');
        }
        document.dispatchEvent(new CustomEvent('attachments-updated', {detail: {
          objectType: this.objectType,
          objectId: this.attachmentObjectId,
        }}));
      }).finally(() => {
        this.images = [];
        this.attachmentUpdated = true;
        if (this.$refs.uploadInput) {
          this.$refs.uploadInput.reset();
        }
      });
    },
    deleteImage(image) {
      if (image.uploadId) {
        const index = this.images.findIndex(file => file.uploadId === image.uploadId);
        if (index >= 0) {
          this.images.splice(index, 1);
          this.images = this.images.slice();
        }
      } else if (image.id) {
        const index = this.attachments.findIndex(file => file.id === image.id);
        if (index >= 0) {
          this.attachmentUpdated = true;
          this.attachments.splice(index, 1);
          this.attachments = this.attachments.slice();
        }
      }
    },
    updateImage(event) {
      if (event?.detail) {
        const updatedImage = event.detail;
        if (updatedImage.oldUploadId?.length) {
          const index = this.images.findIndex(file => file.uploadId === updatedImage.oldUploadId);
          if (updatedImage?.mimetype && updatedImage?.mimetype !== 'image/gif') {
            this.images[index].src = updatedImage.src;
            this.images[index] = updatedImage;
          } else {
            this.images[index].altText = updatedImage.altText;
          }
          
        } else if (updatedImage.id?.length) {
          const index = this.attachments.findIndex(file => file.id === updatedImage.id);
          if (index >= 0) {
            this.attachmentUpdated = false;
            if (updatedImage?.mimetype && updatedImage?.mimetype !== 'image/gif') {
              this.attachments[index].src = updatedImage?.src;
              this.attachments[index] = updatedImage;
            } else {
              this.attachments[index].altText = updatedImage.altText;
            }
            this.attachments = this.attachments.slice();
          }
        }
      }
    },
  },
};
</script>
