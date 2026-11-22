#!/usr/bin/env bash
# Скачивает 40 русских шрифтов (Google Fonts, свободные лицензии) в app/src/main/assets/fonts
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/app/src/main/assets/fonts"
WORK="$(mktemp -d)"

git clone --depth 1 --filter=blob:none --sparse https://github.com/google/fonts.git "$WORK/gf"
cd "$WORK/gf"

paths=()
while IFS='|' read -r folder _; do
  folder="$(echo "$folder" | xargs)"
  [[ -z "$folder" || "$folder" == \#* ]] && continue
  for lic in ofl ufl apache; do paths+=("$lic/$folder"); done
done < "$ROOT/scripts/fonts.txt"

git sparse-checkout set --cone "${paths[@]}"
python3 "$ROOT/scripts/pick_fonts.py" "$WORK/gf" "$ROOT/scripts/fonts.txt" "$OUT" 40
ls -la "$OUT" | head -60
du -sh "$OUT"
