<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects /> 
<%
  PortletPreferences preferences = renderRequest.getPreferences();
  String bannerUrl = renderRequest.getParameter("bannerUrl");
  String captionClass = "white--text";
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
      bannerTitle = "overview.overviewBanner.titlePlaceHolder";
    }
  }
  String bannerCaption = renderRequest.getParameter("bannerCaption");
  if (bannerCaption == null) {
    bannerCaption = preferences.getValue("bannerCaption", "");
    if (bannerCaption == null || bannerCaption.isEmpty()) {
      captionClass = "";
      bannerCaption = "overview.overviewBanner.descriptionPlaceHolder";
    }
  }
  ResourceBundle bundle;
  String title = "";
  String caption = "";
  try {
    bundle = ExoContainerContext.getService(ResourceBundleService.class)
                                .getResourceBundle("locale.portlet.Portlets", request.getLocale());
    title = bundle.getString(bannerTitle);
  } catch (Exception e) {
    //Expected, when the title isn't translated to user locale
    title = bannerTitle;
  }
  try {
    bundle = ExoContainerContext.getService(ResourceBundleService.class)
                                .getResourceBundle("locale.portlet.Portlets", request.getLocale());
    caption = bundle.getString(bannerCaption);
  } catch (Exception e) {
    //Expected, when the caption isn't translated to user locale 
    caption = bannerCaption;
  }
%>
<div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="OverviewBanner">
        <div class="v-application--wrap">
          <div class="flex hiddenable-widget d-flex">
            <div class="layout">
                <div 
                class="justify-content-center"
                style="background-image: url(<%=bannerUrl%>);
                       background-repeat: repeat;
                       background-clip: content-box;
                       height: 133px;
                       width: 100%;">
                        <div 
                          class="d-flex justify-content-center flex-column text-center full-height"
                          style="background-color: <%=bannerOpacity%>; width:100%">
                          <h1 class=" mt-7 d-none d-sm-block">
                            <strong class="<%=titleClass%>">
                              <%=title%>
                            </strong>
                          </h1>
                          <h1 class=" mt-7 d-block d-sm-none text-h5">
                            <strong class="<%=titleClass%>">
                              <%=title%>
                            </strong>
                          </h1>
                          <h3 class="<%=captionClass%> mt-n1 d-none d-sm-flex justify-center"><%=caption%></h3>
                        </div>
                </div>
            </div>
          </div>
        </div>
    </div>
</div>
