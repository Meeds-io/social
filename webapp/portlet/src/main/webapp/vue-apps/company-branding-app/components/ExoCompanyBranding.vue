<template>
  <v-app
    color="transaprent"
    class="uiBrandingPortlet"
    flat>
    <main>
      <div id="mustpng" class="alert">
        <i class="uiIconWarning"></i>{{ $t('mustpng.label') }}
      </div>
      <div id="toobigfile" class="alert">
        <i class="uiIconWarning"></i>{{ $t('toobigfile.label') }}
      </div>
      <div id="savenotok" class="alert">
        <i class="uiIconWarning"></i>{{ $t('info.savenotok.label') }}
      </div>
      <div class="logoForm boxContent">
        <h4 class="labelStyle">{{ $t('companyName.label') }}</h4>
        <div>
          <div class="info"><p>{{ $t('companyName.input.hint') }}</p></div>
          <input
            id="companyNameInput"
            :title="$t('branding.message.edit.label')"
            v-model="branding.companyName"
            :placeholder="$t('companyName.placeholder')"
            type="text"
            name="formOp"
            class="ignore-vuetify-classes inputCompany"
            rel="tooltip"
            data-placement="bottom"
            value="">
        </div>
        <hr class="logo">
        <h4 class="styleSelectLogo">
          {{ $t('selectlogo.label') }}
        </h4>
        <div class="info labelLocation"><p>{{ $t('noteselectlogo.label') }}</p></div>
        <div class="clearfix">
          <div v-show="showLogo" class="pull-left">
            <div id="previewLogo" class="previewLogo">
              <div class="logoPreviewImage">
                <img
                  id="previewLogoImg"
                  :src="logoPreview"
                  class="previewImg">
              </div>
              <div class="btnActionContainer">
                <div class="btnAction">
                  <i
                    v-if="removeLogoButtonDisplayed"
                    :title="$t('branding.message.restore.logo')"
                    class="fas fa-trash-alt iconDelete"
                    rel="tooltip"
                    data-placement="bottom"
                    @click="removeLogo"></i>
                  <i
                    v-else
                    :title="$t('branding.message.change.logo')"
                    class="uiIconEdit iconEdit"
                    rel="tooltip"
                    data-placement="bottom"
                    @click="showLogo = false"></i>
                </div>
              </div>
            </div>
          </div>
          <div v-show="!showLogo" class="pull-left">
            <div class="fileDrop">
              <div ref="dropFileBox" class="dropZone">
                <div>
                  <div class="attachment">
                    <i class="uiIconTemplate uiIcon32x32LightGray colorIcon"></i>
                    <label>{{ $t('branding.drop.attachment') }}</label>
                  </div>
                  <div class="option">
                    <hr class="optionStyle">
                    <label class="labelValue">{{ $t('branding.or.attachment') }}</label>
                    <hr class="closeOption">
                  </div>
                  <div class="upload">
                    <label for="attachLogo" class="iconUpload"><i class="fas fa-download uiIcon32x32LightGray"></i></label>
                    <label class="dropMsg" for="attachLogo">
                      {{ $t('branding.select.attachment') }}
                    </label>
                  </div>
                </div>
                <input
                  id="attachLogo"
                  type="file"
                  class="attachFile"
                  name="file"
                  @change="onFileChange">
              </div>
            </div>
          </div>
        </div>
        <div v-if="!showLogo" class="button_back">
          <a
            href="javascript:void(0)"
            class="linkBack"
            @click="showLogo = true"><i class="fas fa-undo-alt"></i>{{ $t('back.label') }}</a>
        </div>
        <hr class="theme">
        <div class="boxContent themeLabel">
          <h4>
            {{ $t('themeColors.label') }}
          </h4>

          <v-row class="colorsBlock">
            <v-col>
              <exo-company-branding-color-picker v-model="branding.themeColors.primaryColor" :label="$t('themeColors.primaryColor.label')" />
            </v-col>
            <v-col>
              <exo-company-branding-color-picker v-model="branding.themeColors.secondaryColor" :label="$t('themeColors.secondaryColor.label')" />
            </v-col>
            <v-col>
              <exo-company-branding-color-picker v-model="branding.themeColors.tertiaryColor" :label="$t('themeColors.tertiaryColor.label')" />
            </v-col>
          </v-row>
        </div>
        <div class="uiAction boxContent">
          <button
            id="cancel"
            class="btn ignore-vuetify-classes"
            type="button"
            @click="cancel">
            {{ $t('cancel.label') }}
          </button>
          <button
            id="save"
            :disabled="!branding.companyName || !branding.companyName.trim()"
            class="btn btn-primary ignore-vuetify-classes"
            type="button"
            @click="save">
            {{ $t('save.label') }}
          </button>
        </div>
      </div>
    </main>
  </v-app>
