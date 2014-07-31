// package thirdparty.mortennobel

// import com.sksamuel.scrimage.{Image, Raster}
// import ResampleOp._
// //remove if not needed
// import scala.collection.JavaConversions._

// object ResampleOp {

//   private val MAX_CHANNEL_VALUE = 255

//   case class SubSamplingData(
//     arrN: Array[Int],
//     arrPixel: Array[Int],
//     arrWeight: Array[Float],
//     numContributors: Int)

//   def createSubSampling(filter: ResampleFilter, srcSize: Int, dstSize: Int): SubSamplingData = {
//     val scale = dstSize.toFloat / srcSize.toFloat
//     val arrN = Array.ofDim[Int](dstSize)
//     var numContributors: Int = 0
//     var arrWeight: Array[Float] = null
//     var arrPixel: Array[Int] = null
//     val fwidth = filter.getSamplingRadius
//     val centerOffset = 0.5f / scale
//     if (scale < 1.0f) {
//       val width = fwidth / scale
//       numContributors = (width * 2.0f + 2).toInt
//       arrWeight = Array.ofDim[Float](dstSize * numContributors)
//       arrPixel = Array.ofDim[Int](dstSize * numContributors)
//       val fNormFac = (1f / (Math.ceil(width) / fwidth)).toFloat
//       for (i <- 0 until dstSize) {
//         val subindex = i * numContributors
//         val center = i / scale + centerOffset
//         val left = Math.floor(center - width).toInt
//         val right = Math.ceil(center + width).toInt
//         var j = left
//         while (j <= right) {
//           var weight: Float = 0.0f
//           weight = filter.apply((center - j) * fNormFac)
//           if (weight == 0.0f) {
//             //continue
//           }
//           var n: Int = 0
//           n = if (j < 0) -j else if (j >= srcSize) srcSize - j + srcSize - 1 else j
//           val k = arrN(i)
//           arrN(i) += 1
//           if (n < 0 || n >= srcSize) {
//             weight = 0.0f
//           }
//           arrPixel(subindex + k) = n
//           arrWeight(subindex + k) = weight
//           j += 1
//         }
//         val max = arrN(i)
//         var tot = 0
//         for (k <- 0 until max) tot += arrWeight(subindex + k)
//         if (tot != 0f) {
//           for (k <- 0 until max) arrWeight(subindex + k) /= tot
//         }
//       }
//     } else {
//       numContributors = (fwidth * 2.0f + 1).toInt
//       arrWeight = Array.ofDim[Float](dstSize * numContributors)
//       arrPixel = Array.ofDim[Int](dstSize * numContributors)
//       for (i <- 0 until dstSize) {
//         val subindex = i * numContributors
//         val center = i / scale + centerOffset
//         val left = Math.floor(center - fwidth).toInt
//         val right = Math.ceil(center + fwidth).toInt
//         var j = left
//         while (j <= right) {
//           var weight = filter.apply(center - j)
//           if (weight == 0.0f) {
//             //continue
//           }
//           var n: Int = 0
//           n = if (j < 0) -j else if (j >= srcSize) srcSize - j + srcSize - 1 else j
//           val k = arrN(i)
//           arrN(i) += 1
//           if (n < 0 || n >= srcSize) {
//             weight = 0.0f
//           }
//           arrPixel(subindex + k) = n
//           arrWeight(subindex + k) = weight
//           j += 1
//         }
//         val max = arrN(i)
//         var tot = 0
//         for (k <- 0 until max) tot += arrWeight(subindex + k)
//         assert(tot != 0) : "should never happen except bug in filter"
//         if (tot != 0f) {
//           for (k <- 0 until max) arrWeight(subindex + k) /= tot
//         }
//       }
//     }
//     new SubSamplingData(arrN, arrPixel, arrWeight, numContributors)
//   }
// }

// class ResampleOp(
//     val numberOfThreads: Int,
//     val filter: ResampleFilter,
//     destWidth: Int,
//     destHeight: Int) extends AdvancedResizeOp(destWidth, destHeight) {

//   private var nrChannels: Int = _

//   private var srcWidth: Int = _

//   private var srcHeight: Int = _

//   private var dstWidth: Int = _

//   private var dstHeight: Int = _

//   private var horizontalSubsamplingData: SubSamplingData = _

//   private var verticalSubsamplingData: SubSamplingData = _

