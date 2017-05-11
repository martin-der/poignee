#!/bin/sh

SHDOC="$(dirname "$0")/shdoc"

cat "$1" | "$SHDOC" > "$2"