package net.kogics.kojo.picgaming

import java.awt.Color

import scala.collection.mutable.ArrayBuffer

import net.kogics.kojo.gaming.WorldBounds

class PicGdxStage {

  val pictures = ArrayBuffer.empty[Picture]

  def addPicture(p: Picture): Unit = {
    pictures.append(p)
  }

  def removePicture(p: Picture): Unit = {
    var found = false
    var idx = 0
    while (idx < pictures.length && !found) {
      if (pictures(idx) == p) {
        found = true
      }
      else {
        idx += 1
      }
    }
    if (found) {
      pictures.remove(idx)
    }
  }

  def clear(): Unit = {
    pictures.clear()
  }

  def filledPictures: ArrayBuffer[VectorPicture] = pictures.collect {
    case p: VectorPicture if p.hasFill => p
  }

  def outlinedPictures: ArrayBuffer[VectorPicture] = pictures.collect {
    case p: VectorPicture if p.hasPen => p
  }

  def imagePictures: ArrayBuffer[RasterPicture] = pictures.collect {
    case p: RasterPicture => p
  }
}
