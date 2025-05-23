package de.adamyan.antsimulation.Physics;

import java.util.List;

public record CircleWall(double centerX, double centerY, double radius, List<Double> wallSpaceChangingAngles) {
    public boolean isWall(double angle) {
        angle = (angle + 2 * Math.PI) % (2 * Math.PI);
        for (int i = 0; i < wallSpaceChangingAngles.size(); i++) {
            if (wallSpaceChangingAngles.get(i) >= angle) {
                return i % 2 != 0;
            }
        }
        // return true;
        throw new RuntimeException();
    }
}