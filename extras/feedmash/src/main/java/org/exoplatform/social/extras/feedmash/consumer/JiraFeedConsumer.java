/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.extras.feedmash.consumer;

import com.rometools.rome.feed.synd.SyndEntry;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.extras.feedmash.AbstractFeedmashJob;
import org.exoplatform.social.extras.feedmash.Application;
import org.quartz.JobDataMap;


/**
 * Republishes some entries selected from a JIRA feed as activities into an activity stream.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class JiraFeedConsumer extends AbstractFeedmashJob {

  private static final Log LOG = ExoLogger.getLogger(JiraFeedConsumer.class);
  private String jiraLogo = "http://www.getzephyr.com/images/LOGO_JIRA.png";
  private String baseUrl;
  private String project;
  
  public JiraFeedConsumer() {
  }


  /**
   * A feed entry is accepted if its title matches "categoryMatch".
   */
  @SuppressWarnings("unchecked")
  protected boolean accept(SyndEntry entry) {

    // skipping entries already read
    if (alreadyChecked(entry.getUpdatedDate())) {
      return false;
    }

    return true;
  }

  /**
   * Publish the entry title as an activity on the 'targetActivityStream'
   */
  protected void handle(SyndEntry entry) {
    try {
      LOG.debug("republishing jira activity on : " + targetActivityStream + " stream, entry uri: " + entry.getLink());

      Identity jira = getJiraIdentity();

      Identity space = getIdentity(targetActivityStream);
      if (space == null) {
        saveState(feedLastCheck, null);
        return;
      } else {
        saveState(feedLastCheck, entry.getPublishedDate());
      }

      String message = entry.getTitle();

      publishActivity(message, jira, space);

    } catch (Exception e) {
      LOG.error("failed to republish jira activity: " + e.getMessage(), e);
    }
  }

  @Override
  public void beforeJobExecute(JobDataMap dataMap) {
    baseUrl = getStringParam(dataMap, "baseURL", null);
    project = getStringParam(dataMap, "project", null);
    if (feedUrl == null) {
      feedUrl = baseUrl + "/plugins/servlet/streams?key=" + project;
    }
    feedLastCheck = LAST_CHECKED + "." + feedUrl + "." + targetActivityStream;
  }

  private Identity getJiraIdentity() throws Exception {
    Application jiraApp = jiraApp();
    return getAppIdentity(jiraApp);
  }


  private Application jiraApp() {
    Application application = new Application();
    application.setId("jira-" + project);
    application.setName("JIRA (" + project + ")");
    String url = baseUrl + "/browse/" + project;
    application.setUrl(url);
    application.setIcon(jiraLogo);
    return application;
  }

}
