"""
Generates the product / category illustrations used by the demo mock server.
Requires Pillow (pip install pillow). Images are drawn at 2x and downscaled
for smooth edges.
"""
import math
import os

from PIL import Image, ImageDraw, ImageFilter

GOLD = [(122, 84, 18), (184, 134, 40), (231, 190, 92), (252, 232, 170)]
SILVER = [(96, 102, 110), (150, 158, 168), (206, 212, 220), (245, 247, 250)]


def _gradient(size, top, bottom):
    w, h = size
    img = Image.new("RGB", size, top)
    px = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(1, h - 1)
        color = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        px.line([(0, y), (w, y)], fill=color)
    return img


def _shadow(img, box, blur=30, alpha=70):
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(layer).ellipse(box, fill=(0, 0, 0, alpha))
    layer = layer.filter(ImageFilter.GaussianBlur(blur))
    img.paste(layer, (0, 0), layer)


def _metal_ellipse(d, box, width, palette):
    """Ring-like ellipse with a simple metallic shading (dark outer, bright inner)."""
    x0, y0, x1, y1 = box
    steps = len(palette)
    for i, color in enumerate(palette):
        inset = i * width / (steps * 2)
        d.ellipse([x0 + inset, y0 + inset, x1 - inset, y1 - inset], outline=color, width=int(width - inset * 2))


def _pearl(d, cx, cy, r):
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(236, 230, 220), outline=(196, 188, 176), width=2)
    d.ellipse([cx - r * 0.55, cy - r * 0.6, cx - r * 0.05, cy - r * 0.15], fill=(255, 255, 255))


def _diamond(d, cx, cy, s):
    d.polygon([(cx - s, cy), (cx - s * 0.5, cy - s * 0.6), (cx + s * 0.5, cy - s * 0.6), (cx + s, cy), (cx, cy + s)],
              fill=(214, 238, 250), outline=(120, 170, 200))
    d.line([(cx - s, cy), (cx + s, cy)], fill=(150, 196, 222), width=3)
    d.line([(cx - s * 0.5, cy - s * 0.6), (cx, cy + s), (cx + s * 0.5, cy - s * 0.6)], fill=(150, 196, 222), width=3)


def _gem(d, cx, cy, r, color):
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=color, outline=(40, 60, 60), width=3)
    d.ellipse([cx - r * 0.5, cy - r * 0.55, cx - r * 0.05, cy - r * 0.15], fill=(255, 255, 255))


def _chain(d, points, color, width=6):
    d.line(points, fill=color, width=width, joint="curve")


def _arc_points(cx, cy, rx, ry, a0, a1, n=60):
    return [(cx + rx * math.cos(math.radians(a0 + (a1 - a0) * i / n)),
             cy + ry * math.sin(math.radians(a0 + (a1 - a0) * i / n))) for i in range(n + 1)]


def draw_ring_gold(d, w, h):
    _metal_ellipse(d, [w * 0.28, h * 0.36, w * 0.72, h * 0.80], w * 0.075, GOLD)
    _diamond(d, w * 0.5, h * 0.30, w * 0.085)
    for dx in (-0.12, 0.12):
        _diamond(d, w * (0.5 + dx), h * 0.37, w * 0.035)


def draw_ring_turquoise(d, w, h):
    _metal_ellipse(d, [w * 0.30, h * 0.40, w * 0.70, h * 0.80], w * 0.06, SILVER)
    d.ellipse([w * 0.38, h * 0.22, w * 0.62, h * 0.44], fill=(206, 212, 220), outline=(120, 128, 138), width=6)
    _gem(d, w * 0.5, h * 0.33, w * 0.095, (48, 184, 178))


def draw_pearl_necklace(d, w, h):
    pts = _arc_points(w * 0.5, h * 0.30, w * 0.34, h * 0.42, 10, 170, 22)
    for x, y in pts:
        _pearl(d, x, y, w * 0.032)
    _pearl(d, w * 0.5, h * 0.82, w * 0.05)


