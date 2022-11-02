package stage2

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import java.lang.RuntimeException
import javax.imageio.ImageIO

val VALID_BIT_VALUES = arrayOf(24,32)

fun main() {

    try {
        val inputBufferedImage = getInputImage()
        val watermarkBufferedImage = getValidWaterMark()
        ensureSameDimensions(inputBufferedImage, watermarkBufferedImage)
        val weight = getValidWaterMarkWeight()
        val outputFilename = getValidOutPutFileName()
        val resultImage = blendImage(inputBufferedImage, watermarkBufferedImage, weight)
        ImageIO.write(resultImage, outputFilename.split(".").last(), File(outputFilename))

        println("The watermarked image $outputFilename has been created.")
    }
    catch (ex: RuntimeException) {
        println(ex.message)
    }
}

/**
 * Get a valid Image from the user and convert it to buffered Image.
 */
fun getInputImage(): BufferedImage {
    println("Input the image filename:")
    val fileName = readln()

    val image = File(fileName)
    if (!image.exists()){
        throw RuntimeException("The file $fileName doesn't exists")
    }

    val bufferedImage = ImageIO.read(image)
    if (bufferedImage.colorModel.numColorComponents != 3){
        throw RuntimeException("The number of image color components isn't 3.")
    }
    else if (bufferedImage.colorModel.pixelSize !in VALID_BIT_VALUES){
        throw RuntimeException("The image isn't 24 or 32-bit.")
    }
    return bufferedImage
}

fun getValidWaterMark(): BufferedImage {
    println("Input the watermark image filename:")
    val filename = readln()

    val image = File(filename)
    if (!image.exists()) {
        throw RuntimeException("The file $filename doesn't exist.")
    }

    val bufferedImage = ImageIO.read(image)
    if (bufferedImage.colorModel.numColorComponents != 3) {
        throw RuntimeException("The number of watermark color components isn't 3.")
    } else if (bufferedImage.colorModel.pixelSize !in VALID_BIT_VALUES) {
        throw RuntimeException("The watermark isn't 24 or 32-bit.")
    }
    return bufferedImage
}

fun getValidWaterMarkWeight(): Int {
    println("Input the watermark transparency percentage (Integer 0-100):")
    val input = readln()

    if (!input.matches(Regex("\\d+"))){
        throw RuntimeException("The transparency percentage isn't an integer number.")
    }

    val tr = input.toInt()
    if (tr !in 0..100)
        throw RuntimeException("The transparency percentage is out of range.")

    return tr
}

fun getValidOutPutFileName(): String {
    println("Input the output image filename (jpg or png extension):")
    val input = readln()

    if (!input.endsWith(".jpg") && !input.endsWith(".png"))
        throw RuntimeException("The output file extension isn't \"jpg\" or \"png\".")

    return input
}

fun blendImage(image: BufferedImage, watermark: BufferedImage, weight: Int): BufferedImage {
    val resultImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)

    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val i = Color(image.getRGB(x,y))
            val w = Color(watermark.getRGB(x,y))

            val color = Color(
                (weight * w.red + (100 - weight) * i.red) / 100,
                (weight * w.green + (100 - weight) * i.green) / 100,
                (weight * w.blue + (100 - weight) * i.blue) / 100
            )
            resultImage.setRGB(x, y, color.rgb)
        }
    }

    return resultImage
}

fun ensureSameDimensions(image: BufferedImage, watermark: BufferedImage) {
    if (image.width != watermark.width || image.height != watermark.height)
        throw RuntimeException("The image and watermark dimensions are different.")
}

fun printInfo(filename: String, bi: BufferedImage) {
    println("Image file: $filename")

    println("Width: ${bi.width}")
    println("Height: ${bi.height}")
    println("Number of components: ${bi.colorModel.numComponents}")
    println("Number of color components: ${bi.colorModel.numColorComponents}")
    println("Bits per pixel: ${bi.colorModel.pixelSize}")
    println("Transparency: ${getTransparency(bi.transparency)}")

}

fun getTransparency(t: Int) : String {
    return when(t) {
        Transparency.OPAQUE -> "OPAQUE"
        Transparency.BITMASK -> "BITMASK"
        Transparency.TRANSLUCENT -> "TRANSLUCENT"
        else -> throw IllegalArgumentException("illegal value for Transparency: $t")
    }
}