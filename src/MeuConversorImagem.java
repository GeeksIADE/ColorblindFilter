import java.awt.*;
import java.awt.image.BufferedImage;

public class MeuConversorImagem extends ConversorImagemAED {
    public MeuConversorImagem(String imagePath) {
        super(imagePath);
    }

    public BufferedImage redGreenBlindSequence() {
        this.image = this.increaseContrastGeneral(2);
        this.image = this.brightenDarkRed();
        this.image = this.shiftColorGreenRed();
        this.image = this.replacePurpleWithYellowIfBlue();
        return this.image;
    }

    public BufferedImage redGreenAnomalySequence() {
        this.image = this.increaseContrastGeneral(2);
        this.image = this.applyContrastRedGreen();
        return this.image;
    }

    public BufferedImage blueYellowBlindSequence() {
        this.image = this.increaseContrastGeneral(2);
        this.image = this.separatePurpleAndYellow();
        this.image = this.brightenDarkRed();
        this.image = this.addBlueRemoveGreen();
        return this.image;
    }

    public BufferedImage blueYellowAnomalySequence() {
        this.image = this.increaseContrastGeneral(2);
        this.image = this.increaseContrastBlueGreen(60);
        return this.image;
    }

    public BufferedImage achromatopsiaSequence() {
        this.image = this.increaseContrastGeneral(2);
        this.image = this.convertGrayscale();
        return this.image;
    }

    public BufferedImage achromatomalySequence() {
        this.image = this.increaseContrastGeneral(2);
        this.image = this.shiftColorGreenYellow();
        this.image = this.increaseContrastAchromatomaly();
        return this.image;
    }

