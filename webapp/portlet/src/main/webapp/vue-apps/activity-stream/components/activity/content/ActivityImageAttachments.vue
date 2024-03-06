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
  <attachments-image-items
    :object-id="metadataObjectId"
    :object-type="metadataObjectType"
    :attachments="attachments"
    :preview-width="350"
    :preview-height="350" />
</template>
<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypeExtension: {
      type: Object,
      default: null,
    },
    isActivityDetail: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    body: null,
  }),
  computed: {
    activityId() {
      return this.activity?.id;
    },
    metadataObjectId() {
      return this.activity?.templateParams?.metadataObjectId || this.activityId;
    },
    metadataObjectType() {
      return this.activity?.templateParams?.metadataObjectType || 'activity';
    },
    attachments() {
      return this.activity?.metadatas?.attachments?.map(metadata => ({
        id: metadata.name,
        name: metadata.properties.fileName,
        size: metadata.properties.fileSize,
        mimetype: metadata.properties.fileMimeType,
        updated: metadata.properties.fileUpdateDate,
        alt: metadata.properties.alt || '',
      })) || [];
    },
  },
  created() {
    document.addEventListener('attachments-updated', this.updateActivity);
  },
  beforeDestroy() {
    window.setTimeout(() => {
      document.removeEventListener('attachments-updated', this.updateActivity);
    }, 200);
  },
  methods: {
    updateActivity(event) {
      if (this.attachments && event?.detail?.objectType === this.metadataObjectType && this.metadataObjectId === event?.detail?.objectId) {
        this.activity.metadatas.attachments = null;
      }
    },
  },
};
</script>
