<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
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
    id="activityComposerDrawer"
    ref="activityComposerDrawer"
    v-if="singleton"
    v-model="drawer"
    v-draggable="enabled"
    disable-pull-to-refresh
    allow-expand
    right
    fixed>
    <template #title>
      {{ $t('activity.composer.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card flat>
        <div v-if="!activityId">
          <div v-if="audienceTypesDisplay" class="mt-1 px-4 pt-4">
            <div v-if="postVisibility">
              <div class="mb-2"> {{ $t('activity.composer.content.title') }} </div>
              <v-radio-group
                v-if="postToNetwork"
                v-model="audienceChoice"
                class="mt-0"
                mandatory>
                <v-radio value="yourNetwork">
                  <template #label>
                    <span class="ms-1"> {{ $t('activity.composer.content.yourNetwork') }}</span>
                  </template>
                </v-radio>
                <v-radio value="oneOfYourSpaces">
                  <template #label>
                    <span class="ms-1"> {{ $t('activity.composer.content.oneOfYourSpaces') }}</span>
                  </template>
                </v-radio>
              </v-radio-group>
              <exo-identity-suggester
                v-if="spaceSuggesterDisplay"
                ref="audienceComposerSuggester"
                v-model="spaceIdentity"
                :labels="spaceSuggesterLabels"
                :include-users="false"
                :width="220"
                name="audienceComposerSuggester"
                class="user-suggester mt-n2"
                include-spaces
                only-redactor />
            </div>
            <v-list-item v-if="audienceAvatarDisplay" class="text-truncate px-0 mt-n1">
              <exo-space-avatar
                :space-id="spaceId"
                :size="30"
                extra-class="text-truncate"
                avatar />
              <exo-user-avatar
                :profile-id="username"
                :size="spaceId && 25 || 30"
                :extra-class="spaceId && 'ms-n4 mt-6' || ''"
                avatar />
              <v-list-item-content class="py-0 accountTitleLabel text-truncate">
                <v-list-item-title class="font-weight-bold d-flex mb-0">
                  <exo-space-avatar
                    :space-id="spaceId"
                    extra-class="text-truncate"
                    fullname
                    bold-title
                    link-style
                    username-class />
                </v-list-item-title>
                <v-list-item-subtitle class="d-flex flex-row flex-nowrap">
                  <exo-user-avatar
                    :profile-id="username"
                    extra-class="text-truncate ms-2 me-1"
                    fullname
                    link-style
                    small-font-size
                    username-class />
                </v-list-item-subtitle>
              </v-list-item-content>
              <v-list-item-action class="my-0">
                <v-tooltip bottom>
                  <template #activator="{ on, attrs }">
                    <v-btn
                      icon
                      v-bind="attrs"
                      v-on="on"
                      @click="resetAudienceChoice()">
                      <v-icon size="16">
                        fas fa-redo
                      </v-icon>
                    </v-btn>
                  </template>
                  <span>
                    {{ $t('activity.composer.audience.reset.tooltip') }}
                  </span>
                </v-tooltip>
              </v-list-item-action>
            </v-list-item>
          </div>
        </div>
        <v-card-text>
          <rich-editor
            v-if="drawer"
            ref="activityContent"
            v-model="message"
            :max-length="MESSAGE_MAX_LENGTH"
            :template-params="templateParams"
            :placeholder="composerPlaceholder"
            :object-type="metadataObjectType"
            :object-id="metadataObjectId"
            :max-file-size="$root.maxFileSize"
            :suggester-space-id="spaceId"
            :activity-id="activityId"
            :linkable-images="false"
            class="activityRichEditor"
            ck-editor-type="activityContent"
            context-name="activityComposer"
            use-extra-plugins
            use-draft-management
            content-link-enabled
            autofocus
            @attachments-edited="attachmentsEdit" />
        </v-card-text>
        <v-card-actions class="d-flex px-4">
          <extension-registry-components
            :params="extensionParams"
            name="ActivityComposerAction"
            type="activity-composer-action" />
        </v-card-actions>
        <v-divider class="mx-4 my-5" />
        <extension-registry-components
          v-if="!activityId"
          name="ComposerAction"
          type="composer-action-item" />
        <extension-registry-components
          v-if="!activityId"
          :params="extensionParams"
          name="ActivityComposerFooterAction"
          type="activity-composer-footer-action" />
        <category-input
          v-if="allowFilteringPerCategory"
          v-model="selectedCategoryIds"
          :filter-preselection="filterPreselection"
          class="mx-4 mt-5 mb-4">
          <template #label>
            <div class="d-flex flex-column flex-grow-1 flex-shrink-1 text-truncate">
              <div class="text-body font-weight-bold">
                {{ $t('activityStream.label.addCategories') }}
              </div>
              <div v-if="filteredCategoryIds?.length && !selectedCategoryIds?.length" class="text-subtitle">
                {{ $t('activityStream.label.mandatoryCategories') }}
              </div>
            </div>
          </template>
        </category-input>
      </v-card>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-menu
          v-model="scheduleMode"
          :close-on-content-click="false"
          content-class="activityComposerSchedulePopup elevation-2 overflow-visible"
          offset-y
          top
          left>
          <template #activator="{ attrs }">
            <div
              v-bind="attrs"
              class="d-flex">
              <v-btn
                id="activityComposerPostButton"
                :disabled="postDisabled"
                :loading="loading"
                :aria-label="$t(`activity.composer.${composerAction}`)"
                type="button"
                class="primary btn no-box-shadow ms-auto"
                @click="postMessage">
                {{ composerActionLabel }}
              </v-btn>
              <v-menu
                v-if="!activityId || isScheduledActivity"
                v-model="scheduleMenu"
                offset-y
                top
                left>
                <template #activator="{ on, attrs: menuAttrs }">
                  <v-btn
                    id="activityComposerScheduleMenuButton"
                    :disabled="postDisabled"
                    :aria-label="$t('activity.composer.schedule.openMenu')"
                    min-width="28"
                    type="button"
                    class="primary btn no-box-shadow px-0 ms-1"
                    v-bind="menuAttrs"
                    v-on="on">
                    <v-icon size="16">fas fa-caret-down</v-icon>
                  </v-btn>
                </template>
                <v-list
                  class="pa-0"
                  dense>
                  <v-list-item
                    id="activityComposerScheduleAction"
                    :aria-label="$t('activity.composer.schedule')"
                    class="px-2"
                    @click="openScheduleMode">
                    <v-list-item-icon
                      class="me-0 my-auto">
                      <v-icon size="16">fas fa-clock</v-icon>
                    </v-list-item-icon>
                    <v-list-item-title>
                      {{ $t('activity.composer.schedule') }}
                    </v-list-item-title>
                  </v-list-item>
                </v-list>
              </v-menu>
            </div>
          </template>
          <v-card
            id="activityComposerScheduleInputs"
            class="d-flex align-center flex-nowrap pa-2"
            flat>
            <date-picker
              v-model="scheduledDate"
              :attach="false"
              :min-value="minScheduleDate"
              :aria-label="$t('activity.composer.schedule.date')"
              class="flex-grow-0 me-2"
              top
              return-iso
              required />
            <div class="d-flex ms-n4">
              <time-picker
                v-model="scheduledHour"
                :min="minScheduleHour"
                :aria-label="$t('activity.composer.schedule.hour')"
                class="flex-grow-0 me-3" />
            </div>
            <v-btn
              id="activityComposerScheduleConfirmButton"
              :disabled="postDisabled || !scheduledDateTime"
              :loading="loading"
              :aria-label="$t('activity.composer.schedule.confirm')"
              icon
              @click="scheduleMessage">
              <v-icon
                size="20"
                class="success--text">
                fas fa-check
              </v-icon>
            </v-btn>
          </v-card>
        </v-menu>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data() {
    return {
      MESSAGE_MAX_LENGTH: 1300,
      MESSAGE_TIMEOUT: 5000,
      activityId: null,
      message: '',
      files: null,
      templateParams: {},
      singleton: true,
      drawer: false,
      activityBodyEdited: false,
      activityAttachmentsEdited: false,
      originalBody: '',
      messageEdited: false,
      activityType: null,
      loading: false,
      attachments: null,
      activityToolbarAction: false,
      audienceChoice: eXo.env.portal.postToNetworkEnabled && 'yourNetwork' ||  'oneOfYourSpaces',
      spaceIdentity: null,
      spaceId: eXo.env.portal.spaceId,
      username: eXo.env.portal.userName,
      allowFilteringPerCategory: null,
      isFilteredStream: false,
      filterPreselection: false,
      filteredCategoryIds: [],
      selectedCategoryIds: [],
      scheduleMenu: false,
      scheduleMode: false,
      scheduledDate: null,
      scheduledHour: null,
      pendingScheduledTime: null,
      publicationStartTime: null,
    };
  },
  computed: {
    postToNetwork() {
      return eXo.env.portal.postToNetworkEnabled && (!this.$root.settings || this.$root.settings.allowPostToNetwork);
    },
    composerPlaceholder() {
      return this.$t('activity.composer.placeholder');
    },
    composerAction() {
      // The post CTA of a still scheduled activity posts it immediately,
      // cancelling the scheduling
      return this.activityId && !this.isScheduledActivity && 'update' || 'post';
    },
    isScheduledActivity() {
      return !!this.activityId && !!this.publicationStartTime;
    },
    composerActionLabel() {
      return this.$t(`activity.composer.${this.composerAction}`);
    },
    extensionParams() {
      return {
        activityId: this.activityId,
        spaceId: this.spaceId,
        files: this.files,
        templateParams: this.templateParams,
        message: this.message,
        maxMessageLength: this.MESSAGE_MAX_LENGTH,
        activityType: this.activityType
      };
    },
    messageLength(){
      return this.message && this.message.length && this.$utils.htmlToText(this.message).length || 0;
    },
    ckEditorInstance() {
      return this.drawer && this.$refs.activityContent || null;
    },
    postDisabled() {
      return (!this.messageLength && !this.activityBodyEdited && !this.activityAttachmentsEdited)
          || this.messageLength > this.MESSAGE_MAX_LENGTH
          || this.loading
          || (!!this.activityId && !this.activityBodyEdited && !this.activityAttachmentsEdited && !this.isScheduledActivity)
          || (!this.activityAttachmentsEdited && !this.messageLength && !this.activityBodyEdited)
          || (this.postInYourSpacesChoice && !(this.spaceId || this.activityType?.toString()?.includes('poll') && eXo.env.portal.spaceId))
          || (!this.postToNetwork && !eXo.env.portal.spaceId && !this.spaceId && !this.messageEdited)
          || (this.allowFilteringPerCategory && this.isFilteredStream && !this.selectedCategoryIds?.length);
    },
    metadataObjectId() {
      return this.templateParams?.metadataObjectId || this.activityId;
    },
    metadataObjectType() {
      return this.templateParams?.metadataObjectType || 'activity';
    },
    enabled() {
      return eXo.env.portal.editorAttachImageEnabled && this.metadataObjectType?.length && eXo.env.portal.attachmentObjectTypes?.indexOf(this.metadataObjectType) >= 0;
    },
    spaceSuggesterLabels() {
      return {
        placeholder: this.$t('activity.composer.audience.placeholder'),
        noDataLabel: this.$t('activity.composer.audience.noDataLabel'),
      };
    },
    audienceTypesDisplay() {
      return !eXo.env.portal.spaceId;
    },
    postInYourSpacesChoice() {
      return this.audienceChoice === 'oneOfYourSpaces';
    },
    postInYourNetwork() {
      return this.audienceChoice === 'yourNetwork';
    },
    spaceSuggesterDisplay() {
      return (this.postToNetwork && this.postInYourSpacesChoice && !this.spaceId) || !this.postToNetwork ;
    },
    audienceAvatarDisplay() {
      return this.spaceId && this.postInYourSpacesChoice;
    },
    postVisibility() {
      return  this.postInYourNetwork || (this.postInYourSpacesChoice && !this.spaceId);
    },
    minScheduleDate() {
      // User local date, not the UTC one that toISOString would give
      return this.$dateUtil.getISODate(new Date());
    },
    minScheduleHour() {
      // Restrict selectable hours only when the selected date is today, with
      // a one minute margin so the snapped slot is always in the future
      return this.scheduledDate === this.minScheduleDate && new Date(Date.now() + 60000) || null;
    },
    scheduledDateTime() {
      if (!this.scheduledDate || !this.scheduledHour?.getHours) {
        return null;
      }
      const dateTime = this.$dateUtil.getDateObjectFromString(this.scheduledDate, true);
      dateTime.setHours(this.scheduledHour.getHours(), this.scheduledHour.getMinutes(), 0, 0);
      return dateTime.getTime();
    }
  },
  watch: {
    message(newVal, oldVal) {
      // Do not compute again this.activityBodyEdited if it's made true
      // once, else, this can lead to performances issue when editing
      // An activity
      if (this.activityId && !this.activityBodyEdited) {
        this.activityBodyEdited = this.$utils.htmlToText(newVal) !== this.$utils.htmlToText(oldVal);
        this.messageEdited = this.$utils.htmlToText(newVal) !== this.$utils.htmlToText(this.originalBody);
      }
    },
    drawer() {
      if (this.drawer) {
        document.dispatchEvent(new CustomEvent('activity-composer-opened'));
      } else {
        document.dispatchEvent(new CustomEvent('activity-composer-closed'));
      }
    },
    spaceIdentity() {
      if (!this.activityId) {
        this.spaceId = this.spaceIdentity?.spaceId || eXo.env.portal.spaceId;
      }
    },
    audienceChoice(newVal) {
      if (newVal === 'yourNetwork') {
        this.removeAudience();
      }
    }
  },
  created() {
    document.addEventListener('activity-composer-drawer-open', this.open);
    document.addEventListener('activity-composer-edited', this.isActivityBodyEdited);
    document.addEventListener('activity-composer-closed', this.close);
    document.addEventListener('activity-created', this.clearActivityMessage);
    document.addEventListener('activity-updated', this.clearActivityMessage);
  },
  mounted() {
    if (document.querySelectorAll('#activityComposerDrawer').length > 1) {
      this.singleton = false;
      this.clearListeners();
    }
  },
  beforeDestroy() {
    this.clearListeners();
  },
  methods: {
    clearListeners() {
      document.removeEventListener('activity-composer-drawer-open', this.open);
      document.removeEventListener('activity-composer-edited', this.isActivityBodyEdited);
      document.removeEventListener('activity-composer-closed', this.close);
      document.removeEventListener('activity-created', this.clearActivityMessage);
      document.removeEventListener('activity-updated', this.clearActivityMessage);
    },
    isActivityBodyEdited(event) {
      if (this.drawer) {
        this.activityBodyEdited = (this.messageEdited && this.messageLength) || event.detail !== 0 || (event.detail === 0 && this.messageLength);
      }
    },
    attachmentsEdit(attachments, changed) {
      this.attachments = attachments;
      // When creating a post, an attachment change with an empty result
      // (no attachment left) must not enable posting an empty activity
      this.activityAttachmentsEdited = changed && (!!attachments?.length || !!this.activityId);
    },
    open(params) {
      params = params && params.detail;
      if (params) {
        this.message = params.activityBody;
        this.originalBody = params.activityBody;
        this.activityId = params.activityId;
        this.spaceId = params.spaceId;
        this.templateParams = params.activityParams || params.templateParams || {};
        this.files = params.files || [];
        this.activityType = params.activityType;
        this.attachments = this.templateParams?.metadatas?.attachments;
        this.activityToolbarAction = params.activityToolbarAction;
        this.publicationStartTime = params.publicationStartTime || null;
      } else {
        this.activityId = null;
        this.message = '';
        this.templateParams = {};
        this.files = [];
        this.activityType = [];
        this.attachments = null;
        this.publicationStartTime = null;
      }
      this.allowFilteringPerCategory = !params?.activityId && params?.allowFilteringPerCategory || false;
      this.isFilteredStream = !params?.activityId && params?.isFilteredStream || false;
      this.filteredCategoryIds = !params?.activityId && params?.filteredCategoryIds;
      if (this.allowFilteringPerCategory && this.filteredCategoryIds?.length) {
        this.filterPreselection = true;
        this.selectedCategoryIds = this.filteredCategoryIds.slice();
      } else {
        this.filterPreselection = false;
        this.selectedCategoryIds = [];
      }
      this.scheduleMode = false;
      this.scheduledDate = null;
      this.scheduledHour = null;
      this.pendingScheduledTime = null;
      this.$nextTick().then(() => {
        this.activityBodyEdited = false;
        this.activityAttachmentsEdited = false;
        this.messageEdited = false;
        this.loading = false;
        this.$refs.activityComposerDrawer.open();
        document.dispatchEvent(new CustomEvent('message-composer-opened'));
      });
    },
    openScheduleMode() {
      let defaultSchedule;
      if (this.isScheduledActivity) {
        // Review the previously set scheduling of the edited activity
        defaultSchedule = new Date(this.publicationStartTime);
      } else {
        defaultSchedule = new Date();
        defaultSchedule.setDate(defaultSchedule.getDate() + 1);
        defaultSchedule.setHours(8, 0, 0, 0);
      }
      this.scheduledHour = new Date(defaultSchedule.getTime());
      this.scheduledDate = this.$dateUtil.getISODate(defaultSchedule);
      this.scheduleMode = true;
    },
    scheduleMessage() {
      if (!this.scheduledDateTime || this.scheduledDateTime <= Date.now()) {
        this.$root.$emit('alert-message', this.$t('activity.composer.schedule.mustBeInFuture'), 'warning');
        return;
      }
      this.pendingScheduledTime = this.scheduledDateTime;
      this.postMessage();
    },
    close() {
      if (this.ckEditorInstance) {
        this.ckEditorInstance.unload();
      }
      this.$nextTick().then(() => {
        this.$refs.activityComposerDrawer.close();
        this.$root.$emit('message-composer-closed');
      });
    },
    postMessage() {
      if (this.loading) {
        return;
      }
      this.loading = true;
      // Using a ref to the editor component and the getMessage method is mandatory to
      // be sure to get the most up to date value of the message
      const message = this.ckEditorInstance.getMessage();
      if (this.activityId) {
        let activityType = this.activityType;
        if (this.templateParams && this.templateParams.link && !this.activityType) {
          activityType = 'LINK_ACTIVITY';
        } else if (this.templateParams && this.templateParams.link === '-') {
          activityType = null;
        }
        const scheduledTime = this.pendingScheduledTime;
        this.$activityService.updateActivity(this.activityId, message, activityType, this.files, this.templateParams, scheduledTime)
          .then(this.postUpdateMessage)
          .then(() => this.ckEditorInstance && this.ckEditorInstance.saveAttachments())
          .then(() => {
            if (this.isScheduledActivity && !scheduledTime) {
              // Post CTA on a still scheduled activity: publish it
              // immediately, which cancels the scheduling
              return this.$activityService.publishActivity(this.activityId);
            }
          })
          .then(() => {
            document.dispatchEvent(new CustomEvent('activity-updated', {detail: this.activityId}));
            if (scheduledTime) {
              this.$root.$emit('alert-message', this.$t('activity.composer.schedule.success'), 'success');
            }
            this.close();
          })
          .catch(error => {
            // eslint-disable-next-line no-console
            console.error('Error when updating the activity', error);
            this.$root.$emit('alert-message', this.$t('activityStream.errorCreatingActivity'), 'error');
          })
          .finally(() => {
            this.loading = false;
            this.pendingScheduledTime = null;
          });
      } else {
        let activityType = this.activityType;
        if (this.templateParams && this.templateParams.link && !this.activityType) {
          activityType = 'LINK_ACTIVITY';
        }
        if (this.activityType && this.activityType.length !== 0) {
          if (this.activityToolbarAction) {
            document.dispatchEvent(new CustomEvent('post-activity-toolbar-action', {detail: message}));
          } else {
            document.dispatchEvent(new CustomEvent('post-activity', {detail: message}));
          }
        } else {
          if (!this.spaceId && !!eXo.env.portal.spaceId) {
            this.spaceId = eXo.env.portal.spaceId;
          }
          const scheduledTime = this.pendingScheduledTime;
          this.$activityService.createActivity(message, activityType, this.files, this.spaceId, this.templateParams, scheduledTime)
            .then(activity => {
              this.activityId = activity.id;
              this.templateParams = activity.templateParams;
              return this.$nextTick().then(() => activity);
            })
            .then(this.postSaveMessage)
            .then(() => this.ckEditorInstance && this.ckEditorInstance.saveAttachments())
            .then(() => {
              if (this.selectedCategoryIds?.length) {
                return this.$categoryLinkService.updateCategories({
                  objectType: 'activity',
                  objectId: this.activityId,
                  spaceId: this.spaceId,
                  oldCategories: [],
                  newCategories: this.selectedCategoryIds,
                });
              }
            })
            .then(() => {
              document.dispatchEvent(new CustomEvent('activity-created', {detail: this.activityId}));
              if (scheduledTime) {
                this.$root.$emit('alert-message', this.$t('activity.composer.schedule.success'), 'success');
              }
              this.resetAudienceChoice();
              this.close();
            })
            .catch(error => {
              // eslint-disable-next-line no-console
              console.error('Error when posting message', error);
              this.$root.$emit('alert-message', this.$t('activityStream.errorUpdatingActivity'), 'error');
            })
            .finally(() => {
              this.loading = false;
              this.pendingScheduledTime = null;
            });
        }
      }
    },
    postSaveMessage(activity) {
      const postSaveOperations = extensionRegistry.loadExtensions('activity', 'saveAction');
      if (postSaveOperations?.length) {
        const promises = [];
        postSaveOperations.forEach(extension => {
          if (extension.postSave) {
            const result = extension.postSave(activity);
            if (result?.then) {
              promises.push(result);
            }
          }
        });
        return Promise.all(promises).then(() => activity);
      } else {
        return Promise.resolve(activity);
      }
    },
    postUpdateMessage(activity) {
      const postUpdateOperations = extensionRegistry.loadExtensions('activity', 'updateAction');
      if (postUpdateOperations?.length) {
        const promises = [];
        postUpdateOperations.forEach(extension => {
          if (extension.postUpdate) {
            const result = extension.postUpdate(activity);
            if (result?.then) {
              promises.push(result);
            }
          }
        });
        return Promise.all(promises).then(() => activity);
      } else {
        return Promise.resolve(activity);
      }
    },
    clearActivityMessage() {
      if (localStorage.getItem('activity-message-activityComposer')) {
        localStorage.removeItem('activity-message-activityComposer');
      }
    },
    resetAudienceChoice() {
      this.audienceChoice = eXo.env.portal.postToNetworkEnabled && 'yourNetwork' || 'oneOfYourSpaces';
      this.spaceIdentity = null;
    },
    removeAudience() {
      this.spaceIdentity = null;
    }
  },
};
</script>
