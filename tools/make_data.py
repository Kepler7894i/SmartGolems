"""Writes the JSON assets and data (blockstates, item models, loot tables, recipes, tags, lang) for the eight Overflow Chest blocks."""
import json
from pathlib import Path

root = Path(__file__).resolve().parent.parent / "common/src/main/resources"
MOD = "smartgolems"
STAGES = [("", "copper", "Overflow Chest"), ("exposed_", "copper_exposed", "Exposed Overflow Chest"),
          ("weathered_", "copper_weathered", "Weathered Overflow Chest"), ("oxidized_", "copper_oxidized", "Oxidized Overflow Chest")]


def write(path, obj):
    path = root / path
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


lang = {}
names = []
for stage, texture, title in STAGES:
    for waxed in (False, True):
        name = ("waxed_" if waxed else "") + stage + "overflow_chest"
        names.append(name)
        lang[f"block.{MOD}.{name}"] = ("Waxed " if waxed else "") + title
        vanilla_model = f"minecraft:block/{stage}copper_chest"
        write(f"assets/{MOD}/blockstates/{name}.json", {"variants": {"": {"model": vanilla_model}}})
        write(f"assets/{MOD}/items/{name}.json", {"model": {
            "type": "minecraft:special", "base": f"minecraft:item/{stage}copper_chest",
            "model": {"type": "minecraft:chest", "texture": f"{MOD}:overflow" + ("" if not stage else "_" + stage.rstrip("_"))}}})
        write(f"data/{MOD}/loot_table/blocks/{name}.json", {
            "type": "minecraft:block",
            "pools": [{"rolls": 1, "condition": {"type": "minecraft:survives_explosion"}, "entries": [{
                "type": "minecraft:item", "name": f"{MOD}:{name}",
                "modifier": {"type": "minecraft:copy_components", "source": "block_entity", "include": ["minecraft:custom_name"]}}]}],
            "random_sequence": f"{MOD}:blocks/{name}"})

for stage, texture, title in STAGES:
    # Waxing by crafting, like the vanilla copper chests.
    base = stage + "overflow_chest"
    write(f"data/{MOD}/recipe/waxed_{base}_from_honeycomb.json", {
        "type": "minecraft:crafting_shapeless", "category": "building", "group": "waxed_overflow_chest",
        "ingredients": [f"{MOD}:{base}", "minecraft:honeycomb"], "result": {"id": f"{MOD}:waxed_{base}"}})

# Like a shulker box: two of the special material around a chest (copper blocks here, shulker shells there).
write(f"data/{MOD}/recipe/overflow_chest.json", {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "key": {"#": "minecraft:chest", "-": "minecraft:copper_block"},
    "pattern": ["-", "#", "-"], "result": {"id": f"{MOD}:overflow_chest"}})
write(f"data/{MOD}/advancement/recipes/overflow_chest.json", {
    "parent": "minecraft:recipes/root",
    "criteria": {"has_copper_block": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": "minecraft:copper_block"}]}},
                 "has_the_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipes": f"{MOD}:overflow_chest"}}},
    "requirements": [["has_the_recipe", "has_copper_block"]],
    "rewards": {"recipes": [f"{MOD}:overflow_chest"]}})

values = [f"{MOD}:{n}" for n in names]
write("data/minecraft/tags/block/mineable/pickaxe.json", {"values": values})
write("data/minecraft/tags/block/needs_stone_tool.json", {"values": values})

write(f"assets/{MOD}/lang/en_us.json", lang)
print("wrote", len(names), "blocks")
