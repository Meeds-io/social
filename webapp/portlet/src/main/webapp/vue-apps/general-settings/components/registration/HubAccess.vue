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
  <div class="pb-1">
    <v-list-item class="px-0 mb-2" three-line>
      <v-list-item-content class="py-0">
        <v-list-item-title class="my-0">
          <h4 class="font-weight-bold mt-0">
            {{ $t('generalSettings.access') }}
          </h4>
        </v-list-item-title>
        <v-list-item-subtitle class="pt-2">
          <h4 class="my-0 text-color">{{ $t('generalSettings.access.summary1') }}</h4>
        </v-list-item-subtitle>
        <v-list-item-subtitle class="py-2">
          <h5 class="my-0 text-color">{{ $t('generalSettings.access.summary2') }}</h5>
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>

    <v-list-item
      class="px-0 mt-4"
      dense
      @click="accessType = 'OPEN'">
      <v-list-item-action class="me-4">
        <v-radio-group v-model="accessType">
          <v-radio
            value="OPEN"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle"
            @click="accessType = 'OPEN'" />
        </v-radio-group>
      </v-list-item-action>
      <v-list-item-content class="py-0">
        <v-list-item-title class="subtitle-1">
          {{ $t('generalSettings.access.open') }}
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('generalSettings.access.open.subtitle') }}
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
    <v-list-item
      class="mt-2 mb-4"
      dense
      v-on="accessType === 'OPEN' && {
        click: () => externalUserOpenRegistration = !externalUserOpenRegistration,
      }">
      <v-list-item-action class="me-4">
        <v-tooltip
          :disabled="accessType === 'OPEN'"
          bottom>
          <template #activator="{ on, attrs }">
            <div
              v-bind="attrs"
              v-on="on">
              <v-switch
                v-model="externalUserOpenRegistration"
                :disabled="accessType !== 'OPEN'"
                @click.stop="0" />
            </div>
          </template>
          <span>{{ $t('generalSettings.access.openChoiceDisabled') }}</span>
        </v-tooltip>
      </v-list-item-action>
      <v-list-item-content class="py-0">
        <v-list-item-title class="subtitle-1">
          <help-label
            label="generalSettings.access.open.enableExternalUsers"
            tooltip="generalSettings.access.whatIsExternalUser">
            <template slot="helpContent">
              <p>
                {{ $t('generalSettings.access.whatIsExternalUser.paragraph1') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsExternalUser.paragraph2') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsExternalUser.paragraph3') }}
              </p>
            </template>
          </help-label>
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('generalSettings.access.open.enableExternalUsers.subtitle') }}
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>

    <v-list-item
      class="px-0 mt-4"
      dense
      @click="accessType = 'RESTRICTED'">
      <v-list-item-action class="me-4">
        <v-radio-group v-model="accessType">
          <v-radio
            value="RESTRICTED"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle"
            @click="accessType = 'RESTRICTED'" />
        </v-radio-group>
      </v-list-item-action>
      <v-list-item-content class="py-0">
        <v-list-item-title class="subtitle-1">
          {{ $t('generalSettings.access.restricted') }}
        </v-list-item-title>
        <v-list-item-subtitle>
          <help-label
            label="generalSettings.access.restricted.subtitle"
            label-class="text-sub-title"
            tooltip="generalSettings.access.whatIsRegisteredUser">
            <template slot="helpContent">
              <p>
                {{ $t('generalSettings.access.whatIsRegisteredUser.paragraph1') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsRegisteredUser.paragraph2') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsRegisteredUser.paragraph3') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsRegisteredUser.paragraph4') }}
              </p>
            </template>
          </help-label>
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
    <v-list-item
      class="mt-2 mb-4"
      dense
      v-on="accessType === 'RESTRICTED' && {
        click: () => externalUserRestrictedRegistration = !externalUserRestrictedRegistration,
      }">
      <v-list-item-action class="me-4">
        <v-tooltip
          :disabled="accessType === 'RESTRICTED'"
          bottom>
          <template #activator="{ on, attrs }">
            <div
              v-bind="attrs"
              v-on="on">
              <v-switch
                v-model="externalUserRestrictedRegistration"
                :disabled="accessType !== 'RESTRICTED'"
                @click.stop="0" />
            </div>
          </template>
          <span>{{ $t('generalSettings.access.restrictedChoiceDisabled') }}</span>
        </v-tooltip>
      </v-list-item-action>
      <v-list-item-content class="py-0">
        <v-list-item-title class="subtitle-1">
          <help-label
            label="generalSettings.access.restricted.enableExternalUsers"
            tooltip="generalSettings.access.whatIsExternalUser">
            <template slot="helpContent">
              <p>
                {{ $t('generalSettings.access.whatIsExternalUser.paragraph1') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsExternalUser.paragraph2') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsExternalUser.paragraph3') }}
              </p>
            </template>
          </help-label>
        </v-list-item-title>
        <v-list-item-subtitle>
          <help-label
            label="generalSettings.access.restricted.enableExternalUsers.subtitle"
            label-class="text-sub-title"
            tooltip="generalSettings.access.whatIsSpaceHost">
            <template slot="helpContent">
              <p>
                {{ $t('generalSettings.access.whatIsSpaceHost.paragraph1') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsSpaceHost.paragraph2') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsSpaceHost.paragraph3') }}
              </p>
            </template>
          </help-label>
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>

    <v-list-item dense class="px-0 mt-4 mb-2">
      <v-list-item-content class="py-0">
        <v-list-item-title>
          <h4 class="text-color my-0 py-2">{{ $t('generalSettings.access.platformAuthentication') }}</h4>
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
    <v-list-item dense>
      <v-list-item-content>
        <v-list-item-title class="subtitle-1">
          {{ $t('generalSettings.access.passwordAuthentication') }}
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('generalSettings.access.passwordAuthentication.subtitle') }}
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <v-chip class="font-italic">
          {{ $t('generalSettings.access.default') }}
        </v-chip>
      </v-list-item-action>
    </v-list-item>
    <v-list-item dense>
      <v-list-item-content>
        <v-list-item-title class="subtitle-1">
          {{ $t('generalSettings.access.additionalAuthentication') }}
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('generalSettings.access.additionalAuthentication.subtitle') }}
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <v-chip class="font-italic">
          {{ $t('generalSettings.access.contactAdministrator') }}
        </v-chip>
      </v-list-item-action>
    </v-list-item>

    <v-list-item dense class="px-0 mt-4 mb-2">
      <v-list-item-content class="py-0">
        <v-list-item-title class="subtitle-1">
          <h4 class="text-color my-0 py-2">{{ $t('generalSettings.access.startSettingPlatform') }}</h4>
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
    <v-list-item dense class="my-0">
      <v-list-item-content>
        <v-list-item-title class="subtitle-1">
          <help-label
            label="generalSettings.access.startSettingPlatform.spaces"
            tooltip="generalSettings.access.whatIsDefaultSpace">
            <template slot="helpContent">
              <p>
                {{ $t('generalSettings.access.whatIsDefaultSpace.paragraph1') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsDefaultSpace.paragraph2') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsDefaultSpace.paragraph3') }}
              </p>
            </template>
          </help-label>
        </v-list-item-title>
        <v-list-item-subtitle v-sanitized-html="defaultSelectedSpacesTitle" />
      </v-list-item-content>
      <v-list-item-action class="d-flex flex-row align-center my-0">
        <v-btn
          icon
          @click="$refs.defaultSpaceDrawer.open()">
          <v-icon size="24" class="icon-default-color">fa-edit</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <v-list-item dense class="my-0">
      <v-list-item-content>
        <v-list-item-title class="subtitle-1">
          <help-label
            label="generalSettings.access.startSettingPlatform.mandatorySpaces"
            tooltip="generalSettings.access.whatIsMandatorySpace">
            <template slot="helpContent">
              <p>
                {{ $t('generalSettings.access.whatIsMandatorySpace.paragraph1') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsMandatorySpace.paragraph2') }}
              </p>
              <p>
                {{ $t('generalSettings.access.whatIsMandatorySpace.paragraph3') }}
              </p>
            </template>
          </help-label>
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="my-0">
        <v-btn
          :href="mandatorySpacesLink"
          icon>
          <v-icon size="24" class="icon-default-color">fa-external-link-alt</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <v-list-item dense class="my-0">
      <v-list-item-content>
        <v-list-item-title class="subtitle-1 text-color">
          {{ $t('generalSettings.access.startSettingPlatform.createUsers') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="my-0">
        <v-btn
          :href="createUsersLink"
          icon>
          <v-icon size="24" class="icon-default-color">fa-external-link-alt</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <div class="d-flex my-12 mx-4 justify-end">
      <v-btn
        :aria-label="$t('generalSettings.cancel')"
        :disabled="loading"
        class="btn cancel-button me-4"
        elevation="0"
        @click="$emit('close')">
        <span class="text-none">
          {{ $t('generalSettings.cancel') }}
        </span>
      </v-btn>
      <v-btn
        :aria-label="$t('generalSettings.apply')"
        :disabled="!validForm"
        :loading="loading"
        color="primary"
        class="btn btn-primary"
        elevation="0"
        @click="save">
        <span class="text-none">
          {{ $t('generalSettings.apply') }}
        </span>
      </v-btn>
    </div>
    <portal-general-settings-default-spaces-drawer
      ref="defaultSpaceDrawer"
      v-model="defaultSpaceIds" />
  </div>
</template>
<script>
export default {
  props: {
    registrationSettings: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    createUsersLink: '/portal/administration/home/organisation/users',
    mandatorySpacesLink: '/portal/administration/home/organisation/spaces',
    accessType: 'OPEN',
    externalUserOpenRegistration: false,
    externalUserRestrictedRegistration: false,
    defaultSpaceIds: [],
    initialized: false,
  }),
  computed: {
    validForm() {
      return this.changed;
    },
    defaultSelectedSpacesTitle() {
      const spacesCount = this.defaultSpaceIds?.length || 0;
      return spacesCount
          && this.$t(spacesCount === 1 && 'generalSettings.access.defaultSelectedSpaceTitle' || 'generalSettings.access.defaultSelectedSpacesTitle', {0: `<strong>${this.defaultSpaceIds.length}</strong>`})
          || this.$t('generalSettings.access.noDefaultSpace');
    },
    changed() {
      if (!this.registrationSettings) {
        return false;
      }
      const oldSettings = JSON.parse(JSON.stringify(this.registrationSettings));
      const newSettings = Object.assign(JSON.parse(JSON.stringify(this.registrationSettings)), {
        externalUser: this.accessType === 'OPEN' ? this.externalUserOpenRegistration : this.externalUserRestrictedRegistration,
        extraGroupIds: this.defaultSpaceIds,
        type: this.accessType,
      });
      return JSON.stringify(newSettings) !== JSON.stringify(oldSettings);
    },
  },
  watch: {
    errorMessage() {
      if (this.errorMessage) {
        this.$root.$emit('alert-message', this.$t('generalSettings.savingError'), 'error');
      } else {
        this.$root.$emit('close-alert-message');
      }
    },
    changed() {
      this.$emit('changed', this.changed);
    },
    accessType(newVal, oldVal) {
      if (this.initialized) {
        if (newVal !== oldVal) {
          if (this.accessType === 'OPEN') {
            this.$root.$emit('alert-message-html', `
                <div>${this.$t('generalSettings.access.openChangeInformation1')}</div>
                <div>${this.$t('generalSettings.access.openChangeInformation2')}</div>
            `, 'info');
          } else if (this.accessType === 'RESTRICTED') {
            this.$root.$emit('alert-message-html', `
                <div>${this.$t('generalSettings.access.restrictedChangeInformation1')}</div>
                <div>${this.$t('generalSettings.access.restrictedChangeInformation2')}</div>
            `, 'info');
          }
        }
        this.externalUserOpenRegistration = false;
        this.externalUserRestrictedRegistration = false;
      }
    },
  },
  created() {
    this.init();
  },
  mounted() {
    this.$nextTick().then(() => this.initialized = true);
  },
  beforeDestroy() {
    this.$root.$emit('close-alert-message');
  },
  methods: {
    init() {
      this.accessType = this.registrationSettings?.type || 'OPEN';
      this.externalUserOpenRegistration = this.accessType === 'OPEN' && this.registrationSettings?.externalUser || false;
      this.externalUserRestrictedRegistration = this.accessType === 'RESTRICTED' && this.registrationSettings?.externalUser || false;
      this.defaultSpaceIds = this.registrationSettings?.extraGroupIds || [];
    },
    save() {
      this.$root.loading = true;
      return this.$registrationService.saveRegistrationSettings({
        type: this.accessType,
        externalUser: this.accessType === 'OPEN' ? this.externalUserOpenRegistration : this.externalUserRestrictedRegistration,
        extraGroupIds: this.defaultSpaceIds,
      })
        .then(() => this.$emit('saved'))
        .then(() => this.$root.$emit('alert-message', this.$t('generalSettings.registrationSavedSuccessfully'), 'success'))
        .catch(e => this.errorMessage = String(e))
        .finally(() => this.$root.loading = false);
    },
  }
};
</script>
