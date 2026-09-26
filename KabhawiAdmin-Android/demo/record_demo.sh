#!/usr/bin/env bash
# يسجّل فيديو لجولة التطبيق على المحاكي (يُشغَّل من GitHub Actions داخل android-emulator-runner).
# المتطلبات: المحاكي يعمل، الخادم التجريبي (mock_server.py) يعمل على المنفذ 8080 في الجهاز المضيف.
set -uo pipefail

ROOT="${GITHUB_WORKSPACE:-$(cd "$(dirname "$0")/../.." && pwd)}/KabhawiAdmin-Android"
OUT="$ROOT/demo/output"
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
TEST_APK="$ROOT/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
PKG=com.kabhawi.admin
mkdir -p "$OUT"

adb wait-for-device
# تابلت 10 بوصة: 1920x1200 بكثافة 240 = 1280x800dp (وضع أفقي)
adb shell wm density 240
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation 0
adb install -r "$APK"
adb install -r "$TEST_APK"
adb shell pm clear "$PKG" >/dev/null 2>&1 || true
adb logcat -c

# 1) مسجّل المحاكي (على الجهاز المضيف) — الأفضل جودة
adb emu screenrecord start --time-limit 178 --fps 30 "$OUT/demo.webm" | tee "$OUT/emu_record.txt"
REC_START=$(date +%s.%N)
sleep 4
DEVICE_REC=0
if grep -qiE "KO|unknown|error" "$OUT/emu_record.txt"; then
  # 2) بديل: screenrecord داخل الجهاز
  echo "Emulator recorder unavailable, falling back to adb screenrecord"
  adb shell screenrecord --bit-rate 8000000 --time-limit 178 /sdcard/demo.mp4 &
  DEVICE_REC=1
  sleep 2
fi

adb shell am instrument -w -e class "$PKG.DemoTourTest" "$PKG.test/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/instrument.txt"
sleep 2

adb emu screenrecord stop || true
if [ "$DEVICE_REC" = "1" ]; then
  adb shell pkill -INT screenrecord || adb shell killall -INT screenrecord || true
  sleep 4
  adb pull /sdcard/demo.mp4 "$OUT/demo_device.mp4" || true
fi
sleep 3

adb pull "/sdcard/Android/data/$PKG/files/shots" "$OUT/shots" || true

# قص الفيديو من ظهور شاشة الدخول حتى نهاية الجولة (علامات DEMO_START/DEMO_END في logcat)
adb logcat -d -v epoch -s DemoTour:I > "$OUT/markers.txt" || true
python3 - "$OUT/markers.txt" "$REC_START" > "$OUT/trim.txt" <<'PY' || true
import re, sys
start = end = None
for line in open(sys.argv[1], encoding="utf-8", errors="ignore"):
    m = re.match(r"\s*(\d+\.\d+)\s.*(DEMO_START|DEMO_END)", line)
    if m:
        t = float(m.group(1)) - float(sys.argv[2])
        if m.group(2) == "DEMO_START" and start is None:
            start = t
        elif m.group(2) == "DEMO_END":
            end = t
if start is not None and end is not None and 0 <= start < end:
    print(f"{max(0.0, start - 0.5):.2f} {end + 0.5:.2f}")
PY
echo "trim: $(cat "$OUT/trim.txt" 2>/dev/null)"; cat "$OUT/markers.txt" || true
adb logcat -d > "$OUT/logcat.txt" || true
echo "----- crash / test lines from logcat -----"
grep -E "AndroidRuntime|FATAL|TestRunner|DemoTour" "$OUT/logcat.txt" | tail -80 || true
ls -la "$OUT" "$OUT/shots" || true