//   def doFilter(srcImg: Image,
//       dstWidth: Int,
//       dstHeight: Int): Image = {
//     this.dstWidth = dstWidth
//     this.dstHeight = dstHeight
//     if (dstWidth < 3 || dstHeight < 3) {
//       throw new RuntimeException("Error doing rescale. Target size was " + dstWidth + "x" +
//         dstHeight +
//         " but must be" +
//         " at least 3x3.")
//     }
//     if (srcImg.getType == BufferedImage.TYPE_BYTE_BINARY || srcImg.getType == BufferedImage.TYPE_BYTE_INDEXED ||
//       srcImg.getType == BufferedImage.TYPE_CUSTOM) srcImg = ImageUtils.convert(srcImg, if (srcImg.getColorModel.hasAlpha()) BufferedImage.TYPE_4BYTE_ABGR else BufferedImage.TYPE_3BYTE_BGR)
//     this.nrChannels = ImageUtils.nrChannels(srcImg)
//     assert(nrChannels > 0)
//     this.srcWidth = srcImg.getWidth
//     this.srcHeight = srcImg.getHeight
//     var workPixels = Array.ofDim[Byte](srcHeight, dstWidth * nrChannels)
//     horizontalSubsamplingData = createSubSampling(filter, srcWidth, dstWidth)
//     verticalSubsamplingData = createSubSampling(filter, srcHeight, dstHeight)
//     val scrImgCopy = srcImg
//     val workPixelsCopy = workPixels
//     val threads = Array.ofDim[Thread](numberOfThreads - 1)
//     for (i <- 1 until numberOfThreads) {
//       val finalI = i
//       threads(i - 1) = new Thread(new Runnable() {

//         def run() {
//           horizontallyFromSrcToWork(scrImgCopy, workPixelsCopy, finalI, numberOfThreads)
//         }
//       })
//       threads(i - 1).start()
//     }
//     horizontallyFromSrcToWork(scrImgCopy, workPixelsCopy, 0, numberOfThreads)
//     waitForAllThreads(threads)
//     val outPixels = Array.ofDim[Byte](dstWidth * dstHeight * nrChannels)
//     val outPixelsCopy = outPixels
//     for (i <- 1 until numberOfThreads) {
//       val finalI = i
//       threads(i - 1) = new Thread(new Runnable() {

//         def run() {
//           verticalFromWorkToDst(workPixelsCopy, outPixelsCopy, finalI, numberOfThreads)
//         }
//       })
//       threads(i - 1).start()
//     }
//     verticalFromWorkToDst(workPixelsCopy, outPixelsCopy, 0, numberOfThreads)
//     waitForAllThreads(threads)
//     workPixels = null
//     var out: BufferedImage = new BufferedImage(dstWidth, dstHeight, getResultBufferedImageType(srcImg))
//     ImageUtils.setBGRPixels(outPixels, out, 0, 0, dstWidth, dstHeight)
//     out
//   }

//   private def waitForAllThreads(threads: Array[Thread]) {
//     try {
//       for (t <- threads) {
//         t.join(java.lang.Long.MAX_VALUE)
//       }
//     } catch {
//       case e: InterruptedException => {
//         e.printStackTrace()
//         throw new RuntimeException(e)
//       }
//     }
//   }

//   private def verticalFromWorkToDst(workPixels: Array[Array[Byte]],
//       outPixels: Array[Byte],
//       start: Int,
//       delta: Int) {
//     if (nrChannels == 1) {
//       verticalFromWorkToDstGray(workPixels, outPixels, start, numberOfThreads)
//       return
//     }
//     val useChannel3 = nrChannels > 3
//     var x = start
//     while (x < dstWidth) {
//       val xLocation = x * nrChannels
//       var y = dstHeight - 1
//       while (y >= 0) {
//         val yTimesNumContributors = y * verticalSubsamplingData.numContributors
//         val max = verticalSubsamplingData.arrN(y)
//         val sampleLocation = (y * dstWidth + x) * nrChannels
//         var sample0 = 0.0f
//         var sample1 = 0.0f
//         var sample2 = 0.0f
//         var sample3 = 0.0f
//         var index = yTimesNumContributors
//         var j = max - 1
//         while (j >= 0) {
//           val valueLocation = verticalSubsamplingData.arrPixel(index)
//           val arrWeight = verticalSubsamplingData.arrWeight(index)
//           sample0 += (workPixels(valueLocation)(xLocation) & 0xff) * arrWeight
//           sample1 += (workPixels(valueLocation)(xLocation + 1) & 0xff) * arrWeight
//           sample2 += (workPixels(valueLocation)(xLocation + 2) & 0xff) * arrWeight
//           if (useChannel3) {
//             sample3 += (workPixels(valueLocation)(xLocation + 3) & 0xff) * arrWeight
//           }
//           index += 1
//           j -= 1
//         }
//         outPixels(sampleLocation) = toByte(sample0)
//         outPixels(sampleLocation + 1) = toByte(sample1)
//         outPixels(sampleLocation + 2) = toByte(sample2)
//         if (useChannel3) {
//           outPixels(sampleLocation + 3) = toByte(sample3)
//         }
//         y -= 1
//       }
//       x += delta
//     }
//   }

