# StoneWalled (NeoForge 1.21.1)

A client-side utility mod targeting **NeoForge 1.21.1 (Java 21)**. This version of the mod is configured for **always-on client-side enforcement**. It asks for nothing (no remote API calls or external checks) and programmatically enforces loading the hidden anti-Xray resource pack at all times, making it impossible to run the client without it.

---

## 🏛️ Architecture & Mechanics

### 1. In-Jar Dynamic Pack Creation (`PackHelper` & `SecurityPackSource`)
* **Asset Location:** The resource files reside inside the mod jar under the directory `/security_assets/` (which contains its own custom `pack.mcmeta`).
* **Instance Assembly:** Programmatically compiles a `Pack` instance using `Pack.readMetaAndCreate()` with:
  * `PackLocationInfo` containing a custom ID (`stonewalled:enforced_pack`), display name, and `PackSource.BUILT_IN`.
  * `PathPackResources` sourcing directly from the mod container's inside-the-jar path resolved through `ModList.get().getModContainerById()`.
  * `PackSelectionConfig` configured with `defaultEnabled = true`, `position = Pack.Position.TOP`, and `fixed = true`.
* **Automatic Registration:** The pack is registered directly via `AddPackFindersEvent` on client startup.

### 2. Repository Injection & Selection Enforcement (`PackRepositoryMixin`)
* **Active List Injection:** A mixin injects into `PackRepository#rebuildSelected` and automatically appends the security pack to the end of the selected list.
* **Top Priority:** Minecraft resolves resources in order; the last pack in the selected list overrides all previous textures. Appending our pack at the end guarantees it resides at the `TOP` priority.
* **Non-Removable:** The selection config defines it as `fixed`, preventing the client configuration from overriding or disabling it.

### 3. UI Hiding & Isolation (`PackSelectionModelMixin`)
* **Complete UI Isolation:** To prevent players from knowing this pack is applied, we redirect calls to `getAvailablePacks()` and `getSelectedPacks()` inside the `PackSelectionModel` constructor.
* **Invisible Card:** Our custom pack (`stonewalled:enforced_pack`) is filtered out of the model data, making it completely absent from the in-game "Resource Packs" configuration menu, leaving no interactable cards or visible traces in the UI.

---

## 📂 Project Directory Structure

```text
1.21.1/
├── build.gradle                                     # Mod build dependencies and gradle configuration
├── gradle.properties                                # Version declarations (NeoForge, Minecraft, etc.)
├── settings.gradle                                  # Plugin and repository configurations
├── README.md                                        # This documentation file
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── mrpotato/
        │           └── stonewalled/
        │               ├── StoneWalled.java          # Mod initialization and event handlers
        │               ├── PackHelper.java          # Programmatic pack construction helper
        │               └── mixin/
        │                   ├── PackRepositoryMixin.java # selected pack injection mixin
        │                   └── PackSelectionModelMixin.java # UI list hiding mixin
        └── resources/
            ├── META-INF/
            │   └── neoforge.mods.toml               # NeoForge mod metadata
            ├── stonewalled.mixins.json              # Mixin registration file
            └── security_assets/                     # Base directory for hidden resource pack
                └── pack.mcmeta                      # Resource pack metadata (Format 34)
```

---

## 🛠️ Building & Compiling

1. Place your assets (custom anti-xray textures) under `src/main/resources/security_assets/assets/minecraft/textures/block/`.
2. Build the mod using Gradle:
   ```bash
   gradle build
   ```

