#!/bin/bash

source "bin/init/env.sh"
source "bin/plugin/juicity/init.sh"

DIR="$ROOT/x86"
mkdir -p $DIR
env CC=$ANDROID_X86_CC GOARCH=386 go build -v -o $DIR/$LIB_OUTPUT -trimpath -ldflags "-s -w -buildid=" ./cmd/client
