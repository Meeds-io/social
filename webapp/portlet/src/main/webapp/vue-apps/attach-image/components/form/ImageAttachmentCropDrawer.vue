<template>
  <image-crop-drawer
    ref="attachedImageCropDrawer"
    max-image-width="1280"
    :src="imageCropperSrc"
    :crop-options="cropOptions" />
</template>

<script>
export default {
  data () {
    return {
      imageItem: null,
      cropOptions: {
        aspectRatio: 1,
        viewMode: 1,
      },
    };
  },
  computed: {
    imageCropperSrc() {
      return this.imageItem;
    },
  },
  created() {
    document.addEventListener('attachments-image-open-crop-drawer', this.openAttachmentCropDrawer);
  },
  methods: {
    openAttachmentCropDrawer(event) {
      if (event?.detail) {
        this.imageItem = event?.detail;
        this.$refs.attachedImageCropDrawer.open();
      }
      
    },
  },
};
</script>
