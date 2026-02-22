# FlowchartAR

An Android AR application that visualizes flowchart shapes in augmented reality.

## Features

✅ Beautiful splash screen with gradient animation
✅ Clean home screen with scan button
✅ Real-time camera preview with CameraX
✅ ARCore surface detection (when available)
✅ Flowchart shape detection using image recognition
✅ 3D-style rendering with shadows, gradients, and depth effects
✅ Support for 5 flowchart shapes:
  - Start/End (Ellipsoid)
  - Process (Cube)
  - Decision (Diamond)
  - Input/Output (Parallelogram)
  - Connector (Sphere)

## Current Implementation

The app currently uses **2.5D rendering** with Canvas drawing that simulates 3D with:
- Multiple faces (front, back, top, sides)
- Drop shadows
- Radial gradients for lighting effects
- Highlights for glossy appearance
- Proper perspective with offset faces

## Adding True 3D GLTF Models

To add real 3D GLTF/GLB models:

1. **Create or Download 3D Models**
   - Use Blender to create flowchart shapes
   - Export as GLTF 2.0 (.gltf or .glb)
   - Or download from sites like Sketchfab, Poly Haven

2. **Add Models to Project**
   ```
   app/src/main/assets/models/
   ├── cube.glb          (Process shape)
   ├── sphere.glb        (Start/End shape)
   ├── diamond.glb       (Decision shape)
   ├── cylinder.glb      (Input/Output shape)
   └── connector.glb     (Connector shape)
   ```

3. **Use Filament for Rendering**
   - Filament dependency is already added
   - Implement GLTF loader in ARViewScreen
   - Render models on detected AR planes

## For Professional 3D Rendering

### Option 1: Filament (Current approach)
```kotlin
// Load GLTF model
val assetLoader = AssetLoader(engine, materialProvider, entityManager)
val asset = assetLoader.createAssetFromBinary(buffer)
val instance = asset.getInstance()
scene.addEntity(instance)
```

### Option 2: Unity with AR Foundation
- Full 3D engine
- Visual editor for 3D scenes
- Export to Android
- Best for complex 3D interactions

### Option 3: Unreal Engine
- High-quality graphics
- Blueprint visual scripting
- AR support via ARCore plugin

## Requirements

- Android 7.0 (API 24) or higher
- Camera permission
- ARCore compatible device (optional - falls back to camera-only mode)

## Building

```bash
./gradlew assembleDebug
```

## Installation

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Current Limitations

- Uses 2.5D rendering instead of true 3D GLTF models
- ARCore surface detection requires ARCore app installation
- Image recognition is basic color-based detection

## Future Enhancements

- [ ] Add real GLTF 3D models
- [ ] Implement Filament rendering pipeline
- [ ] Use ML Kit for better shape detection
- [ ] Add gesture controls (rotate, scale shapes)
- [ ] Save AR scenes
- [ ] Share AR experiences

## License

GPL-3.0
