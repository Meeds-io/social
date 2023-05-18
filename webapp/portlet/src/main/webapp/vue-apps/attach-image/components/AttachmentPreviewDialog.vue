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
  <div>
    <v-dialog
      v-model="dialog"
      :persistent="false"
      width="84vw"
      overlay-opacity="0.9"
      content-class="uiPopup overflow-y-initial "
      max-width="80vw">
      <div class="ignore-vuetify-classes ClearFix preview-attachment-action d-flex justify-end">
        <v-btn 
          :href="url" 
          :download="filename"
          icon
          class="icon-large-size white--text"
          aria-hidden="true">
          <i class="fas fa-download"></i>
        </v-btn>
        <v-btn
          icon
          class="icon-large-size white--text ml-4"
          aria-hidden="true"
          @click="close">
          <i class="fas fa-times"></i>
        </v-btn>
      </div>
      <v-card 
        flat
        max-height="80vh"
        class="transparent">
        <v-carousel 
          v-model="currentAttchmentId"
          :id="`previewCarousel-${activityId}`"
          :touchless="true"
          :value="currentAttchmentId"
          :show-arrows-on-hover="!isMobile"
          reverse-transition="fade-transition"
          transition="fade-transition"
          hide-delimiters                        
          height="80vh"
          class="AttachmentCarouselPreview white border-radius">
          <v-carousel-item
            v-for="attachment in attachments"
            :key="attachment.id"
            :value="attachment.id">
            <v-img 
              :src="attachment.thumbnailUrl" 
              aspect-ratio="2"
              contain />
          </v-carousel-item>
        </v-carousel>
      </v-card>
    </v-dialog>
  </div>
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
    url: '#',
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
    },
    currentAttchmentId() {
      if (this.attachments?.length) {
        this.downloadAttachment(this.currentAttchmentId);
      }
      
    }
  },
  computed: {
    activityId() {
      return this.activity?.id;
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
    });
  },
  methods: {
    open(attachments, id) {
      this.attachments = attachments;
      this.currentAttchmentId = id;
      this.dialog = true;
    },
    close() {
      this.dialog = false;
    },
    downloadAttachment(id) {
      this.$fileAttachmentService.getAttachment(this.objectType,this.activity.id,id)
        .then(resp => {
          this.filename = resp.headers.get('Content-Disposition').split(';')[0].split('=')[1].replaceAll('"', '');
          resp.blob().then(blob => {
            if (blob != null) {
              this.url = URL.createObjectURL(blob);
            }
          });
        });
    }
  }
};
</script>

