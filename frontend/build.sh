#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# Build script for TaskApp NGINX Frontend Podman Container
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

IMAGE_TAG="taskapp-web:latest"
NO_CACHE=""

print_usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Build the TaskApp NGINX Podman container (compiles Angular and configures NGINX).

Options:
  -t, --tag TAG       Specify image tag (default: taskapp-web:latest)
  --no-cache          Do not use cache when building the image
  -h, --help          Show this help message and exit

Examples:
  ./build.sh
  ./build.sh --tag taskapp-web:v1.0.0
  ./build.sh --no-cache
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        -t|--tag)
            IMAGE_TAG="$2"
            shift 2
            ;;
        --no-cache)
            NO_CACHE="--no-cache"
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

echo "============================================================"
echo " Building Podman Image: ${IMAGE_TAG}"
echo " Dockerfile:           ${SCRIPT_DIR}/Dockerfile"
echo " Build Context:        ${SCRIPT_DIR}"
echo "============================================================"

podman build \
    ${NO_CACHE} \
    -f "${SCRIPT_DIR}/Dockerfile" \
    -t "${IMAGE_TAG}" \
    "${SCRIPT_DIR}"

echo ""
echo " Successfully built ${IMAGE_TAG}"
echo "Run './run.sh' or 'podman run -p 80:80 ${IMAGE_TAG}' to start the container."
