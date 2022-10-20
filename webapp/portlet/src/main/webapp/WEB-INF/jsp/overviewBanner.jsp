<%@ page import="javax.portlet.PortletPreferences" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%
	PortletPreferences preferences = renderRequest.getPreferences();
	String bannerUrl = renderRequest.getParameter("bannerUrl");
	if (bannerUrl == null) {
		bannerUrl = preferences.getValue("bannerUrl", "");
		if (bannerUrl == null) {
			bannerUrl = "";
		}
	}

  String bannerDescription = renderRequest.getParameter("bannerDescription");
	if (bannerDescription == null) {
		bannerDescription = preferences.getValue("bannerDescription", "");
		if (bannerDescription == null) {
			bannerDescription = "";
		}
	}

  String bannerCaption = renderRequest.getParameter("bannerCaption");
	if (bannerCaption == null) {
		bannerCaption = preferences.getValue("bannerCaption", "");
		if (bannerCaption == null) {
			bannerCaption = "";
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
                style="background-image: url(<%=bannerUrl%>);height:142px;width:1150px">
                        <div 
                          class="d-flex justify-content-center flex-column text-center"
                          style="background-color: rgba(0, 0, 0, 0.5);height:100%;width:100%">
                          <h1 class=" mt-7">
                            <strong class="white--text">
                              <%=bannerDescription%>
                            </strong>
                          </h1>
                          <h3 class="white--text mt-n1"><%=bannerCaption%></h3>
                        </div>
                </div>

            </div>
          </div>
        </div>
    </div>
</div>