//   private def verticalFromWorkToDstGray(workPixels: Array[Array[Byte]],
//       outPixels: Array[Byte],
//       start: Int,
//       delta: Int) {
//     var x = start
//     while (x < dstWidth) {
//       val xLocation = x
//       var y = dstHeight - 1
//       while (y >= 0) {
//         val yTimesNumContributors = y * verticalSubsamplingData.numContributors
//         val max = verticalSubsamplingData.arrN(y)
//         val sampleLocation = (y * dstWidth + x)
//         var sample0 = 0.0f
//         var index = yTimesNumContributors
//         var j = max - 1
//         while (j >= 0) {
//           val valueLocation = verticalSubsamplingData.arrPixel(index)
//           val arrWeight = verticalSubsamplingData.arrWeight(index)
//           sample0 += (workPixels(valueLocation)(xLocation) & 0xff) * arrWeight
//           index += 1
//           j -= 1
//         }
//         outPixels(sampleLocation) = toByte(sample0)
//         y -= 1
//       }
//       x += delta
//     }
//   }

//   private def horizontallyFromSrcToWork(srcImg: BufferedImage,
//       workPixels: Array[Array[Byte]],
//       start: Int,
//       delta: Int) {
//     if (nrChannels == 1) {
//       horizontallyFromSrcToWorkGray(srcImg, workPixels, start, delta)
//       return
//     }
//     val tempPixels = Array.ofDim[Int](srcWidth)
//     val srcPixels = Array.ofDim[Byte](srcWidth * nrChannels)
//     val useChannel3 = nrChannels > 3
//     var k = start
//     while (k < srcHeight) {
//       ImageUtils.getPixelsBGR(srcImg, k, srcWidth, srcPixels, tempPixels)
//       var i = dstWidth - 1
//       while (i >= 0) {
//         val sampleLocation = i * nrChannels
//         val max = horizontalSubsamplingData.arrN(i)
//         var sample0 = 0.0f
//         var sample1 = 0.0f
//         var sample2 = 0.0f
//         var sample3 = 0.0f
//         var index = i * horizontalSubsamplingData.numContributors
//         var j = max - 1
//         while (j >= 0) {
//           val arrWeight = horizontalSubsamplingData.arrWeight(index)
//           val pixelIndex = horizontalSubsamplingData.arrPixel(index) * nrChannels
//           sample0 += (srcPixels(pixelIndex) & 0xff) * arrWeight
//           sample1 += (srcPixels(pixelIndex + 1) & 0xff) * arrWeight
//           sample2 += (srcPixels(pixelIndex + 2) & 0xff) * arrWeight
//           if (useChannel3) {
//             sample3 += (srcPixels(pixelIndex + 3) & 0xff) * arrWeight
//           }
//           index += 1
//           j -= 1
//         }
//         workPixels(k)(sampleLocation) = toByte(sample0)
//         workPixels(k)(sampleLocation + 1) = toByte(sample1)
//         workPixels(k)(sampleLocation + 2) = toByte(sample2)
//         if (useChannel3) {
//           workPixels(k)(sampleLocation + 3) = toByte(sample3)
//         }
//         i -= 1
//       }
//       k = k + delta
//     }
//   }

//   private def horizontallyFromSrcToWorkGray(srcImg: BufferedImage,
//       workPixels: Array[Array[Byte]],
//       start: Int,
//       delta: Int) {
//     val tempPixels = Array.ofDim[Int](srcWidth)
//     val srcPixels = Array.ofDim[Byte](srcWidth)
//     var k = start
//     while (k < srcHeight) {
//       ImageUtils.getPixelsBGR(srcImg, k, srcWidth, srcPixels, tempPixels)
//       var i = dstWidth - 1
//       while (i >= 0) {
//         val sampleLocation = i
//         val max = horizontalSubsamplingData.arrN(i)
//         var sample0 = 0.0f
//         var index = i * horizontalSubsamplingData.numContributors
//         var j = max - 1
//         while (j >= 0) {
//           val arrWeight = horizontalSubsamplingData.arrWeight(index)
//           val pixelIndex = horizontalSubsamplingData.arrPixel(index)
//           sample0 += (srcPixels(pixelIndex) & 0xff) * arrWeight
//           index += 1
//           j -= 1
//         }
//         workPixels(k)(sampleLocation) = toByte(sample0)
//         i -= 1
//       }
//       k = k + delta
//     }
//   }

//   private def toByte(f: Float): Byte = {
//     if (f < 0) {
//       return 0
//     }
//     if (f > MAX_CHANNEL_VALUE) {
//       return MAX_CHANNEL_VALUE.toByte
//     }
//     (f + 0.5f).toByte
//   }

//   protected def getResultBufferedImageType(srcImg: BufferedImage): Int = {
//     if (nrChannels == 3) BufferedImage.TYPE_3BYTE_BGR
//     else{
//       if (nrChannels == 4) BufferedImage.TYPE_4BYTE_ABGR
//       else (if (srcImg.getSampleModel.getDataType == DataBuffer.TYPE_USHORT) BufferedImage.TYPE_USHORT_GRAY else BufferedImage.TYPE_BYTE_GRAY))
//     }
//   }
// }
