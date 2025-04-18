<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="java.util.Locale"%>
<%@page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@page import="java.util.ResourceBundle"%>
<%
  ResourceBundle bundle;
  String tooltip;
  try {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.Portlets", request.getLocale());
    tooltip = bundle.getString("UITopBarFavoritesPortlet.label.iconTooltip");
  } catch (Exception e) {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.Portlets", Locale.ENGLISH);
    tooltip = bundle.getString("UITopBarFavoritesPortlet.label.iconTooltip");
  }
%>
<div class="VuetifyApp">
  <div
    id="favoritesListPortlet"
    data-app="true"
    class="v-application v-application--is-ltr theme--light">
    <div class="v-application--wrap">
      <button
        title="<%=tooltip%>"
        type="button"
        class="v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default"
        onclick="Vue.startApp('PORTLET/social/TopBarFavorites', 'init')">
        <span class="v-btn__content">
          <i
            aria-hidden="true"
            class="v-icon notranslate fa fa-star theme--light pb-1"
            style="font-size: 20px;"></i>
        </span>
      </button>
    </div>
    <script type="text/javascript">
      if (window.location.pathname?.endsWith?.('/search')) {
        window.require(['PORTLET/social/Search'], app => app.init());
      }
      window.require(['SHARED/commonVueComponents'], () => Vue.prototype.$utils.addShortcutsListener(['b'], () => {
        window.require(['PORTLET/social/TopBarFavorites'], app => {
          app.init();
          document.dispatchEvent(new CustomEvent('favorites-open'));
        });
      }));
    </script>
  </div>
</div>
