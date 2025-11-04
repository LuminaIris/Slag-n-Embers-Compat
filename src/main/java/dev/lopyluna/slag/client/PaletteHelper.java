package dev.lopyluna.slag.client;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class PaletteHelper {
    public static BufferedImage gradientToImage(int size, int... colors) {
        var list = new ArrayList<Integer>();
        for (int color : colors) list.add(color);
        return gradientToImage(size, list);
    }

    public static BufferedImage gradientToImage(int size, List<Integer> colors) {
        return paletteToImage(colorsToGradient(size, colors));
    }

    public static BufferedImage paletteToImage(int... colors) {
        var list = new ArrayList<Integer>();
        for (int color : colors) list.add(color);
        return paletteToImage(list);
    }

    public static BufferedImage paletteToImage(List<Integer> colors) {
        var width = colors.size();
        var height = 1;
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < colors.size(); i++) image.setRGB(i, 0, colors.get(i));
        return image;
    }

    public static List<Integer> colorsToGradient(int size, int... colors) {
        var list = new ArrayList<Integer>();
        for (int color : colors) list.add(color);
        return colorsToGradient(size, list);
    }
    
    public static List<Integer> colorsToGradient(int size, List<Integer> colors) {
        if (size == colors.size()) return new ArrayList<>(colors);
        var gradient = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            float position = (float) i / (size - 1);

            float scaledPosition = position * (colors.size() - 1);
            int segmentIndex = (int) scaledPosition;
            float segmentT = scaledPosition - segmentIndex;

            if (segmentIndex >= colors.size() - 1) {
                gradient.add(colors.getLast());
                continue;
            }

            int color = getColor(colors, segmentIndex, segmentT);
            gradient.add(color);
        }
        return gradient;
    }

    private static int getColor(List<Integer> colors, int segmentIndex, float segmentT) {
        var color1 = colors.get(segmentIndex);
        var color2 = colors.get(segmentIndex + 1);

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * segmentT);
        int g = (int) (g1 + (g2 - g1) * segmentT);
        int b = (int) (b1 + (b2 - b1) * segmentT);

        return (r << 16) | (g << 8) | b;
    }

}
