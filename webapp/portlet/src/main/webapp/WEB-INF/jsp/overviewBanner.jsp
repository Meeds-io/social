<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects /> 
<%
  PortletPreferences preferences = renderRequest.getPreferences();
  String bannerUrl = renderRequest.getParameter("bannerUrl");
  String captionClass = "white--text mt-n1";
  String titleClass = "white--text" ;
  String bannerOpacity = "rgba(0, 0, 0, 0.5)";
  if (bannerUrl == null) {
    bannerUrl = preferences.getValue("bannerUrl", "");
    if (bannerUrl == null || bannerUrl.isEmpty()) {
      bannerOpacity = "rgb(213 213 213 / 50%)";
      bannerUrl = "/social-portlet/images/overviewBanner.webp";
    }
  }
  String bannerTitle = renderRequest.getParameter("bannerTitle");
  if (bannerTitle == null) {
    bannerTitle = preferences.getValue("bannerTitle", "");
    if (bannerTitle == null || bannerTitle.isEmpty()) {
      titleClass = "";
      bannerTitle = "change the title";
    }
  }
  String bannerCaption = renderRequest.getParameter("bannerCaption");
  if (bannerCaption == null) {
    bannerCaption = preferences.getValue("bannerCaption", "");
    if (bannerCaption == null || bannerCaption.isEmpty()) {
      captionClass = "mt-n1";
      bannerCaption = "change the caption";
    }
  }
  ResourceBundle bundle;
  String title = "";
  String caption = "";
  try {
    bundle = ExoContainerContext.getService(ResourceBundleService.class)
                                .getResourceBundle("locale.navigation.portal.meeds", request.getLocale());
    title = bundle.getString(bannerTitle);
    caption = bundle.getString(bannerCaption);
  } catch (Exception e) {
    try {
      bundle = ExoContainerContext.getService(ResourceBundleService.class)
                                  .getResourceBundle("locale.navigation.portal.meeds", Locale.ENGLISH);
      title = bundle.getString(bannerTitle);
      caption = bundle.getString(bannerCaption);
    } catch (Exception e1) {
      title = bannerTitle;
      caption = bannerCaption;
    }
  }
%>
<div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="OverviewPage">
        <div class="v-application--wrap">
          <div class="flex hiddenable-widget d-flex xs12 sm12">
            <div class="layout row wrap mx-0">

                <div 
                class="justify-content-center mx-auto mt-2" 
                style="background-image: url(<%=bannerUrl%>);
                       height:142px;
                       width:1150px">
                        <div 
                          class="d-flex justify-content-center flex-column text-center"
                          style="background-color: <%=bannerOpacity%>; height:100%; width:100%">
                          <h1 class=" mt-7">
                            <strong class="<%=titleClass%>">
                              <%=title%>
                            </strong>
                          </h1>
                          <h3 class="<%=captionClass%>"><%=caption%></h3>
                        </div>
                </div>

            </div>
          </div>
        </div>
    </div>
</div>
