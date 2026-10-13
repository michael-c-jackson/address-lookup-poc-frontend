#!/bin/bash
set -euo pipefail

cd -- "$(dirname -- "$0")"
exec sbt "run 9000"
