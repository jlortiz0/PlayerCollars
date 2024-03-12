# Player Collars

Lets players wear pet collars. Only for 1.19.2 Forge at time of writing. I have no plans to port this to another version.

## Usage

Recipe is
```
 L 
LIL
 D 
```

where L is leather, I is a vanilla ingot or gem, and D is a dye.

When crafted, collars are red. They can be dyed similar to leather armor. Alternatively, shift right click to edit the colors manually. This menu also allows editing of the paw color (which is intially based on the dye used) and setting the collar's owner.

### Owner mechanics

Collars can have an "owner", the presence of which will affect the wearer of the collar. Owners can use leads to move the wearer of the collar. Some enchantments will provide additional effects.

### Enchantments

- Mending: Wearer will recieve Regeneration I when within 16 blocks of the owner.
- Loyalty: Wearer will be pulled closer to the owner when a lead is used. Default follow distance is 4 blocks, each level of Loyalty reduces this by 1 to a minimum of 2 blocks.
- Thorns: Works the same as it does on armor with no durability penalty.
- Curse of Binding: Works exactly like you think it does. I suggest using this mod with [Kawaii Dishes](https://www.curseforge.com/minecraft/mc-mods/kawaii-dishes) so that there is a way to remove bound items; this mod contains a Mixin that patches KawaiiDishes to also remove bound Curios.

### Clickers

...
```
 # 
OIO
 O
```

where `#` is a button, `O` is planks, and `I` is an iron ingot.

If an owner uses a clicker, nearby owned players will be forced to look at the owner. The radius is determined by the level of Lure the clicker is enchanted with.

Clickers can be dyed in the same manner as leather armor.

## Attribution

The player leashing code was derived from Fabric mod [Leashable Players](https://modrinth.com/mod/leashable-players). Additionally, the original collar model was derived from [Player Collars](https://www.curseforge.com/minecraft/mc-mods/player-collars), although it has been reworked.

This mod is licensed under the MIT license. Please try not to bully your players/partner(s) too hard with this mod. That's for me only.

i really have no shame do i
