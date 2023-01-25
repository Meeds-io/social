<template>
  <v-app
    :class="owner && 'profileContactInformation' || 'profileContactInformationOther'"
    class="white">
    <v-toolbar
      color="white"
      flat
      class="border-box-sizing">
      <div
        class="text-header-title text-sub-title profileContactTitle">
        {{ title }}
      </div>
      <v-spacer />
      <v-btn
        v-if="owner"
        id="profileContactEditButton"
        icon
        outlined
        small
        @click="editContactInformation">
        <v-icon size="18">fas fa-edit</v-icon>
      </v-btn>
    </v-toolbar>
    <div v-if="user" class="px-4 pb-6 white">
      <v-flex class="d-flex">
        <div
          class="align-start text-no-wrap font-weight-bold me-3">
          {{ $t('profileContactInformation.fullName') }}
        </div>
        <div
          :title="user.fullname"
          id="profileContactUserFullname"
          class="align-end flex-grow-1 text-truncate text-end">
          {{ user.fullname }}
        </div>
      </v-flex>
      <v-divider class="my-4" />
      <v-flex class="d-flex">
        <div class="align-start text-no-wrap font-weight-bold me-3">
          {{ $t('profileContactInformation.email') }}
        </div>
        <div
          :title="user.email"
          id="profileContactUserEmail"
          class="align-end flex-grow-1 text-truncate text-end">
          <span v-autolinker="user.email"></span>
        </div>
      </v-flex>
      <v-divider v-if="user.position" class="my-4" />
      <v-flex v-if="user.position" class="d-flex">
        <div class="align-start text-no-wrap font-weight-bold me-3">
          {{ $t('profileContactInformation.jobTitle') }}
        </div>
        <div
          :title="user.position || ''"
          id="profileContactUserPosition"
          class="align-end flex-grow-1 text-truncate text-end">
          {{ user.position || '' }}
        </div>
      </v-flex>
      <template v-if="user.company">
        <v-divider class="my-4" />
        <v-flex class="d-flex">
          <div class="align-start text-no-wrap font-weight-bold me-3">
            {{ $t('profileContactInformation.company') }}
          </div>
          <div
            :title="user.company"
            id="profileContactUserCompany"
            class="align-end flex-grow-1 text-truncate text-end">
            {{ user.company }}
          </div>
        </v-flex>
      </template>
      <template v-if="user.location">
        <v-divider class="my-4" />
        <v-flex class="d-flex">
          <div class="align-start text-no-wrap font-weight-bold me-3">
            {{ $t('profileContactInformation.location') }}
          </div>
          <div
            v-autolinker="user.location"
            :title="user.location"
            id="profileContactUserLocation"
            class="align-end flex-grow-1 text-truncate text-end">
          </div>
        </v-flex>
      </template>
      <template v-if="user.department">
        <v-divider class="my-4" />
        <v-flex class="d-flex">
          <div class="align-start text-no-wrap font-weight-bold me-3">
            {{ $t('profileContactInformation.department') }}
          </div>
          <div
            :title="user.department"
            id="profileContactUserDepartment"
            class="align-end flex-grow-1 text-truncate text-end">
            {{ user.department }}
          </div>
        </v-flex>
      </template>
      <template v-if="user.team">
        <v-divider class="my-4" />
        <v-flex class="d-flex">
          <div class="align-start text-no-wrap font-weight-bold me-3">
            {{ $t('profileContactInformation.team') }}
          </div>
          <div
            :title="user.team"
            id="profileContactUserTeam"
            class="align-end flex-grow-1 text-truncate text-end">
            {{ user.team }}
          </div>
        </v-flex>
      </template>
      <template v-if="user.profession">
        <v-divider class="my-4" />
        <v-flex class="d-flex">
          <div class="align-start text-no-wrap font-weight-bold me-3">
            {{ $t('profileContactInformation.profession') }}
          </div>
          <div
            :title="user.profession"
            id="profileContactUserProfession"
            class="align-end flex-grow-1 text-truncate text-end">
            {{ user.profession }}
          </div>
        </v-flex>
      </template>
      <template v-if="user.country">
        <v-divider class="my-4" />
        <v-flex class="d-flex">
          <div class="align-start text-no-wrap font-weight-bold me-3">
            {{ $t('profileContactInformation.country') }}
          </div>
          <div
            :title="user.country"
            id="profileContactUserCountry"
            class="align-end flex-grow-1 text-truncate text-end">
            {{ user.country }}
          </div>
        </v-flex>
      </template>
      <template v-if="user.city">
        <v-divider class="my-4" />
        <v-flex class="d-flex">
          <div class="align-start text-no-wrap font-weight-bold me-3">
            {{ $t('profileContactInformation.city') }}
          </div>
          <div
            :title="user.city"
            id="profileContactUserCity"
            class="align-end flex-grow-1 text-truncate text-end">
            {{ user.city }}
          </div>
        </v-flex>
      </template>
      <profile-contact-phone :user="user" />
      <profile-contact-ims :user="user" />
      <profile-contact-urls :user="user" />
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
    uploadLimit: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    user: null,
  }),
  computed: {
    title() {
      return this.owner && this.$t('profileContactInformation.yourContactInformation') || this.$t('profileContactInformation.contactInformation');
    },
  },
  created() {
    return this.$userService.getUser(eXo.env.portal.profileOwner, 'all')
      .then(user => this.refresh(user))
      .finally(() => this.$root.$applicationLoaded());
  },
  mounted() {
    document.addEventListener('userModified', event => {
      if (event && event.detail) {
        const user = event.detail;
        Object.assign(this.user, user);
        this.user.ims = user.ims && user.ims.slice() || [];
        this.user.phones = user.phones && user.phones.slice() || [];
        this.user.urls = user.urls && user.urls.slice() || [];
        this.user.experiences = user.experiences && user.experiences.slice() || [];
        this.$nextTick().then(() => this.$root.$emit('application-loaded'));
      }
    });

    if (this.user) {
      this.$nextTick().then(() => this.$root.$emit('application-loaded'));
    }
  },
  methods: {
    refresh(user) {
      this.user = user;
      this.$nextTick().then(() => this.$root.$emit('application-loaded'));
    },
    editContactInformation() {
      this.$refs.contactInformationEdit.open();
    },
  },
};
</script>