#!/usr/bin/env bash

LOG_DIR="../../../../../logs"

echo "Are you sure you want to delete the all the png files? (y/n)"
read answer

if [ "$answer" == "y" ]; then
    for d in "${LOG_DIR}/"1*M*/ ; do
        pushd "${d}"
        rm 1*M*.png
        f="$(basename ${d})" 2>/dev/null && echo "Deleted files in directory \"${f}.\""
        popd
    done
else
    echo "Deletion canceled."
fi