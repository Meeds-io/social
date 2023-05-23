<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association
  contact@meeds.io
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
    content-class="uiPopup overflow-y-initial "
    max-width="80vw">
    <div class="ignore-vuetify-classes ClearFix preview-attachment-action d-flex justify-end">
      <v-btn 
        :href="attachmentURL" 
        :download="attachmentFilename"
        icon
        class="white--text"
        :class="!isMobile && 'icon-large-size' || 'icon-medium-size'"
        aria-hidden="true">
        <i class="fas fa-download"></i>
      </v-btn>
      <v-btn
        icon
        class="white--text ml-4"
        :class="!isMobile && 'icon-large-size' || 'icon-medium-size'"
        aria-hidden="true"
        @click="close">
        <i class="fas fa-times"></i>
      </v-btn>
    </div>
    <v-card 
      flat
      :max-height="!isMobile && '80vh' || '75vh'"
      class="transparent">
      <v-carousel 
        :id="`previewCarousel-${activityId}`"
        v-model="currentAttchmentId"
        :value="currentAttchmentId"
        :show-arrows-on-hover="!isMobile"                       
        :height="!isMobile && '80vh' || '75vh'"
        hide-delimiters   
        class="AttachmentCarouselPreview white border-radius">
        <v-carousel-item
          v-for="attachment in attachments"
          :key="attachment.id"
          :value="attachment.id"
          reverse-transition="fade-transition"
          transition="fade-transition">
          <v-img 
            :src="attachment.thumbnailUrl" 
            :aspect-ratio="2"
            :height="!isMobile && '80vh' || '75vh'"
            contain />
        </v-carousel-item>
      </v-carousel>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    }
  },
  data: () => ({
    dialog: false,
    currentAttchmentId: 0, 
    filename: '',
    objectType: 'activity'
  }),
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
  computed: {
    activityId() {
      return this.activity?.id;
    },
    attachmentURL() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/activity/${this.activity.id}/${this.currentAttchmentId}?size=0x0&download=true`;
    },
    attachmentFilename() {
      return  this.attachments?.length && this.attachments.filter(attachment => attachment.id === this.currentAttchmentId).finename || this.filename;
    }, 
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'md';
    }
  },
  created() {
    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape') {
        this.dialog = false;
      }
      if (event.key === 'ArrowLeft') {
        document.querySelector(`#previewCarousel-${this.activityId} .v-window__prev button`).click();
      }
      if (event.key === 'ArrowRight') {
        document.querySelector(`#previewCarousel-${this.activityId} .v-window__next button`).click();
      }
    });
  },
  methods: {
    open(attachments, id) {
      this.attachments = attachments;
      this.currentAttchmentId = id;
      this.filename = this.attachments.filter(attachment => attachment.id === id)[0].filename;
      this.dialog = true;
    },
    close() {
      this.dialog = false;
    },
  }
};
</script>

