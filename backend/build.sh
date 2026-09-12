#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IMAGE_TAG="${1:-taskapp-backend:latest}"

echo "============================================================"
echo " Building Spring Boot Podman Image: ${IMAGE_TAG}"
echo " Context: ${SCRIPT_DIR}"
echo "============================================================"

podman build -f "${SCRIPT_DIR}/Dockerfile" -t "${IMAGE_TAG}" "${SCRIPT_DIR}"

echo ""
echo " Successfully built ${IMAGE_TAG}"
