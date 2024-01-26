#!/usr/bin/env bash

GRAPH_BIN="../Python/graph.py"
LOG_DIR="../../../../../logs"
NO_OF_THREADS=6
SECONDS_OF_SLEEP=60

mkdir -p ${LOG_DIR}/{MIN,MAX}{,_ANGLE}
while true; do
    # Check if there are less than NO_OF_THREADS background processes
    if [ "$(jobs | wc -l)" -lt ${NO_OF_THREADS} ]; then
        # Find files matching the pattern 1*M*.txt
        files=(${LOG_DIR}/1*M*.txt)
        # Check if any files exist
        if [ ${#files[@]} -gt 0 ]; then
            f="$(basename "${files[0]}")"
            d="${LOG_DIR}/${f//.txt}"
            mkdir -p "${d}"
            mv "${LOG_DIR}/${f}" "${d}/"
            # Launch a new command in the background
            python "${GRAPH_BIN}" "${d}/${f}" &
        else
            # No matching files found, break out of the loop
            break
        fi
    fi
    # Sleep for a while before the next iteration
    sleep ${SECONDS_OF_SLEEP}
done