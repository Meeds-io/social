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
    <div>
      <h4 class="font-weight-bold mt-0">
        {{ $t('generalSettings.preview.title') }}
      </h4>
    </div>
    <div class="mt-4 position-relative border-color full-width overflow-hidden border-radius" style="height:470px;">
      <iframe 
        id="previewIframe" 
        :title="$t('generalSettings.preview.title')"
        :src="pageHomeLink" 
        name="pageHomeLink"
        width="167%"
        height="220%"
        style="transform: scale(0.6); transform-origin: 0 0;"
        class="no-border">
      </iframe>
      <div class="position-absolute full-width full-height t-0"></div>
    </div>
  </div>
</template>
<script>
export default {
  data: ()=> ({
    pageHomeLink: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/stream?sticky=false`,
  }),
  mounted() {
    this.$root.$on('refresh-style-property', this.setStyleProperty);
    this.$root.$on('refresh-iframe', this.reloadURL);
    this.$root.$on('refresh-company-name', this.refreshCompanyName);
    this.$root.$on('refresh-company-logo', this.refreshCompanyLogo);
  },
  beforeDestroy() {
    this.$root.$off('refresh-style-property', this.setStyleProperty);
    this.$root.$off('refresh-iframe', this.reloadURL);
    this.$root.$off('refresh-company-name', this.refreshCompanyName);
    this.$root.$off('refresh-company-logo', this.refreshCompanyLogo);
  },
  methods: {
    reloadURL() {
      document.getElementById('previewIframe').src = this.pageHomeLink;
    },
    setStyleProperty(event) {
      if (event?.detail && event.detail?.propertyName && event.detail?.propertyValue) {
        const propertyName = event.detail.propertyName;
        const propertyValue = event.detail.propertyValue;
        const iframeDoc = document.getElementById('previewIframe').contentWindow.document.documentElement;
        iframeDoc.style.setProperty(propertyName, propertyValue);
      }
    },
    refreshCompanyName(event) {
      if (event) {
        const companyNameElement =  document.getElementById('previewIframe').contentWindow.document.getElementsByClassName('logoTitle')[0];
        if (companyNameElement) {
          companyNameElement.innerHTML = event;
        }
      }
    },
    refreshCompanyLogo(event) {
      if (event) {
        const companyNameElement =  document.getElementById('previewIframe').contentWindow.document.getElementById('UserHomePortalLink').getElementsByTagName('img')[0];
        if (companyNameElement) {
          companyNameElement.src = event;
        }
      }
    }
  }
};
</script>
