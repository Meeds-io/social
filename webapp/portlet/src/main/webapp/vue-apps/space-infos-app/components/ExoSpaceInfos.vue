<template>
  <v-app>
    <div>
      <div class="body-1 text-uppercase text-sub-title">{{ $t("social.space.description.title") }}</div>
      <p id="spaceDescription">{{ description }}</p>
      <div id="spaceManagersList" class="px-1">
        <h5>{{ $t("social.space.description.managers") }}</h5>
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
            bold-title
            link-style
            popover />
        </div>
      </div>
      <div
        v-if="redactors && redactors.length"
        id="spaceRedactorsList"
        class="px-1">
        <h5>{{ $t("social.space.description.redactors") }}</h5>
        <div id="spaceRedactors">
          <exo-user-avatar
            v-for="redactor in redactors"
            :key="redactor"
            :ref="redactor.id"
            :identity="redactor"
            :size="30"
            :extra-class="'my-2'"
            :popover-left-position="true"
            popover />
        </div>
      </div>
    </div>
  </v-app>
</template>

<script>
export default {
  data() {
    return {
      description: '',
      managers: [],
      redactors: [],
      profileUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/`
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
