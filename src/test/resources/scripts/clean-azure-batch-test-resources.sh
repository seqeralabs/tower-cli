#!/usr/bin/env bash
#
# Cleanup script for Azure Batch CLI test resources (COMP-1464)
#
# Removes all compute environments and credentials created by the test script
# (resources matching the test-* naming pattern).
#
# Prerequisites:
#   - TOWER_ACCESS_TOKEN set
#   - TOWER_API_ENDPOINT set
#   - tw CLI binary on PATH or TW variable set
#
# Usage:
#   export TOWER_ACCESS_TOKEN=<token>
#   export TOWER_API_ENDPOINT=<endpoint>
#   ./clean-azure-batch-test-resources.sh [--workspace <workspace>]
#

set -uo pipefail

WORKSPACE="${WORKSPACE:-}"
TW="${TW:-tw}"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --workspace)
            WORKSPACE="$2"
            shift 2
            ;;
        --workspace=*)
            WORKSPACE="${1#*=}"
            shift
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

ws_flag() {
    if [[ -n "$WORKSPACE" ]]; then
        echo "--workspace=$WORKSPACE"
    fi
}

echo "============================================================"
echo " Cleanup Azure Batch CLI Test Resources"
echo "============================================================"
echo ""
echo "Endpoint:  ${TOWER_API_ENDPOINT:-api.cloud.seqera.io}"
echo "Workspace: ${WORKSPACE:-<user default>}"
echo ""

# ---------------------------------------------------------------------------
# Remove test compute environments
# ---------------------------------------------------------------------------
echo "--- Removing test compute environments ---"

ce_list=$($TW -o json compute-envs list $(ws_flag) 2>/dev/null) || ce_list="[]"

for name in $(echo "$ce_list" | grep -o '"name":"test-[^"]*"' | sed 's/"name":"//;s/"//'); do
    echo -n "  Deleting CE '$name' ... "
    if $TW compute-envs delete --name="$name" $(ws_flag) 2>/dev/null; then
        echo "done"
    else
        echo "failed (may already be deleted)"
    fi
done

echo ""

# ---------------------------------------------------------------------------
# Remove test credentials
# ---------------------------------------------------------------------------
echo "--- Removing test credentials ---"

cred_list=$($TW -o json credentials list $(ws_flag) 2>/dev/null) || cred_list="[]"

for name in $(echo "$cred_list" | grep -o '"name":"test-[^"]*"' | sed 's/"name":"//;s/"//'); do
    echo -n "  Deleting credential '$name' ... "
    if $TW credentials delete --name="$name" $(ws_flag) 2>/dev/null; then
        echo "done"
    else
        echo "failed (may already be deleted)"
    fi
done

echo ""
echo "Cleanup complete."
