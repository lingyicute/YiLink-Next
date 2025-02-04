#!/usr/bin/env bash

source "bin/init/env.sh"

cd library/core
./build.sh || exit 1

mkdir -p "$PROJECT/app/libs"
cp -f libcore.aar "$PROJECT/app/libs"
