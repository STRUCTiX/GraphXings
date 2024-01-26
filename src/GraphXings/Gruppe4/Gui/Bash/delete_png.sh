#!/usr/bin/env bash

LOG_DIR="../../../../../logs"

echo "Are you sure you want to delete the all the png files? (y/n)"
read answer

if [ "$answer" == "y" ]; then
    for d in "${LOG_DIR}/"1*M*/ ; do
        pushd "${d}" 1>/dev/null
        rm 1*M*.png 2>/dev/null && ( f="$(basename ${d})" && echo "Deleted files in directory \"${f}.\"" )
        popd 1>/dev/null
    done
else
    echo "Deletion canceled."
fi