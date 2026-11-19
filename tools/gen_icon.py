#!/usr/bin/env python3
"""Draw the Couchcraft Omega icon: the four-quadrant mark with a capital Omega in the bottom-right quadrant.

usage: gen_icon.py <font.ttf> <out512.png> [<out128.png>]
"""
import sys
from PIL import Image, ImageDraw, ImageFont

SS = 4            # supersampling factor
N = 192           # design grid; the mark is drawn on a 192 x 192 grid and scaled
FRAME, INK = (17, 17, 17, 255), (17, 17, 17, 255)
QUADS = {(10, 10): (246, 198, 68, 255), (100, 10): (58, 90, 246, 255),
         (10, 100): (211, 71, 55, 255), (100, 100): (74, 158, 97, 255)}


def render(size, font_path):
    s = size * SS / N
    im = Image.new("RGBA", (size * SS, size * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rounded_rectangle([0, 0, size * SS - 1, size * SS - 1], radius=30 * s, fill=FRAME)
    for (x, y), c in QUADS.items():
        d.rounded_rectangle([x * s, y * s, (x + 82) * s - 1, (y + 82) * s - 1], radius=10 * s, fill=c)
    font = ImageFont.truetype(font_path, int(80 * s))
    # centre the glyph's ink box in the bottom-right quadrant
    l, t, r, b = d.textbbox((0, 0), "Ω", font=font)
    cx, cy = (100 + 41) * s, (100 + 41) * s
    d.text((cx - (l + r) / 2, cy - (t + b) / 2), "Ω", font=font, fill=INK)
    return im.resize((size, size), Image.LANCZOS)


if __name__ == "__main__":
    render(512, sys.argv[1]).save(sys.argv[2])
    if len(sys.argv) > 3:
        render(128, sys.argv[1]).save(sys.argv[3])
