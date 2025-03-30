# Player Collars

Lets players wear pet collars. See branches for possible versions. Some pre-compiled versions are available on [Modrinth](https://modrinth.com/mod/leashable-collars).

This mod requires [Trinkets](https://modrinth.com/mod/trinkets) for Fabric <=1.21.1 or [Accessories](https://modrinth.com/mod/accessories) for Fabric 1.21.4. For the legacy Forge version, use [Curios](https://www.curseforge.com/minecraft/mc-mods/curios).

## Usage

Collars can be crafted with the following recipe (any dye will work):

![Gold ingot with a dye below and leather on the other three sides](https://raw.githubusercontent.com/jlortiz0/PlayerCollars/assets/collar_recipe.png)

When crafted, collars are red. They can be dyed similar to leather armor. Alternatively, shift right click while holding the collar to edit the colors manually. This menu also allows editing of the paw color (blue by default) and setting the collar's owner.

### Owner mechanics

Collars can have an "owner", the presence of which will affect the wearer of the collar. Owners can use leads to move the wearer of the collar. Some enchantments will provide additional effects.
The maximum length of the lead can be added by modifying the `playercollars:leash_distance` attribute of the player. The default value is 6 but can be increaded up to 16 blocks!

`/attribute <username> playercollars:leash_distance base set 14`

### Enchantments

- Healing: Wearer will recieve Regeneration when within 16 blocks of the owner.
- Tight Leash: Wearer will be pulled closer to the owner when a lead is used. Default follow distance is 6 blocks, each level of Loyalty reduces this by 1 to a minimum of 2 blocks.
- Spiked: Works like Thorns, but with no durability penalty.

Collars can be enchanted in an enchanting table:

![Collar in an enchanting table](https://raw.githubusercontent.com/jlortiz0/PlayerCollars/assets/collar_enchant.png)

### Clickers

Clickers can be crafted with the following recipe (any wood type will work):

![Iron ingot with a button on top and planks on the other three sides](https://raw.githubusercontent.com/jlortiz0/PlayerCollars/assets/clicker_recipe.png)

If an owner uses a clicker, nearby owned players will be forced to look at the owner. The radius is determined by the level of Audible the clicker is enchanted with.

Like collars, clickers can be enchanted in an enchanting table and dyed in the same manner as leather armor.

## Attribution

The player leashing code was derived from [Leashable Players](https://modrinth.com/mod/leashable-players).

This mod is licensed under the MIT license. Please try not to bully your players/partner(s) too hard with this mod. That's for me only.

i really have no shame do i
