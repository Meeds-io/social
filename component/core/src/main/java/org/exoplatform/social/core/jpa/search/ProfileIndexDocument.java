package org.exoplatform.social.core.jpa.search;

import java.util.*;

import org.exoplatform.commons.search.domain.Document;

public class ProfileIndexDocument extends Document {

  public ProfileIndexDocument(String id, String url, Date lastUpdatedDate, Set<String> permissions, Map<String, String> fields) {
    super(id, url, lastUpdatedDate, permissions, fields);
  }

  @Override
  public String toJSON() {
    String json = super.toJSON();
    return json.replace("\"@@@[", "[").replace("]@@@\"", "]");
  }
}
