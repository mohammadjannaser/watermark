import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {

    println("Input the image filename:")
    val filename = readln()

    val image = File(filename)
    if (!image.exists()){
        println("The file $filename doesn't exist.")
        return
    }

    val bi = ImageIO.read(image)
    stage2.printInfo(filename, bi)

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