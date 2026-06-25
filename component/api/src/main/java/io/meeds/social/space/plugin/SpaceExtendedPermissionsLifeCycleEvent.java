package io.meeds.social.space.plugin;

import lombok.Getter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

import java.util.List;

public class SpaceExtendedPermissionsLifeCycleEvent extends SpaceLifeCycleEvent {

  @Getter
  List<String> changedpermissions;

  public SpaceExtendedPermissionsLifeCycleEvent(Space space, String userId, Type type, List<String> changedPermissions) {
    super(space, userId, type);
    this.changedpermissions = changedPermissions;
  }
}
