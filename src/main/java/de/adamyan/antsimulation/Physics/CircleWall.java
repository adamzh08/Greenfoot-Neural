package de.adamyan.antsimulation.Physics;

import java.util.Collections;
import java.util.List;

public record CircleWall(double centerX, double centerY, double radius, List<Double> wallSpaceChangingAngles) {

    public static CircleWall create(double centerX, double centerY, double radius, List<Double> wallSpaceChangingAngles) {
        wallSpaceChangingAngles.addFirst(0.);
        wallSpaceChangingAngles.addLast(Math.TAU);
        return new CircleWall(centerX, centerY, radius, wallSpaceChangingAngles);
    }

    public boolean isWall(double angle) {
        angle = (angle + 2 * Math.PI) % (2 * Math.PI);

        double idx = Collections.binarySearch(wallSpaceChangingAngles, angle);
        if (idx < 0) {
            // not contained in the list (see Collections.binarySearch() documentation);
            idx = -idx - 1;
        }
        return idx % 2 != 0;
    }
}