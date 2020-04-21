<template>
  <v-app
    :class="owner && 'profileContactInformation' || 'profileContactInformationOther'"
    class="white">
    <v-toolbar color="white" flat class="border-box-sizing">
      <div class="text-header-title text-sub-title">
        {{ $t('profileContactInformation.contactInformation') }}
      </div>
      <v-spacer />
      <v-btn
        v-if="owner"
        icon
        outlined
        small
        @click="editContactInformation">
        <i class="uiIconEdit uiIconLightBlue pb-2" />
      </v-btn>
    </v-toolbar>
    <div v-if="user" class="px-4 pb-6 white">
      <v-flex class="d-flex">
        <div class="align-start text-no-wrap font-weight-bold mr-3">
          {{ $t('profileContactInformation.fullName') }}
        </div>
        <div :title="user.fullname" class="align-end flex-grow-1 text-truncate text-end">
          {{ user.fullname }}
        </div>
      </v-flex>
      <v-divider class="my-4" />
      <v-flex class="d-flex">
        <div class="align-start text-no-wrap font-weight-bold mr-3">
          {{ $t('profileContactInformation.email') }}
        </div>
        <div :title="user.email" class="align-end flex-grow-1 text-truncate text-end">
          {{ user.email }}
        </div>
      </v-flex>
      <v-divider class="my-4" />
      <v-flex class="d-flex">
        <div class="align-start text-no-wrap font-weight-bold mr-3">
          {{ $t('profileContactInformation.jobTitle') }}
        </div>
        <div :title="user.position || ''" class="align-end flex-grow-1 text-truncate text-end">
          {{ user.position || '' }}
        </div>
      </v-flex>
      <v-divider class="my-4" />
      <profile-contact-phone :user="user"/>
      <v-divider class="my-4" />
      <profile-contact-ims :user="user"/>
      <v-divider class="my-4" />
      <profile-contact-urls :user="user"/>
    </div>
    <profile-contact-information-drawer
      v-if="owner"
      ref="contactInformationEdit"
      :user="user"
      :upload-limit="uploadLimit"
      @refresh="refresh" />
  </v-app>
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
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
  }),
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    document.addEventListener('userModified', event => {
      if (event && event.detail) {
        const user = event.detail;
        Object.assign(this.user, user);
        this.user.ims = user.ims && user.ims.slice() || [];
        this.user.phones = user.phones && user.phones.slice() || [];
        this.user.urls = user.urls && user.urls.slice() || [];
        this.user.experiences = user.experiences && user.experiences.slice() || [];
      }
    });
  },
  methods: {
    refresh(user) {
      this.user = user;
    },
    editContactInformation() {
      this.$refs.contactInformationEdit.open();
    },
  },
};
</script>