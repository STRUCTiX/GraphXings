#!/usr/bin/env bash

source vars.sh

# https://stackoverflow.com/questions/24961127/how-to-create-a-video-from-images-with-ffmpeg
for d in "${LOG_DIR}/"1*M*/ ; do
    pushd "${d}" 1>/dev/null
    f="$(basename ${d})"
    # Add flag -y to overwrite existing mp4 files
    # Add flag -n to skip existing mp4 files
    ffmpeg -framerate 30 -pattern_type glob -i '*.png' -c:v libx264 -pix_fmt yuv420p "${f}.mp4" -hide_banner -loglevel error
    popd 1>/dev/null
done