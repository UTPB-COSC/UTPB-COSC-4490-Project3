package src;

import java.awt.Point;
import java.awt.geom.Point2D;

public class RotationUtil {
    public static Point2D[] getRotatedCorners(int xPos, int yPos, double relWidth, double relHeight, double width, double height, double rotation) {
        Point2D[] corners = new Point2D[4];

        // Calculate the center of the rectangle (rotation point)
        double centerX = xPos + relWidth / 2.0;
        double centerY = yPos + relHeight / 2.0;

        // Define the four corners of the rectangle relative to the center
        double[][] cornerOffsets = {
            {-width / 2.0, -height / 2.0}, // Top-left
            { width / 2.0, -height / 2.0}, // Top-right
            { width / 2.0,  height / 2.0}, // Bottom-right
            {-width / 2.0,  height / 2.0}  // Bottom-left
        };

        // Rotate each corner around the center by the specified rotation angle
        for (int i = 0; i < 4; i++) {
            double offsetX = cornerOffsets[i][0];
            double offsetY = cornerOffsets[i][1];

            // Calculate rotated coordinates for each corner
            double rotatedX = offsetX * Math.cos(rotation) - offsetY * Math.sin(rotation);
            double rotatedY = offsetX * Math.sin(rotation) + offsetY * Math.cos(rotation);

            // Translate the rotated corner back to world space
            corners[i] = new Point((int) (centerX + rotatedX), (int) (centerY + rotatedY));
        }

        return corners;
    }
}
