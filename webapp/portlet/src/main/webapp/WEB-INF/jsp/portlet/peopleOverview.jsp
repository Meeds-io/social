<%@page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@page import="java.util.Locale"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="java.util.ResourceBundle"%>
<%
  ResourceBundle bundle;
  try {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.social.PeopleOverview", request.getLocale());
  } catch (Exception e) {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.social.PeopleOverview", Locale.ENGLISH);
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="PeopleOverview">
    <div class="flex position-relative v-application--wrap">
      <div role="progressbar" aria-valuemin="0" aria-valuemax="100" class="v-progress-linear v-progress-linear--rounded theme--light app-cached-content ${cacheId}-cached-content" style="height: 1px; position: absolute; z-index: 1">
        <div class="v-progress-linear__indeterminate v-progress-linear__indeterminate--active">
          <div class="v-progress-linear__indeterminate short primary"></div>
        </div>
      </div>
      <div
        class="pa-4 text-center v-card v-card--flat v-sheet theme--light">
        <div
          class="border-box-sizing d-flex flex-row justify-center ma-0 v-card v-card--flat v-sheet theme--light">
          <div class="peopleOverviewCard d-flex flex-column">
            <div class="ma-auto">
              <div class="peopleOverviewCount text-center pb-1">-</div>
              <div class="peopleOverviewTitle text-center text-truncate pt-1">
                <%=bundle.getString("peopleOverview.label.invitations")%>
              </div>
            </div>
          </div>
          <hr role="separator" aria-orientation="vertical"
            class="peopleOverviewVertivalSeparator ma-auto v-divider v-divider--vertical theme--light">
          <div class="peopleOverviewCard d-flex flex-column clickable">
            <div class="ma-auto">
              <div class="peopleOverviewCount text-center pb-1">-</div>
              <div class="peopleOverviewTitle text-center text-truncate pt-1">
                <%=bundle.getString("peopleOverview.label.pending")%>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>