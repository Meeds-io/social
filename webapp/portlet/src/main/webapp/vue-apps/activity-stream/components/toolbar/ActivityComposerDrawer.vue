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
    v-model="drawer"
    disable-pull-to-refresh
    right
    fixed>
    <template #title>
      {{ $t('activity.composer.title') }}
    </template>
    <template #content>
      <v-card flat>
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
            class="activityRichEditor"
            ck-editor-type="activityContent"
            context-name="activityComposer"
            use-extra-plugins
            use-draft-management
            autofocus
            @attachments-edited="attachmentsEdit" />
        </v-card-text>
        <v-card-actions class="d-flex px-4">
          <extension-registry-components
            :params="extensionParams"
            name="ActivityComposerAction"
            type="activity-composer-action" />
        </v-card-actions>
        <v-card-text class="composerActions">
          <extension-registry-components
            v-if="!activityId"
            :params="extensionParams"
            name="ActivityComposerFooterAction"
            type="activity-composer-footer-action">
            <template #header>
              <v-divider class="mb-6" />
            </template>
          </extension-registry-components>
        </v-card-text>
      </v-card>
      <extension-registry-components
        :params="dropFilesParams"
        name="ActivityComposerDropZone"
        type="activity-composer-drop-zone" />
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          id="activityComposerPostButton"
          :disabled="postDisabled"
          :loading="loading"
          :aria-label="$t(`activity.composer.${composerAction}`)"
          type="button"
          class="primary btn no-box-shadow ms-auto"
          @click="postMessage()">
          {{ composerActionLabel }}
        </v-btn>
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
      spaceId: null,
      message: '',
      files: null,
      templateParams: {},
      drawer: false,
      activityBodyEdited: false,
      activityAttachmentsEdited: false,
      originalBody: '',
      messageEdited: false,
      activityType: null,
      loading: false,
      attachments: null,
      component: null
    };
  },
  computed: {
    composerPlaceholder() {
      return this.$t('activity.composer.placeholder');
    },
    composerAction() {
      return this.activityId && 'update' || 'post';
    },
    composerActionLabel() {
      return this.$t(`activity.composer.${this.composerAction}`);
    },
    composerButtonLabel() {
      if (eXo.env.portal.spaceDisplayName){
        return this.$t('activity.composer.link', {0: eXo.env.portal.spaceDisplayName});
      } else {
        return this.$t('activity.composer.post');
      }
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
      return (!this.messageLength && !this.activityBodyEdited && !this.activityAttachmentsEdited) || this.messageLength > this.MESSAGE_MAX_LENGTH || this.loading || (!!this.activityId && !this.activityBodyEdited && !this.attachments?.length);
    },
    metadataObjectId() {
      return this.templateParams?.metadataObjectId || this.activityId;
    },
    metadataObjectType() {
      return this.templateParams?.metadataObjectType || 'activity';
    },
    attachmentEnabled() {
      return eXo.env.portal.editorAttachImageEnabled && this.metadataObjectType?.length && eXo.env.portal.attachmentObjectTypes?.indexOf(this.metadataObjectType) >= 0;
    },
    dropFilesParams() {
      return {
        dropEnabled: this.attachmentEnabled
      };
    },
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
  },
  created() {
    document.addEventListener('activity-composer-drawer-open', this.open);
    document.addEventListener('activity-composer-edited', this.isActivityBodyEdited);
    document.addEventListener('activity-composer-closed', this.close);
  },
  methods: {
    isActivityBodyEdited(event) {
      this.activityBodyEdited = (this.messageEdited && this.messageLength) || event.detail !== 0 || (event.detail === 0 && this.messageLength);
    },
    attachmentsEdit(attachments) {
      this.attachments = attachments;
      this.activityAttachmentsEdited = true;
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
      } else {
        this.activityId = null;
        this.spaceId = null;
        this.message = '';
        this.templateParams = {};
        this.files = [];
        this.activityType = [];
      }
      this.$nextTick().then(() => {
        this.activityBodyEdited = false;
        this.messageEdited = false;
        this.$refs.activityComposerDrawer.open();
        document.dispatchEvent(new CustomEvent('message-composer-opened'));
        if (this.attachmentEnabled) {
          const dropZone = document.getElementById('activityComposerDrawer');
          this.dropFiles(dropZone);
        }
      });
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
        this.loading = true;
        this.$activityService.updateActivity(this.activityId, message, activityType, this.files, this.templateParams)
          .then(this.postUpdateMessage)
          .then(() => this.ckEditorInstance && this.ckEditorInstance.saveAttachments())
          .then(() => {
            document.dispatchEvent(new CustomEvent('activity-updated', {detail: this.activityId}));
            this.cleareActivityMessage();
            this.close();
          })
          .catch(error => {
            // eslint-disable-next-line no-console
            console.error(`Error when updating the activity: ${error}`);
            // TODO Display error Message
          })
          .finally(() => this.loading = false);
      } else {
        let activityType = this.activityType;
        if (this.templateParams && this.templateParams.link && !this.activityType) {
          activityType = 'LINK_ACTIVITY';
        }
        if (this.activityType && this.activityType.length !== 0) {
          document.dispatchEvent(new CustomEvent('post-activity', {detail: message}));
        } else {
          this.loading = true;
          this.$activityService.createActivity(message, activityType, this.files, eXo.env.portal.spaceId, this.templateParams)
            .then(activity => {
              this.activityId = activity.id;
              this.templateParams = activity.templateParams;
              return this.$nextTick().then(() => activity);
            })
            .then(this.postSaveMessage)
            .then(() => this.ckEditorInstance && this.ckEditorInstance.saveAttachments())
            .then(() => {
              document.dispatchEvent(new CustomEvent('activity-created', {detail: this.activityId}));
              this.cleareActivityMessage();
              this.close();
            })
            .catch(error => {
              // eslint-disable-next-line no-console
              console.error(`Error when posting message: ${error}`);
              // TODO Display error Message
            })
            .finally(() => this.loading = false);
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
    dropFiles(el) {
      const componentIsLoaded = extensionRegistry.loadComponents('ActivityComposerDropZone') || [];
      if (componentIsLoaded?.length) {
        const component = componentIsLoaded[0];
        if (component?.componentOptions?.dropFiles) {
          component.componentOptions.dropFiles(el);
        }
      }
      /*console.warn('componentIsLoaded', componentIsLoaded);
      const dropFilesOperations = extensionRegistry.loadExtensions('activity', 'dropFiles');
      if (dropFilesOperations?.length) {
        dropFilesOperations.forEach(extension => {
          if (extension.dropFiles) {
            extension.dropFiles(el);
          }
        });
      }*/
    },
    cleareActivityMessage() {
      if (localStorage.getItem('activity-message-activityComposer')) {
        localStorage.removeItem('activity-message-activityComposer');
      }
    },
  },
};
</script>
