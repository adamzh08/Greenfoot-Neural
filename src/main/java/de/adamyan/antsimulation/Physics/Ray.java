package de.adamyan.antsimulation.Physics;

import de.adamyan.antsimulation.AIGenerated;
import de.adamyan.antsimulation.Ant;

public class Ray {

    /**
     *
     * @param ant is the Ant object where the ray comes from
     * @param rayIdx is the index of the ray (anticlockwise)
     * @return a RayCast
     */
    public static RayCast cast(Ant ant, int rayIdx) {

        double rayAngle = ant.getAngleOfRay(rayIdx);

        return new RayCast(
                ant.getX(),
                ant.getY(),
                Math.cos(rayAngle),
                Math.sin(rayAngle),
                Ant.MAX_RAY_TRAVEL_DISTANCE
        );
    }


    /**
     * @return the intersection coordinates of 2 straight walls (used to check if ant crosses wall while moving)
     */
    @AIGenerated
    public static double[] intersectionCoordinates(LineSegmentWall path, LineSegmentWall wall) {
        double x1 = path.startX(), y1 = path.startY();
        double x2 = path.endX(), y2 = path.endY();
        double x3 = wall.startX(), y3 = wall.startY();
        double x4 = wall.endX(), y4 = wall.endY();

        double denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        if (denominator == 0) {
            // Lines are parallel or coincident
            return null;
        }

        double px = ((x1*y2 - y1*x2) * (x3 - x4) - (x1 - x2) * (x3*y4 - y3*x4)) / denominator;
        double py = ((x1*y2 - y1*x2) * (y3 - y4) - (y1 - y2) * (x3*y4 - y3*x4)) / denominator;

        // Check if the intersection point (px, py) is within both segments
        if (isBetween(px, x1, x2) && isBetween(py, y1, y2) &&
                isBetween(px, x3, x4) && isBetween(py, y3, y4)) {
            return new double[] { px, py };
        }

        return null;
    }

    @AIGenerated
    private static boolean isBetween(double val, double start, double end) {
        return val >= Math.min(start, end) - 1e-10 && val <= Math.max(start, end) + 1e-10;
    }
}
