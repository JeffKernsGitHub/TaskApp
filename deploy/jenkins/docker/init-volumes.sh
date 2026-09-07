#!/bin/bash
# ==============================================================================
# Jenkins External Volumes Initialization Script
# ==============================================================================
# Creates the required persistent external Docker volumes if they do not exist.
# ==============================================================================

set -euo pipefail

VOLUMES=("jenkins_data" "jenkins_maven_cache")

echo "==> Verifying external volumes for Jenkins..."

for vol in "${VOLUMES[@]}"; do
    if docker volume inspect "$vol" >/dev/null 2>&1; then
        echo "  [✓] Volume '$vol' already exists."
    else
        echo "  [+] Creating external volume '$vol'..."
        docker volume create "$vol"
        echo "  [✓] Volume '$vol' created successfully."
    fi
done

echo ""
echo "All required external volumes are initialized."
echo "You can now run: docker compose up -d"
