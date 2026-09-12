#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# Run script for TaskApp NGINX Frontend Podman Container
# ==============================================================================

IMAGE_NAME="taskapp-web:latest"
CONTAINER_NAME="taskapp-web"
HOST_PORT="80"
BACKEND_HOST="host.containers.internal"
BACKEND_PORT="8080"
PODMAN_NETWORK=""
DETACHED=true

print_usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Run the TaskApp NGINX Podman container.

Options:
  -p, --port PORT             Host port to publish NGINX on (default: 80)
  -b, --backend-host HOST     Spring Boot backend host (default: host.containers.internal)
  -s, --backend-port PORT     Spring Boot backend port (default: 8080)
  -n, --name NAME             Container name (default: taskapp-web)
  -i, --image IMAGE           Podman image to run (default: taskapp-web:latest)
  --network NETWORK           Connect container to a specific Podman network
  -f, --foreground            Run in foreground (interactive mode)
  -h, --help                  Show this help message and exit

Examples:
  # Standard launch (NGINX on port 80, proxying to host Spring Boot on 8080)
  ./run.sh

  # Run on port 8085 (if port 80 is privileged or busy)
  ./run.sh --port 8085

  # Run on a custom Podman network with backend container named 'taskmanager'
  ./run.sh --network taskapp-net --backend-host taskmanager --backend-port 8080
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        -p|--port)
            HOST_PORT="$2"
            shift 2
            ;;
        -b|--backend-host)
            BACKEND_HOST="$2"
            shift 2
            ;;
        -s|--backend-port)
            BACKEND_PORT="$2"
            shift 2
            ;;
        -n|--name)
            CONTAINER_NAME="$2"
            shift 2
            ;;
        -i|--image)
            IMAGE_NAME="$2"
            shift 2
            ;;
        --network)
            PODMAN_NETWORK="$2"
            shift 2
            ;;
        -f|--foreground)
            DETACHED=false
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1" >&2
            print_usage
            exit 1
            ;;
    esac
done

# Check if image exists
if ! podman image inspect "${IMAGE_NAME}" >/dev/null 2>&1; then
    echo "Image '${IMAGE_NAME}' not found locally."
    echo "Running build script first..."
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    bash "${SCRIPT_DIR}/build.sh" --tag "${IMAGE_NAME}"
fi

# Stop and remove existing container if running
if podman ps -a --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
    echo "Removing existing container '${CONTAINER_NAME}'..."
    podman rm -f "${CONTAINER_NAME}" >/dev/null 2>&1
fi

PODMAN_ARGS=(
    --name "${CONTAINER_NAME}"
    -p "${HOST_PORT}:80"
    -e "BACKEND_HOST=${BACKEND_HOST}"
    -e "BACKEND_PORT=${BACKEND_PORT}"
    --add-host=host.containers.internal:host-gateway
    --add-host=host.docker.internal:host-gateway
)

if [[ -n "${PODMAN_NETWORK}" ]]; then
    PODMAN_ARGS+=(--network "${PODMAN_NETWORK}")
fi

if [[ "${DETACHED}" == true ]]; then
    PODMAN_ARGS+=(-d)
fi

echo "============================================================"
echo " Starting NGINX Container"
echo " Container Name:       ${CONTAINER_NAME}"
echo " Image:                ${IMAGE_NAME}"
echo " Host Port:            http://localhost:${HOST_PORT}"
echo " Upstream Backend:     http://${BACKEND_HOST}:${BACKEND_PORT}"
if [[ -n "${PODMAN_NETWORK}" ]]; then
echo " Podman Network:       ${PODMAN_NETWORK}"
fi
echo "============================================================"

podman run "${PODMAN_ARGS[@]}" "${IMAGE_NAME}"

if [[ "${DETACHED}" == true ]]; then
    echo ""
    echo "Container '${CONTAINER_NAME}' started successfully in background."
    echo "View logs:      podman logs -f ${CONTAINER_NAME}"
    echo "Stop container: ./stop.sh (or 'podman stop ${CONTAINER_NAME}')"
fi
