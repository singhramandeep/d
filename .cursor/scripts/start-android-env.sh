#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SDK_DIR="${REPO_ROOT}/.android-sdk"

if [[ -d "${SDK_DIR}" ]]; then
  export ANDROID_SDK_ROOT="${SDK_DIR}"
  export ANDROID_HOME="${SDK_DIR}"
  export PATH="${PATH}:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin"
fi

if [[ ! -f "${REPO_ROOT}/local.properties" ]]; then
  cat > "${REPO_ROOT}/local.properties" <<EOF
sdk.dir=${SDK_DIR}
EOF
fi

chmod +x "${REPO_ROOT}/gradlew" || true
