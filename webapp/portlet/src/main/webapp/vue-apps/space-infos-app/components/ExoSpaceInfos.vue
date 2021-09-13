<template>
  <div id="spaceInfosApp" class="uiBox">
    <h5 class="center">{{ $t("social.space.description.title") }}</h5>
    <p id="spaceDescription">{{ description }}</p>
    <div id="spaceManagersList">
      <h5>{{ $t("social.space.description.managers") }}</h5>
      <ul id="spaceManagers">
        <li
          v-for="manager in managers"
          :key="manager"
          class="entry">
          <a :href="`${profileUrl}${manager.username}`">
            <v-avatar :size="30">
              <img
                :src="manager.avatar"
                alt="avatar"
                class="object-fit-cover ma-auto"
                loading="lazy">
            </v-avatar>
            {{ manager.fullname }}
          </a>
        </li>
      </ul>
    </div>
    <div v-if="redactors && redactors.length" id="spaceRedactorsList">
      <h5>{{ $t("social.space.description.redactors") }}</h5>
      <ul id="spaceRedactors">
        <li
          v-for="redactor in redactors"
          :key="redactor"
          class="entry">
          <a :href="`${profileUrl}${redactor.username}`">
            <v-avatar :size="30">
              <img
                :src="redactor.avatar"
                alt="avatar"
                class="object-fit-cover ma-auto"
                loading="lazy">
            </v-avatar>
            {{ redactor.fullname }}
          </a>
        </li>
      </ul>
    </div>
  </div>
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
            this.initPopup();
          });
      }
    },
    initPopup() {
      const restUrl = `${eXo.env.portal.context}/${eXo.env.portal.rest}/social/people/getPeopleInfo/{0}.json`;
      const labels = {
        youHaveSentAnInvitation: this.$t('message.label'),
        StatusTitle: this.$t('Loading.label'),
        Connect: this.$t('Connect.label'),
        Confirm: this.$t('Confirm.label'),
        CancelRequest: this.$t('CancelRequest.label'),
        RemoveConnection: this.$t('RemoveConnection.label'),
        Ignore: this.$t('Ignore.label')
      };
      $('#spaceManagers, #spaceRedactors').find('a').each(function (idx, el) {
        $(el).userPopup({
          restURL: restUrl,
          labels: labels,
          content: false,
          defaultPosition: 'left',
          keepAlive: true,
          maxWidth: '240px'
        });
      });
    }
  }
};
</script>
