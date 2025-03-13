<template>
  <div
    v-if="embeddedHTML"
    class="d-flex flex-column flex activity-embedded-box mt-3 mx-auto border-radius overflow-hidden light-grey-background-color hover-elevation mb-4"
    :style="parentStyle">
    <div
      v-if="elementReady"
      class="border-radius"
      :style="embeddedHTMLStyle"
      v-html="embeddedHTML"></div>
    <a
      v-if="titleText"
      class="pa-3 text-color z-index-one light-grey-background-color"
      :href="link"
      :target="linkTarget"
      :title="titleText">
      <div
        class="font-weight-bold text-color ma-0 text-wrap text-break text-truncate-2"
        v-text="titleText"></div>
    </a>
  </div>
</template>

<script>
  export default {
    props: {
      activity: {
        type: Object,
        default: null,
      },
      activityTypeExtension: {
        type: String,
        default: null,
      },
      isActivityDetail: {
        type: Boolean,
        default: false,
      },
    },
    data: () => ({
      maxWidth: 320,
      elementReady: false,
    }),
    computed: {
      embeddedHTML () {
        const htmlElement = this.activity?.templateParams?.html;
        return this.computeEmbeddedHTML(htmlElement);
      },
      sourceLink () {
        return this.activity && this.activity.templateParams && this.activity.templateParams.link || '';
      },
      link () {
        return this.sourceLink || 'javascript:void(0)';
      },
      linkClass () {
        return this.sourceLink === '#' ? 'not-clickable' : '';
      },
      linkTarget () {
        return this.sourceLink && (this.sourceLink.indexOf('/') === 0 || this.sourceLink.indexOf('#') === 0) && '_self' || (this.sourceLink && '_blank') || '';
      },
      title () {
        return this.activity && this.activity.templateParams && this.activity.templateParams.title;
      },
      titleText () {
        return this.title && eXo.$utils.htmlToText(this.title) || '';
      },
      previewWidth () {
        return Number(this.activity.templateParams && this.activity.templateParams.previewWidth || this.maxWidth);
      },
      parentStyle () {
        const width = this.previewWidth > this.maxWidth && this.maxWidth || this.previewWidth;
        return {
          width: `${width}px`,
        };
      },
      embeddedHTMLStyle () {
        const width = this.previewWidth > this.maxWidth && this.maxWidth || this.previewWidth;
        return {
          width: `${width}px`,
          maxWidth: `${this.maxWidth}px`,
        };
      },
    },
    mounted () {
      this.maxWidth = this.$el && this.$el.parentElement.offsetWidth < this.maxWidth && String(this.$el.parentElement.offsetWidth - 2) || `${this.maxWidth}`;
      this.elementReady = true;
    },
    methods: {
      computeEmbeddedHTML (htmlElement) {
        const tempdiv = document.createElement('div');
        tempdiv.innerHTML = htmlElement;
        if (tempdiv.firstElementChild.style.maxWidth) {
          tempdiv.firstElementChild.style.maxWidth = `${this.maxWidth}px`;
        }
        return tempdiv.innerHTML;
      },
    },
  };
</script>
