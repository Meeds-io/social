<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="java.util.Locale"%>
<%@page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@page import="java.util.ResourceBundle"%>
<%
  ResourceBundle bundle;
  try {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.Portlets", request.getLocale());
  } catch (Exception e) {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.Portlets", Locale.ENGLISH);
  }
  String tooltip = bundle.getString("Search.button.tooltip");
%>
<div class="VuetifyApp">
  <div
    id="favoritesListPortlet"
    data-app="true"
    class="v-application v-application--is-ltr theme--light">
    <div class="v-application--wrap d-none">
      <v-tooltip bottom>
        <template #activator="{on, attrs}">
          <v-btn
            v-on="on"
            v-bind="attrs"
            aria-label="<%=tooltip%>"
            icon
            class="transparent"
            @click="Vue.startApp('PORTLET/social/TopBarFavorites', 'init')">
            <v-icon size="20">fa-star</v-icon>
          </v-btn>
        </template>
        <span class="tooltip"><%=tooltip%></span>
      </v-tooltip>
    </div>
  </div>
  <script type="text/javascript">
    window.require(['SHARED/commonVueComponents', 'SHARED/vuetify'], () => {
      new Vue({
        el: '#favoritesListPortlet',
        vuetify: Vue.prototype.vuetifyOptions,
        mounted() {
          document.querySelector('#favoritesListPortlet .v-application--wrap').classList.remove('d-none');
        },
      });
    });
  </script>
</div>
