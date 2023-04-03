/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.Color;

/**
 *
 * @author alexandrebarao
 */
public class ConversorImagemAED {

    protected BufferedImage image; // changed private to protected, so we can access it from subclass

    public ConversorImagemAED(String urlImagemOrigem) {

        try {
            image = ImageIO.read(new File(urlImagemOrigem)); // changed variable name
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public BufferedImage converteBW(String urlImagemDestino) {

        try {

            BufferedImage imagemBW = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

            for (int i = 0; i < image.getHeight(); i++) {
                for (int j = 0; j < image.getWidth(); j++) {

                    Color cor = new Color(image.getRGB(j, i));
                    imagemBW.setRGB(j, i, getCinza(cor).getRGB());
                }
            }

            ImageIO.write(imagemBW, "png", new File(urlImagemDestino));
            System.out.println("Imagem convertida para B&W");
            return imagemBW;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // changed private to protected, so we can access it from subclass
    protected Color getCinza(Color cor) {

        int r = (int) (cor.getRed() * 0.3);
        int g = (int) (cor.getGreen() * 0.59);
        int b = (int) (cor.getBlue() * 0.11);

        return new Color(r+g+b, r+g+b, r+g+b);
    }

}
