package info.jeffkerns.taskmanager.entity;

public enum UserRole {
  USER,
  ADMIN;

  public String getAuthority() {
    return switch (this) {
      case USER -> "ROLE_USER";
      case ADMIN -> "ROLE_ADMIN";
    };
  }
}
