#!/bin/bash
set -e

# ==============================================================================
# Jenkins Podman Entrypoint Wrapper
# ==============================================================================
# Handles runtime dynamic permissions for host Podman/Docker socket
# before handing over execution to the official Jenkins startup script as 'jenkins'.
# ==============================================================================

PODMAN_SOCK=""
if [ -S "/run/podman/podman.sock" ]; then
    PODMAN_SOCK="/run/podman/podman.sock"
elif [ -S "/var/run/docker.sock" ]; then
    PODMAN_SOCK="/var/run/docker.sock"
fi

if [ -n "$PODMAN_SOCK" ]; then
    SOCK_GID=$(stat -c '%g' "$PODMAN_SOCK")
    
    # Check if a group with this GID already exists in /etc/group
    EXISTING_GROUP=$(getent group "$SOCK_GID" | cut -d: -f1 || true)
    
    if [ -z "$EXISTING_GROUP" ]; then
        # Group doesn't exist, create podman-host group with the socket's GID
        groupadd -for -g "$SOCK_GID" podman-host
        usermod -aG podman-host jenkins
    else
        # Add jenkins user to the existing group
        usermod -aG "$EXISTING_GROUP" jenkins
    fi
fi

# Ensure jenkins home directory ownership
if [ -d "/var/jenkins_home" ]; then
    chown -R jenkins:jenkins /var/jenkins_home || true
fi

# Execute standard Jenkins startup command as 'jenkins' user using gosu
if [ "$1" = "/usr/bin/tini" ] || [ "$1" = "/usr/local/bin/jenkins.sh" ]; then
    exec gosu jenkins "$@"
elif [ "$#" -eq 0 ]; then
    exec gosu jenkins /usr/bin/tini -- /usr/local/bin/jenkins.sh
else
    exec "$@"
fi
