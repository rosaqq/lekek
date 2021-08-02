![Requires Java 11+](https://img.shields.io/badge/Java-11%2B-blue)
# LWJGL project
- Wannabe Game Engine
- Wannabe Multiplayer Game

## Core ideas to implement
- Logically organised render engine
- Collisions
- Multiplayer
- Terrain

## Sorta abstract road map

- [ ] Working collisions
- [ ] Player Character
- [ ] Walkable terrain
- [ ] Multiplayer
- [ ] A GUN?

## Run instructions

Inside project folder open cmd and do:

`./gradlew run` to run on linux  
`gradlew.bat run` to run on windows

wow such gradle many amaze

## (Probably outdated) controls
`a w s d` to move
`space shift` for up/down
`mouse` to look around

`t` to open command line
`left click` to select gameItem

`arrow keys` to move selected item
`x y z` to rotate selected item
`j` to rotate selected item in world Y axis

press power button to paint screen black

# About
This section holds documentation of the inner workings of the engine.

## Game Item concept
Everything that's renderable must implement `IRenderable`.  
Currently, there's:
- `AbstractGameItem` - Has Position, Scale, Rotation and Mesh attributes and requires children to implement a render method.
- `SkyBox` - Exists to provide a `render` implementation without the other attributes.

`AbstractGameItem` is extended by:
- `HudElement` - A special `render` implementation to draw the HUD.
- `Phantom` - Traditional `render` implementation to display geometries in the world.
   - Extended by `Collider` - Adds physics capabilities. (Should we interface this?)
    
`Terrain` should fit in here somewhere... currently WIP.

### Summary Chart:  
![Renderable Chart](https://i.imgur.com/DyHGZi6.png)

## Scene
Everything required to construct a level:
- `Terrain`  
  Terrain rendering note: while terrain is made of `AbstractGameItem` chunks, these are not included in the `scene.getGameItems()` list.  
  They are separately rendered inside `renderScene()` with:
  ```java
  for (AbstractGameItem terrainBlock : scene.getTerrain().getGameItems()) {
            terrainBlock.render(shaderProgram, viewMatrix);
  }
  ```
- `GameItems`
- `SkyBox`
- `Lighting`

## Camera
Camera uses Quaternion rotation (maybe not stonks performance wise?).  
Key concepts:
- Yaw is **global**: whatever happens always yaw on global Y Axis.  
  Apply Yaw before any transformation (Obj Y = Global Y)
- Pitch is **local**: always pitches up/down from current yaw.  
  Apply pitch on axis after yaw transformation

Thus, the order is `pitch * this.rotation * yaw`.  
Conclusion: multiply on the right to apply first.

## DBZ Collision Engine
Sorcery fo sure  
![I am confuse](https://i.imgur.com/55r5cV1.png)