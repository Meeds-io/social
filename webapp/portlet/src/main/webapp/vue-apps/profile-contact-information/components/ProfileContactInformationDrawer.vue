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
        <div v-for="property in properties" :key="property.id">
          <profile-contact-edit-multi-field
            v-if="property.editable && (property.multiValued || (property.children && property.children.length))"
            :property="property"
            @propertyUpdated="propertyUpdated" />
          <div v-else-if="property.editable">
            <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
              {{ getResolvedName(property) }}<span v-if="property.required">*</span>
            </v-card-text>
            <v-card-text class="d-flex py-0">
              <v-card-text v-exo-tooltip.bottom.body="disabledField(property) ? $t('profileContactInformation.synchronizedUser.tooltip') :$t('profileContactInformation.'+property.propertyName)" class="d-flex pa-0">
                <input
                  v-model="property.value"
                  :disabled="saving || disabledField(property)"
                  :type="property.propertyName==='email' ? 'email' : 'text'"
                  class="ignore-vuetify-classes"
                  maxlength="2000"
                  :required="property.required"
                  :ref="`${property.propertyName}Input`" 
                  @change="propertyUpdated(property)">
              </v-card-text>
            </v-card-text>
          </div>
        </div>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
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
  data: () => ({
    properties: [],
    error: null,
    saving: null,
    fieldError: false,
  }),
  watch: {
    error() {
      if (this.error) {
        this.$root.$emit('alert-message', this.error, 'error');
      } else {
        this.$root.$emit('close-alert-message');
      }
    },
  },
  created() {
    this.$root.$on('open-profile-contact-information-drawer', this.open);
  },
  methods: {
    
    resetCustomValidity() {
      if (this.$refs.emailInput) { this.$refs.emailInput[0].setCustomValidity('');}
      if (this.$refs.firstNameInput) { this.$refs.firstNameInput[0].setCustomValidity('');}
      if (this.$refs.lastNameInput) { this.$refs.lastNameInput[0].setCustomValidity('');}
    },

    disabledField(property){
      return !property.internal && (property.propertyName==='firstName' || property.propertyName==='lastName' || property.propertyName==='email');
    },
    
    save() {
      this.error = null;
      this.fieldError = false;
      
      this.resetCustomValidity();
      let proptocheck = this.properties.find(property => property.propertyName === 'urls');
      if (proptocheck && proptocheck.children.length > 0) {
        if (proptocheck.children.some(property => property.value.length > 100 || property.value.length < 10)){
          const error = this.$t('profileWorkExperiences.invalidFieldLength', {
            0: this.$t('profileContactInformation.urls'),
            1: 10,
            2: 100,
          });
          this.handleError(error);
          return;
        }
      }

      proptocheck = this.properties.find(property => property.propertyName === 'email');
      if (proptocheck){
        if ((proptocheck.value && (proptocheck.value.length > 100 || proptocheck.value.length  < 10)) || !proptocheck.value) {
          this.$refs.emailInput[0].setCustomValidity(this.$t('profileWorkExperiences.invalidFieldLength', {
            0: this.$t('profileContactInformation.email'),
            1: 10,
            2: 100,
          }));
        } else {
          this.$refs.emailInput[0].setCustomValidity('');
        }
      } 

      if (!this.$refs.profileContactForm.validate() // Vuetify rules
          || !this.$refs.profileContactForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }

      this.$refs.profileContactInformationDrawer.startLoading();
      this.saving = true;
     
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${eXo.env.portal.userName}/profile/properties`, {
        method: 'PATCH',
        credentials: 'include',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.properties),
      }).then(resp => {
        if (!resp || !resp.ok) {
          if (resp.status === 400) {
            return resp.text().then(error => {
              this.fieldError = error;
              throw new Error(error);
            });
          } else {
            throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
          }
        }
      }).then(() => document.dispatchEvent(new CustomEvent('userModified')))
        .then(() => this.$refs.profileContactInformationDrawer.close())
        .catch(this.handleError)
        .finally(() => {
          this.saving = false;
          this.$refs.profileContactInformationDrawer.endLoading();
        });
    },

    handleError(error) {
      this.resetCustomValidity();
      
      if (error) {
        if (String(error).indexOf(this.$uploadService.avatarExcceedsLimitError) >= 0) {
          this.error = this.$t('profileHeader.label.avatarExcceededAllowedSize', {0: this.uploadLimit});
        } else if (this.fieldError && this.fieldError.indexOf('FIRSTNAME:') === 0) {
          const firstNameError = this.fieldError.replace('FIRSTNAME:', '');
          if (this.$refs.firstNameInput) { this.$refs.firstNameInput[0].setCustomValidity(firstNameError);}
        } else if (this.fieldError && this.fieldError.indexOf('LASTNAME:') === 0) {
          const lastNameError = this.fieldError.replace('LASTNAME:', '');
          if (this.$refs.lastNameInput) { this.$refs.lastNameInput[0].setCustomValidity(lastNameError);}
        } else if (this.fieldError && this.fieldError.indexOf('EMAIL:') === 0) {
          if (this.fieldError === 'EMAIL:ALREADY_EXISTS') {
            if (this.$refs.emailInput) { this.$refs.emailInput[0].setCustomValidity(this.$t('UsersManagement.message.userWithSameEmailAlreadyExists'));}
          } else {
            const emailError = this.fieldError.replace('EMAIL:', '');
            if (this.$refs.emailInput) { this.$refs.emailInput[0].setCustomValidity(emailError); }
          }
        } else {
          error = error.message || String(error);
          const errorI18NKey = `UsersManagement.error.${error}`;
          this.error = this.$te(errorI18NKey) && this.$t(errorI18NKey) || error;
          window.setTimeout(() => {
            this.error = null;
          }, 5000);
        }

        window.setTimeout(() => {
          if (!this.$refs.profileContactForm.validate() // Vuetify rules
            || !this.$refs.profileContactForm.$el.reportValidity()) { // Standard HTML rules
            return;
          }
        }, 200);
      } else {
        this.error = null;
      }
    },
    cancel() {
      this.$refs.profileContactInformationDrawer.close();
    },
    open(properties) {
      this.error = null;
      this.properties = properties && JSON.parse(JSON.stringify(properties)) || [];
      this.$refs.profileContactInformationDrawer.open();
    },
    propertyUpdated(item){
      if (!this.properties.some(e => e.id === item.id)) {
        this.properties.push(item);
      }    
    },
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileContactInformation.${item.propertyName}`)!==`profileContactInformation.${item.propertyName}`?this.$t(`profileContactInformation.${item.propertyName}`):item.propertyName;
    }
  },
};
</script>
