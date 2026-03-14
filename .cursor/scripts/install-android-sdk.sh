#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TOOLS_DIR="${REPO_ROOT}/.tools"
SDK_DIR="${REPO_ROOT}/.android-sdk"
CMDLINE_TOOLS_DIR="${SDK_DIR}/cmdline-tools/latest"
SDKMANAGER_BIN="${CMDLINE_TOOLS_DIR}/bin/sdkmanager"
ZIP_PATH="${TOOLS_DIR}/commandlinetools-linux.zip"
CMDLINE_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

mkdir -p "${TOOLS_DIR}" "${SDK_DIR}/cmdline-tools"

if [[ ! -x "${SDKMANAGER_BIN}" ]]; then
  curl -fsSL "${CMDLINE_URL}" -o "${ZIP_PATH}"
  TMP_EXTRACT_DIR="${TOOLS_DIR}/cmdline-tools-extract"
  rm -rf "${TMP_EXTRACT_DIR}"
  mkdir -p "${TMP_EXTRACT_DIR}"
  unzip -q -o "${ZIP_PATH}" -d "${TMP_EXTRACT_DIR}"
  rm -rf "${CMDLINE_TOOLS_DIR}"
  mkdir -p "${SDK_DIR}/cmdline-tools"
  mv "${TMP_EXTRACT_DIR}/cmdline-tools" "${CMDLINE_TOOLS_DIR}"
  rm -rf "${TMP_EXTRACT_DIR}"
fi

export ANDROID_SDK_ROOT="${SDK_DIR}"
export ANDROID_HOME="${SDK_DIR}"
export PATH="${PATH}:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin"

# Accepting licenses via `yes | ...` may return non-zero due to SIGPIPE; ignore that safely.
yes | "${SDKMANAGER_BIN}" --sdk_root="${SDK_DIR}" --licenses >/dev/null || true

"${SDKMANAGER_BIN}" --sdk_root="${SDK_DIR}" \
  "platform-tools" \
  "platforms;android-35" \
  "build-tools;35.0.0"

cat > "${REPO_ROOT}/local.properties" <<EOF
sdk.dir=${SDK_DIR}
EOF

chmod +x "${REPO_ROOT}/gradlew" || true
