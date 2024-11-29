<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    ref="spaceFormDrawer"
    v-model="drawer"
    right
    class="spaceFormDrawer"
    @opened="stepper = 1"
    @closed="stepper = 0">
    <template slot="title">
      {{ $t('spacesList.label.addNewSpace') }}
    </template>
    <template v-if="drawer" slot="content">
      <v-expand-transition v-if="!spaceTemplate">
        <div
          v-if="templates?.length"
          class="d-flex flex-wrap align-center justify-space-between ma-5">
          <v-card
            v-for="item in sortedTemplates"
            :key="item.id"
            class="border-color d-flex flex-column align-center me-auto my-2 px-2 py-1"
            width="160"
            height="136"
            hover
            flat
            @click="chooseTemplate(item)">
            <v-icon size="32" class="py-2">{{ item.icon }}</v-icon>
            <div
              :title="item.name"
              class="text-truncate flex-grow-1 flex-shrink-0 full-width pb-1">
              {{ item.name }}
            </div>
            <div
              v-if="item.description"
              :title="item.description"
              class="text-subtitle text-truncate-3 flex-grow-1 flex-shrink-0">
              {{ item.description }}
            </div>
          </v-card>
        </div>
      </v-expand-transition>
      <v-expand-transition v-else>
        <v-stepper
          v-model="stepper"
          :class="{
            'pe-3' : isMobile,
            'mt-5' : singleStep,
          }"
          class="ma-0 py-0"
          vertical
          flat>
          <template v-if="includeName">
            <v-stepper-step
              v-if="!singleStep"
              :complete="stepper > 1"
              class="ma-0 px-5 py-4"
              step="1"
              editable>
              {{ $t('spacesList.label.nameTitle') }}
            </v-stepper-step>
            <v-stepper-content step="1" class="pa-0 ma-0 no-border">
              <form
                ref="form1"
                class="px-5"
                @submit="nextStep">
                <v-label for="name">
                  {{ $t('spacesList.label.nameLabel') }}
                </v-label>
                <input
                  ref="autoFocusInput1"
                  v-model="space.displayName"
                  :aria-label="$t('spacesList.label.namePlaceholder')"
                  :placeholder="$t('spacesList.label.namePlaceholder')"
                  class="input-block-level ignore-vuetify-classes my-3"
                  type="text"
                  name="name">
              </form>
            </v-stepper-content>
          </template>
          <template v-if="includeProperties">
            <v-stepper-step
              v-if="!singleStep"
              :complete="stepper > propertiesStep"
              :step="propertiesStep"
              class="ma-0 px-5 py-4"
              editable>
              {{ $t('spacesList.label.propertiesTitle') }}
            </v-stepper-step>
            <v-stepper-content :step="propertiesStep" class="pa-0 ma-0 no-border">
              <form
                ref="form2"
                class="px-5"
                @submit="nextStep">
                <v-label for="description">
                  {{ $t('spacesList.label.descriptionLabel') }}
                </v-label>
                <rich-editor
                  id="spaceDescriptionRichEditor"
                  v-model="space.description"
                  :placeholder="$t('spacesList.label.descriptionPlaceholder')"
                  :max-length="maxDescriptionLength"
                  tag-enabled
                  class="my-3"
                  ck-editor-type="spaceDescription"
                  disable-suggester />
                <space-form-avatar
                  v-model="space.avatarId"
                  :name="space.displayName"
                  class="mt-4" />
                <space-form-banner
                  v-model="space.bannerId"
                  :default-banner-url="bannerUrl"
                  class="mt-4" />
              </form>
            </v-stepper-content>
          </template>
          <template v-if="includeInvitation">
            <v-stepper-step
              v-if="!singleStep"
              :complete="stepper > invitationStep"
              :step="invitationStep"
              class="ma-0 px-5 py-4"
              editable>
              {{ $t('spacesList.label.inviteUsers') }}
            </v-stepper-step>
            <v-stepper-content :step="invitationStep" class="pa-0 ma-0 no-border">
              <space-form-invitation
                :class="{
                  'mt-n3' : singleStep && !$root.isExternalFeatureEnabled,
                }"
                @invited-members="space.invitedMembers = $event"
                @invited-email="space.externalInvitedUsers = $event" />
            </v-stepper-content>
          </template>
          <template v-if="includeAccess">
            <v-stepper-step
              v-if="!singleStep"
              :complete="stepper > accessStep"
              :step="accessStep"
              class="ma-0 px-5 py-4"
              editable>
              {{ $t('spacesList.label.spaceAccessTitle') }}
            </v-stepper-step>
            <v-stepper-content :step="accessStep" class="pa-0 ma-0 no-border">
              <space-form-access
                :visibility="space.visibility"
                :subscription="space.subscription"
                @visibility="space.visibility = $event"
                @subscription="space.subscription = $event" />
            </v-stepper-content>
          </template>
        </v-stepper>
      </v-expand-transition>
    </template>
    <template v-if="spaceTemplate" slot="footer">
      <div class="d-flex">
        <v-btn
          v-if="stepper > 1 || !noGoBack"
          class="btn"
          @click="previousStep">
          {{ $t('spacesList.button.back') }}
        </v-btn>
        <v-spacer />
        <v-btn
          :disabled="savingSpace || spaceSaved"
          class="btn me-2"
          @click="cancel">
          <template>
            {{ $t('spacesList.button.cancel') }}
          </template>
        </v-btn>
        <v-btn
          v-if="stepper < lastStep"
          class="btn btn-primary"
          @click="nextStep">
          {{ $t('spacesList.button.next') }}
        </v-btn>
        <v-btn
          v-else
          :loading="savingSpace"
          :disabled="saveButtonDisabled"
          class="btn btn-primary"
          @click="saveSpace">
          <v-icon v-if="spaceSaved">mdi-check-all</v-icon>
          <template v-else-if="spaceToUpdate">
            {{ $t('spacesList.button.updateSpace') }}
          </template>
          <template v-else>
            {{ $t('spacesList.button.createSpace') }}
          </template>
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    savingSpace: false,
    spaceSaved: false,
    space: {},
    stepper: 0,
    templateId: null,
    templates: [],
    selectedSpacesWithExternals: [],
    externalAlert: false,
    noGoBack: false,
    maxDescriptionLength: 2000,
    defaultBannerSrc: '/social/images/defaultSpaceBanner.webp',
  }),
  computed: {
    saveButtonDisabled() {
      return this.savingSpace
        || this.spaceSaved
        || this.stepper < this.lastStep && !this.space.id
        || (this.space.description?.length || 0) > this.maxDescriptionLength;
    },
    sortedTemplates() {
      const spaceTemplates = this.templates?.filter?.(t => t.name) || [];
      spaceTemplates.sort((a, b) => this.$root.collator.compare(a.name.toLowerCase(), b.name.toLowerCase()));
      return this.keyword?.length && spaceTemplates.filter(t => {
        const name = this.$te(t.name) ? this.$t(t.name) : t.name;
        const description = this.$te(t.description) ? this.$t(t.description) : t.description;
        return name?.toLowerCase?.()?.includes(this.keyword.toLowerCase())
          || this.$utils.htmlToText(description)?.toLowerCase?.()?.includes(this.keyword.toLowerCase());
      }) || spaceTemplates;
    },
    spaceTemplate() {
      return this.templates?.find?.(t => t.id === this.templateId);
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('spacesList.label.inviteMembers'),
        noDataLabel: this.$t('spacesList.label.noDataLabel'),
      };
    },
    invitedSpacesWithExternals() {
      return this.$t && this.$t('spaceList.checkExternals.warning', {
        0: `<strong>[${this.selectedSpacesWithExternals.join(',')}]</strong>`,
      });
    },
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
    },
    spaceInvitedMembers() {
      return this.space?.invitedMembers;
    },
    bannerUrl() {
      return this.spaceTemplate?.bannerFileId && `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/spaceTemplateBanner/${this.spaceTemplate?.id}/${this.spaceTemplate?.bannerFileId}?size=0` || this.defaultBannerSrc;
    },
    includeName() {
      return this.spaceTemplate?.spaceFields?.includes?.('name');
    },
    includeProperties() {
      return this.spaceTemplate?.spaceFields?.includes?.('properties');
    },
    includeAccess() {
      return this.spaceTemplate?.spaceFields?.includes?.('access');
    },
    includeInvitation() {
      return this.spaceTemplate?.spaceFields?.includes?.('invitation');
    },
    propertiesStep() {
      return (this.includeName ? 1 : 0) + 1;
    },
    invitationStep() {
      return (this.includeName ? 1 : 0) + (this.includeProperties ? 1 : 0) + 1;
    },
    accessStep() {
      return (this.includeName ? 1 : 0) + (this.includeProperties ? 1 : 0) + (this.includeInvitation ? 1 : 0) + 1;
    },
    lastStep() {
      return (this.includeName ? 1 : 0) + (this.includeProperties ? 1 : 0) + (this.includeInvitation ? 1 : 0) + (this.includeAccess ? 1 : 0);
    },
    singleStep() {
      return this.lastStep === 1;
    },
  },
  watch: {
    savingSpace() {
      if (this.savingSpace) {
        this.$refs.spaceFormDrawer.startLoading();
      } else {
        this.$refs.spaceFormDrawer.endLoading();
      }
    },
    stepper() {
      if (this.stepper) {
        // Used to focus on space name field
        this.$nextTick().then(() => {
          let elementToFocusOn = this.$refs[`autoFocusInput${this.stepper}`];
          if (elementToFocusOn) {
            elementToFocusOn = elementToFocusOn.focus || !elementToFocusOn.$el ? elementToFocusOn : elementToFocusOn.$el || elementToFocusOn;
          }
          if (elementToFocusOn) {
            window.setTimeout(() => {
              elementToFocusOn.focus();
            }, 200);
          }
        });
      } else {
        this.spaceSaved = false;
        this.savingSpace = false;
      }
    },
    spaceTemplate() {
      if (!this.space?.id) {
        this.setSpaceTemplateProperties();
      }
    },
    externalAlert() {
      if (this.externalAlert) {
        this.$root.$emit('alert-message-html', this.invitedSpacesWithExternals, 'warning');
      }
    },
    spaceInvitedMembers() {
      if (this.spaceInvitedMembers) {
        this.selectedSpacesWithExternals = [];
        this.externalAlert = false;
        this.spaceInvitedMembers.filter(item => item.providerId === 'space')
          .forEach(space => {
            this.$spaceService.checkExternals(space.spaceId).then(hasExternals => {
              if (hasExternals && hasExternals === 'true') {
                this.selectedSpacesWithExternals.push(space.displayName);
                this.$nextTick().then(() => this.externalAlert = true);
              }
            });
          });
      }
    }
  },
  created() {
    window.spaceFormAdded = true;
    const search = window.location.search && window.location.search.substring(1);
    if (search) {
      const parameters = JSON.parse(
        `{"${decodeURI(search)
          .replace(/"/g, '\\"')
          .replace(/&/g, '","')
          .replace(/=/g, '":"')}"}`
      );
      const createSpace = parameters['createSpace'];
      if (createSpace && Boolean(createSpace)) {
        this.$root.$once('application-loaded', () => {
          this.$nextTick().then(this.open);
        });
      }
    }

    this.$root.$on('addNewSpace', this.open);
    document.addEventListener('addNewSpace', this.openByEvent);
  },
  beforeDestroy() {
    this.$root.$off('addNewSpace', this.open);
    document.removeEventListener('addNewSpace', this.openByEvent);
  },
  methods: {
    openByEvent(e) {
      this.open(e?.detail);
    },
    async open(templateId) {
      this.templateId = templateId && Number(templateId);
      this.noGoBack = !!templateId;
      this.space = {
        templateId: templateId,
        subscription: 'open',
        visibility: 'private',
      };
      if (!this.$root.spaceTemplates) {
        this.$root.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates();
      }
      this.templates = this.$root.spaceTemplates;
      if (this.templates?.length === 1) {
        this.templateId = this.templates[0].id;
      }
      this.setSpaceTemplateProperties();
      this.$refs.spaceFormDrawer.open();
    },
    async chooseTemplate(template) {
      this.templateId = template?.id;
      await this.$nextTick();
      this.setSpaceTemplateProperties();
    },
    setSpaceTemplateProperties() {
      if (this.spaceTemplate) {
        this.$set(this.space, 'templateId', this.spaceTemplate.id);
        this.$set(this.space, 'subscription', this.spaceTemplate.spaceDefaultRegistration?.toLowerCase?.());
        this.$set(this.space, 'visibility', this.spaceTemplate.spaceDefaultVisibility?.toLowerCase?.());
      }
    },
    previousStep() {
      if (this.stepper > 1) {
        this.stepper--;
      } else {
        this.templateId = null;
      }
    },
    nextStep(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.stepper++;
    },
    cancel() {
      this.$refs.spaceFormDrawer.close();
    },
    saveSpace() {
      if (this.spaceSaved || this.savingSpace) {
        return;
      }
      this.savingSpace = true;
      this.space.templateId = this.templateId;
      return this.$spaceService.createSpace(this.space)
        .then(space => {
          this.spaceSaved = true;
          window.location.href = `${eXo.env.portal.context}/s/${space.id}`;
        })
        .catch(() => this.$root.$emit(this.$t('spacesList.error.unknownErrorWhenSavingSpace'), 'error'))
        .finally(() => this.savingSpace = false);
    },
  },
};
</script>
