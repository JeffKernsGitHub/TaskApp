#!/bin/bash
set -e

# ==============================================================================
# Jenkins Docker Entrypoint Wrapper
# ==============================================================================
# Handles runtime dynamic permissions for host Docker socket (/var/run/docker.sock)
# before handing over execution to the official Jenkins startup script as 'jenkins'.
# ==============================================================================

DOCKER_SOCK="/var/run/docker.sock"

if [ -S "$DOCKER_SOCK" ]; then
    DOCKER_GID=$(stat -c '%g' "$DOCKER_SOCK")
    
    # Check if a group with this GID already exists in /etc/group
    EXISTING_GROUP=$(getent group "$DOCKER_GID" | cut -d: -f1 || true)
    
    if [ -z "$EXISTING_GROUP" ]; then
        # Group doesn't exist, create docker-host group with the socket's GID
        groupadd -for -g "$DOCKER_GID" docker-host
        usermod -aG docker-host jenkins
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
    # Allow running custom commands (e.g. docker run --rm ... /opt/java/openjdk-25/bin/java -version)
    exec "$@"
fi
