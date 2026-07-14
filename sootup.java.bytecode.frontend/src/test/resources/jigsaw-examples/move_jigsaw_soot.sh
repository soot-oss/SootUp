#!/bin/bash

# Source of jigsaw path:
# https://github.com/accso/java9-jigsaw-examples/tree/master/jigsaw-examples
jigsaw_path="$DEV/Java/java9-jigsaw-examples/jigsaw-examples"
soot_path="$DEV/Java/soot-reloaded/shared-test-resources/jigsaw-examples/auto_import"

cd "$jigsaw_path" || exit 1
for dir in */; do

  if [ -d "$dir" ]; then

    dir_name="$(basename -- $dir)"
    echo "[+] $dir_name"

    # remove example_ from sample
    string_to_clean="example_"
    empty=""
    clean_name="${dir_name/$string_to_clean/$empty}"
    target_path="$soot_path/$clean_name"

    echo "[+] Create Directory: $target_path"
    mkdir -p "$target_path/jar"

    echo "[+] Compile: $target_path"

    # We need to enter the dir since the compile scripts refer to
    # the env.sh _relative_ to their location
    cd "$dir" || exit 1
    sh "./compile.sh"
    cd "$jigsaw_path" || exit 1

    cp -R "$dir/src" "$target_path/src"
    cp -R "$dir/classes" "$target_path/exploded_module"

    # Sometimes compiled classes are put into this directory
    cp -R "$dir/mods" "$target_path/mods"

    # Copy all .jars
    find "$dir" -name "*.jar" -exec cp {} "$target_path/jar/" \;

    # Remove countless .gitignores, since we actually want to check in
    # .class and .jar files
    find "$target_path" -name ".gitignore" -exec rm {} \;

  else
    echo "[-] $dir not found! Skipping..."
  fi
done
