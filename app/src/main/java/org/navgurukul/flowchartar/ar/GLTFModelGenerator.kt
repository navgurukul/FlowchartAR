package org.navgurukul.flowchartar.ar

import android.content.Context
import org.navgurukul.flowchartar.model.ShapeType
import java.io.File
import java.io.FileOutputStream

/**
 * Generates simple GLTF models for flowchart shapes
 */
object GLTFModelGenerator {
    
    fun generateModels(context: Context) {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
        
        // Generate each shape model
        generateCube(File(modelsDir, "cube.gltf"))
        generateSphere(File(modelsDir, "sphere.gltf"))
        generateDiamond(File(modelsDir, "diamond.gltf"))
        generateCylinder(File(modelsDir, "cylinder.gltf"))
    }
    
    private fun generateCube(file: File) {
        val gltf = """
{
  "asset": {
    "version": "2.0",
    "generator": "FlowchartAR"
  },
  "scene": 0,
  "scenes": [{"nodes": [0]}],
  "nodes": [{"mesh": 0}],
  "meshes": [{
    "primitives": [{
      "attributes": {"POSITION": 0, "NORMAL": 1},
      "indices": 2,
      "material": 0
    }]
  }],
  "materials": [{
    "pbrMetallicRoughness": {
      "baseColorFactor": [0.13, 0.59, 0.95, 1.0],
      "metallicFactor": 0.0,
      "roughnessFactor": 0.5
    }
  }],
  "accessors": [
    {"bufferView": 0, "componentType": 5126, "count": 24, "type": "VEC3", "max": [0.5, 0.5, 0.5], "min": [-0.5, -0.5, -0.5]},
    {"bufferView": 1, "componentType": 5126, "count": 24, "type": "VEC3"},
    {"bufferView": 2, "componentType": 5123, "count": 36, "type": "SCALAR"}
  ],
  "bufferViews": [
    {"buffer": 0, "byteOffset": 0, "byteLength": 288},
    {"buffer": 0, "byteOffset": 288, "byteLength": 288},
    {"buffer": 0, "byteOffset": 576, "byteLength": 72}
  ],
  "buffers": [{"uri": "data:application/octet-stream;base64,${getCubeData()}"}]
}
        """.trimIndent()
        
        file.writeText(gltf)
    }
    
    private fun generateSphere(file: File) {
        val gltf = """
{
  "asset": {"version": "2.0", "generator": "FlowchartAR"},
  "scene": 0,
  "scenes": [{"nodes": [0]}],
  "nodes": [{"mesh": 0}],
  "meshes": [{
    "primitives": [{
      "attributes": {"POSITION": 0, "NORMAL": 1},
      "indices": 2,
      "material": 0
    }]
  }],
  "materials": [{
    "pbrMetallicRoughness": {
      "baseColorFactor": [0.30, 0.69, 0.31, 1.0],
      "metallicFactor": 0.0,
      "roughnessFactor": 0.5
    }
  }],
  "accessors": [
    {"bufferView": 0, "componentType": 5126, "count": 12, "type": "VEC3", "max": [0.5, 0.5, 0.5], "min": [-0.5, -0.5, -0.5]},
    {"bufferView": 1, "componentType": 5126, "count": 12, "type": "VEC3"},
    {"bufferView": 2, "componentType": 5123, "count": 36, "type": "SCALAR"}
  ],
  "bufferViews": [
    {"buffer": 0, "byteOffset": 0, "byteLength": 144},
    {"buffer": 0, "byteOffset": 144, "byteLength": 144},
    {"buffer": 0, "byteOffset": 288, "byteLength": 72}
  ],
  "buffers": [{"uri": "data:application/octet-stream;base64,${getSphereData()}"}]
}
        """.trimIndent()
        
        file.writeText(gltf)
    }
    
    private fun generateDiamond(file: File) {
        val gltf = """
{
  "asset": {"version": "2.0", "generator": "FlowchartAR"},
  "scene": 0,
  "scenes": [{"nodes": [0]}],
  "nodes": [{"mesh": 0}],
  "meshes": [{
    "primitives": [{
      "attributes": {"POSITION": 0, "NORMAL": 1},
      "indices": 2,
      "material": 0
    }]
  }],
  "materials": [{
    "pbrMetallicRoughness": {
      "baseColorFactor": [1.0, 0.76, 0.03, 1.0],
      "metallicFactor": 0.0,
      "roughnessFactor": 0.5
    }
  }],
  "accessors": [
    {"bufferView": 0, "componentType": 5126, "count": 6, "type": "VEC3", "max": [0.5, 0.5, 0.5], "min": [-0.5, -0.5, -0.5]},
    {"bufferView": 1, "componentType": 5126, "count": 6, "type": "VEC3"},
    {"bufferView": 2, "componentType": 5123, "count": 24, "type": "SCALAR"}
  ],
  "bufferViews": [
    {"buffer": 0, "byteOffset": 0, "byteLength": 72},
    {"buffer": 0, "byteOffset": 72, "byteLength": 72},
    {"buffer": 0, "byteOffset": 144, "byteLength": 48}
  ],
  "buffers": [{"uri": "data:application/octet-stream;base64,${getDiamondData()}"}]
}
        """.trimIndent()
        
        file.writeText(gltf)
    }
    
    private fun generateCylinder(file: File) {
        val gltf = """
{
  "asset": {"version": "2.0", "generator": "FlowchartAR"},
  "scene": 0,
  "scenes": [{"nodes": [0]}],
  "nodes": [{"mesh": 0}],
  "meshes": [{
    "primitives": [{
      "attributes": {"POSITION": 0, "NORMAL": 1},
      "indices": 2,
      "material": 0
    }]
  }],
  "materials": [{
    "pbrMetallicRoughness": {
      "baseColorFactor": [0.61, 0.15, 0.69, 1.0],
      "metallicFactor": 0.0,
      "roughnessFactor": 0.5
    }
  }],
  "accessors": [
    {"bufferView": 0, "componentType": 5126, "count": 24, "type": "VEC3", "max": [0.5, 0.5, 0.5], "min": [-0.5, -0.5, -0.5]},
    {"bufferView": 1, "componentType": 5126, "count": 24, "type": "VEC3"},
    {"bufferView": 2, "componentType": 5123, "count": 36, "type": "SCALAR"}
  ],
  "bufferViews": [
    {"buffer": 0, "byteOffset": 0, "byteLength": 288},
    {"buffer": 0, "byteOffset": 288, "byteLength": 288},
    {"buffer": 0, "byteOffset": 576, "byteLength": 72}
  ],
  "buffers": [{"uri": "data:application/octet-stream;base64,${getCylinderData()}"}]
}
        """.trimIndent()
        
        file.writeText(gltf)
    }
    
    // Base64 encoded binary data for cube vertices
    private fun getCubeData(): String {
        // Simplified - in production, generate proper vertex data
        return "AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/"
    }
    
    private fun getSphereData(): String {
        return "AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/"
    }
    
    private fun getDiamondData(): String {
        return "AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/"
    }
    
    private fun getCylinderData(): String {
        return "AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/"
    }
}
