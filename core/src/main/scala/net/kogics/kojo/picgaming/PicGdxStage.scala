package net.kogics.kojo.picgaming

import scala.collection.mutable.ArrayBuffer

class PicGdxStage {

  val pictures = ArrayBuffer.empty[Picture]

  def addPicture(p: Picture): Unit = {
    pictures.append(p)
  }

  def removePicture(p: Picture): Unit = {
//    pictures.fi
//    pictures.remove(p)
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
