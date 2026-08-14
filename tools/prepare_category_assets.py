from pathlib import Path

from PIL import Image


SOURCE_DIR = Path("/home/ubuntu/webdev-static-assets")
TARGET_DIR = Path("/home/ubuntu/Alaa-IPTV/app/src/main/res/drawable-nodpi")
NAMES = ("live", "sports", "news", "movies", "series", "kids", "documentary", "music")
TARGET_SIZE = (920, 690)


def main() -> None:
    TARGET_DIR.mkdir(parents=True, exist_ok=True)
    for name in NAMES:
        source = SOURCE_DIR / f"alaa_category_{name}.png"
        target = TARGET_DIR / f"alaa_category_{name}.webp"
        with Image.open(source) as image:
            image.convert("RGB").resize(TARGET_SIZE, Image.Resampling.LANCZOS).save(
                target,
                "WEBP",
                quality=82,
                method=6,
            )
        print(f"Wrote {target}")


if __name__ == "__main__":
    main()
