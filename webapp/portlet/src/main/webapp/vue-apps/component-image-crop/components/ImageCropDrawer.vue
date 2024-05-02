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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :go-back-button="backIcon"
    :allow-expand="!noExpandIcon"
    id="cropperDrawer"
    right
    disable-pull-to-refresh
    @closed="resetCropper">
    <template #title>
      {{ $t(title) }}
    </template>
    <template #content>
      <v-card
        ref="imageCropperCanvasParent"
        v-resize="onCanvasResize"
        max-width="100%"
        class="pa-4 content-box-sizing overflow-hidden"
        flat>
        <div class="overflow-hidden position-relative">
          <div v-if="isImageGif" class="d-flex position-absolute full-width full-height mask-color z-index-one rounded">
            <div class="d-flex flex-column align-center flex ma-auto">
              <v-icon size="52" class="white--text mb-4">fa-ban</v-icon>
              <div class="white--text text-wrap subtitle-1">{{ $t('imageCropDrawer.gitImage.label.option') }}</div>
              <div class="white--text text-wrap subtitle-1">{{ $t('imageCropDrawer.gitImage.label.gif') }}</div>
            </div>
          </div>
          <div :class="isImageGif && 'filter-blur-3' || ''">
            <v-card
              :height="height"
              :width="width"
              :max-height="height"
              :max-width="maxWidth"
              :class="circle && 'cropper-circle' || rounded && 'cropper-rounded'"
              class="border-color mx-auto primary position-relative"
              flat>
              <img
                v-if="imageData && !isImageGif"
                ref="imageCrop"
                :src="imageData"
                :width="`${width}px`"
                :height="`${height}px`"
                alt="Picture to crop">
            </v-card>
            <div class="d-flex mt-4">
              <div :title="$t('imageCropDrawer.uploadImage')" class="d-flex align-center flex-grow-0">
                <v-file-input
                  v-if="displayUploadIcon"
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
                <v-btn
                  :title="$t('imageCropDrawer.resetCropper')"
                  id="cancelChanges"
                  icon
                  outlined
                  @click="resetCropperData()">
                  <v-icon size="18" class="fa-flip-horizontal">fas fa-ban</v-icon>
                </v-btn>
              </div>
              <div class="d-flex flex-grow-1 d-flex align-center justify-end">
                <v-btn
                  :title="$t('imageCropDrawer.moveRight')"
                  id="resetCropper"
                  icon
                  outlined
                  @click="move(-10, 0)">
                  <v-icon size="18" class="fa-flip-horizontal">fas fa-arrow-right</v-icon>
                </v-btn>
                <v-btn
                  :title="$t('imageCropDrawer.moveLeft')"
                  id="moveImageToLeft"
                  icon
                  outlined
                  @click="move(10, 0)">
                  <v-icon size="18" class="fa-flip-horizontal">fas fa-arrow-left</v-icon>
                </v-btn>
                <v-btn
                  :title="$t('imageCropDrawer.moveUp')"
                  id="moveImageToUp"
                  icon
                  outlined
                  @click="move(0, -10)">
                  <v-icon size="18" class="fa-flip-horizontal">fas fa-arrow-up</v-icon>
                </v-btn>
                <v-btn
                  :title="$t('imageCropDrawer.moveDown')"
                  id="moveImageToBottom"
                  icon
                  outlined
                  @click="move(0, 10)">
                  <v-icon size="18" class="fa-flip-horizontal">fas fa-arrow-down</v-icon>
                </v-btn>
                <v-btn
                  :title="$t('imageCropDrawer.rotateRight')"
                  id="rotateImageToRight"
                  icon
                  outlined
                  @click="rotateRight">
                  <v-icon size="18" class="fa-flip-horizontal">fas fa-undo</v-icon>
                </v-btn>
                <v-btn
                  :title="$t('imageCropDrawer.rotateLeft')"
                  id="rotateImageToLeft"
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
                  id="zoomImageOut"
                  icon
                  outlined
                  @click="zoom -= stepZoom">
                  <v-icon size="18">fas fa-isImageGifsearch-minus</v-icon>
                </v-btn>
                <v-slider
                  v-model="zoom"
                  :step="stepZoom"
                  :min="minZoom"
                  :max="maxZoom"
                  class="mx-n1" />
                <v-btn
                  :title="$t('imageCropDrawer.zoomIn')"
                  id="zoomImageIn"
                  icon
                  outlined
                  @click="zoom += stepZoom">
                  <v-icon size="18">fas fa-search-plus</v-icon>
                </v-btn>
              </div>
            </div>
          </div>
        </div>
        <div v-if="useFormat" class="d-flex flex-column mt-4">
          <div class="flex-grow-0 subtitle-1 pt-1 pe-2">
            {{ $t('imageCropDrawer.format') }}
          </div>
          <div class="flex-grow-1 d-flex mx-n2">
            <v-card
              v-for="item in imageDisplayFormat"
              :key="item.value"
              :outlined="format !== item.value"
              :class="format === item.value && 'primary-border-color'"
              class="col-4 pa-0 flex-grow-1 flex-shrink-1 mx-2 border-box-sizing"
              flat
              @click="selectFormat(item.value)">
              <v-responsive :aspect-ratio="110 / 70">
                <div class="d-flex flex-column align-center justify-center fill-height">
                  <div class="d-flex flex-grow-1 align-center justify-center">
                    <v-card
                      :width="item.width"
                      :height="item.height"
                      max-width="100%"
                      color="grey lighten-2"
                      flat />
                  </div>
                  <div>{{ item.text }}</div>
                </div>
              </v-responsive>
            </v-card>
          </div>
        </div>
        <div v-if="alt" class="d-flex flex-column mt-4">
          <div class="flex-grow-0 subtitle-1 pt-1 pe-2">
            {{ $t('imageCropDrawer.altText.title') }}
          </div>
          <div class="flex-grow-1 d-flex">
            <extended-textarea
              v-model="alternativeText"
              :max-length="altTextMaxLength"
              :placeholder="$t('imageCropDrawer.altText.placeholder')"
              extra-class="width-auto"
              class="pt-0"
              @input="$emit('alt-text',alternativeText)" />
          </div>
        </div>
      </v-card>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          id="imageCropDrawerCancel"
          class="btn me-2"
          @click="close">
          {{ $t('imageCropDrawer.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!imageData"
          :loading="sendingImage"
          id="imageCropDrawerApply"
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
    maxImageWidth: {
      type: Number,
      default: () => 1280,
    },
    altTextMaxLength: {
      type: Number,
      default: () => 1000,
    },
    useFormat: {
      type: Boolean,
      default: false,
    },
    backIcon: {
      type: Boolean,
      default: false,
    },
    noExpandIcon: {
      type: Boolean,
      default: false,
    },
    drawerTitle: {
      type: String,
      default: null,
    },
    maxFileSize: {
      type: Number,
      default: () => 102400,
    },
    circle: {
      type: Boolean,
      default: false,
    },
    canUpload: {
      type: Boolean,
      default: true,
    },
    alt: {
      type: Boolean,
      default: false,
    },
    defaultFormat: {
      type: String,
      default: () => 'landscape',
    },
    rounded: {
      type: Boolean,
      default: false,
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
    format: null,
    zoom: 1,
    stepZoom: 0.1,
    minZoom: 1,
    maxZoom: 2,
    maxWidth: 850,
    width: 388,
    cropper: null,
    cropperReady: false,
    imageData: null,
    resetInput: false,
    sendingImage: false,
    alternativeText: null,
    mimetype: null,
    checkFormat: false,
    specificFormatSelected: false,
    imageAspectRatio: 0,
  }),
  computed: {
    aspectRatio() {
      return this.formaCropOptions?.aspectRatio || 1;
    },
    maxImageHeight() {
      return this.maxImageWidth && this.aspectRatio * this.maxImageWidth;
    },
    height() {
      return parseInt((this.width + 32) * 9 / 16) - 32;
    },
    displayUploadIcon() {
      return !this.resetInput && this.canUpload;
    },
    isImageGif() {
      return this.mimetype && this.mimetype === 'image/gif';
    },
    imageDisplayFormat() {
      return [{
        value: 'landscape',
        text: this.$t('imageCropDrawer.landscape'),
        width: 100,
        height: (100 / 1280 * 175),
      },{
        value: 'portrait',
        text: this.$t('imageCropDrawer.portrait'),
        width: 25,
        height: 35,
      },{
        value: 'square',
        text: this.$t('imageCropDrawer.square'),
        width: 35,
        height: 35,
      }];
    },
    selectedFormat() {
      return this.useFormat && this.imageDisplayFormat.find(f => f.value === this.format);
    },
    formatCropOptions() {
      return this.selectedFormat && {
        ...this.cropOptions,
        aspectRatio: this.selectedFormat.width / this.selectedFormat.height,
      } || this.cropOptions;
    },
  },
  watch: {
    imageData() {
      if (this.drawer) {
        this.resetCropper();
        this.init();
      }
    },
    sendingImage(newVal, oldVal) {
      if (this.sendingImage) {
        this.$refs?.drawer?.startLoading();
      } else {
        this.$refs?.drawer?.endLoading();
      }
      if (this.useFormat && oldVal && !newVal) {
        this.checkFormat = true;
      }
    },
    zoom(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.zoomOut(newVal - oldVal);
      }
    },
    width(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.$nextTick().then(() => this.init(true));
      }
    },
    format() {
      this.$emit('format', this.format);
    },
    formatCropOptions() {
      this.resetCropper();
      this.init();
    },
    cropperReady() {
      if (this.checkFormat && this.cropperReady) {
        this.checkFormat = false;
        this.imageAspectRatio = this.cropper?.getImageData?.()?.aspectRatio;
      }
    },
    imageAspectRatio() {
      if (this.imageAspectRatio && this.useFormat && !this.specificFormatSelected) {
        if (this.imageAspectRatio < 0.9) {
          this.format = 'portrait';
        } else if (this.imageAspectRatio > 1.5) {
          this.format = 'landscape';
        } else {
          this.format = 'square';
        }
      }
    },
  },
  methods: {
    open(imageItem) {
      this.title = this.drawerTitle || 'imageCropDrawer.defaultTitle';
      this.imageData = imageItem?.src || this.src || null;
      this.mimetype = imageItem?.mimetype || imageItem?.data &&  this.getBase64Mimetype(imageItem?.data) || null;
      this.alternativeText = imageItem?.altText || null;
      this.format = imageItem?.format || 'landscape';
      this.specificFormatSelected = !!imageItem?.format;
      this.$nextTick().then(() => {
        this.$refs.drawer.open();
        window.setTimeout(() => {
          this.resetCropper();
          this.init();
        }, 50);
      });
    },
    close() {
      this.$refs.drawer.close();
    },
    onCanvasResize() {
      window.setTimeout(() => {
        this.computeWidthSize();
      }, 200); // Wait for animation to finish to compute real width
    },
    init(avoidChangingWidth) {
      if (this.imageData && this.drawer) {
        if (!avoidChangingWidth) {
          this.computeWidthSize();
        }
        this.cropperReady = false;
        this.$nextTick()
          .then(() => {
            if (this.cropper) {
              this.cropper.minCropBoxWidth = this.width;
              this.cropper.minCropBoxHeight = this.height;
              if (this.cropper.onResize) {
                this.cropper.onResize();
              }
              this.cropperReady = true;
            } else {
              this.zoom = 1;
              this.cropper = new Cropper(this.$refs.imageCrop, Object.assign({
                minCropBoxWidth: this.width,
                minCropBoxHeight: this.height,
                autoCropArea: 1,
                ready: () => {
                  this.cropperReady = true;
                },
              }, this.formatCropOptions));
            }
          });
      } else {
        this.resetCropper();
      }
    },
    resetCropper() {
      this.cropperReady = false;
      if (this.cropper) {
        this.cropper.destroy();
        this.cropper = null;
      }
      this.zoom = 1;
      this.resetInput = true;
      this.$nextTick().then(() => this.resetInput = false);
    },
    computeWidthSize() {
      if (this.$refs.imageCropperCanvasParent) {
        this.width = Math.min(this.$refs.imageCropperCanvasParent.$el.clientWidth, this.maxWidth) - 32;
      }
    },
    rotateRight() {
      if (this.cropperReady) {
        this.cropper.rotate(45);
      }
    },
    rotateLeft() {
      if (this.cropperReady) {
        this.cropper.rotate(-45);
      }
    },
    move(x, y) {
      if (this.cropperReady) {
        this.cropper.move(x, y);
      }
    },
    zoomOut(value) {
      if (this.cropperReady) {
        this.cropper.zoom(value);
      }
    },
    resetCropperData() {
      this.cropper?.reset?.();
    },
    apply() {
      if (this.isImageGif) {
        this.close();
      } else {
        this.uploadCroppedImage()
          .then(() => this.close());
      }
    },
    uploadCroppedImage() {
      this.$root.$emit('close-alert-message');
      this.sendingImage = true;
      const self = this;
      return new Promise((resolve, reject) => {
        this.getCroppedCanvas()
          .toBlob((blob) => {
            if (blob.size > this.maxFileSize) {
              if (this.maxFileSize < 1024) {
                this.$root.$emit('alert-message', this.$t('imageCropDrawer.tooBigFile.bytes.label', {
                  0: blob.size,
                  1: this.maxFileSize,
                }), 'error');
              } else if (this.maxFileSize < (1024 * 1024)) {
                this.$root.$emit('alert-message', this.$t('imageCropDrawer.tooBigFile.kilobytes.label', {
                  0: Number.parseFloat(blob.size / 1024).toFixed(2).replace('.00', ''),
                  1: parseInt(this.maxFileSize / 1024),
                }), 'error');
              } else {
                this.$root.$emit('alert-message', this.$t('imageCropDrawer.tooBigFile.megabytes.label', {
                  0: Number.parseFloat(blob.size / 1024 / 1024).toFixed(2).replace('.00', ''),
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
                    self.$emit('data', e.target.result);
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
          }, null, 1);
      });
    },
    getCroppedCanvas() {
      if (this.circle) {
        const croppedCanvas = this.cropper.getCroppedCanvas(this.maxImageWidth && {
          maxWidth: this.maxImageWidth * 2,
          maxHeight: this.maxImageHeight * 2,
          imageSmoothingQuality: 'high'
        });
        const width = croppedCanvas.width;
        const height = croppedCanvas.height;

        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;

        const context = canvas.getContext('2d');
        context.imageSmoothingEnabled = true;
        context.imageSmoothingQuality = 'high';
        context.drawImage(croppedCanvas, 0, 0, width, height);
        context.globalCompositeOperation = 'destination-in';
        context.beginPath();
        context.arc(width / 2, height / 2, Math.min(width, height) / 2, 0, 2 * Math.PI, true);
        context.fill();
        return canvas;
      } else {
        return this.cropper.getCroppedCanvas(this.maxImageWidth && {
          maxWidth: this.maxImageWidth,
          maxHeight: this.maxImageHeight,
          imageSmoothingQuality: 'high'
        });
      }
    },
    uploadFile(file) {
      this.$root.$emit('close-alert-message');
      if (file?.size) {
        if (file.type && file.type.indexOf('image/') !== 0) {
          this.$root.$emit('alert-message', this.$t('imageCropDrawer.mustImage.label'), 'error');
          return;
        }
        const self = this;
        const reader = new FileReader();
        this.sendingImage = true;
        reader.onload = e => {
          self.imageData = e.target.result;
          this.sendingImage = false;
          self.$forceUpdate();
        };
        reader.readAsDataURL(file);
      }
    },
    selectFormat(format) {
      this.specificFormatSelected = true;
      this.format = format;
    },
    getBase64Mimetype(dataUrl) {
      let mimetype = null;
      if (typeof dataUrl !== 'string') {
        return mimetype;
      }
      const dataMimetype = dataUrl.match(/data:([a-zA-Z0-9]+\/[a-zA-Z0-9-.+]+).*,.*/);
      if (dataMimetype && dataMimetype.length) {
        mimetype = dataMimetype[1];
      }
      return mimetype;
    }
  },
};
</script>