def draw_crescent_pendant(d, w, h):
    _chain(d, _arc_points(w * 0.5, h * 0.12, w * 0.30, h * 0.40, 15, 165), (214, 170, 70), 7)
    cx, cy, r = w * 0.5, h * 0.66, w * 0.16
    # هلال = دائرة ناقص دائرة مزاحة (عبر قناع)
    mask = Image.new("L", (int(w), int(h)), 0)
    md = ImageDraw.Draw(mask)
    md.ellipse([cx - r, cy - r, cx + r, cy + r], fill=255)
    md.ellipse([cx - r * 0.45, cy - r * 1.05, cx + r * 1.35, cy + r * 0.75], fill=0)
    edge = mask.filter(ImageFilter.MaxFilter(7))
    d.bitmap((0, 0), edge, fill=(150, 104, 26))
    d.bitmap((0, 0), mask, fill=(226, 180, 72))
    d.line([(cx, cy - r * 1.02), (cx, h * 0.5)], fill=(214, 170, 70), width=6)
    _gem(d, cx - r * 0.55, cy + r * 0.1, w * 0.03, (180, 30, 60))


def draw_watch_leather(d, w, h):
    d.rounded_rectangle([w * 0.40, h * 0.04, w * 0.60, h * 0.96], radius=w * 0.05, fill=(120, 70, 38), outline=(80, 44, 20), width=4)
    for y in (0.12, 0.2, 0.8, 0.88):
        d.line([(w * 0.43, h * y), (w * 0.57, h * y)], fill=(90, 52, 26), width=3)
    cx, cy, r = w * 0.5, h * 0.5, w * 0.2
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(196, 150, 60), outline=(120, 84, 18), width=6)
    r2 = r * 0.82
    d.ellipse([cx - r2, cy - r2, cx + r2, cy + r2], fill=(250, 246, 236))
    for i in range(12):
        a = math.radians(i * 30)
        d.line([(cx + r2 * 0.82 * math.cos(a), cy + r2 * 0.82 * math.sin(a)),
                (cx + r2 * 0.95 * math.cos(a), cy + r2 * 0.95 * math.sin(a))], fill=(60, 60, 60), width=4)
    d.line([(cx, cy), (cx + r2 * 0.45, cy - r2 * 0.35)], fill=(30, 30, 30), width=7)
    d.line([(cx, cy), (cx - r2 * 0.1, cy - r2 * 0.72)], fill=(30, 30, 30), width=5)
    d.ellipse([cx - 8, cy - 8, cx + 8, cy + 8], fill=(150, 30, 30))


def draw_watch_gold(d, w, h):
    for i in range(9):
        y0 = h * (0.03 + i * 0.105)
        d.rounded_rectangle([w * 0.42, y0, w * 0.58, y0 + h * 0.09], radius=10, fill=GOLD[2], outline=GOLD[0], width=3)
    cx, cy, r = w * 0.5, h * 0.5, w * 0.17
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=GOLD[1], outline=GOLD[0], width=6)
    r2 = r * 0.78
    d.ellipse([cx - r2, cy - r2, cx + r2, cy + r2], fill=(250, 236, 238))
    for i in range(12):
        a = math.radians(i * 30)
        _gem(d, cx + r2 * 0.85 * math.cos(a), cy + r2 * 0.85 * math.sin(a), 5, (230, 230, 240))
    d.line([(cx, cy), (cx + r2 * 0.4, cy + r2 * 0.3)], fill=(60, 40, 20), width=6)
    d.line([(cx, cy), (cx, cy - r2 * 0.65)], fill=(60, 40, 20), width=4)


def draw_bracelet_gold(d, w, h):
    for off in (-0.03, 0.0, 0.03):
        _metal_ellipse(d, [w * (0.2 + off), h * (0.30 - off), w * (0.8 + off), h * (0.74 - off)], w * 0.045, GOLD)


def draw_bracelet_silver(d, w, h):
    _chain(d, _arc_points(w * 0.5, h * 0.48, w * 0.30, h * 0.24, 0, 360, 90), SILVER[1], 9)
    for i, color in enumerate([(236, 96, 120), (90, 170, 230), (250, 200, 60), (120, 200, 120)]):
        a = math.radians(40 + i * 33)
        x, y = w * 0.5 + w * 0.30 * math.cos(a), h * 0.48 + h * 0.24 * math.sin(a)
        d.line([(x, y), (x, y + h * 0.06)], fill=SILVER[1], width=4)
        if i % 2 == 0:
            d.regular_polygon((x, y + h * 0.09, w * 0.035), 5, fill=color, outline=(80, 80, 80))
        else:
            d.ellipse([x - w * 0.03, y + h * 0.06, x + w * 0.03, y + h * 0.12], fill=color, outline=(80, 80, 80), width=2)


