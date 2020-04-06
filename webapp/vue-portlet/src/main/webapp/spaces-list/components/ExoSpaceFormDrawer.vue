<template>
  <exo-drawer
    ref="spaceFormDrawer"
    right
    @opened="stepper = 1"
    @closed="stepper = 0">
    <template slot="title">
      <span class="subtitle-2 font-weight-bold text-truncate">
        {{ $t('spacesList.label.addNewSpace') }}
      </span>
    </template>
    <template slot="content">
      <v-stepper v-model="stepper" vertical flat>
        <v-stepper-step :complete="stepper > 1" step="1">
          {{ $t('spacesList.label.spaceDetails') }}
        </v-stepper-step>
        <v-stepper-content step="1">
          <form ref="form1" @submit="nextStep">
            <v-label for="name">
              {{ $t('spacesList.label.name') }}
            </v-label>
            <input
              ref="autoFocusInput1"
              v-model="space.displayName"
              :placeholder="$t('spacesList.label.displayName')"
              type="text"
              name="name"
              class="input-block-level ignore-vuetify-classes my-3"
              maxlength="200"
              required />
            <v-label for="description">
              {{ $t('spacesList.label.description') }}
            </v-label>
            <textarea
              v-model="space.description"
              :placeholder="$t('spacesList.label.description')"
              name="description"
              rows="20"
              maxlength="2000"
              class="input-block-level ignore-vuetify-classes my-3" >
            </textarea>
            <v-label for="spaceTemplate">
              {{ $t('spacesList.label.spaceTemplate') }}
            </v-label>
            <select
              v-model="template"
              name="spaceTemplate"
              class="input-block-level ignore-vuetify-classes my-3"
              required>
              <option
                v-for="item in templates"
                :key="item.name"
                :value="item.name">
                {{ item.resolvedLabel || item.name }}
              </option>
            </select>
            <div class="caption font-italic font-weight-light pl-1 muted">{{ spaceTemplate && spaceTemplate.resolvedDescription || '' }}</div>
            <v-card-actions class="px-0">
              <v-spacer />
              <v-btn class="btn btn-primary" @click="nextStep">
                {{ $t('spacesList.button.continue') }}
              </v-btn>
            </v-card-actions>
          </form>
        </v-stepper-content>
        <v-stepper-step :complete="stepper > 2" step="2">
          {{ $t('spacesList.label.spaceAccess') }}
        </v-stepper-step>
        <v-stepper-content step="2">
          <form ref="form2" @submit="nextStep">
            <div class="d-flex clearfix pt-2">
              <label for="hidden" class="v-label theme--light my-auto float-left">
                {{ $t('spacesList.label.hidden') }}
              </label>
              <v-switch
                ref="autoFocusInput2"
                v-model="space.visibility"
                true-value="hidden"
                false-value="private"
                class="float-left my-0 ml-4"
                inset />
            </div>
            <div class="caption font-italic font-weight-light pl-1 muted mb-2">{{ $t(`spacesList.description.${space.visibility}`) }}</div>
            <div class="d-flex clearfix py-2">
              <label for="hidden" class="v-label theme--light my-auto float-left">
                {{ $t('spacesList.label.registration') }}
              </label>
              <v-radio-group
                v-model="space.subscription"
                class="float-left my-0 ml-4"
                mandatory
                row
                inset>
                <v-radio :label="$t('spacesList.label.open')" value="open"></v-radio>
                <v-radio :label="$t('spacesList.label.validation')" value="validation"></v-radio>
                <v-radio :label="$t('spacesList.label.closed')" value="closed"></v-radio>
              </v-radio-group>
            </div>
            <div class="caption font-italic font-weight-light pl-1 muted">{{ $t(`spacesList.description.${space.subscription}`) }}</div>
            <v-card-actions class="mt-4">
              <v-btn class="btn" @click="previousStep">
                {{ $t('spacesList.button.back') }}
              </v-btn>
              <v-spacer />
              <v-btn class="btn btn-primary" @click="nextStep">
                {{ $t('spacesList.button.continue') }}
              </v-btn>
            </v-card-actions>
          </form>
        </v-stepper-content>
        <v-stepper-step :complete="stepper > 3" step="3">
          {{ $t('spacesList.label.inviteUsers') }}
        </v-stepper-step>
        <v-stepper-content step="3">
          <form
            ref="form3"
            :disabled="savingSpace || spaceSaved"
            @submit="saveSpace">
            <v-label for="inviteMembers">
              {{ $t('spacesList.label.users') }}
            </v-label>
            <exo-identity-suggester
              ref="autoFocusInput3"
              v-model="space.invitedMembers"
              :labels="suggesterLabels"
              :disabled="savingSpace || spaceSaved"
              name="inviteMembers"
              include-users
              include-spaces
              multiple />
            <v-card-actions class="mt-10">
              <v-btn
                :disabled="savingSpace || spaceSaved"
                class="btn"
                @click="previousStep">
                {{ $t('spacesList.button.back') }}
              </v-btn>
              <v-spacer />
              <v-btn
                :disabled="savingSpace || spaceSaved"
                class="btn"
                @click="cancel">
                <template>
                  {{ $t('spacesList.button.cancel') }}
                </template>
              </v-btn>
              <v-btn
                :loading="savingSpace"
                :disabled="savingSpace || spaceSaved"
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
            </v-card-actions>
            <v-alert v-if="error" type="error">
              {{ error }}
            </v-alert>
          </form>
        </v-stepper-content>
      </v-stepper>
    </template>
  </exo-drawer>
