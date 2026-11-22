#!/usr/bin/env python3
"""Выбирает из клона google/fonts первые N шрифтов с кириллицей и кладёт их в assets/fonts."""
import glob
import os
import re
import shutil
import sys

from fontTools.ttLib import TTFont

gf, listfile, out, need = sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4])
MAX_BYTES = 1_800_000
CYR = (0x0416, 0x0436, 0x042F, 0x0451, 0x0401)  # Ж ж Я ё Ё


def has_cyrillic(path):
    try:
        cmap = TTFont(path, lazy=True).getBestCmap()
        return all(c in cmap for c in CYR)
    except Exception as e:  # noqa: BLE001
        print(f"  ! не прочитать {os.path.basename(path)}: {e}")
        return False


def choose(files):
    files = [f for f in files if not re.search(r"italic", os.path.basename(f), re.I)]
    files = [f for f in files if os.path.getsize(f) <= MAX_BYTES]
    if not files:
        return []
    regular = [f for f in files if re.search(r"regular", os.path.basename(f), re.I)]
    variable = [f for f in files if "[" in os.path.basename(f)]
    plain = sorted(files, key=lambda f: (len(os.path.basename(f)), f))
    return regular + variable + plain


os.makedirs(out, exist_ok=True)
for old in glob.glob(os.path.join(out, "*")):
    os.remove(old)

picked = []
for line in open(listfile, encoding="utf-8"):
    line = line.strip()
    if not line or line.startswith("#"):
        continue
    folder, title = [x.strip() for x in line.split("|", 1)]
    found = []
    for lic in ("ofl", "ufl", "apache"):
        found += glob.glob(os.path.join(gf, lic, folder, "*.ttf"))
    ok = None
    for cand in choose(found):
        if has_cyrillic(cand):
            ok = cand
            break
    if not ok:
        print(f"- пропуск: {title} (нет файла или нет кириллицы)")
        continue
    name = title.replace(" ", "_") + ".ttf"
    shutil.copyfile(ok, os.path.join(out, name))
    picked.append(title)
    print(f"+ {title:28s} <- {os.path.relpath(ok, gf)}")
    if len(picked) >= need:
        break

print(f"\nИтого шрифтов: {len(picked)}")
if len(picked) < need:
    print(f"ОШИБКА: нужно {need}")
    sys.exit(1)