</template>

<script>
import { brandingConstants }  from '../companyBrandingConstants';
import * as  brandingServices  from '../companyBrandingServices';

export default {
  data(){
    return {
      showLogo: true,
      branding: {
        id: null,
        companyName: null,
        topBarTheme: null,
        logo: {
          uploadId: null,
          data: [],
          size: 0,
        },
        themeColors: {
          primaryColor: '#ffffff',
          primaryBackground: '#ffffff',
          secondaryColor: '#ffffff',
          secondaryBackground: '#ffffff',
        },
      },
      color: null,
      uploadInProgress: false,
      uploadProgress: 0,
      maxFileSize: 2097152,
    };
  },
  computed: {
    logoPreview() {
      if (!this.branding || !this.branding.logo || !this.branding.logo.data) {
        return brandingConstants.HOMEICON;
      }

      if (Array.isArray(this.branding.logo.data)) {
        return this.convertImageDataAsSrc(this.branding.logo.data);
      } else {
        return this.branding.logo.data;
      }
    },
    removeLogoButtonDisplayed() {
      return this.branding.logo.uploadId || this.branding.logo.id;
    }
  },
  created() {
    this.initBrandingInformation()
      .finally(() => this.$root.$applicationLoaded());
  },
  mounted() {
    // init top bar preview
    $('#PlatformAdminToolbarContainer').clone().attr('id', 'PlatformAdminToolbarContainer-preview').appendTo($('#StylePreview'));
    const toolbarPreview = $('#StylePreview #PlatformAdminToolbarContainer-preview');
    ['hover', 'click', 'blur'].forEach(evt => {
      toolbarPreview.bind(evt, (e) => {
        e.stopPropagation();
        e.preventDefault();
      });
    });

    // init file drop zone
    ['drag', 'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave', 'drop'].forEach( function( evt ) {
      this.$refs.dropFileBox.addEventListener(evt, function(e) {
        e.preventDefault();
        e.stopPropagation();
      }.bind(this), false);
    }.bind(this));

    this.$refs.dropFileBox.addEventListener('drop', function(e) {
      this.onFileChange(e);
    }.bind(this));
  },
  methods: {
    convertImageDataAsSrc(imageData) {
      let binary = '';
      const bytes = new Uint8Array(imageData);
      bytes.forEach(byte => binary += String.fromCharCode(byte));
      return `data:image/png;base64,${btoa(binary)}`;
    },
    onFileChange(e) {
      const files = e.target.files || e.dataTransfer.files;
      const self = this;
      if (!files.length) {
        return;
      }

      if (!this.isLogoFileExtensionValid(files[0])) {
        this.$el.querySelector('#mustpng').style.display = 'block';
        setTimeout(function () {
          self.cleanMessage();
        },// eslint-disable-next-line no-magic-numbers
        5000);
        return;
      }

      if (!this.isLogoFileSizeValid(files[0])) {
        this.$el.querySelector('#toobigfile').style.display = 'block';
        setTimeout(function () {
          self.cleanMessage();
        },// eslint-disable-next-line no-magic-numbers
        5000);
        return;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        if (!this.branding.logo.defaultData) {
          this.branding.logo.defaultData = this.branding.logo.data;
        }
        this.branding.logo.data = e.target.result;
      };
      reader.readAsDataURL(files[0]);

      this.branding.logo.name = files[0].name;
      this.branding.logo.size = files[0].size;

      this.uploadFile(files[0]);
    },
    isLogoFileExtensionValid(logoFile) {
      const logoName = logoFile.name;
      const logoNameExtension = logoName.substring(logoName.lastIndexOf('.')+1, logoName.length) || logoName;
      return logoNameExtension.toLowerCase() === 'png';
    },
    isLogoFileSizeValid(logoFile) {
      const logoSize = logoFile.size;
      return logoSize <= this.maxFileSize;
    },
    changePreviewStyle() {
      const topBarPreviewContainer = document.querySelector('#StylePreview #UIToolbarContainer');
      if (topBarPreviewContainer) {
        topBarPreviewContainer.setAttribute('class', `UIContainer UIToolbarContainer UIToolbarContainer${this.branding.topBarTheme}`);
      }
    },
    save() {
      this.cleanMessage();
      if (this.branding.logo.uploadId && !this.isLogoFileExtensionValid(this.branding.logo)) {
        this.$el.querySelector('#mustpng').style.display = 'block';
        this.branding.logo.data = this.branding.logo.defaultData;
        this.branding.logo.uploadId = null;
        return;
      }
      this.changePreviewStyle();
      brandingServices.updateBrandingInformation(this.branding).then(() => document.location.reload(true));
    },
    cancel() {
      document.location.href = brandingConstants.PORTAL;
    },
    initBrandingInformation() {
      return brandingServices.getBrandingInformation()
        .then(data => {
          this.branding = data;
        });
    },
    cleanMessage() {
      this.$el.querySelector('#savenotok').style.display = 'none';
      this.$el.querySelector('#mustpng').style.display = 'none';
      this.$el.querySelector('#toobigfile').style.display = 'none';
    },
    uploadFile(data) {
      const formData = new FormData();
      formData.append('file', data);
      const MAX_RANDOM_NUMBER = 100000;
      const uploadId = Math.round(Math.random() * MAX_RANDOM_NUMBER);
      this.branding.logo.uploadId = uploadId;
      if (!this.branding.logo.defaultData) {
        this.branding.logo.defaultData = this.branding.logo.data;
      }
      this.branding.logo.data = data;

      const maxProgress = 100;

      this.uploadInProgress = true;

      const self = this;
      
      fetch(`${brandingConstants.PORTAL}/upload?uploadId=${uploadId}&action=upload`, {
        method: 'POST',
        credentials: 'include',
        body: formData,
      }).then(() => {
        // Check if the file has correctly been uploaded (progress=100) before refreshing the upload list
        const progressUrl = `${brandingConstants.PORTAL}/upload?action=progress&uploadId=${uploadId}`;
        fetch(progressUrl, {
          method: 'GET',
          credentials: 'include',
        })
          .then(response => response.text())
          .then(responseText => {
            // TODO fix malformed json from upload service
            let responseObject;
            try {
              // trick to parse malformed json
              eval(`responseObject = ${responseText}`); // eslint-disable-line no-eval

              if (!responseObject.upload[uploadId] || !responseObject.upload[uploadId].percent ||
                responseObject.upload[uploadId].percent !== maxProgress.toString()) {
                self.$el.querySelector('#savenotok').style.display = 'block';
                setTimeout(function () {
                  self.cleanMessage();
                },// eslint-disable-next-line no-magic-numbers
                5000);
                self.uploadInProgress = false;
              } else {
                self.uploadProgress = maxProgress;
              }
            } catch (err) {
              return;
            }
          });
      });
      this.$nextTick(() => {
        this.showLogo = true;
      });
    },
    removeLogo() {
      this.branding.logo.uploadId = null;
      this.branding.logo.data = this.branding.logo.defaultData;
    }
  }
};
</script>