    public BufferedImage convertGrayscale() {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int[] colors = this.getColors(rgb);
                int alpha = colors[0];
                if (alpha == 0) {
                    result.setRGB(x, y, 0);
                }

                else {
                    int red = (rgb >> 16) & 0xff;
                    int green = (rgb >> 8) & 0xff;
                    int blue = rgb & 0xff;
                    int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                    int newRGB = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                    result.setRGB(x, y, newRGB);
                }
            }
        }

        return result;
    }

    public BufferedImage applyContrastRedGreen() {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int[] colors = this.getColors(rgb);
                int red = colors[1];
                int green = colors[2];
                int blue = colors[3];

                if (red > green && red > blue) {
                    // if dominantly red, transition into yellow
                    red = Math.min(255, red + 20);
                    green = Math.max(0, green - 10);
                    blue = Math.max(0, blue - 10);
                } else if (green > red && green > blue) {
                    // if dominantly green, transition into cyan
                    red = Math.max(0, red - 10);
                    green = Math.min(255, green + 20);
                    blue = Math.max(0, blue - 10);
                } else if (blue > red && blue > green) {
                    // if dominantly blue, transition into magenta
                    red = Math.max(0, red - 10);
                    green = Math.max(0, green - 10);
                    blue = Math.min(255, blue + 20);
                }

                // get rgb
                int newRgb = (rgb & 0xff000000) | (red << 16) | (green << 8) | blue;
                newImage.setRGB(x, y, newRgb);
            }
        }

        return newImage;
    }

    public BufferedImage shiftColorGreenRed() {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int red, green, blue, alpha, newRed, newGreen, newBlue;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] colors = this.getColors(image.getRGB(x, y));
                alpha = colors[0];
                red = colors[1];
                green = colors[2];
                blue = colors[3];

                newRed = blue;
                if (green > red) {
                    newGreen = blue;
                    newBlue = green;
                } else {
                    newGreen = red;
                    newBlue = blue;
                }

                // get rgb
                int newPixel = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
                output.setRGB(x, y, newPixel);
            }
        }

        return output;
    }

    public BufferedImage shiftColorGreenYellow() {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] colors = this.getColors(image.getRGB(x, y));
                int alpha = colors[0];
                int red = colors[1];
                int green = colors[2];
                int blue = colors[3];

                if (green > blue) {
                    // make green more yellow
                    int deltaG = green / 4;
                    int newGreen = Math.min(green + deltaG, 255);
                    int newRed = red + deltaG;

                    // get rgb
                    int newRGB = (alpha << 24) | (newRed << 16) | (newGreen << 8) | blue;
                    result.setRGB(x, y, newRGB);
                } else {
                    result.setRGB(x, y, image.getRGB(x, y));
                }
            }
        }

        return result;
    }

    public BufferedImage increaseContrastAchromatomaly() {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        float[] ACHROMA_WEIGHTS = { 22f, 0.72f, 0.07f }; // Luminance weights for general achromatomaly (estimated)
        // YIQ color space https://en.wikipedia.org/wiki/YIQ
        float[] YIQ_MATRIX = { 0.299f, 0.587f, 0.114f, 0.596f, -0.275f, -0.321f, 0.212f, -0.523f, 0.311f };

        // Convert RGB image to YIQ https://en.wikipedia.org/wiki/YIQ
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                float red = ((argb >> 16) & 0xFF) / 255.0f;
                float green = ((argb >> 8) & 0xFF) / 255.0f;
                float blue = (argb & 0xFF) / 255.0f;

                float yiqY = YIQ_MATRIX[0] * red + YIQ_MATRIX[1] * green + YIQ_MATRIX[2] * blue;
                float yiqI = YIQ_MATRIX[3] * red + YIQ_MATRIX[4] * green + YIQ_MATRIX[5] * blue;
                float yiqQ = YIQ_MATRIX[6] * red + YIQ_MATRIX[7] * green + YIQ_MATRIX[8] * blue;

                // Adjust luminance of the image
                float luminance = ACHROMA_WEIGHTS[0] * red + ACHROMA_WEIGHTS[1] * green + ACHROMA_WEIGHTS[2] * blue;
                if (luminance < 0.5f) {
                    yiqY *= 2.0f;
                } else {
                    yiqY = 1.0f - 2.0f * (1.0f - yiqY);
                }

                // Convert back to RGB color space https://en.wikipedia.org/wiki/YIQ
                float newRed = yiqY + 0.9563f * yiqI + 0.6210f * yiqQ;
                float newGreen = yiqY - 0.2721f * yiqI - 0.6474f * yiqQ;
                float newBlue = yiqY - 1.1070f * yiqI + 1.7046f * yiqQ;

                newRed = Math.min(1.0f, Math.max(0.0f, newRed));
                newGreen = Math.min(1.0f, Math.max(0.0f, newGreen));
                newBlue = Math.min(1.0f, Math.max(0.0f, newBlue));

                int alpha = (argb >> 24) & 0xFF;
                int newRGB = (alpha << 24) | ((int) (newRed * 255) << 16) | ((int) (newGreen * 255) << 8)
                        | (int) (newBlue * 255);

                result.setRGB(x, y, newRGB);
            }
        }

        return result;
    }

    public BufferedImage increaseContrastBlueGreen(int threshold) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++)
            for (int y = 0; y < image.getHeight(); y++) {
                int[] colors = this.getColors(image.getRGB(x, y));
                int alpha = colors[0];
                int red = colors[1];
                int green = colors[2];
                int blue = colors[3];

                if (blue >= threshold) {
                    blue = Math.min(255, blue + Math.max(30, (int) (0.5 * blue))); // adjust to desired value
                }

                if (green >= threshold && green > blue) {
                    green = Math.min(255, green - Math.min(20, (int) (0.5 * green)));
                }

                if (red >= 10 && red <= 40) {
                    red = Math.min(255, red + Math.max(30, (int) (0.8 * red)));
                }
                result.setRGB(x, y, new Color(red, green, blue, alpha).getRGB());
            }
        return result;
    }

    public BufferedImage addBlueRemoveGreen() {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                if (green > blue && green > red) {
                    green = Math.min(green / 2, 40);
                    blue = Math.min(3 * blue, 255);
                    red = Math.min(3 * red, 255);
                }

                int newPixel = (rgb & 0xFF000000) | (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newPixel);
            }
        }

        return result;
    }

    public BufferedImage brightenDarkRed() {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        float[] hsv = new float[3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, hsv);
                if (hsv[0] >= 0.05 && hsv[0] < 0.14 && hsv[1] > 0.4 && hsv[2] > 0.1) {
                    hsv[0] = 0.0f; // set hue to red
                    hsv[1] = Math.min(hsv[1] + 0.2f, 1.0f); // increase saturation
                    hsv[2] = Math.min(hsv[2] + 0.2f, 1.0f); // increase brightness
                    int newRGB = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) | (rgb & 0xFF000000);
                    result.setRGB(x, y, newRGB);
                } else {
                    result.setRGB(x, y, rgb);
                }
            }
        }
        return result;
    }

    public BufferedImage replacePurpleWithYellowIfBlue() {
        float threshold = 0.5f; // minimum value
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // blue average
        float totalBlue = 0.0f;
        float totalPixels = image.getWidth() * image.getHeight();
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                int[] colors = this.getColors(image.getRGB(x, y));
                int blue = colors[3];
                totalBlue += (float) blue / 255.0f;
            }
        float avgBlue = totalBlue / totalPixels;

        // Switch purple with yellow
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                int[] colors = this.getColors(image.getRGB(x, y));
                int alpha = colors[0];
                int red = colors[1];
                int green = colors[2];
                int blue = colors[3];

                // check for purple shade and mainly blue image
                if (red > 100 && green < 100 && blue > 100 && avgBlue >= threshold) {
                    red = 255;
                    green = 255;
                    blue = 0;
                }
                int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newRgb);
            }

        return result;
    }

    public BufferedImage separatePurpleAndYellow() {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int argb = image.getRGB(i, j);
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                float[] hsb = Color.RGBtoHSB(red, green, blue, null);
                float hue = hsb[0];
                float saturation = hsb[1];
                float brightness = hsb[2];

                boolean isPurple = hue >= 0.75 && hue <= 0.92 && saturation >= 0.3 && brightness >= 0.3;
                boolean isYellow = hue >= 0.11 && hue <= 0.18 && saturation >= 0.3 && brightness >= 0.3;

                if (isPurple)
                    result.setRGB(i, j, Color.MAGENTA.getRGB());
                else if (isYellow)
                    result.setRGB(i, j, Color.YELLOW.getRGB());
                else
                    result.setRGB(i, j, argb);
            }
        }

        return result;
    }

    public BufferedImage increaseContrastGeneral(double factor) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                int[] colors = this.getColors(image.getRGB(x, y));
                int alpha = colors[0];
                int red = colors[1];
                int green = colors[2];
                int blue = colors[3];

                // check for transparency
                if (alpha == 0)
                    result.setRGB(x, y, 0);
                else {
                    // increase the contrast
                    double newRed = (((red / 255.0) - 0.5) * factor) + 0.5;
                    double newGreen = (((green / 255.0) - 0.5) * factor) + 0.5;
                    double newBlue = (((blue / 255.0) - 0.5) * factor) + 0.5;

                    // make sure the values are between 0 and 1
                    newRed = Math.max(0.0, Math.min(1.0, newRed));
                    newGreen = Math.max(0.0, Math.min(1.0, newGreen));
                    newBlue = Math.max(0.0, Math.min(1.0, newBlue));

                    // convert to rgb
                    int newRedInt = (int) (newRed * 255);
                    int newGreenInt = (int) (newGreen * 255);
                    int newBlueInt = (int) (newBlue * 255);

                    // set alpha value
                    alpha = alpha << 24;

                    int newRgba = alpha | (newRedInt << 16) | (newGreenInt << 8) | newBlueInt;
                    result.setRGB(x, y, newRgba);
                }
            }
        return result;
    }

    private int[] getColors(int rgb) {
        int alpha = (rgb >> 24) & 0xFF;
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return new int[] { alpha, red, green, blue };
    }
}
