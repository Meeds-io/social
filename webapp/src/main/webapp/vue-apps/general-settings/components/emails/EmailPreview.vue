<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <div class="d-flex flex-column">
    <div class="d-flex align-center flex-wrap mb-4">
      <v-btn-toggle
        v-model="template"
        mandatory
        dense>
        <v-btn
          v-for="item in templates"
          :key="item"
          :value="item"
          :aria-label="$t(`generalSettings.emailPreview.template.${item}`)"
          class="text-none"
          small>
          {{ $t(`generalSettings.emailPreview.template.${item}`) }}
        </v-btn>
      </v-btn-toggle>
      <v-btn-toggle
        v-model="scheme"
        mandatory
        dense
        class="ms-4">
        <v-btn
          v-for="item in schemes"
          :key="item.value"
          :value="item.value"
          :aria-label="$t(`generalSettings.emailPreview.scheme.${item.value}`)"
          class="text-none"
          small>
          <v-icon size="14" class="me-2">{{ item.icon }}</v-icon>
          {{ $t(`generalSettings.emailPreview.scheme.${item.value}`) }}
        </v-btn>
      </v-btn-toggle>
    </div>
    <div class="text-caption text-sub-title mb-2">
      {{ $t('generalSettings.emailPreview.description') }}
    </div>
    <div class="position-relative border-color border-radius overflow-hidden">
      <v-progress-linear
        v-if="loading"
        indeterminate
        absolute
        top />
      <iframe
        :srcdoc="framedHtml"
        :title="$t('generalSettings.emailPreview.title')"
        class="no-border full-width email-preview-frame"
        sandbox
        height="600"></iframe>
      <div
        v-if="error"
        class="error--text pa-4">
        {{ $t('generalSettings.emailPreview.error') }}
      </div>
    </div>
  </div>
</template>
<script>
export default {
  data: () => ({
    templates: ['invitation', 'reset'],
    template: 'invitation',
    html: '',
    scheme: 'light',
    schemes: Object.freeze([
      {
        value: 'light',
        icon: 'fa-sun',
      },
      {
        value: 'dark',
        icon: 'fa-moon',
      },
    ]),
    loading: false,
    error: false,
    timer: null,
    request: 0,
  }),
  computed: {
    // The email follows the dark mode of the reader: the preview shows both of them whatever the screen of the administrator is
    framedHtml() {
      const darkMode = /@media\s*\(prefers-color-scheme:\s*dark\)/g;
      const themed = this.scheme === 'dark' ? this.html.replace(darkMode, '@media all') : this.html.replace(darkMode, '@media not all');
      // A preview only shows the email: its links and buttons cannot be clicked nor reached with the keyboard
      const inert = themed.replace(/(<a\b[^>]*?)\shref=/gi, '$1 data-href=');
      return inert.replace('</head>', '<style>a,button{pointer-events:none;cursor:default}</style></head>');
    },
    values() {
      return [this.template];
    },
  },
  watch: {
    values: {
      immediate: true,
      handler() {
        window.clearTimeout(this.timer);
        this.timer = window.setTimeout(this.refresh, 200);
      },
    },
  },
  beforeDestroy() {
    window.clearTimeout(this.timer);
  },
  methods: {
    refresh() {
      const request = ++this.request;
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding/email-preview`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          template: this.template,
        }),
      }).then(resp => {
        if (!resp?.ok) {
          throw new Error(resp?.status);
        }
        return resp.text();
      }).then(html => {
        // an answer of an older request must not replace a newer one
        if (request === this.request) {
          this.html = html;
          this.error = false;
        }
      }).catch(() => {
        if (request === this.request) {
          this.error = true;
        }
      }).finally(() => {
        if (request === this.request) {
          this.loading = false;
        }
      });
    },
  },
};
</script>
