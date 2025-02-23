# Player Collars

Lets players wear pet collars. See branches for possible versions. Some pre-compiled versions are available on [Modrinth](https://modrinth.com/mod/leashable-collars).

This mod requires [Curios](https://www.curseforge.com/minecraft/mc-mods/curios) under Forge and [Trinkets](https://modrinth.com/mod/trinkets) under fabric or [Accessories](https://modrinth.com/mod/accessories) for 1.21.4.

## Usage
Collars can be crafted with the following recipe:

![image](https://github.com/user-attachments/assets/d1c56231-384e-450e-b7c2-a873d78a7cbe)


When crafted, collars are red. They can be dyed similar to leather armor. Alternatively, shift right click (while holding the collar) to edit the colors manually. This menu also allows editing of the paw color (blue by default) and setting the collar's owner.

### Owner mechanics

Collars can have an "owner", the presence of which will affect the wearer of the collar. Owners can use leads to move the wearer of the collar. Some enchantments will provide additional effects.
The maximum length of the lead can be added by modifying the `playercollars:leash_distance` attribute of the player. The default value is 6 but can be increaded up to 16 blocks!

`/attribute <username> playercollars:leash_distance base set 14`

### Enchantments

- Healing: Wearer will recieve Regeneration when within 16 blocks of the owner.
- Tight Leash: Wearer will be pulled closer to the owner when a lead is used. Default follow distance is 6 blocks, each level of Loyalty reduces this by 1 to a minimum of 2 blocks.
- Spiked: Works like Thorns, but with no durability penalty.

Collars can be enchanted in an enchanting table

![image](https://github.com/user-attachments/assets/63cc26f4-067b-44aa-848a-0d2e4d773082)


### Clickers
Clickers can be crafted with the following recipe:

![image](https://github.com/user-attachments/assets/909ef7e4-bdde-483f-8718-443c7ca7bad7)


If an owner uses a clicker, nearby owned players will be forced to look at the owner. The radius is determined by the level of Audible the clicker is enchanted with.

Clickers can be enchanted in an enchanting table

![image](https://github.com/user-attachments/assets/193809fd-21cb-4ef3-b432-055186464393)


Clickers can be dyed in the same manner as leather armor.

## Attribution

The player leashing code was derived from [Leashable Players](https://modrinth.com/mod/leashable-players).

This mod is licensed under the MIT license. Please try not to bully your players/partner(s) too hard with this mod. That's for me only.

i really have no shame do i
