#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="${1:-taskapp-web}"

if podman ps -a --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
    echo "Stopping container '${CONTAINER_NAME}'..."
    podman stop "${CONTAINER_NAME}"
    echo "Removing container '${CONTAINER_NAME}'..."
    podman rm "${CONTAINER_NAME}"
    echo "Container '${CONTAINER_NAME}' stopped and removed."
else
    echo "No container named '${CONTAINER_NAME}' found."
fi
