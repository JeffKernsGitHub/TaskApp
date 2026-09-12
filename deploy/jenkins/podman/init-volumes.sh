#!/bin/bash
# ==============================================================================
# Jenkins External Volumes Initialization Script (Podman)
# ==============================================================================
# Creates the required persistent external Podman volumes if they do not exist.
# ==============================================================================

set -euo pipefail

VOLUMES=("jenkins_data" "jenkins_maven_cache")

echo "==> Verifying external volumes for Jenkins..."

for vol in "${VOLUMES[@]}"; do
    if podman volume inspect "$vol" >/dev/null 2>&1; then
        echo "  [✓] Volume '$vol' already exists."
    else
        echo "  [+] Creating external volume '$vol'..."
        podman volume create "$vol"
        echo "  [✓] Volume '$vol' created successfully."
    fi
done

echo ""
echo "All required external volumes are initialized."
echo "You can now run: podman compose up -d"
