<%@page import="org.exoplatform.social.core.model.GettingStartedStep"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page import="org.exoplatform.social.core.service.GettingStartedService"%>
<%
  String title = "Getting Started";
  ResourceBundle bundle;
  try {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.social.GettingStartedPortlet", request.getLocale());
    title = bundle.getString("locale.portlet.gettingStarted.title");
  } catch (Exception e) {
    bundle = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.portlet.social.GettingStartedPortlet", Locale.ENGLISH);
    title = bundle.getString("locale.portlet.gettingStarted.title");
  }

%>
<div class="VuetifyApp" id="GettingStartedPortletParent">
<%
  List<GettingStartedStep> steps = ExoContainerContext.getService(GettingStartedService.class).getUserSteps(request.getRemoteUser());
  if (steps != null && steps.size() > 0) {
    boolean canClose = true;
    for (int i = 0; i < steps.size(); i++) {
      GettingStartedStep step = steps.get(i);
      canClose = canClose && step.getStatus() != null && step.getStatus().booleanValue();
    }
%>
  <div style="" id="GettingStartedPortlet" data-can-close="<%=canClose%>">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light" id="app">
      <div class="v-application--wrap">
        <div class="flex hiddenable-widget application-body d-flex xs12 sm12">
          <div class="layout row wrap mx-0">
            <div class="flex d-flex xs12">
              <div class="flex v-card v-card--flat v-sheet card-border-radius pa-5 theme--light">
                <div
                  class="pb-5">
                  <span class="widget-text-header">
                    <%=title%>
                    <% if (canClose) { %>
                      <a title="Close" href="#" rel="tooltip" data-placement="bottom" class="btClose">x</a>
                    <% } %>
                  </span>
                </div>
                <div role="list"
                  class="v-list getting-started-list py-0 v-sheet theme--light v-list--dense">
                  <% 
                  for (int i = 0; i < steps.size(); i++) {
                    GettingStartedStep step = steps.get(i);
                    String stepTitle = bundle.getString("locale.portlet.gettingStarted.step." + step.getName());
                    if (step.getStatus() != null && step.getStatus().booleanValue()) { %>
                    <div tabindex="-1" role="listitem"
                      class="getting-started-list-item v-list-item theme--light px-0">
                      <div class="v-list-item__icon me-3 steps-icon">
                        <i class="UICheckIcon white--text"></i>
                      </div>
                      <div class="v-list-item__content pb-3">
                        <div class="v-list-item__title">
                          <span><%=stepTitle%> </span>
                        </div>
                      </div>
                    </div>
                  <% } else { %>
                    <div tabindex="-1" role="listitem"
                      class="getting-started-list-item v-list-item theme--light px-0">
                      <div class="v-list-item__icon me-3 steps-icon">
                        <span
                          class="step-number font-weight-bold text-center white--text"><span><%=i + 1%></span></span>
                      </div>
                      <div class="v-list-item__content pb-3 inactiveStep">
                        <div class="v-list-item__title">
                          <span><%=stepTitle%> </span>
                        </div>
                      </div>
                    </div>
                  <% }
                  }%>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
<% } %>
  <script type="text/javascript">
    window.require(['PORTLET/social-portlet/GettingStarted'], app => app.init());
  </script>
</div>