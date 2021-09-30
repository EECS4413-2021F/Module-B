#!/bin/bash

echo "Content-Type: application/json"
echo

cat << EOF
{
  "AUTH_TYPE": "${AUTH_TYPE}",
  "REMOTE_IDENT": "${REMOTE_IDENT}",
  "QUERY_STRIN": "${QUERY_STRIN}",
  "CONTENT_LENGTH": "${CONTENT_LENGTH}",
  "REMOTE_USER": "${REMOTE_USER}",
  "REMOTE_ADDR": "${REMOTE_ADDR}",
  "CONTENT_TYPE": "${CONTENT_TYPE}",
  "REQUEST_METHOD": "${REQUEST_METHOD}",
  "REMOTE_HOST": "${REMOTE_HOST}",
  "GATEWAY_INTERFACE": "${GATEWAY_INTERFACE}",
  "SCRIPT_NAME": "${SCRIPT_NAME}",
  "SERVER_PROT": "${SERVER_PROT}",
  "PATH_INFO": "${PATH_INFO}",
  "SERVER_NAME": "${SERVER_NAME}",
  "SERVER_SOFT": "${SERVER_SOFT}",
  "PATH_TRANSLATED": "${PATH_TRANSLATED}",
  "SERVER_PORT": "${SERVER_PORT}",
}
EOF

exit 0
