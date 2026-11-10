# Sword Throw (Fabric 1.21.1)

Throw swords like daggers, stick them in blocks, pull them out, with first-person arm animations.

The 1.20.1 version lives on the `1.20.1` branch.

## Controls
- **R** (rebindable): throw the sword in your main hand
- **Right-click** a stuck sword (main hand): pull it out and get it back

## Features
- Thrown swords deal their normal attack damage and lose 1 durability per hit
- Swords stick in blocks and stay there (no despawn, no auto-pickup)
- Creative-mode throws don't consume the sword and can't be duped
- First-person wind-up/throw animation and a pull-out tug animation (client mixin on `HeldItemRenderer`)

## Build
Push to GitHub: the **build** workflow produces `sword-throw-jar` under Actions > Artifacts.
Locally: `gradle build` (Java 21, Gradle 8.10+), jar in `build/libs/`.
Requires Fabric Loader 0.16+ and Fabric API 0.105.0+1.21.1.
