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
  <div v-if="attachmentsCount">
    <card-carousel parent-class="attachments-image-parent">
      <attachments-image-item
        v-for="attachment in sortedAttachments"
        :key="attachment.id"
        :attachment="attachment"
        :object-type="objectType"
        :object-id="objectId"
        :preview-width="previewWidth"
        :preview-height="previewHeight"
        class="attachments-image-item"
        @preview-attachment="openPreview(attachment.id)" />
    </card-carousel>
  </div>
</template>

<script>
export default {
  props: {
    objectType: {
      type: String,
      default: null,
    },
    objectId: {
      type: String,
      default: null,
    },
    attachments: {
      type: Array,
      default: null,
    },
    previewHeight: {
      type: Number,
      default: () => 50,
    },
    previewWidth: {
      type: Number,
      default: () => 50,
    },
  },
  data: () => ({
    updatedAttachments: null,
  }),
  computed: {
    imageAttachments() {
      return this.updatedAttachments || this.attachments || [];
    },
    attachmentsCount() {
      return this.imageAttachments?.length || 0;
    },
    sortedAttachments() {
      const sortedAttachments = this.attachmentsCount && this.imageAttachments.slice() || [];
      sortedAttachments.sort((a1, a2) => Number(a1.id) - Number(a2.id));
      return sortedAttachments;
    },
  },
  created() {
    document.addEventListener('attachments-updated', this.updateAttachments);
    if (!this.attachments) {
      this.retrieveAttachments();
    }
  },
  beforeDestroy() {
    document.removeEventListener('attachments-updated', this.updateAttachments);
  },
  methods: {
    updateAttachments(event) {
      if (event?.detail?.objectType === this.objectType && this.objectId === event?.detail?.objectId) {
        this.retrieveAttachments();
      }
    },
    retrieveAttachments() {
      return this.$fileAttachmentService.getAttachments(this.objectType, this.objectId)
        .then(data => this.updatedAttachments = data?.attachments || []);
    },
    openPreview(attachmentId) {
      this.$root.$emit('open-attachments-preview', this.objectType, this.objectId, this.imageAttachments || [], attachmentId);
    },
  },
};
</script>
