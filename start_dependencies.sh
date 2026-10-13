#!/bin/bash
set -euo pipefail

docker start mongodb

services=(
  GG_AUTH_SERVICES
  IDENTITY_VERIFICATION
  ADDRESS_SEARCH_API
  ADDRESS_LOOKUP
  BAS_GATEWAY
  BAS_GATEWAY_FRONTEND
  CONTACT_FRONTEND
  FEEDBACK_FRONTEND
)

sm2 --start "${services[@]}"
sm2 --verify "${services[@]}"
