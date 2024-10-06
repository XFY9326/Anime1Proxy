#!/usr/bin/env bash

current_dir="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
jar_name="Anime1Proxy-all.jar"

echo "Running at: $current_dir"

java -jar "$current_dir/$jar_name"
