#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Startup Script for Linux/macOS
# ----------------------------------------------------------------------------
set -e

if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
else
    echo "Notice: Maven not detected on PATH. In containers or environments with Maven installed, running 'mvn $@'..."
    exec mvn "$@"
fi
