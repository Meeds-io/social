<template>
  <div class="d-flex flex-column white border-radius">
    <img
      :height="height"
      :style="{maxHeight: height}"
      src="/social-portlet/images/emptyUserStream.png"
      class="mx-auto">
    <div class="mx-4 mb-4">
      <p v-sanitized-html="welcomeTitle"></p>
      <div v-sanitized-html="thanksForJoingMeeds"></div>
      <div v-sanitized-html="welcomeParagraph1"></div>
      <div v-sanitized-html="welcomeParagraph2" class="mb-4"></div>
      <div v-sanitized-html="welcomeParagraph3"></div>
      <div v-sanitized-html="welcomeParagraph4" class="mb-4"></div>
      <div v-sanitized-html="welcomeParagraph5" class="mb-4"></div>
      <div v-sanitized-html="conclusionParagraph"></div>
    </div>
  </div>
</template>
<script>
export default {
  data: function() {
    return {
      branding: null,
      height: '380px',
      productName: eXo.env.portal.productName,
    };
  },
  computed: {
    profileName() {
      return this.$currentUserIdentity && this.$currentUserIdentity.profile && this.$currentUserIdentity.profile.fullname;
    },
    profileUri() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile`;
    },
    profilelink() {
      return `<a href="${this.profileUri}"><strong class="text-color">${this.profileName}</strong></a>`;
    },
    welcomeTitle() {
      return this.$t && this.$t('UIActivity.label.Welcome_Activity_Welcome_Onboard', {
        'user full name': this.profilelink,
      });
    },
    companyName() {
      return this.branding && this.branding.companyName;
    },
    thanksForJoingMeeds() {
      return this.$t && this.$t('UIActivity.label.Thanks_for_joing_company', {
        'company name': this.companyName,
      });
    },
    welcomeParagraph1() {
      return this.$t && this.$t('UIActivity.label.empty_stream_paragraph_one', {
        'product name': this.productName,
      });
    },
    welcomeParagraph2() {
      return this.$t && this.$t('UIActivity.label.empty_stream_paragraph_two', {
        'product name': this.productName,
      });
    },
    welcomeParagraph3() {
      return this.$t && this.$t('UIActivity.label.empty_stream_paragraph_three');
    },
    welcomeParagraph4() {
      return this.$t && this.$t('UIActivity.label.empty_stream_paragraph_four');
    },
    welcomeParagraph5() {
      return this.$t && this.$t('UIActivity.label.empty_stream_paragraph_five');
    },
    conclusionParagraph() {
      return this.$t && this.$t('UIActivity.label.empty_stream_lets_begin');
    },
  },
  created() {
    fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding`, {
      method: 'GET',
      credentials: 'include',
    })
      .then(resp => resp && resp.ok && resp.json())
      .then(branding => this.branding = branding);
  }
};
</script>