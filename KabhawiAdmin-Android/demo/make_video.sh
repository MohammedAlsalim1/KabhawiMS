#!/usr/bin/env bash
# يحوّل التسجيل إلى MP4 (H.264) ويجهّز لقطات الشاشة في docs/media.
set -euo pipefail
ROOT="${GITHUB_WORKSPACE:-$(cd "$(dirname "$0")/../.." && pwd)}/KabhawiAdmin-Android"
OUT="$ROOT/demo/output"
MEDIA="$ROOT/docs/media"
mkdir -p "$MEDIA"
command -v ffmpeg >/dev/null || { sudo apt-get update -qq && sudo apt-get install -y -qq ffmpeg; }

SRC=""
for candidate in "$OUT/demo.webm" "$OUT/demo_device.mp4"; do
  if [ -s "$candidate" ] && [ "$(stat -c %s "$candidate")" -gt 200000 ]; then SRC="$candidate"; break; fi
done

VIDEO="$MEDIA/kabhawi-admin-demo.mp4"
TRIM=()
if [ -s "$OUT/trim.txt" ]; then
  read -r T_START T_END < "$OUT/trim.txt"
  TRIM=(-ss "$T_START" -to "$T_END")
  echo "Trimming to $T_START..$T_END s"
fi

if [ -n "$SRC" ]; then
  echo "Encoding $SRC"
  ffmpeg -y -loglevel error -i "$SRC" "${TRIM[@]}" -an \
    -vf "scale=1280:-2:flags=lanczos,fps=30,format=yuv420p" \
    -c:v libx264 -preset slow -crf 21 -movflags +faststart "$VIDEO"
else
  echo "No screen recording found, building a slideshow from screenshots"
  ffmpeg -y -loglevel error -framerate 1/3 -pattern_type glob -i "$OUT/shots/*.png" -an \
    -vf "scale=1280:-2:flags=lanczos,fps=30,format=yuv420p" \
    -c:v libx264 -preset slow -crf 22 -movflags +faststart "$VIDEO"
fi

rm -f "$MEDIA"/*.jpg
for png in "$OUT"/shots/*.png; do
  [ -e "$png" ] || continue
  ffmpeg -y -loglevel error -i "$png" -vf "scale=1280:-2:flags=lanczos" -q:v 3 "$MEDIA/$(basename "${png%.png}").jpg"
done
ffprobe -v error -show_entries format=duration,size -of default=nw=1 "$VIDEO"
ls -la "$MEDIA"
