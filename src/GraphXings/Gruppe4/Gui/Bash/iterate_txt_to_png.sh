#!/usr/bin/env bash

PYTHON_BIN="python3"
GRAPH_SCRIPT="../Python/graph.py"
LOG_DIR="../../../../../logs"
NO_OF_THREADS=6
SECONDS_OF_SLEEP=60

mkdir -p ${LOG_DIR}/{MIN,MAX}{,_ANGLE}
while true; do
    # Check if there are less than NO_OF_THREADS background processes
    if [ "$(jobs | wc -l)" -lt ${NO_OF_THREADS} ]; then
        # Find files matching the pattern 1*M*.txt
        files=(${LOG_DIR}/1*M*.txt)
        # Check if any files exist (that aren't just the literal pattern)
        if [ ${#files[@]} -gt 0 ] && [ "${files[0]}" != "${LOG_DIR}/1*M*.txt" ] ; then
            f="$(basename "${files[0]}")"
            d="${LOG_DIR}/${f//.txt}"
            strategy="$(echo "${f}" | grep -oE 'M[A-Z_]{3,}')"
            mkdir -p "${d}" "${LOG_DIR}/${strategy}"
            mv "${LOG_DIR}/${f}" "${d}/"
            ln -s "../${f//.txt}" "${LOG_DIR}/${strategy}/"
            # Launch a new command in the background
            "${PYTHON_BIN}" "${GRAPH_SCRIPT}" "${d}/${f}" &
        else
            # No matching files found, break out of the loop
            break
        fi
    fi
    # Sleep for a while before the next iteration
    sleep ${SECONDS_OF_SLEEP}
done
