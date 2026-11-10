# Sword Throw (Fabric 1.20.1)

Throw swords like daggers, stick them in blocks, pull them out, with first-person arm animations.

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
Locally: `gradle build` (Java 17, Gradle 8.5+), jar in `build/libs/`.
