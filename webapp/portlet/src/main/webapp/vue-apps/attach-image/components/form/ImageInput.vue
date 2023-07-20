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
      :object-id="objectId"
      :object-type="objectType"
      :images="attachedFiles"
      @delete="deleteImage" />
    <attachments-image-input-multi-upload
      ref="uploadInput"
      :images.sync="images"
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
  },
  data: () => ({
    images: [],
    attachments: [],
  }),
  computed: {
    attachedFiles() {
      if (this.attachments.length) {
        this.attachments.forEach(attachment => {
          attachment.src = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/${this.objectType}/${this.objectId}/${attachment.id}?size=120x120`;
        });
      }
      return [...this.attachments, ...this.images];
    },
  },
  watch: {
    attachedFiles() {
      this.$emit('changed', this.attachedFiles);
    },
  },
  created() {
    document.addEventListener('attachment-save', this.triggerAttachmentsSave);
  },
  beforeDestroy() {
    document.removeEventListener('attachment-save', this.triggerAttachmentsSave);
  },
  methods: {
    init() {
      if (this.objectType && this.objectId) {
        return this.$fileAttachmentService.getAttachments(this.objectType, this.objectId)
          .then(data => this.attachments = data?.attachments || [])
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

      if (objectType !== this.objectType || objectId !== this.objectId) {
        return;
      }
      return this.save();
    },
    uploadFiles(files) {
      this.$refs.uploadInput.uploadFiles(files);
    },
    save() {
      const uploadIds = this.images
        .filter((file) => file.progress === 100)
        .map((file) => file.uploadId)
        .filter((uploadId) => !!uploadId);
      const fileIds = this.attachments
        .filter(file => file.id)
        .map(file => file.id);
      fileIds.sort((a1, a2) => Number(a1.id) - Number(a2.id));

      return this.$fileAttachmentService.saveAttachments({
        objectType: this.objectType,
        objectId: this.objectId,
        uploadIds,
        fileIds,
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
          objectId: this.objectId,
        }}));
      }).finally(() => {
        this.images = [];
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
          this.attachments.splice(index, 1);
          this.attachments = this.attachments.slice();
        }
      }
    },
  },
};
</script>
