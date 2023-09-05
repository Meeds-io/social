<template>
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    class="userNotificationDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template #title>
      <span class="text-wrap">
        {{ $t('NotificationAdmin.title') }}
      </span>
    </template>
    <template v-if="drawer" #content>
      <v-form ref="form" class="pa-4">
        <div class="subtitle-1 pb-8">
          {{ $t('NotificationAdmin.sender.drawer.message') }}
        </div>
        <div class="subtitle-1">
          {{ $t('NotificationAdmin.sender.drawer.name') }}
        </div>
        <v-text-field
          ref="name"
          id="companyName"
          v-model="name"
          class="notification-company-name border-box-sizing pt-0"
          name="companyName"
          type="text"
          aria-required="true"
          required="required"
          autocomplete="off"
          outlined
          dense />
        <div class="subtitle-1">
          {{ $t('NotificationAdmin.sender.drawer.address') }}
        </div>
        <v-text-field
          ref="email"
          id="companyEmail"
          v-model="email"
          class="notification-company-email border-box-sizing pt-0"
          name="companyEmail"
          type="email"
          aria-required="true"
          required="required"
          autocomplete="off"
          outlined
          dense />
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="cancel">
          {{ $t('UserSettings.button.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="save">
          {{ $t('UserSettings.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    name: null,
    email: null,
    invalidEmail: false,
    invalidName: false,
  }),
  watch: {
    name() {
      if (this.invalidName) {
        this.invalidName = false;
      }
    },
    email() {
      if (this.invalidEmail) {
        this.invalidEmail = false;
      }
    },
    invalidEmail() {
      if (this.$refs.email) {
        if (this.invalidEmail) {
          this.$refs.email.$el.querySelector('input').setCustomValidity(this.$t('NotificationAdmin.invalidSenderEmail'));
        } else {
          this.$refs.email.$el.querySelector('input').setCustomValidity('');
        }
        this.$refs.form.$el.reportValidity();
      }
    },
    invalidName() {
      if (this.$refs.name) {
        if (this.invalidName) {
          this.$refs.name.$el.querySelector('input').setCustomValidity(this.$t('NotificationAdmin.invalidSenderName'));
        } else {
          this.$refs.name.$el.querySelector('input').setCustomValidity('');
        }
        this.$refs.form.$el.reportValidity();
      }
    },
  },
  methods: {
    open() {
      this.name = this.settings.senderName;
      this.email = this.settings.senderEmail;
      this.invalidEmail = false;
      this.invalidName = false;
      this.$refs.drawer.open();
    },
    save() {
      if (!this.$refs.form.validate()) {
        return;
      }
      this.invalidEmail = false;
      this.invalidName = false;
      this.$refs.drawer.startLoading();
      return this.$notificationAdministration.saveSenderEmail(this.name, this.email)
        .then(() => {
          this.$root.$emit('refresh');
          this.$refs.drawer.close();
        })
        .catch(e => {
          this.invalidEmail = String(e).includes('invalidSenderEmail');
          this.invalidName = String(e).includes('invalidSenderName');
        })
        .finally(() => this.$refs.drawer.endLoading());
    },
    cancel() {
      this.$refs.drawer.close();
    },
  },
};
</script>

