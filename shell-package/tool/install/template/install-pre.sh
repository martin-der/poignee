#!/bin/sh

MDU_INSTALL_TMP_DIR=`mktemp -d` || exit 1
trap "echo \"m - Removing '$MDU_INSTALL_TMP_DIR'\" ; rm -rf '$MDU_INSTALL_TMP_DIR'" EXIT

echo "m - Working in '$MDU_INSTALL_TMP_DIR'"
cd "$MDU_INSTALL_TMP_DIR" || exit 1
