package Cilent;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageTransparencyUtil {

public static void main(String[] args){
    try {
        // 读取原始图片
        BufferedImage original = ImageIO.read(new File("D:\\退出.png"));
        // 将白色背景转换为透明
        BufferedImage transparent = ImageTransparencyUtil.makeColorTransparent(original, Color.WHITE);
        // 保存处理后的图片
        ImageIO.write(transparent, "PNG", new File("mexit.png"));
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    /**
     * 将指定的背景颜色转换为透明
     *
     * @param image 原始图片
     * @param color 要设为透明的背景颜色，例如 Color.WHITE
     * @return 返回处理后的图片，背景颜色部分将变为透明
     */
    public static BufferedImage makeColorTransparent(BufferedImage image, Color color) {
        // 创建一个支持透明度的图片
        BufferedImage transparentImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        // 获取背景颜色的 RGB 值（忽略 alpha 通道）
        int markerRGB = color.getRGB() & 0x00FFFFFF;

        // 遍历每个像素
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int pixel = image.getRGB(x, y);
                // 如果当前像素的 RGB 值与背景色匹配，则设置透明（alpha=0）
                if ((pixel & 0x00FFFFFF) == markerRGB) {
                    transparentImage.setRGB(x, y, 0x00000000);
                } else {
                    // 否则保持原来的颜色
                    transparentImage.setRGB(x, y, pixel);
                }
            }
        }
        return transparentImage;
    }
}
