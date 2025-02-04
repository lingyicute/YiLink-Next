#!/bin/bash

source "bin/init/env.sh"
source "bin/plugin/mieru/init.sh"

DIR="$ROOT/x86_64"
mkdir -p $DIR
env CC=$ANDROID_X86_64_CC GOARCH=amd64 go build -v -o $DIR/$LIB_OUTPUT -trimpath -ldflags "-X 'github.com/enfein/mieru/pkg/log.LogPrefix=C ' -s -w -buildid=" ./cmd/mieru

