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
  <v-dialog
    v-model="dialog"
    :persistent="false"
    width="80vw"
    overlay-opacity="0.9"
    content-class="overflow-y-initial"
    max-width="80vw">
    <template v-if="dialog">
      <div class="ignore-vuetify-classes ClearFix preview-attachment-action d-flex justify-end">
        <v-btn
          id="preview-attachment-download"
          :href="downloadURL"
          :download="attachmentFilename"
          :class="!isMobile && 'icon-large-size' || 'icon-medium-size'"
          :title="$t('attachment.imageDownload')"
          icon
          class="white--text">
          <i class="fas fa-download"></i>
        </v-btn>
        <v-btn
          id="preview-attachment-close"
          :class="!isMobile && 'icon-large-size' || 'icon-medium-size'"
          :title="$t('attachment.closePreview')"
          icon
          class="white--text ml-4"
          @click="close">
          <i class="fas fa-times"></i>
        </v-btn>
      </div>
      <v-card 
        flat
        :max-height="!isMobile && '80vh' || '75vh'"
        class="transparent">
        <v-carousel
          :id="`previewCarousel`"
          ref="attachmentsCarousel"
          v-model="currentAttachmentId"
          :show-arrows-on-hover="!isMobile"
          :show-arrows="attachments.length > 1"                         
          :height="!isMobile && '80vh' || '75vh'"
          hide-delimiters   
          class="AttachmentCarouselPreview white border-radius">
          <v-carousel-item
            v-for="attachment in sortedAttachments"
            :key="attachment.id"
            :value="attachment.id"
            reverse-transition="fade-transition"
            transition="fade-transition">
            <extension-registry-component
              :component="getExtension(attachment)"
              :params="getParams(attachment)"
              :element="div" />
          </v-carousel-item>
        </v-carousel>
      </v-card>
    </template>
  </v-dialog>
</template>
<script>
export default {
  data: () => ({
    dialog: false,
    currentAttachmentId: 0, 
    filename: '',
    fileUrl: '',
    objectType: '',
    attachments: null,
    previewExtensionApp: 'Preview',
    previewExtensionType: 'previewExtensions',
    previewExtensions: [],
  }),
  computed: {
    downloadURL() {
      return `${eXo.env.portal.context}${ this.attachments?.length && this.attachments.find(attachment => attachment.id === this.currentAttachmentId).downloadUrl}?size=0x0&download=true` || this.fileUrl;
    },
    attachmentFilename() {
      return  this.attachments?.length && this.attachments.find(attachment => attachment.id === this.currentAttachmentId).filename || this.filename;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'md';
    },
    sortedAttachments() {
      const sortedAttachments = this.attachments?.length && this.attachments.slice() || [];
      sortedAttachments.sort((a1, a2) => Number(a1.id) - Number(a2.id));
      return sortedAttachments;
    },
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.$emit('dialog-opened');
        document.dispatchEvent(new CustomEvent('modalOpened'));
      } else {
        this.$emit('dialog-closed');
        document.dispatchEvent(new CustomEvent('modalClosed'));
      }
    }
  },
  created() {
    this.refreshPreviewExtensions();
    document.addEventListener(`extension-${this.previewExtensionApp}-${this.previewExtensionType}-updated`, this.refreshPreviewExtensions);
    document.addEventListener('open-preview-dialog', this.openPreview);
    document.addEventListener('keydown', (event) => {
      if (this.$refs.attachmentsCarousel) {
        if (event.key === 'Escape') {
          this.dialog = false;
        } else if (event.key === 'ArrowLeft') {
          this.$refs.attachmentsCarousel.prev();
        } else if (event.key === 'ArrowRight') {
          this.$refs.attachmentsCarousel.next();
        }
      }
    });
  },
  methods: {
    refreshPreviewExtensions() {
      this.previewExtensions = extensionRegistry.loadExtensions(this.previewExtensionApp, this.previewExtensionType);
    },
    getParams(attachment) {
      return {
        attachment: attachment,
        objectType: this.objectType,
        objectId: this.objectId,
      };
    },
    getExtension(attachment) {
      return this.previewExtensions.find((element) => attachment.mimetype.includes(element.fileType)) || this.previewExtensions.find((element) => element.default);
    },
    openPreview(event) {
      const attachment = event?.detail;
      this.attachments = attachment.attachments;
      this.currentAttachmentId = attachment.id;
      this.filename = this.attachments.find(file => file.id === attachment.id).filename;
      this.fileUrl = this.attachments.find(file => file.id === attachment.id).downloadUrl;
      this.dialog = true;
    },
    close() {
      this.dialog = false;
      this.attachments = null;
      this.currentAttachmentId = null;
      this.filename = null;
      this.fileUrl = null;
    },
  }
};
</script>