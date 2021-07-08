# Litematica-Printer

## Setup

To set this up just add the latest litematica version to your mods folder as well as this, it will replace easy place with printer. This does not use accurate placement protocal and therefore means that in order to place blocks in some rotations, you need to rotate your player. If I stop lazy, I will add one that does use accurate placement.

## Settings

### Printer settings:

`easyPlaceModeRange (x,y,z)`:&emsp;	"X,Y,Z Range for EasyPlace"
`easyPlaceModeMaxBlocks`:&emsp;		"Max block interactions per cycle"
`easyPlaceModeBreakBlocks`:&emsp;	"Automatically breaks blocks. Currently only works in creative."

### Handy litematica settings:

`easyPlaceMode`:&emsp;				"When enabled, then simply trying to use an item/place a block\non schematic blocks will place that block in that position."
`easyPlaceModeHoldEnabled`:&emsp;	"When enabled, then simply holding down the use key\nand looking at different schematic blocks will place them"
`easyPlaceClickAdjacent`:&emsp;		"If enabled, then the Easy Place mode will try to\nclick on existing adjacent blocks. This may help on Spigot\nor similar servers, which don't allow clicking on air blocks."
`pickBlockAuto`:&emsp;				"Automatically pick block before every placed block"
`pickBlockEnabled`:&emsp;			"Enables the schematic world pick block hotkeys.\nThere is also a hotkey for toggling this option to toggle those hotkeys... o.o", "Pick Block Hotkeys"
`pickBlockIgnoreNBT`:&emsp;			"Ignores the NBT data on the expected vs. found items for pick block.\nAllows the pick block to work for example with renamed items."
`pickBlockableSlots`:&emsp;			"The hotbar slots that are allowed to be\nused for the schematic pick block.\nCan use comma separated individual slots and dash\nseparated slot ranges (no spaces anywhere).\nExample: 2,4-6,9"
`placementInfrontOfPlayer`:&emsp;	"When enabled, created placements or moved placements are\npositioned so that they are fully infront of the player,\ninstead of the placement's origin point being at the player's location"
`renderMaterialListInGuis`:&emsp;	"Whether or not the material list should\nbe rendered inside GUIs"
`signTextPaste`:&emsp;				"Automatically set the text in the sign GUIs from the schematic"

`easyPlaceActivation`:&emsp;		"When the easyPlaceMode is enabled, this key must\nbe held to enable placing the blocks when\nusing the vanilla Use key"
`easyPlaceToggle`:&emsp;			"Allows quickly toggling on/off the Easy Place mode"

## Support
If you have any issues with this mod **DO NOT** contact and bother masa with it. Please message me in discord, I am usually around Scicraft, Mechanists, and Hekate. 

## Credits
Masa is the writer of the actual litematica mod and allowed all of this to be possible.
Andrews is the one who made the litematica printer implimentation, I just converted it to mixin.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
