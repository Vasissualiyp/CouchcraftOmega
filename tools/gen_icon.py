#!/usr/bin/env python3
"""Draw the Couchcraft Omega icon: a capital Omega filling the square, coloured by quadrant (yellow, blue, red,
green as in the Couchcraft mark) with a thin gap along the quadrant seams. Everything outside the Omega is transparent.

usage: gen_icon.py <font.ttf> <out512.png> [<out128.png>]
"""
import sys
from PIL import Image, ImageChops, ImageDraw, ImageFont

SS = 4           # supersampling factor
N = 192          # design grid
GAP = 8          # seam width on the design grid
BOX = (6, 6, 186, 186)   # the Omega's ink box on the design grid
STROKE = 3       # extra weight added to the glyph, design grid units
TL, TR, BL, BR = (246, 198, 68), (58, 90, 246), (211, 71, 55), (74, 158, 97)


def render(size, font_path):
    big = size * SS
    s = big / N
    font = ImageFont.truetype(font_path, 400)
    probe = ImageDraw.Draw(Image.new("L", (1, 1)))
    l, t, r, b = probe.textbbox((0, 0), "Ω", font=font, stroke_width=int(STROKE * 400 / 100))
    # glyph mask, scaled so its ink box fills BOX
    mask = Image.new("L", (r - l, b - t), 0)
    ImageDraw.Draw(mask).text((-l, -t), "Ω", font=font, fill=255, stroke_width=int(STROKE * 400 / 100), stroke_fill=255)
    bw, bh = (BOX[2] - BOX[0]) * s, (BOX[3] - BOX[1]) * s
    k = min(bw / mask.width, bh / mask.height)
    mask = mask.resize((round(mask.width * k), round(mask.height * k)), Image.LANCZOS)
    full = Image.new("L", (big, big), 0)
    full.paste(mask, (round((big - mask.width) / 2), round((big - mask.height) / 2)))
    # colour by quadrant
    col = Image.new("RGB", (big, big))
    d = ImageDraw.Draw(col)
    h = big // 2
    d.rectangle([0, 0, h, h], fill=TL)
    d.rectangle([h, 0, big, h], fill=TR)
    d.rectangle([0, h, h, big], fill=BL)
    d.rectangle([h, h, big, big], fill=BR)
    # seams
    g = GAP * s / 2
    seam = Image.new("L", (big, big), 255)
    ds = ImageDraw.Draw(seam)
    ds.rectangle([h - g, 0, h + g, big], fill=0)
    ds.rectangle([0, h - g, big, h + g], fill=0)
    alpha = ImageChops.multiply(full, seam)
    out = col.convert("RGBA")
    out.putalpha(alpha)
    return out.resize((size, size), Image.LANCZOS)


if __name__ == "__main__":
    render(512, sys.argv[1]).save(sys.argv[2])
    if len(sys.argv) > 3:
        render(128, sys.argv[1]).save(sys.argv[3])
