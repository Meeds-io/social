package org.exoplatform.social.service.test;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

import java.util.ArrayList;
import java.util.List;

public class MockSpacesAdministrationService implements SpacesAdministrationService {

  private List<MembershipEntry> spacesAdministrators = new ArrayList<>();

  private List<MembershipEntry> spacesCreators = new ArrayList<>();

  @Override
  public List<MembershipEntry> getSpacesAdministratorsMemberships() {
    return spacesAdministrators;
  }

  @Override
  public void updateSpacesAdministratorsMemberships(List<MembershipEntry> permissionsExpressions) {
    if(permissionsExpressions != null) {
      spacesAdministrators = new ArrayList<>(permissionsExpressions);
    } else {
      spacesAdministrators.clear();
    }
  }

  @Override
  public List<MembershipEntry> getSpacesCreatorsMemberships() {
    return spacesCreators;
  }

  @Override
  public void updateSpacesCreatorsMemberships(List<MembershipEntry> permissionsExpressions) {
    if(permissionsExpressions != null) {
      spacesCreators = new ArrayList<>(permissionsExpressions);
    } else {
      spacesCreators.clear();
    }
  }

  @Override
  public boolean canCreateSpace(String username) {
    if (StringUtils.isBlank(username) || IdentityConstants.ANONIM.equals(username) || IdentityConstants.SYSTEM.equals(username)) {
      return false;
    }
    else {
      return true;
    }
  }
}