def _flower(d, cx, cy, r, petal, center):
    for i in range(6):
        a = math.radians(i * 60)
        px, py = cx + r * 0.9 * math.cos(a), cy + r * 0.9 * math.sin(a)
        d.ellipse([px - r * 0.62, py - r * 0.62, px + r * 0.62, py + r * 0.62], fill=petal, outline=GOLD[0], width=3)
    d.ellipse([cx - r * 0.55, cy - r * 0.55, cx + r * 0.55, cy + r * 0.55], fill=center, outline=GOLD[0], width=3)


def draw_earrings(d, w, h):
    for cx in (0.32, 0.68):
        _chain(d, _arc_points(w * cx, h * 0.26, w * 0.05, h * 0.08, 180, 360), GOLD[1], 6)
        d.line([(w * cx, h * 0.26), (w * cx, h * 0.46)], fill=GOLD[1], width=5)
        _flower(d, w * cx, h * 0.58, w * 0.1, GOLD[2], (214, 238, 250))


def draw_bridal_set(d, w, h):
    _chain(d, _arc_points(w * 0.5, h * 0.10, w * 0.36, h * 0.38, 12, 168), GOLD[1], 8)
    for i in range(9):
        a = math.radians(40 + i * 12.5)
        x, y = w * 0.5 + w * 0.36 * math.cos(a), h * 0.10 + h * 0.38 * math.sin(a)
        _diamond(d, x, y + h * 0.03, w * 0.028)
    _gem(d, w * 0.5, h * 0.56, w * 0.05, (40, 120, 90))
    _metal_ellipse(d, [w * 0.18, h * 0.70, w * 0.36, h * 0.88], w * 0.03, GOLD)
    _flower(d, w * 0.72, h * 0.78, w * 0.07, GOLD[2], (40, 120, 90))


PRODUCT_ART = {
    "ring_gold": (draw_ring_gold, ((252, 246, 232), (232, 214, 184))),
    "ring_turquoise": (draw_ring_turquoise, ((232, 246, 244), (190, 222, 218))),
    "pearl_necklace": (draw_pearl_necklace, ((70, 76, 96), (36, 40, 56))),
    "crescent_pendant": (draw_crescent_pendant, ((244, 238, 250), (214, 204, 232))),
    "watch_leather": (draw_watch_leather, ((236, 232, 226), (206, 198, 186))),
    "watch_gold": (draw_watch_gold, ((250, 236, 240), (228, 196, 206))),
    "bracelet_gold": (draw_bracelet_gold, ((22, 72, 70), (10, 44, 43))),
    "bracelet_silver": (draw_bracelet_silver, ((236, 244, 252), (200, 218, 238))),
    "earrings_flower": (draw_earrings, ((250, 242, 232), (236, 218, 198))),
    "bridal_set": (draw_bridal_set, ((250, 248, 244), (224, 218, 208))),
}

CATEGORY_ART = {
    "cat_rings": "ring_gold",
    "cat_necklaces": "pearl_necklace",
    "cat_watches": "watch_leather",
    "cat_bracelets": "bracelet_gold",
    "cat_earrings": "earrings_flower",
    "cat_sets": "bridal_set",
}


def render(name, size):
    draw_fn, (top, bottom) = PRODUCT_ART[name]
    scale = 2
    w, h = size[0] * scale, size[1] * scale
    img = _gradient((w, h), top, bottom)
    # the art is drawn in a square centred in the canvas
    side = min(w, h)
    art = _gradient((side, side), top, bottom)
    _shadow(art, [side * 0.22, side * 0.84, side * 0.78, side * 0.96], blur=side // 40)
    draw_fn(ImageDraw.Draw(art), side, side)
    img.paste(art, ((w - side) // 2, (h - side) // 2))
    return img.resize(size, Image.LANCZOS)


def generate(out_dir):
    os.makedirs(out_dir, exist_ok=True)
    for name in PRODUCT_ART:
        render(name, (720, 720)).save(os.path.join(out_dir, f"{name}.jpg"), quality=88)
    for cat, art in CATEGORY_ART.items():
        render(art, (960, 540)).save(os.path.join(out_dir, f"{cat}.jpg"), quality=88)


if __name__ == "__main__":
    import sys
    generate(sys.argv[1] if len(sys.argv) > 1 else "demo_images")
