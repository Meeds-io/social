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
  <div class="d-none d-lg-block">
    <div class="text-header">
      {{ $t('generalSettings.preview.title') }}
    </div>
    <div
      ref="iframeParent"
      class="mt-4 position-relative border-color full-width aspect-ratio-1 overflow-hidden border-radius">
      <iframe
        v-if="initialized && parentWidth"
        id="previewIframe" 
        :title="$t('generalSettings.preview.title')"
        :src="pageHomeLink" 
        :width="`${iframeWidthPercentage}%`"
        :height="`${iframeHeightPercentage}%`"
        :style="`transform: scale(${zoom}); transform-origin: 0 0;`"
        name="pageHomeLink"
        class="no-border">
      </iframe>
      <div class="flex-grow-0 flex-shrink-1 position-absolute full-width full-height t-0"></div>
    </div>
  </div>
</template>
<script>
export default {
  data: ()=> ({
    initialized: false,
    sites: null,
    parentWidth: 0,
    zoom: 0.4,
  }),
  computed: {
    iframeWidthPercentage() {
      return parseInt((1 / this.zoom) * 100);
    },
    iframeHeightPercentage() {
      return parseInt((1 / this.zoom) * 100);
    },
    pageHomeLink() {
      return `${eXo.env.portal.context}/${this.sites?.[0]?.name || eXo.env.portal.metaPortalName}?sticky=false`;
    },
  },
  created() {
    this.$root.$on('refresh-style-property', this.setStyleProperty);
    this.$root.$on('refresh-body-style-property', this.setBodyStyleProperty);
    this.$root.$on('refresh-iframe', this.reloadURL);
    this.$root.$on('refresh-company-name', this.refreshCompanyName);
    this.$root.$on('refresh-company-logo', this.refreshCompanyLogo);
    this.init();
  },
  mounted() {
    this.computeWidth();
  },
  beforeDestroy() {
    this.$root.$off('refresh-style-property', this.setStyleProperty);
    this.$root.$off('refresh-body-style-property', this.setBodyStyleProperty);
    this.$root.$off('refresh-iframe', this.reloadURL);
    this.$root.$off('refresh-company-name', this.refreshCompanyName);
    this.$root.$off('refresh-company-logo', this.refreshCompanyLogo);
  },
  methods: {
    init() {
      return this.$siteService.getSites('PORTAL', null, 'global', true, true, false, true, true, true, true, true, true, ['displayed', 'temporal'])
        .then(data => this.sites = data || [])
        .then(this.computeWidth)
        .finally(() => this.initialized = true);
    },
    computeWidth() {
      if (this.$refs.iframeParent) {
        this.parentWidth = this.$refs.iframeParent.clientWidth;
      }
    },
    reloadURL() {
      document.getElementById('previewIframe').src = this.pageHomeLink;
    },
    setStyleProperty(event) {
      if (this.initialized
          && event?.detail
          && event.detail?.propertyName
          && event.detail?.propertyValue) {
        const propertyName = event.detail.propertyName;
        const propertyValue = event.detail.propertyValue;
        const iframeDoc = document.getElementById('previewIframe')?.contentWindow?.document?.documentElement;
        if (iframeDoc) {
          iframeDoc.style.setProperty(propertyName, propertyValue);
        }
      }
    },
    setBodyStyleProperty(property) {
      const propertyName = property.name;
      const propertyValue = property.value;
      const iframeDoc = document.getElementById('previewIframe')?.contentWindow?.document?.body;
      if (iframeDoc) {
        iframeDoc.style.setProperty(propertyName, propertyValue);
      }
    },
    refreshCompanyName(event) {
      if (event) {
        const companyNameElement =  document.getElementById('previewIframe')?.contentWindow?.document?.getElementsByClassName?.('logoTitle')[0];
        if (companyNameElement) {
          companyNameElement.innerHTML = event;
        }
      }
    },
    refreshCompanyLogo(event) {
      if (event) {
        const companyNameElement =  document.getElementById('previewIframe')?.contentWindow?.document?.getElementById?.('UserHomePortalLink').getElementsByTagName('img')[0];
        if (companyNameElement) {
          companyNameElement.src = event;
        }
      }
    }
  }
};
</script>
