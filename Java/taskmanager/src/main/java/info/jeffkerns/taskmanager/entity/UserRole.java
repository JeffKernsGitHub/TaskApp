package info.jeffkerns.taskmanager.entity;

public enum UserRole {
  USER,
  ADMIN;

  public String getAuthority() {
    return "ROLE_" + name();
  }
}