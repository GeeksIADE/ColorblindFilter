import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class App {
    private JButton loadImageButton;
    private JButton processImageButton;
    private JPanel panelMain;
    private JLabel logoLabel;
    private JLabel imageLabel;
    private JPanel logoField;
    private JRadioButton protanopiaRadio;
    private JRadioButton achromatopsiaRadio;
    private JRadioButton tritanopiaRadio;
    private JRadioButton protanomalyRadio;
    private JRadioButton tritanomalyRadio;
    private JRadioButton achromatomalyRadio;
    private JButton revertChanges;
    private JButton generateOutput;
    private String filePath;

    private static String outputFolder = "output";

    public App() {
        ButtonGroup group = new ButtonGroup();
        group.add(achromatopsiaRadio);
        group.add(protanomalyRadio);
        group.add(protanopiaRadio);
        group.add(tritanopiaRadio);
        group.add(tritanomalyRadio);
        group.add(achromatomalyRadio);
        ImageIcon imageIcon = new ImageIcon("geeks.png");
        imageIcon = resize(imageIcon, 200, 100);

        logoLabel.setSize(100, 50);
        logoLabel.setIcon(imageIcon);
        System.out.println(imageIcon.getIconHeight());
        loadImageButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int res = fc.showOpenDialog(null);
            try {
                System.out.println("Res: " + res);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    String path = file.getAbsolutePath();
                    filePath = path;
                    imageLabel.setBorder(BorderFactory.createMatteBorder(
                            1, 1, 1, 1, Color.black));
                    imageLabel.setIcon(new ImageIcon(scaleImageByHeight(ImageIO.read(new File(path)), 600)));
                } else {
                    JOptionPane.showMessageDialog(null,
                            "You must select an image.", "Error",
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception iOException) {
                System.out.println("Unknown error");
            }
        });

        processImageButton.addActionListener(e -> {
            MeuConversorImagem cc = new MeuConversorImagem(filePath);
            cc.image = cc.increaseContrastGeneral(2);

            switch (getSelectedButtonText(group)) {
                case "Red-Green blind" -> cc.image = cc.redGreenBlindSequence();
                case "Red-Green anomaly" -> cc.image = cc.redGreenAnomalySequence();
                case "Blue-Yellow blind" -> cc.image = cc.blueYellowBlindSequence();
                case "Blue-Yellow anomaly" -> cc.image = cc.blueYellowAnomalySequence();
                case "Achromatopsia" -> cc.image = cc.achromatopsiaSequence();
                case "Achromatomaly" -> cc.image = cc.achromatomalySequence();
                default -> {
                    return;
                }
            }
            imageLabel.setIcon(new ImageIcon(scaleImageByHeight(cc.image, 600)));
        });

        revertChanges.addActionListener(e -> {
            try {
                imageLabel.setIcon(new ImageIcon(scaleImageByHeight(ImageIO.read(new File(filePath)), 600)));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        generateOutput.addActionListener(e -> {
            MeuConversorImagem redGreenImageConverter = new MeuConversorImagem("templates/red-green.png");
            MeuConversorImagem blueYellowRestImageConverter = new MeuConversorImagem("templates/blue-yellow-rest.png");

            File outputFolder = new File(App.outputFolder);
            if (!outputFolder.exists()) {
                outputFolder.mkdirs(); // Create the folder if it doesn't exist
            }

            try {
                // Red-Green blind
                ImageIO.write(redGreenImageConverter.redGreenBlindSequence(), "png",
                        new File("output/red-green-blind.png"));

                // Red-Green anomaly
                redGreenImageConverter = new MeuConversorImagem("templates/red-green.png");
                ImageIO.write(redGreenImageConverter.redGreenAnomalySequence(), "png",
                        new File("output/red-green-anomaly.png"));

                // Blue-Yellow blind
                ImageIO.write(blueYellowRestImageConverter.blueYellowBlindSequence(), "png",
                        new File("output/blue-yellow-blind.png"));

                // Blue-Yellow anomaly
                blueYellowRestImageConverter = new MeuConversorImagem("templates/blue-yellow-rest.png");
                ImageIO.write(blueYellowRestImageConverter.blueYellowAnomalySequence(), "png",
                        new File("output/blue-yellow-anomaly.png"));

                // Achromatopsia
                blueYellowRestImageConverter = new MeuConversorImagem("templates/blue-yellow-rest.png");
                ImageIO.write(blueYellowRestImageConverter.achromatopsiaSequence(), "png",
                        new File("output/achromatopsia.png"));

                // Achromatomaly
                blueYellowRestImageConverter = new MeuConversorImagem("templates/blue-yellow-rest.png");
                ImageIO.write(blueYellowRestImageConverter.achromatomalySequence(), "png",
                        new File("output/achromatomaly.png"));

                JOptionPane.showMessageDialog(null, "Done, placed in: " + outputFolder, "Images processed",
                        JOptionPane.PLAIN_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }

    public BufferedImage scaleImageByHeight(BufferedImage originalImage, int newHeight) {
        double aspectRatio = (double) originalImage.getWidth() / (double) originalImage.getHeight();
        int newWidth = (int) Math.round(newHeight * aspectRatio);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    public static void main(String[] args) {
        // setting a dark theme
        UIManager.put("control", new Color(128, 128, 128));
        UIManager.put("info", new Color(128, 128, 128));
        UIManager.put("nimbusBase", new Color(18, 30, 49));
        UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
        UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
        UIManager.put("nimbusFocus", new Color(115, 164, 209));
        UIManager.put("nimbusGreen", new Color(176, 179, 50));
        UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
        UIManager.put("nimbusLightBackground", new Color(18, 30, 49));
        UIManager.put("nimbusOrange", new Color(191, 98, 4));
        UIManager.put("nimbusRed", new Color(169, 46, 34));
        UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
        UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
        UIManager.put("text", new Color(230, 230, 230));
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Geeks Colorblind Correction");
        frame.setContentPane(new App().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static ImageIcon resize(ImageIcon image, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = bi.createGraphics();
        g2d.addRenderingHints(// ww w . jav a2 s. c o m
                new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.drawImage(image.getImage(), 0, 0, width, height, null);
        g2d.dispose();
        return new ImageIcon(bi);
    }

}
