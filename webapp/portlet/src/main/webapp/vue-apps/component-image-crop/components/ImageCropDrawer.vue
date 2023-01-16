<template>
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    id="translationDrawer"
    allow-expand
    right
    disable-pull-to-refresh>
    <template #title>
      <span class="text-capitalize-first-letter">
        {{ $t(title) }}
      </span>
    </template>
    <template #content>
      <v-card class="pa-4" flat>
        <v-card
          :min-width="width"
          :width="width"
          :height="height"
          class="border-color mx-auto primary position-relative"
          flat>
          <img
            v-if="dataImageSrc"
            ref="imageCrop"
            :src="dataImageSrc"
            :width="`${width}px`"
            :height="`${height}px`"
            alt="Picture to crop">
        </v-card>

        <div class="d-flex mt-4">
          <div :title="$t('imageCropDrawer.uploadImage')" class="flex-grow-0">
            <v-file-input
              v-if="!resetInput"
              :title="$t('imageCropDrawer.uploadImage')"
              id="imageFileInput"
              ref="imageFileInput"
              accept="image/*"
              prepend-icon="fas fa-camera z-index-two rounded-circle primary-border-color white py-1 ms-3"
              class="file-selector pa-0 ma-0"
              rounded
              clearable
              dense
              @change="uploadFile" />
          </div>
          <div class="d-flex flex-grow-1 d-flex align-center justify-end">
            <v-btn
              :title="$t('imageCropDrawer.moveRight')"
              icon
              outlined
              @click="move(-10, 0)">
              <v-icon size="18" class="fa-flip-horizontal">fas fa-arrow-right</v-icon>
            </v-btn>
            <v-btn
              :title="$t('imageCropDrawer.moveLeft')"
              icon
              outlined
              @click="move(10, 0)">
              <v-icon size="18" class="fa-flip-horizontal">fas fa-arrow-left</v-icon>
            </v-btn>
            <v-btn
              :title="$t('imageCropDrawer.moveUp')"
              icon
              outlined
              @click="move(0, -10)">
              <v-icon size="18" class="fa-flip-horizontal">fas fa-arrow-up</v-icon>
            </v-btn>
            <v-btn
              :title="$t('imageCropDrawer.moveDown')"
              icon
              outlined
              @click="move(0, 10)">
              <v-icon size="18" class="fa-flip-horizontal">fas fa-arrow-down</v-icon>
            </v-btn>
            <v-btn
              :title="$t('imageCropDrawer.rotateRight')"
              icon
              outlined
              @click="rotateRight">
              <v-icon size="18" class="fa-flip-horizontal">fas fa-undo</v-icon>
            </v-btn>
            <v-btn
              :title="$t('imageCropDrawer.rotateLeft')"
              icon
              outlined
              @click="rotateLeft">
              <v-icon size="18">fas fa-undo</v-icon>
            </v-btn>
          </div>
        </div>
        <div class="d-flex mt-4">
          <div class="flex-grow-0 pt-1 pe-2">
            {{ $t('imageCropDrawer.zoom') }}
          </div>
          <div class="flex-grow-1 d-flex">
            <v-btn
              :title="$t('imageCropDrawer.zoomOut')"
              icon
              outlined
              @click="zoom -= stepZoom">
              <v-icon size="18">fas fa-search-minus</v-icon>
            </v-btn>
            <v-slider
              v-model="zoom"
              :step="stepZoom"
              :min="minZoom"
              :max="maxZoom"
              class="mx-n1"
              dense />
            <v-btn
              :title="$t('imageCropDrawer.zoomIn')"
              icon
              outlined
              @click="zoom += stepZoom">
              <v-icon size="18">fas fa-search-plus</v-icon>
            </v-btn>
          </div>
        </div>
      </v-card>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('imageCropDrawer.cancel') }}
        </v-btn>
        <v-btn
          :loading="sendingImage"
          class="btn btn-primary"
          @click="apply">
          {{ $t('imageCropDrawer.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
    src: {
      type: Object,
      default: null,
    },
    drawerTitle: {
      type: String,
      default: null,
    },
    maxFileSize: {
      type: Number,
      default: () => 102400,
    },
    cropOptions: {
      type: Object,
      default: () => ({
        aspectRatio: 16 / 9,
      }),
    },
  },
  data: () => ({
    drawer: false,
    title: null,
    zoom: 1,
    stepZoom: 0.1,
    minZoom: 1,
    maxZoom: 2,
    width: 380,
    height: 200,
    cropper: null,
    data: null,
    resetInput: false,
    sendingImage: false,
  }),
  computed: {
    dataImageSrc() {
      return this.data || this.src;
    },
  },
  watch: {
    dataImageSrc() {
      this.init();
    },
    drawer() {
      this.init();
    },
    sendingImage() {
      if (this.sendingImage) {
        this.$refs?.drawer?.startLoading();
      } else {
        this.$refs?.drawer?.endLoading();
      }
    },
    zoom(newVal, oldVal) {
      if (this.cropper && newVal !== oldVal) {
        this.zoomOut(newVal - oldVal);
      }
    },
  },
  methods: {
    open() {
      this.title = this.drawerTitle || 'imageCropDrawer.defaultTitle';
      this.$refs.drawer.open();
      this.$nextTick().then(() => this.init());
    },
    close() {
      this.$refs.drawer.close();
      this.data = null;
    },
    init() {
      if (this.dataImageSrc && this.drawer) {
        this.$nextTick()
          .then(() => {
            if (this.cropper) {
              this.cropper.destroy();
            }
            this.zoom = 1;
            this.cropper = new Cropper(this.$refs.imageCrop, Object.assign({
              minCropBoxWidth: this.width,
              minCropBoxHeight: this.height,
              autoCropArea: 1,
            }, this.cropOptions));
          });
      } else {
        if (this.cropper) {
          this.cropper.destroy();
        }
        this.data = null;
        this.zoom = 1;
        this.resetInput = true;
        this.$nextTick().then(() => this.resetInput = false);
      }
    },
    rotateRight() {
      if (this.cropper) {
        this.cropper.rotate(45);
      }
    },
    rotateLeft() {
      if (this.cropper) {
        this.cropper.rotate(-45);
      }
    },
    move(x, y) {
      if (this.cropper) {
        this.cropper.move(x, y);
      }
    },
    zoomOut(value) {
      this.cropper.zoom(value);
    },
    apply() {
      this.uploadCroppedImage()
        .then(() => this.close());
    },
    uploadCroppedImage() {
      this.$root.$emit('close-alert-message');
      this.sendingImage = true;
      const self = this;
      return new Promise((resolve, reject) => {
        this.cropper.getCroppedCanvas().toBlob((blob) => {
          if (blob.size > this.maxFileSize) {
            if (this.maxFileSize < 1024) {
              this.$root.$emit('alert-message', this.$t('imageCropDrawer.tooBigFile.bytes.label', {
                0: blob.size,
                1: this.maxFileSize,
              }), 'error');
            } else if (this.maxFileSize < (1024 * 1024)) {
              this.$root.$emit('alert-message', this.$t('imageCropDrawer.tooBigFile.kilobytes.label', {
                0: parseInt(blob.size / 1024),
                1: parseInt(this.maxFileSize / 1024),
              }), 'error');
            } else {
              this.$root.$emit('alert-message', this.$t('imageCropDrawer.tooBigFile.megabytes.label', {
                0: parseInt(blob.size / 1024 / 1024),
                1: parseInt(this.maxFileSize / 1024 / 1024),
              }), 'error');
            }
            this.sendingImage = false;
            return;
          }
          this.$uploadService.upload(blob)
            .then(uploadId => {
              if (uploadId) {
                const reader = new FileReader();
                reader.onload = (e) => {
                  self.data = e.target.result;
                  self.$emit('data', self.data);
                  self.$forceUpdate();
                };
                reader.readAsDataURL(blob);
                this.$emit('input', uploadId);
                resolve(uploadId);
              } else {
                this.$root.$emit('alert-message', this.$t('imageCropDrawer.uploadingError'), 'error');
                reject(this.$t('imageCropDrawer.uploadingError'));
              }
            })
            .catch(error => {
              this.$root.$emit('alert-message', this.$t(String(error)), 'error');
              reject(error);
            })
            .finally(() => this.sendingImage = false);
        });
      });
    },
    uploadFile(file) {
      this.$root.$emit('close-alert-message');
      if (file && file.size) {
        if (file.type && file.type.indexOf('image/') !== 0) {
          this.$root.$emit('alert-message', this.$t('imageCropDrawer.mustImage.label'), 'error');
          return;
        }
        const self = this;
        const reader = new FileReader();
        this.sendingImage = true;
        reader.onload = (e) => {
          self.data = e.target.result;
          this.sendingImage = false;
          self.$forceUpdate();
        };
        reader.readAsDataURL(file);
      }
    },
  },
};
</script>
