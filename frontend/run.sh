#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# Run script for TaskApp NGINX Frontend Docker Container
# ==============================================================================

IMAGE_NAME="taskapp-web:latest"
CONTAINER_NAME="taskapp-web"
HOST_PORT="80"
BACKEND_HOST="host.docker.internal"
BACKEND_PORT="8080"
DOCKER_NETWORK=""
DETACHED=true

print_usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Run the TaskApp NGINX Docker container.

Options:
  -p, --port PORT             Host port to publish NGINX on (default: 80)
  -b, --backend-host HOST     Spring Boot backend host (default: host.docker.internal)
  -s, --backend-port PORT     Spring Boot backend port (default: 8080)
  -n, --name NAME             Container name (default: taskapp-web)
  -i, --image IMAGE           Docker image to run (default: taskapp-web:latest)
  --network NETWORK           Connect container to a specific Docker network
  -f, --foreground            Run in foreground (interactive mode)
  -h, --help                  Show this help message and exit

Examples:
  # Standard launch (NGINX on port 80, proxying to host Spring Boot on 8080)
  ./run.sh

  # Run on port 8085 (if port 80 is privileged or busy)
  ./run.sh --port 8085

  # Run on a custom Docker network with backend container named 'taskmanager'
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
            DOCKER_NETWORK="$2"
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
if ! docker image inspect "${IMAGE_NAME}" >/dev/null 2>&1; then
    echo "Image '${IMAGE_NAME}' not found locally."
    echo "Running build script first..."
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    bash "${SCRIPT_DIR}/build.sh" --tag "${IMAGE_NAME}"
fi

# Stop and remove existing container if running
if docker ps -a --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
    echo "Removing existing container '${CONTAINER_NAME}'..."
    docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1
fi

DOCKER_ARGS=(
    --name "${CONTAINER_NAME}"
    -p "${HOST_PORT}:80"
    -e "BACKEND_HOST=${BACKEND_HOST}"
    -e "BACKEND_PORT=${BACKEND_PORT}"
    --add-host=host.docker.internal:host-gateway
)

if [[ -n "${DOCKER_NETWORK}" ]]; then
    DOCKER_ARGS+=(--network "${DOCKER_NETWORK}")
fi

if [[ "${DETACHED}" == true ]]; then
    DOCKER_ARGS+=(-d)
fi

echo "============================================================"
echo " Starting NGINX Container"
echo " Container Name:       ${CONTAINER_NAME}"
echo " Image:                ${IMAGE_NAME}"
echo " Host Port:            http://localhost:${HOST_PORT}"
echo " Upstream Backend:     http://${BACKEND_HOST}:${BACKEND_PORT}"
if [[ -n "${DOCKER_NETWORK}" ]]; then
echo " Docker Network:       ${DOCKER_NETWORK}"
fi
echo "============================================================"

docker run "${DOCKER_ARGS[@]}" "${IMAGE_NAME}"

if [[ "${DETACHED}" == true ]]; then
    echo ""
    echo "Container '${CONTAINER_NAME}' started successfully in background."
    echo "View logs:      docker logs -f ${CONTAINER_NAME}"
    echo "Stop container: ./stop.sh (or 'docker stop ${CONTAINER_NAME}')"
fi
