#!/usr/bin/env python3
"""Генерирует иконку Rin-pin: флаг Китая (фон) + картонная коробка-посылка (передний план)."""
import math
import os
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
RES = os.path.join(ROOT, "app", "src", "main", "res")


def star(cx, cy, r, rot_deg=-90.0):
    pts = []
    rin = r * 0.382
    for k in range(10):
        ang = math.radians(rot_deg + k * 36)
        rr = r if k % 2 == 0 else rin
        pts.append((cx + rr * math.cos(ang), cy + rr * math.sin(ang)))
    return pts


def path(pts):
    s = "M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in pts) + " Z"
    return s


RED = "#DE2910"
YELLOW = "#FFDE00"

# --- флаг: большая звезда и 4 малых, повёрнутых к большой ---
big_c = (30.0, 32.0)
big = star(big_c[0], big_c[1], 11.0)
small_offsets = [(18, -10.8), (25.2, -3.6), (25.2, 7.2), (18, 14.4)]
smalls = []
for dx, dy in small_offsets:
    cx, cy = big_c[0] + dx, big_c[1] + dy
    toward = math.degrees(math.atan2(big_c[1] - cy, big_c[0] - cx))
    smalls.append(star(cx, cy, 3.6, toward))

# --- коробка (изометрия) ---
cx, ty = 64.0, 54.0
top = [(cx, ty - 10), (cx + 20, ty), (cx, ty + 10), (cx - 20, ty)]
left = [(cx - 20, ty), (cx, ty + 10), (cx, ty + 34), (cx - 20, ty + 24)]
right = [(cx, ty + 10), (cx + 20, ty), (cx + 20, ty + 24), (cx, ty + 34)]
# лента скотча: по верхней грани и вниз по левой грани
tape_top = [(cx - 4, ty - 8), (cx + 4, ty - 4), (cx - 16, ty + 6), (cx - 24, ty + 2)]
# сдвиг ленты: проходит от середины задней-правой кромки к середине передней-левой
mid_back = (cx + 10, ty - 5)
mid_front = (cx - 10, ty + 5)
tape_top = [
    (mid_back[0] - 4, mid_back[1] - 2),
    (mid_back[0] + 4, mid_back[1] + 2),
    (mid_front[0] + 4, mid_front[1] + 2),
    (mid_front[0] - 4, mid_front[1] - 2),
]
tape_left = [
    (mid_front[0] - 4, mid_front[1] - 2),
    (mid_front[0] + 4, mid_front[1] + 2),
    (mid_front[0] + 4, mid_front[1] + 2 + 24),
    (mid_front[0] - 4, mid_front[1] - 2 + 24),
]

C_TOP, C_LEFT, C_RIGHT, C_TAPE, C_EDGE = "#F6D08F", "#C98F4E", "#DFA65F", "#FBEBC8", "#8F5A25"


def vec(inner, w=108, h=108):
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'    android:width="{w}dp" android:height="{h}dp"\n'
        f'    android:viewportWidth="{w}" android:viewportHeight="{h}">\n'
        f"{inner}</vector>\n"
    )


def p(d, fill=None, stroke=None, sw=0.0):
    a = f'  <path android:pathData="{d}"'
    if fill:
        a += f' android:fillColor="{fill}"'
    if stroke:
        a += f' android:strokeColor="{stroke}" android:strokeWidth="{sw}" android:strokeLineJoin="round"'
    return a + "/>\n"


bg = p("M0,0 L108,0 L108,108 L0,108 Z", fill=RED)
bg += p(path(big), fill=YELLOW)
for s in smalls:
    bg += p(path(s), fill=YELLOW)

fg = ""
# мягкая тень под коробкой
fg += p("M%.1f,%.1f L%.1f,%.1f L%.1f,%.1f L%.1f,%.1f Z" % (cx - 22, ty + 28, cx, ty + 40, cx + 24, ty + 27, cx + 2, ty + 18), fill="#40000000")
fg += p("M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in left) + " Z", fill=C_LEFT, stroke=C_EDGE, sw=0.8)
fg += p("M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in right) + " Z", fill=C_RIGHT, stroke=C_EDGE, sw=0.8)
fg += p("M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in top) + " Z", fill=C_TOP, stroke=C_EDGE, sw=0.8)
fg += p("M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in tape_top) + " Z", fill=C_TAPE)
fg += p("M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in tape_left) + " Z", fill=C_TAPE)

os.makedirs(os.path.join(RES, "drawable"), exist_ok=True)
os.makedirs(os.path.join(RES, "mipmap-anydpi-v26"), exist_ok=True)
with open(os.path.join(RES, "drawable", "ic_launcher_background.xml"), "w") as f:
    f.write(vec(bg))
with open(os.path.join(RES, "drawable", "ic_launcher_foreground.xml"), "w") as f:
    f.write(vec(fg))

adaptive = (
    '<?xml version="1.0" encoding="utf-8"?>\n'
    '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
    '    <background android:drawable="@drawable/ic_launcher_background"/>\n'
    '    <foreground android:drawable="@drawable/ic_launcher_foreground"/>\n'
    "</adaptive-icon>\n"
)
for n in ("ic_launcher.xml", "ic_launcher_round.xml"):
    with open(os.path.join(RES, "mipmap-anydpi-v26", n), "w") as f:
        f.write(adaptive)


# --- SVG-превью для проверки глазами ---
def poly(pts, fill, stroke=None, sw=0.0, opacity=None):
    s = '<polygon points="%s" fill="%s"' % (" ".join(f"{x:.2f},{y:.2f}" for x, y in pts), fill)
    if stroke:
        s += ' stroke="%s" stroke-width="%s" stroke-linejoin="round"' % (stroke, sw)
    return s + "/>"


svg = ['<svg xmlns="http://www.w3.org/2000/svg" width="432" height="432" viewBox="0 0 108 108">',
       '<defs><clipPath id="c"><circle cx="54" cy="54" r="54"/></clipPath></defs><g clip-path="url(#c)">',
       f'<rect width="108" height="108" fill="{RED}"/>', poly(big, YELLOW)]
svg += [poly(s, YELLOW) for s in smalls]
svg += [
    poly([(cx - 22, ty + 28), (cx, ty + 40), (cx + 24, ty + 27), (cx + 2, ty + 18)], "#000", None, 0, 0.25),
    poly(left, C_LEFT, C_EDGE, 0.8), poly(right, C_RIGHT, C_EDGE, 0.8), poly(top, C_TOP, C_EDGE, 0.8),
    poly(tape_top, C_TAPE), poly(tape_left, C_TAPE), "</g></svg>"]
out = sys.argv[1] if len(sys.argv) > 1 else None
if out:
    with open(out, "w") as f:
        f.write("\n".join(svg).replace('fill="#000"/>', 'fill="#000" fill-opacity="0.25"/>'))
print("icon generated")
