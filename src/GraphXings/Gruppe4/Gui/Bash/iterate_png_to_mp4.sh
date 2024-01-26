#!/usr/bin/env bash

LOG_DIR="../../../../../logs"

# https://stackoverflow.com/questions/24961127/how-to-create-a-video-from-images-with-ffmpeg
for d in "${LOG_DIR}/"1*M*/ ; do pushd "${d}" ; f="$(basename ${d})"; ffmpeg -framerate 30 -pattern_type glob -i '*.png' -c:v libx264 -pix_fmt yuv420p "${f}.mp4" ; popd ; done