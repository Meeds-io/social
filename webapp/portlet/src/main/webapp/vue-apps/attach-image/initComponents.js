/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import AttachImageComponent from './components/AttachImageComponent.vue';
import AttachmentsMultiUploadInput from './components/AttachmentsMultiUploadInput.vue';
import ImageItems from './components/ImageItems.vue';
import ImageItem from './components/ImageItem.vue';
import AttachedItem from './components/AttachedItem.vue';
import AttachedItems from './components/AttachedItems.vue';
import AttachmentPreviewDialog from './components/AttachmentPreviewDialog.vue';

const components = {
  'attach-image-component': AttachImageComponent,
  'attachments-multi-upload-input': AttachmentsMultiUploadInput,
  'image-items': ImageItems,
  'image-item': ImageItem,
  'attached-item': AttachedItem,
  'attached-items': AttachedItems,
  'attachment-preview-dialog': AttachmentPreviewDialog
};

for (const key in components) {
  Vue.component(key, components[key]);
}