</template>
<script>
import * as spaceService from '../js/SpaceService.js'; 

export default {
  data: () => ({
    savingSpace: false,
    spaceSaved: false,
    space: {},
    spaceToUpdate: {},
    error: null,
    stepper: 0,
    template: null,
    spaceTemplate: null,
    templates: [],
  }),
  computed: {
    displayedForm() {
      return this.$refs && this.$refs[`form${this.stepper}`];
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('spacesList.label.inviteMembers'),
        noDataLabel: this.$t('spacesList.label.noDataLabel'),
      };
    }
  },
  watch: {
    stepper() {
      this.error = null;
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
        this.error = null;
      }
    },
    template() {
      if (this.template) {
        this.spaceTemplate = this.templates.find(temp => temp.name === this.template);
        if (this.spaceTemplate) {
          this.$set(this.space, 'template', this.template);
          this.$set(this.space, 'subscription', this.spaceTemplate.registration);
          this.$set(this.space, 'visibility', this.spaceTemplate.visibility);
          this.$set(this.space, 'invitedMembers', this.spaceTemplate.invitees && this.spaceTemplate.invitees.split(',') || []);
          if (this.space.invitedMembers && this.space.invitedMembers.length) {
            this.space.invitedMembers = this.space.invitedMembers.map(user => ({
              id: `organization:${user}`,
              providerId: 'organization',
              remoteId: user,
            }));
          }
        }
      }
    },
  },
  mounted() {
    this.$root.$on('addNewSpace', () => {
      this.spaceToUpdate = null;
      this.space = {};
      this.$refs.spaceFormDrawer.open();
    });
    this.$root.$on('editSpace', (space) => {
      if (!space || !space.id) {
        // eslint-disable-next-line no-console
        console.warn('space does not have an id ', space, ' ignore user action');
        return;
      }
      this.spaceToUpdate = space;
      this.space = Object.assign({}, space);
      this.$refs.spaceFormDrawer.open();
    });
    spaceService.getSpaceTemplates()
      .then(data => {
        this.templates = data || [];
        this.spaceTemplate = this.templates.length && this.templates[0] || null;
        this.template = this.spaceTemplate && this.spaceTemplate.name;
        return this.$nextTick();
      });
  },
  methods: {
    previousStep() {
      this.stepper--;
    },
    nextStep(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      if (this.displayedForm && this.displayedForm.reportValidity()) {
        this.stepper++;
      }
    },
    cancel() {
      this.$refs.spaceFormDrawer.close();
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
      if (this.space.id) {
        spaceService.updateSpace({
          id: this.space.id,
          displayName: this.space.displayName,
          description: this.space.description,
          template: this.space.template,
          visibility: this.space.visibility,
          subscription: this.space.subscription,
          invitedMembers: this.space.invitedMembers,
        })
          .then(space => {
            Object.assign(this.spaceToUpdate, space);
            this.spaceSaved = true;

            window.setTimeout(() => {
              this.$refs.spaceFormDrawer.close();
            }, 200);
          })
          .catch(e => {
            // eslint-disable-next-line no-console
            console.warn('Error updating space ', this.space, e);
            if (String(e).indexOf('SPACE_ALREADY_EXIST') >= 0) {
              this.error = this.$t('spacesList.error.spaceWithSameNameExists');
            } else {
              this.error = this.$t('spacesList.error.unknownErrorWhenSavingSpace');
            }
          })
          .finally(() => this.savingSpace = false);
      } else {
        spaceService.createSpace(this.space)
          .then(space => {
            this.spaceSaved = true;
            window.location.href = `${eXo.env.portal.context}/g/${space.groupId.replace(/\//g, ':')}`;
          })
          .catch(e => {
            // eslint-disable-next-line no-console
            console.warn('Error creating space ', this.space, e);
            if (String(e).indexOf('SPACE_ALREADY_EXIST') >= 0) {
              this.error = this.$t('spacesList.error.spaceWithSameNameExists');
            } else {
              this.error = this.$t('spacesList.error.unknownErrorWhenSavingSpace');
            }
          })
          .finally(() => this.savingSpace = false);
      }
    },
  },
};
</script>