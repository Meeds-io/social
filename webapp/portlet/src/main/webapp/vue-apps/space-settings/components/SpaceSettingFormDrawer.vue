<template>
  <div>
    <exo-drawer
      ref="spaceFormDrawer"
      v-model="drawer"
      right
      class="spaceFormDrawer">
      <template slot="title">
        {{ $t('SpaceSettings.general') }}
      </template>
      <template v-if="drawer" slot="content">
        <form
          ref="form"
          :disabled="savingSpace || spaceSaved"
          class="ma-4 d-flex row"
          @submit="saveSpace">
          <div class="width-full d-flex">
            <space-setting-avatar
              ref="spaceAvatar"
              :avatar-url="`${space.avatarUrl}&size=165x165`"
              :avatar-data="avatarData"
              hover
              class="mx-auto mb-6 mt-2"
              @edit="$refs.imageCropDrawer.open()" />
          </div>
          <v-label for="name">
            {{ $t('SpaceSettings.label.name') }}
          </v-label>
          <input
            v-model="space.displayName"
            :placeholder="$t('SpaceSettings.label.displayName')"
            type="text"
            name="name"
            class="input-block-level ignore-vuetify-classes my-3"
            maxlength="200"
            autofocus
            required>
          <v-label for="description">
            {{ $t('SpaceSettings.label.description') }}
          </v-label>
          <textarea
            v-model="space.description"
            :placeholder="$t('SpaceSettings.label.description')"
            name="description"
            rows="20"
            maxlength="2000"
            noresize
            class="input-block-level ignore-vuetify-classes my-3">
          </textarea>
          <v-label for="spaceTemplate">
            {{ $t('SpaceSettings.label.spaceTemplate') }}
          </v-label>
          <select
            v-model="template"
            name="spaceTemplate"
            class="input-block-level ignore-vuetify-classes my-3"
            disabled
            required>
            <option
              v-for="item in templates"
              :key="item.name"
              :value="item.name">
              {{ item.resolvedLabel || item.name }}
            </option>
          </select>
          <div class="caption font-italic font-weight-light ps-1 muted">{{ spaceTemplate && spaceTemplate.resolvedDescription || '' }}</div>
          <div class="d-flex flex-wrap pt-2">
            <label for="hidden" class="v-label theme--light my-auto float-left">
              {{ $t('SpaceSettings.label.hidden') }}
            </label>
            <v-switch
              v-model="space.visibility"
              true-value="hidden"
              false-value="private"
              class="float-left my-0 ms-4" />
          </div>
          <div class="caption font-italic font-weight-light ps-1 muted mb-2 mt-1">
            {{ $t(`SpaceSettings.description.${space.visibility || 'hidden'}`) }}
          </div>
          <div class="d-flex flex-wrap pt-2">
            <label for="hidden" class="v-label theme--light">
              {{ $t('SpaceSettings.label.registration') }}
            </label>
            <v-radio-group
              v-model="space.subscription"
              class="mt-2 ms-2"
              mandatory
              row
              inset>
              <v-radio
                :label="$t('SpaceSettings.label.open')"
                value="open"
                class="my-0" />
              <v-radio
                :label="$t('SpaceSettings.label.validation')"
                value="validation"
                class="my-0" />
              <v-radio
                :label="$t('SpaceSettings.label.closed')"
                value="closed"
                class="my-0" />
            </v-radio-group>
          </div>
          <div class="caption font-italic font-weight-light ps-1 muted">{{ $t(`SpaceSettings.description.${space.subscription || 'open'}`) }}</div>
        </form>
      </template>
      <template slot="footer">
        <div class="d-flex">
          <v-spacer />
          <v-btn
            :disabled="savingSpace || spaceSaved"
            class="btn me-2"
            @click="cancel">
            <template>
              {{ $t('SpaceSettings.button.cancel') }}
            </template>
          </v-btn>
          <v-btn
            :loading="savingSpace"
            :disabled="saveButtonDisabled"
            class="btn btn-primary"
            @click="saveSpace">
            <v-icon v-if="spaceSaved">mdi-check-all</v-icon>
            <template v-else>
              {{ $t('SpaceSettings.button.updateSpace') }}
            </template>
          </v-btn>
        </div>
      </template>
    </exo-drawer>
    <image-crop-drawer
      v-if="drawer"
      ref="imageCropDrawer"
      v-model="space.avatarId"
      :crop-options="cropOptions"
      :max-file-size="maxUploadSizeInBytes"
      :src="avatarData || `${space.avatarUrl}&size=0`"
      max-image-width="350"
      drawer-title="UIChangeAvatarContainer.label.ChangeAvatar"
      rounded
      @data="avatarData = $event" />
  </div>
