"""Derives the Overflow Chest textures from the vanilla copper chest textures: same weathering stages, but the (gray) lock is black.

Usage: python tools/make_textures.py <vanilla chest texture dir>   (e.g. assets/minecraft/textures/entity/chest from the Minecraft jar)
"""
import sys
from pathlib import Path
from PIL import Image

src = Path(sys.argv[1])
out = Path(__file__).resolve().parent.parent / "common/src/main/resources/assets/smartgolems/textures/entity/chest"
out.mkdir(parents=True, exist_ok=True)

STAGES = [("copper", "overflow"), ("copper_exposed", "overflow_exposed"),
          ("copper_weathered", "overflow_weathered"), ("copper_oxidized", "overflow_oxidized")]

# Single chests and the two halves of a double chest each have their own texture.
for part in ("", "_left", "_right"):
    # The lock is the only gray in the unaffected copper texture (copper itself is clearly orange); the same pixels are the lock in every stage.
    base = Image.open(src / f"copper{part}.png").convert("RGBA")
    lock = {(x, y) for y in range(base.height) for x in range(base.width)
            if base.getpixel((x, y))[3] and max(base.getpixel((x, y))[:3]) - min(base.getpixel((x, y))[:3]) < 30}
    for vanilla, ours in STAGES:
        im = Image.open(src / f"{vanilla}{part}.png").convert("RGBA")
        for x, y in lock:
            r, g, b, a = im.getpixel((x, y))
            v = int((r + g + b) / 3 * 0.22)
            im.putpixel((x, y), (v, v, v + 2, a))
        im.save(out / f"{ours}{part}.png")
        print(f"{ours}{part}", "lock pixels:", len(lock))
