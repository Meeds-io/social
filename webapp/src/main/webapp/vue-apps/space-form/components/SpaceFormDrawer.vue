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
    :go-back-button="goBackButton && spaceTemplate"
    right
    class="spaceFormDrawer"
    @opened="stepper = 1"
    @closed="stepper = 0"
    @go-back="goBack">
    <template #title>
      {{ drawerTitle }}
    </template>
    <template v-if="drawer && space" #content>
      <div class="d-none d-lg-block">
        <space-form-preview
          v-if="drawer && spaceTemplate && !isParentSpaceSelection"
          :space="space"
          :preview-avatar="previewAvatar"
          class="pa-4 position-absolute"
          style="right: 450px;" />
      </div>
      <v-expand-transition>
        <div
          v-if="templates?.length && !spaceTemplate && !isEdit"
          class="d-flex flex-wrap align-center justify-space-between my-4 me-4">
          <v-card
            v-for="item in sortedTemplates"
            :key="item.id"
            class="space-template-card col-6 mt-0 mb-4 mx-0 ps-4 pa-0"
            height="136"
            flat>
            <v-hover v-slot="{hover}">
              <v-card
                class="d-flex flex-column border-color align-center full-height full-width pb-3 px-2"
                :class="{'border-primary-dashed': focusedCardId === item.id}"
                flat
                tabindex="0"
                role="button"
                :aria-label="item.name"
                @focusin="focusTemplateCard(item, $event)"
                @focusout="blurTemplateCard(item, $event)"
                @keydown.enter="handleAddNewSpace(item.id, templates, parentSpaceId)"
                @keydown.esc="escapeTemplateCard(item, $event)"
                @click="handleAddNewSpace(item.id, templates, parentSpaceId)">
                <div
                  class="mt-auto mb-2">
                  <v-icon size="32" class="py-2">{{ item.icon }}</v-icon>
                </div>
                <v-card
                  :title="item.name"
                  class="mb-auto full-width"
                  flat
                  tabindex="0"
                  role="button">
                  {{ item.name }}
                </v-card>
                <v-expand-transition>
                  <div
                    v-show="hover || focusedDetailsId === item.id"
                    class="absolute-full-size text-start pa-2 border-radius mask-color">
                    <div
                      :title="item.name"
                      class="text-truncate-2 font-weight-bold white--text full-width pb-1">
                      {{ item.name }}
                    </div>
                    <div
                      :title="getTemplateDescriptionText(item.description)"
                      class="text-subtitle white--text full-width text-truncate-5">
                      {{ getTemplateDescriptionText(item.description) }}
                    </div>
                  </div>
                </v-expand-transition>
              </v-card>
            </v-hover>
          </v-card>
        </div>
        <div v-else-if="isParentSpaceSelection" class="pa-5">
          <div class="mb-5">
            Please, select the parent space
          </div>
          <v-list-item
            v-for="s in parentSpaces"
            :key="s.id"
            class="px-0"
            dense
            @click="selectParentSpace(s)">
            <v-list-item-content>
              <space-avatar
                :space="s"
                class="not-clickable-link text-truncate"
                list-style />
            </v-list-item-content>
          </v-list-item>
        </div>
        <v-stepper
          v-else-if="spaceTemplate || isEdit"
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
              :step="1"
              class="ma-4 pa-0"
              editable>
              {{ $t('spacesList.label.nameTitle') }}
            </v-stepper-step>
            <v-stepper-content :step="1" class="pa-0 ma-0 no-border">
              <form
                v-if="stepper === 1"
                ref="form1"
                class="px-4"
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
                  name="name"
                  autofocus>
              </form>
            </v-stepper-content>
          </template>
          <template v-if="includeProperties">
            <v-stepper-step
              v-if="!singleStep"
              :step="propertiesStep"
              class="ma-4 pa-0"
              editable>
              {{ $t('spacesList.label.propertiesTitle') }}
            </v-stepper-step>
            <v-stepper-content :step="propertiesStep" class="pa-0 ma-0 no-border">
              <form
                v-if="stepper === propertiesStep"
                ref="form2"
                class="px-4"
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
                  ck-editor-id="spaceFormDescription"
                  ck-editor-type="spaceDescription"
                  disable-suggester
                  autofocus />
                <space-form-avatar
                  v-model="space.avatarId"
                  :name="space.displayName"
                  :src="space.avatarUrl"
                  class="mt-4"
                  @avatar-updated="avatarUpdated" />
                <space-form-banner
                  v-model="space.bannerId"
                  :default-banner-url="bannerUrl"
                  :src="space.bannerUrl"
                  class="mt-4" />
              </form>
            </v-stepper-content>
          </template>
          <template v-if="includeInvitation">
            <v-stepper-step
              v-if="!singleStep"
              :step="invitationStep"
              class="ma-4 pa-0"
              editable>
              {{ $t('spacesList.label.inviteUsers') }}
            </v-stepper-step>
            <v-stepper-content :step="invitationStep" class="pa-0 ma-0 no-border">
              <space-form-invitation
                v-if="stepper === invitationStep"
                @invited-members="space.invitedMembers = $event"
                @invited-email="space.externalInvitedUsers = $event" />
            </v-stepper-content>
          </template>
          <template v-if="includeAccess">
            <v-stepper-step
              v-if="!singleStep"
              :step="accessStep"
              class="ma-4 pa-0"
              editable>
              {{ $t('spacesList.label.spaceAccessTitle') }}
            </v-stepper-step>
            <v-stepper-content :step="accessStep" class="pa-0 ma-0 no-border">
              <space-form-access
                v-if="stepper === accessStep"
                :visibility="space.visibility"
                :subscription="space.subscription"
                @visibility="space.visibility = $event"
                @subscription="space.subscription = $event" />
            </v-stepper-content>
          </template>
        </v-stepper>
      </v-expand-transition>
    </template>
    <template v-if="drawer && !isParentSpaceSelection && (spaceTemplate || isEdit)" #footer>
      <div class="d-flex">
        <v-btn
          v-if="stepper > 1 && !isEdit"
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
          <template v-else-if="isEdit">
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
    subspaceTemplateIds: [],
    selectedSpacesWithExternals: [],
    externalAlert: false,
    goBackButton: false,
    maxDescriptionLength: 2000,
    defaultBannerSrc: '/social/images/defaultSpaceBanner.webp',
    previewAvatar: null,
    isParentSpaceSelection: false,
    parentSpaces: [],
    parentSpacesSize: 0,
    selectedParentSpace: null,
    parentSpaceId: null,
    focusedCardId: null,
    focusedDetailsId: null,
  }),
  computed: {
    drawerTitle() {
      if (this.isEdit) {
        return this.$t('spacesList.label.editSpace', {
          0: this.space.displayName
        });
      } else {
        return this.spaceTemplate && this.$t('spacesList.label.addNewSpaceWithTemplate', {
          0: this.spaceTemplate.name,
        }) || this.$t('spacesList.label.addNewSpace');
      }
    },
    saveButtonDisabled() {
      return this.savingSpace
        || this.spaceSaved
        || this.stepper < this.lastStep && !this.space.id
        || (this.space.description?.length || 0) > this.maxDescriptionLength;
    },
    sortedTemplates() {
      const spaceTemplates = this.templates?.filter?.(t => t.name && t.enabled) || [];
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
      return this.isEdit ||  this.spaceTemplate?.spaceFields?.includes?.('name');
    },
    includeProperties() {
      return this.isEdit || this.spaceTemplate?.spaceFields?.includes?.('properties');
    },
    includeAccess() {
      return !this.isEdit && this.spaceTemplate?.spaceFields?.includes?.('access');
    },
    includeInvitation() {
      return !this.isEdit && !!eXo.env.portal.userName && this.spaceTemplate?.spaceFields?.includes?.('invitation');
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
    isEdit() {
      return this.space?.id;
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
      if (!this.isEdit) {
        this.setSpaceTemplateProperties();
      }
      this.parentSpaces = [];
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

    this.$root.$on('addNewSpace', this.openByRootEvent);
    document.addEventListener('editSpace', this.editByEvent);
    document.addEventListener('addNewSpace', this.openByEvent);
    document.addEventListener('addNewSpaceWithAppId', this.openByAppId);
  },
  beforeDestroy() {
    this.$root.$off('addNewSpace', this.openByRootEvent);
    document.removeEventListener('editSpace', this.editByEvent);
    document.removeEventListener('addNewSpace', this.openByEvent);
    document.removeEventListener('addNewSpaceWithAppId', this.openByAppId);
  },
  methods: {
    focusTemplateCard(item, event) {
      this.focusedCardId = item.id;
      this.focusedDetailsId = event.target === event.currentTarget ? null : item.id;
    },
    blurTemplateCard(item, event) {
      if (!event.currentTarget.contains(event.relatedTarget)) {
        this.focusedCardId = null;
        this.focusedDetailsId = null;
      }
    },
    escapeTemplateCard(item, event) {
      if (this.focusedDetailsId === item.id) {
        event.stopPropagation();
        event.target.blur();
      }
    },
    async handleAddNewSpace(templateId, spaceTemplates, parentSpaceId, isParentSpaceSelection) {
      this.templateId = templateId;
      this.parentSpaceId = parentSpaceId;
      this.templates = spaceTemplates;
      if (isParentSpaceSelection) {
        const data = await this.$spaceService.getSpacesByFilter({
          offset: 0,
          limit: 20,
          onlyParentSpaces: true,
          filter: 'accessible'
        });
        this.parentSpaces = data.spaces || [];
        this.isParentSpaceSelection = true;
        this.$refs.spaceFormDrawer.open();
        return;
      }
      await this.refreshTemplates();
      if (this.templates?.length === 1 && !templateId) {
        this.templateId = this.templates[0].id;
      }
      this.setSpaceTemplateProperties();
      if (!this.templateId && !this.parentSpaceId && !isParentSpaceSelection) {
        this.isParentSpaceSelection = false;
        this.$refs.spaceFormDrawer.open();
        return;
      }
      if (this.parentSpaceId) {
        const parentSpace = await this.$spaceService.getSpaceById(parentSpaceId);
        await this.selectParentSpace(parentSpace);
        if (!spaceTemplates) {
          this.templates = await this.$spaceTemplateService.getAllowedSubspaceTemplates(parentSpace?.templateId);
          if (this.templates?.length === 1) {
            this.templateId = this.templates[0].id;
          }
        }
        this.$refs.spaceFormDrawer.open();
        return;
      }
      if (this.templateId) {
        const isSubspaceTemplate = this.subspaceTemplateIds.includes(this.templateId);
        if (!isSubspaceTemplate || this.selectedParentSpace) {
          this.isParentSpaceSelection = false;
          this.$refs.spaceFormDrawer.open();
          return;
        }
        const data = await this.$spaceService.getSpacesByFilter({
          offset: 0,
          limit: 20,
          subspaceTemplateId: this.templateId,
          filter: 'accessible'
        });
        this.parentSpaces = data.spaces || [];
        this.parentSpacesSize = data.size || [];
        if (this.parentSpacesSize === 1) {
          await this.selectParentSpace(this.parentSpaces[0]);
          this.$refs.spaceFormDrawer.open();
          return;
        }
        this.isParentSpaceSelection = this.parentSpacesSize > 1;
        this.$refs.spaceFormDrawer.open();
      }
    },
    async selectParentSpace(space) {
      this.selectedParentSpace = space;
      this.$set(this.space, 'parentSpaceId', space?.id);
      this.isParentSpaceSelection = false;
      if (!this.templateId && !this.parentSpaceId) {
        const allowedSubspaceTemplates = await this.$spaceTemplateService.getAllowedSubspaceTemplates(space?.templateId);
        await this.handleAddNewSpace(null, allowedSubspaceTemplates, space?.id);
      }
    },
    openByAppId(e) {
      if (typeof e?.detail === 'number' && this.$root.id && this.$root.id !== e?.detail) {
        return;
      }
      this.open();
    },
    openByEvent(e) {
      this.openByRootEvent(e?.detail?.templateId, e?.detail?.spaceTemplates, e?.detail?.parentSpaceId, e?.detail?.isParentSpaceSelection);
    },
    openByRootEvent(templateId, spaceTemplates, parentSpaceId, isParentSpaceSelection) {
      this.goBackButton = !templateId;
      this.handleAddNewSpace(templateId, spaceTemplates, parentSpaceId, isParentSpaceSelection);
    },
    editByEvent(e) {
      this.goBackButton = false;
      this.open(null, e?.detail);
    },
    async open(templateId, space, spaceTemplates) {
      if (space) {
        this.space = JSON.parse(JSON.stringify(space));
      } else {
        Object.assign(this.space, {
          templateId: templateId,
          subscription: 'open',
          visibility: 'private',
        });
      }
      this.templateId = this.space.templateId && Number(this.space.templateId);
      if (spaceTemplates) {
        this.templates = spaceTemplates;
      } else {
        await this.refreshTemplates();
      }
      if (this.templates?.length === 1 && !this.templateId) {
        this.templateId = this.templates[0].id;
      }
      this.setSpaceTemplateProperties();
      this.$refs.spaceFormDrawer.open();
    },
    setSpaceTemplateProperties() {
      if (!this.isEdit && this.spaceTemplate) {
        this.$set(this.space, 'templateId', this.spaceTemplate.id);
        this.$set(this.space, 'subscription', this.spaceTemplate.spaceDefaultRegistration?.toLowerCase?.());
        this.$set(this.space, 'visibility', this.spaceTemplate.spaceDefaultVisibility?.toLowerCase?.());
      }
    },
    previousStep() {
      if (this.stepper > 1) {
        this.stepper--;
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
      this.close();
    },
    close() {
      this.$refs.spaceFormDrawer.close();
    },
    saveSpace() {
      if (this.spaceSaved || this.savingSpace) {
        return;
      }
      this.savingSpace = true;
      this.space.templateId = this.templateId;
      if (this.isEdit) {
        this.$spaceService.updateSpace({
          id: this.space.id,
          displayName: this.space.displayName,
          description: this.space.description,
          avatarId: this.space.avatarId,
          bannerId: this.space.bannerId,
        })
          .then(space => {
            document.dispatchEvent(new CustomEvent('space-settings-updated', {detail: space}));
            this.$root.$emit('alert-message', this.$t('SpaceSettings.successfullySaved'), 'success');
            this.close();
          })
          .catch(e => {
            if (String(e).indexOf('SPACE_ALREADY_EXIST') >= 0) {
              this.$root.$emit('alert-message', this.$t('SpaceSettings.error.spaceWithSameNameExists'), 'error');
            } else if (String(e).indexOf('INVALID_SPACE_NAME') >= 0) {
              this.$root.$emit('alert-message', this.$t('spacesList.error.InvalidSpaceName'), 'error');
            } else {
              this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingSpace'), 'error');
            }
          })
          .finally(() => this.savingSpace = false);
      } else if (eXo.env.portal.userName) {
        return this.$spaceService.createSpace(this.space)
          .then(space => {
            this.spaceSaved = true;
            this.close();
            window.location.href = `${eXo.env.portal.context}/s/${space.id}`;
          })
          .catch((err) => {
            const code = err.message;
            switch (code) {
            case 'SUBSPACES_LIMIT_REACHED':
              this.$root.$emit('alert-message', this.$t('spacesList.error.subspacesLimitReached'), 'error');
              break;
            case 'SUBSPACES_PERMISSIONS':
              this.$root.$emit('alert-message', this.$t('spacesList.error.notAllowedToCreateSubspace'), 'error');
              break;
            default:
              this.$root.$emit(this.$t('spacesList.error.unknownErrorWhenSavingSpace'), 'error');
            }
          })
          .finally(() => this.savingSpace = false);

      } else {
        return this.$spaceService.prepareSpaceInstance(this.space)
          .then(() => {
            this.spaceSaved = true;
            window.location.href = `${eXo.env.portal.context}/login`;
          })
          .catch(() => this.$root.$emit(this.$t('spacesList.error.unknownErrorWhenSavingSpace'), 'error'))
          .finally(() => this.savingSpace = false);
      }
    },
    avatarUpdated(avatar) {
      this.previewAvatar = avatar;
    },
    async refreshTemplates() {
      if (!this.templates) {
        if (!this.$root.spaceTemplates) {
          try {
            this.$root.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates();
            this.templates = this.$root.spaceTemplates;
          } catch (e) {
            this.templates = [];
          }
        } else {
          this.templates = this.$root.spaceTemplates;
        }
      }
      if (!this.subspaceTemplateIds?.length) {
        if (!this.$root?.subspaceTemplateIds) {
          try {
            this.$root.subspaceTemplateIds = await this.$spaceTemplateService.getSubspaceTemplateIds();
          } catch (e) {
            this.templates = [];
          }
        }
        this.subspaceTemplateIds = this.$root.subspaceTemplateIds;
      }
    },
    goBack() {
      this.templateId = null;
      if (!this.parentSpaceId) {
        this.selectedParentSpace = null;
      }
    },
    getTemplateDescriptionText(description) {
      return this.$utils.htmlToText(description);
    },
  },
};
</script>
