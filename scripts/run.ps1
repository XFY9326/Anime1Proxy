$current_dir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$jar_name = "Anime1Proxy-all.jar"

echo "Running at: $current_dir"

java -jar "$current_dir\$jar_name"