</template>
<script>
export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    drawer: false,
    savingSpace: false,
    spaceSaved: false,
    space: {},
    spaceToUpdate: {},
    template: null,
    spaceTemplate: null,
    templates: [],
    avatarData: null,
    cropOptions: {
      aspectRatio: 1,
      viewMode: 1,
    },
  }),
  computed: {
    saveButtonDisabled() {
      return this.savingSpace || this.spaceSaved || !this.space.id;
    },
    maxUploadSizeInBytes() {
      return Number(this.maxUploadSize) * 1024 *1024;
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
  },
  mounted() {
    this.$spaceService.getSpaceTemplates()
      .then(data => {
        this.templates = data || [];
        this.spaceTemplate = this.templates.length && this.templates[0] || null;
        this.template = this.spaceTemplate && this.spaceTemplate.name;
        return this.$nextTick();
      });
  },
  methods: {
    open(spaceId) {
      this.savingSpace = false;
      this.spaceSaved = false;
      this.spaceSaved = false;
      this.template = null;
      this.spaceTemplate = null;
      this.avatarData = null;
      if (this.$refs.spaceAvatar) {
        this.$refs.spaceAvatar.reset();
      }
      this.space = {};
      this.$spaceService.getSpaceById(spaceId)
        .then(this.editSpace);
    },
    editSpace(space) {
      space = space.detail && space.detail.data || space;
      if (!space || !space.id) {
        // eslint-disable-next-line no-console
        console.warn('space does not have an id ', space, ' ignore user action');
        return;
      }
      this.spaceToUpdate = space;
      this.space = Object.assign({}, space);
      this.template = this.space.template;
      this.$refs.spaceFormDrawer.open();
    },
    cancel() {
      this.$refs.spaceFormDrawer.close();
    },
    handleImageUploadError(error) {
      if (error) {
        if (String(error).indexOf(this.$uploadService.avatarExcceedsLimitError) >= 0) {
          error = this.$t('SpaceSettings.label.avatarExcceededAllowedSize', {0: this.maxUploadSize});
        } else {
          error = String(error);
        }
        this.$root.$emit('alert-message', error, 'error');
      }
    },
    saveSpace(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      if (this.spaceSaved || this.savingSpace) {
        return;
      }
      this.error = null;
      this.savingSpace = true;
      this.$spaceService.updateSpace({
        id: this.space.id,
        displayName: this.space.displayName,
        description: this.space.description,
        visibility: this.space.visibility,
        subscription: this.space.subscription,
        avatarId: this.space.avatarId,
      })
        .then(space => {
          Object.assign(this.spaceToUpdate, space, {managers: this.spaceToUpdate.managers});
          this.spaceSaved = true;

          document.dispatchEvent(new CustomEvent('spaceDetailUpdated', {
            detail: this.spaceToUpdate,
          }));

          window.setTimeout(() => {
            this.$refs.spaceFormDrawer.close();
          }, 200);

          window.location.href = `${eXo.env.portal.context}/g/${space.groupId.replace(/\//g, ':')}/${space.prettyName}/settings`;
        })
        .catch(e => {
          // eslint-disable-next-line no-console
          console.warn('Error updating space ', this.space, e);
          if (String(e).indexOf('SPACE_ALREADY_EXIST') >= 0) {
            this.error = this.$t('SpaceSettings.error.spaceWithSameNameExists');
          } else if (String(e).indexOf('INVALID_SPACE_NAME') >= 0) {
            this.error = this.$t('spacesList.error.InvalidSpaceName');
          } else {
            this.error = this.$t('SpaceSettings.error.unknownErrorWhenSavingSpace');
          }
        })
        .finally(() => this.savingSpace = false);
    },
  },
};
</script>
