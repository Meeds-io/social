<template>
  <v-app>
    <widget-wrapper :title="$t('social.space.description.title')">
      <p 
        id="spaceDescription"
        :class="description?.length && 'mb-4' || 'mb-0'"
        class="text-color"> 
        {{ description }} 
      </p>
      <div id="spaceManagersList">
        <div class="d-flex align-center mb-4">
          <v-icon size="16" class="me-2 icon-default-color">fa-user-cog</v-icon>
          <span class="me-2 subtitle-1 text-color">{{ $t("social.space.description.managers") }}</span>
          <v-divider />
        </div>
        <div id="spaceManagers">
          <exo-user-avatar
            v-for="manager in managers"
            :key="manager"
            :identity="manager"
            :size="30"
            :extra-class="'my-2'"
            :popover-left-position="true"
            :offset-x="true"
            :offset-y="false"
            popover
            link-style />
        </div>
      </div>
      <div
        v-if="redactors?.length"
        id="spaceRedactorsList">
        <div class="d-flex align-center mb-4 mt-5">
          <v-icon size="16" class="me-2 icon-default-color">fa-user-edit</v-icon>
          <span class="me-2 subtitle-1 text-color">{{ $t("social.space.description.redactors") }}</span>
          <v-divider />
        </div>
        <div id="spaceRedactors">
          <exo-user-avatar
            v-for="redactor in redactors"
            :key="redactor"
            :ref="redactor.id"
            :identity="redactor"
            :size="30"
            :extra-class="'my-2'"
            :popover-left-position="true"
            popover
            link-style />
        </div>
      </div>
    </widget-wrapper>
  </v-app>
</template>

<script>
export default {
  data() {
    return {
      description: '',
      managers: [],
      redactors: [],
      profileUrl: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/`
    };
  },
  created() {
    Promise.resolve(this.init(eXo.env.portal.spaceId))
      .finally(() => this.$root.$applicationLoaded());
  },
  methods: {
    init(spaceId) {
      if (spaceId) {
        return this.$spaceService.getSpaceById(spaceId, 'managers,redactors')
          .then(space => {
            if (space) {
              this.description = space.description || '';
              this.managers = space.managers && space.managers.filter(manager => manager.enabled && !manager.deleted) || [];
              this.redactors = space.redactors && space.redactors.filter(redactor => redactor.enabled && !redactor.deleted) || [];
            }
            return this.$nextTick();
          })
          .then(() => {
            this.$root.$emit('application-loaded');
          });
      }
    },
  }
};
</script>
