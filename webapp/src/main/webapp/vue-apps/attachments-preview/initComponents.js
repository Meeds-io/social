/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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

import AttachmentsPreviewDialog from './components/preview/AttachmentsPreviewDialog.vue';
import AttachmentsImagePreview from './components/preview/AttachmentsImagePreview.vue';
import AttachmentsDefaultPreview from './components/preview/AttachmentsDefaultPreview.vue';
import AttachmentPreviewActions from './components/preview/AttachmentPreviewActions.vue';
import AttachmentsPreviewDownloadAction from './components/preview/AttachmentsPreviewDownloadAction.vue';
import AttachmentsPreviewCloseAction from './components/preview/AttachmentsPreviewCloseAction.vue';

const components = {
  'attachments-preview-dialog': AttachmentsPreviewDialog,
  'attachments-image-preview': AttachmentsImagePreview,
  'attachments-default-preview': AttachmentsDefaultPreview,
  'attachment-preview-actions': AttachmentPreviewActions,
  'attachment-preview-download-action': AttachmentsPreviewDownloadAction,
  'attachment-preview-close-action': AttachmentsPreviewCloseAction,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
