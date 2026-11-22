<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <attachments-default-preview
    v-if="error"
    :attachment="attachment"
    :error-message="$t('attachment.markdown.unreadable')" />
  <v-card
    v-else
    :height="height"
    flat
    class="markdownPreview overflow-y-auto text-start pa-4 pa-sm-8">
    <div
      v-if="loading"
      class="d-flex full-height align-center justify-center">
      <v-progress-circular
        :aria-label="$t('attachment.markdown.loading')"
        color="primary"
        indeterminate />
    </div>
    <template v-else>
      <div
        v-sanitized-document="html"
        class="markdownPreviewContent reset-style-box rich-editor-content text-break"></div>
      <v-divider v-if="truncated" class="mt-6" />
      <div
        v-if="truncated"
        class="markdownPreviewTruncated d-flex flex-wrap align-center pt-4">
        <span class="me-2">{{ $t('attachment.markdown.truncated') }}</span>
        <v-btn
          :href="attachment.downloadUrl"
          :download="attachment.filename"
          color="primary"
          text>
          {{ $t('attachment.downloadFile') }}
        </v-btn>
      </div>
    </template>
  </v-card>
</template>
<script>
import {loadRenderer, readBeginning, toPreviewHtml} from '../../js/MarkdownPreview.js';

export default {
  props: {
    attachment: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    loading: true,
    error: false,
    html: '',
    truncated: false,
  }),
  computed: {
    /**
     * Whether the screen is a phone's or a tablet's, as the other previews
     * tell it.
     *
     * @returns {boolean} true on a small, extra-small or medium breakpoint
     */
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'md';
    },
    /**
     * The preview's height, a little lower on a phone or a tablet.
     *
     * @returns {string} the card's height, as a viewport share
     */
    height() {
      return this.isMobile ? '75vh' : '80vh';
    },
  },
  /**
   * Starts reading and rendering the file as soon as the preview is created.
   *
   * @returns {void}
   */
  created() {
    this.load();
  },
  methods: {
    /**
     * Reads the beginning of the file with the viewer's own session, and
     * renders it. The rendered HTML is bound once, through
     * v-sanitized-document, which sanitizes it before it reaches the page.
     * A file that cannot be read shows the default preview with an error.
     *
     * @returns {Promise<void>} resolved once the file is shown
     */
    async load() {
      this.loading = true;
      this.error = false;
      try {
        const [renderer, file] = await Promise.all([
          loadRenderer(),
          readBeginning(this.attachment?.downloadUrl),
        ]);
        this.html = toPreviewHtml(file.text, renderer);
        this.truncated = file.truncated;
      } catch (e) {
        console.warn('Error while previewing a Markdown file', e); // eslint-disable-line no-console
        this.error = true;
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>
