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
import ImageInput from './components/form/ImageInput.vue';
import ImageInputMultiUpload from './components/form/ImageInputMultiUpload.vue';
import ImageInputItems from './components/form/ImageInputItems.vue';
import ImageInputItem from './components/form/ImageInputItem.vue';
import ImageAttachmentCropDrawer from './components/form/ImageAttachmentCropDrawer.vue';

import ImageItems from './components/view/ImageItems.vue';
import ImageItem from './components/view/ImageItem.vue';

import ImagePreviewDialog from './components/preview/ImagePreviewDialog.vue';
import ImagePreviewItem from './components/preview/ImagePreviewItem.vue';

const components = {
  'attachments-image-input': ImageInput,
  'attachments-image-input-multi-upload': ImageInputMultiUpload,
  'attachments-image-input-items': ImageInputItems,
  'attachments-image-input-item': ImageInputItem,
  'attachments-image-crop-drawer': ImageAttachmentCropDrawer,
  'attachments-image-items': ImageItems,
  'attachments-image-item': ImageItem,
  'attachments-image-preview-dialog': ImagePreviewDialog,
  'attachments-image-preview-item': ImagePreviewItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
