<template>
  <exo-drawer
    ref="profileContactInformationDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    class="profileContactInformationDrawer"
    right>
    <template slot="title">
      {{ $t('profileContactInformation.contactInformation') }}
    </template>
    <template slot="content">
      <v-form
        ref="profileContactForm"
        class="form-horizontal"
        flat>
        <v-flex class="d-flex justify-center">
          <profile-header-avatar
            ref="avatar"
            v-model="userToSave.avatarUploadId"
            :user="userToSave"
            :max-upload-size="maxUploadSizeInBytes"
            :size="150"
            owner
            hover
            @error="handleImageUploadError" />
        </v-flex>
        <v-card-text v-if="error" class="errorMessage">
          <v-alert type="error">
            {{ error }}
          </v-alert>
        </v-card-text>
        <v-card-text class="d-flex fullnameLabels text-color pb-2">
          <div class="align-start flex-grow-1 text-no-wrap text-left font-weight-bold mr-3">
            {{ $t('profileContactInformation.firstName') }}*
          </div>
          <div class="align-start flex-grow-1 text-no-wrap text-left font-weight-bold px-3">
            {{ $t('profileContactInformation.lastName') }}*
          </div>
        </v-card-text>
        <v-card-text class="d-flex fullnameFields py-0">
          <div class="align-start flex-grow-0 text-no-wrap text-left font-weight-bold mr-3">
            <input
              v-model="userToSave.firstname"
              :disabled="saving"
              type="text"
              class="ignore-vuetify-classes"
              maxlength="2000"
              required />
          </div>
          <div class="align-end flex-grow-0 text-no-wrap text-left font-weight-bold">
            <input
              v-model="userToSave.lastname"
              :disabled="saving"
              type="text"
              class="ignore-vuetify-classes"
              maxlength="2000"
              required />
          </div>
        </v-card-text>
        <v-card-text class="d-flex emailLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileContactInformation.email') }}*
        </v-card-text>
        <v-card-text class="d-flex emailField py-0">
          <input
            v-model="userToSave.email"
            :disabled="saving"
            type="email"
            class="ignore-vuetify-classes"
            maxlength="2000"
            required />
        </v-card-text>
        <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileContactInformation.jobTitle') }}*
        </v-card-text>
        <v-card-text class="d-flex positionField py-0">
          <input
            v-model="userToSave.position"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes"
            maxlength="2000"
            required />
        </v-card-text>
        <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileContactInformation.company') }}
        </v-card-text>
        <v-card-text class="d-flex positionField py-0">
          <input
            v-model="userToSave.company"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes"
            maxlength="2000"
          />
        </v-card-text>
        <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileContactInformation.location') }}
        </v-card-text>
        <v-card-text class="d-flex positionField py-0">
          <input
            v-model="userToSave.location"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes"
            maxlength="2000"
          />
        </v-card-text>
        <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileContactInformation.department') }}
        </v-card-text>
        <v-card-text class="d-flex positionField py-0">
          <input
            v-model="userToSave.department"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes"
            maxlength="2000"
          />
        </v-card-text>
        <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileContactInformation.team') }}
        </v-card-text>
        <v-card-text class="d-flex positionField py-0">
          <input
            v-model="userToSave.team"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes"
            maxlength="2000"
          />
        </v-card-text>
        <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileContactInformation.profession') }}
        </v-card-text>
        <v-card-text class="d-flex positionField py-0">
          <input
            v-model="userToSave.profession"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes"
            maxlength="2000"
          />
        </v-card-text>
        <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileContactInformation.country') }}
        </v-card-text>
        <v-card-text class="d-flex positionField py-0">
          <input
            v-model="userToSave.country"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes"
            maxlength="2000"
          />
        </v-card-text>
        <v-card-text class="d-flex positionLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileContactInformation.city') }}
        </v-card-text>
        <v-card-text class="d-flex positionField py-0">
          <input
            v-model="userToSave.city"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes"
            maxlength="2000"
          />
        </v-card-text>
        <profile-contact-edit-phone v-model="userToSave.phones" />
        <profile-contact-edit-ims v-model="userToSave.ims" />
        <profile-contact-edit-urls v-model="userToSave.urls" />
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn mr-2"
          @click="cancel">
          {{ $t('profileContactInformation.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="saving"
          :loading="saving"
          class="btn btn-primary"
          @click="save">
          {{ $t('profileContactInformation.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
    uploadLimit: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    userToSave: {},
    error: null,
    saving: null,
  }),
  computed: {
    maxUploadSizeInBytes() {
      return this.uploadLimit * 1024 *1024;
    },
  },
  methods: {
    save() {
      this.error = null;
      const regex =  /^[a-zA-Z0-9-_]+$/;
      const isValidFirstName = regex.test(this.userToSave.firstname);
      
      if (this.userToSave.firstname.length > 45 || this.userToSave.firstname.length < 3) {
        this.$refs.profileContactForm.$el[3].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
          0: this.$t('profileContactInformation.firstName'),
          1: 3,
          2: 45,
        }));
      } else {
        this.$refs.profileContactForm.$el[3].setCustomValidity('');
      }

      if (this.userToSave.firstname.length && !isValidFirstName) {
        this.$refs.profileContactForm.$el[3].setCustomValidity(this.$t('profileContactInformation.error.invalidField', {
          0: this.$t('profileContactInformation.firstName'),
        }));
      } else {
        this.$refs.profileContactForm.$el[3].setCustomValidity('');
      }
      
      if (this.userToSave.lastname.length > 45 || this.userToSave.lastname.length < 3) {
        this.$refs.profileContactForm.$el[4].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
          0: this.$t('profileContactInformation.lastName'),
          1: 3,
          2: 45,
        }));
      } else {
        this.$refs.profileContactForm.$el[4].setCustomValidity('');
      }
      
      if (this.userToSave.email.length > 100 || this.userToSave.email.length < 10) {
        this.$refs.profileContactForm.$el[5].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
          0: this.$t('profileContactInformation.email'),
          1: 10,
          2: 100,
        }));
      } else {
        this.$refs.profileContactForm.$el[5].setCustomValidity('');
      }

      if (!this.$refs.profileContactForm.validate() // Vuetify rules
          || !this.$refs.profileContactForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }

      this.$refs.profileContactInformationDrawer.startLoading();
      this.saving = true;
      if (this.userToSave.avatarUploadId) {
        this.userToSave.avatar = this.userToSave.avatarUploadId;
      } else {
        this.userToSave.avatar = null;
      }
      if (this.userToSave.firstname !== this.user.firstname || this.userToSave.lastname !== this.user.lastname) {
        this.userToSave.fullname = `${this.userToSave.firstname} ${this.userToSave.lastname}`;
      }
      this.$userService.updateProfileFields(eXo.env.portal.userName, this.userToSave, [
        'avatar',
        'firstname',
        'lastname',
        'fullname',
        'email',
        'position',
        'company',
        'location',
        'department',
        'team',
        'profession',
        'country',
        'city',
        'phones',
        'ims',
        'urls'
      ])
        .then(this.refresh)
        .then(() => {
          this.$refs.profileContactInformationDrawer.close();
        })
        .catch(this.handleImageUploadError)
        .finally(() => {
          this.saving = false;
          this.$refs.profileContactInformationDrawer.endLoading();
        });
    },
    handleImageUploadError(error) {
      if (error) {
        if (String(error).indexOf(this.$uploadService.avatarExcceedsLimitError) >= 0) {
          this.error = this.$t('profileHeader.label.avatarExcceededAllowedSize', {0: this.uploadLimit});
        } else {
          this.error = String(error);
        }

        window.setTimeout(() => {
          this.error = null;
        }, 5000);
      }
    },
    refresh() {
      return this.$userService.getUser(eXo.env.portal.profileOwner, 'all')
        .then(user => {
          this.user = user;

          document.dispatchEvent(new CustomEvent('userModified', {detail: user}));
        })
        .catch((e) => {
          console.warn('Error while retrieving user details', e); // eslint-disable-line no-console
        });
    },
    cancel() {
      this.$refs.profileContactInformationDrawer.close();
    },
    open() {
      this.error = null;

      this.$refs.avatar.reset();

      // Avoid to change on original user
      this.userToSave = JSON.parse(JSON.stringify(this.user));
      if (!this.userToSave.phones || !this.userToSave.phones.length) {
        this.userToSave.phones =  [{}];
      }
      if (!this.userToSave.ims || !this.userToSave.ims.length) {
        this.userToSave.ims =  [{}];
      }
      if (!this.userToSave.urls || !this.userToSave.urls.length) {
        this.userToSave.urls =  [{}];
      }
      this.$refs.profileContactInformationDrawer.open();
    },
  },
};
</script>