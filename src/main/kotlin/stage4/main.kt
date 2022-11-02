package stage4

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import java.lang.IllegalArgumentException
import javax.imageio.ImageIO

val VALID_BIT_VALUES = arrayOf(24, 32)


fun main() {
    try {
        val inputBufferedImage = getValidInputImage()
        val watermarkBufferedImage = getValidWatermarkImage()
        ensureSameDimensions(inputBufferedImage, watermarkBufferedImage)
        val transparencyColor = getTransparencyColor(watermarkBufferedImage)
        val useWatermarkTransparency = getValidWatermarkTransparency(watermarkBufferedImage)
        val weight = getValidWatermarkWeight()
        val outputFilename = getValidOutputFilename()
        val resultImage =
            blendImages(inputBufferedImage, watermarkBufferedImage, weight, useWatermarkTransparency, transparencyColor)
        ImageIO.write(resultImage, outputFilename.split(".").last(), File(outputFilename))

        println("The watermarked image $outputFilename has been created.")
    } catch (ex: RuntimeException) {
        println(ex.message)
    }
}


fun getTransparencyColor(watermark: BufferedImage) : Color? {
    if (watermark.colorModel.pixelSize == 32)
        return null

    println("Do you want to set a transparency color?")
    val input = readln()
    if (input != "yes")
        return null

    println("Input a transparency color ([Red] [Green] [Blue]):")
    val colorInput = readln()
    if ("(\\d+) (\\d+) (\\d+)".toRegex().matches(colorInput)){
        val (r, g, b) = "(\\d+) (\\d+) (\\d+)".toRegex().matchEntire(colorInput)!!.destructured

        try {
            return Color(r.toInt(), g.toInt(), b.toInt())
        }
        catch (ex: IllegalArgumentException){
            throw IllegalArgumentException("The transparency color input is invalid.")
        }
    }
    else {
        throw IllegalArgumentException("The transparency color input is invalid.")
    }
}

fun getValidInputImage(): BufferedImage {
    println("Input the image filename:")
    val filename = readLine()!!

    val image = File(filename)
    if (!image.exists()) {
        throw RuntimeException("The file $filename doesn't exist.")
    }

    val bufferedImage = ImageIO.read(image)
    if (bufferedImage.colorModel.numColorComponents != 3) {
        throw RuntimeException("The number of image color components isn't 3.")
    } else if (bufferedImage.colorModel.pixelSize !in VALID_BIT_VALUES) {
        throw RuntimeException("The image isn't 24 or 32-bit.")
    }
    return bufferedImage
}

fun getValidWatermarkImage(): BufferedImage {
    println("Input the watermark image filename:")
    val filename = readLine()!!

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

fun getValidWatermarkWeight(): Int {
    println("Input the watermark transparency percentage (Integer 0-100):")
    val input = readLine()!!

    if (!input.matches(Regex("\\d+"))) {
        throw RuntimeException("The transparency percentage isn't an integer number.")
    }

    val tr = input.toInt()
    if (tr !in 0..100) {
        throw RuntimeException("The transparency percentage is out of range.")
    }
    return tr
}

fun getValidWatermarkTransparency(watermark: BufferedImage): Boolean {
    if (watermark.colorModel.transparency == Transparency.TRANSLUCENT) {
        println("Do you want to use the watermark's Alpha channel?")
        val input = readln()
        if (input.lowercase() == "yes") return true
    }
    return false
}

fun getValidOutputFilename(): String {
    println("Input the output image filename (jpg or png extension):")
    val input = readln()

    if (!input.endsWith(".jpg") && !input.endsWith(".png")) {
        throw RuntimeException("The output file extension isn't \"jpg\" or \"png\".")
    }
    return input
}

fun blendImages(
    image: BufferedImage, watermark: BufferedImage, weight: Int,
    useWatermarkTransparency: Boolean,
    transparencyColor: Color?
): BufferedImage {
    val resultImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)

    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val i = Color(image.getRGB(x, y))
            val w = Color(watermark.getRGB(x, y), useWatermarkTransparency)

            val watermarkColorIsTransparencyColor =
                w.red   == transparencyColor?.red &&
                        w.green == transparencyColor.green &&
                        w.blue  == transparencyColor.blue

            val color = if (w.alpha == 0 || watermarkColorIsTransparencyColor) {
                Color(i.red, i.green, i.blue)
            } else {
                Color(mixColors(w.red,   i.red,   weight),
                    mixColors(w.green, i.green, weight),
                    mixColors(w.blue,   i.blue, weight))
            }

            resultImage.setRGB(x, y, color.rgb)
        }
    }

    return resultImage
}

fun mixColors(watermarkColor: Int, imageColor: Int, weight: Int): Int {
    return (weight * watermarkColor + (100 - weight) * imageColor) / 100
}

fun ensureSameDimensions(image: BufferedImage, watermark: BufferedImage) {
    if (image.width != watermark.width ||
        image.height != watermark.height
    ) {
        throw RuntimeException("The image and watermark dimensions are different.")
    }
}