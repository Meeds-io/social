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
	if (bannerUrl == null) {
		bannerUrl = preferences.getValue("bannerUrl", "");
		if (bannerUrl == null) {
			bannerUrl = "images/overviewBanner.webp";
		}
	}
  String bannerTitle = renderRequest.getParameter("bannerTitle");
	if (bannerTitle == null) {
		bannerTitle = preferences.getValue("bannerTitle", "");
		if (bannerTitle == null) {
			bannerTitle = "";
		}
	}
  String bannerCaption = renderRequest.getParameter("bannerCaption");
	if (bannerCaption == null) {
		bannerCaption = preferences.getValue("bannerCaption", "");
		if (bannerCaption == null) {
			bannerCaption = "";
		}
	}
  ResourceBundle bundle;
  String         title   = "";
  String         caption = "";
  
  try {
    bundle  = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.navigation.portal.meeds", request.getLocale());
    title   = bundle.getString(bannerTitle.toString());
    caption = bundle.getString(bannerCaption.toString());
  } catch (Exception e) {
    bundle  = ExoContainerContext.getService(ResourceBundleService.class).getResourceBundle("locale.navigation.portal.meeds", Locale.ENGLISH);
    title   = bundle.getString(bannerTitle.toString());
    caption = bundle.getString(bannerCaption.toString());
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
                          style="background-color: rgba(0, 0, 0, 0.5);height:100%;width:100%">
                          <h1 class=" mt-7">
                            <strong class="white--text">
                              <%=title%>
                            </strong>
                          </h1>
                          <h3 class="white--text mt-n1"><%=caption%></h3>
                        </div>
                </div>

            </div>
          </div>
        </div>
    </div>
</div>