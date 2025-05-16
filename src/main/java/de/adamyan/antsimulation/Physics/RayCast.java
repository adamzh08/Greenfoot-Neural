package de.adamyan.antsimulation.Physics;

import de.adamyan.antsimulation.*;

public record RayCast(double startX, double startY, double cosAngle, double sinAngle, double length) {
    public double[] getIntersection(GameManager gameManager) {

        double smallestDistSquared = Double.MAX_VALUE;

        double[] shortestIntersectionPoint = null;

        for (LineSegmentWall wall : gameManager.getWalls()) {
            double[] intersection = getIntersection_line(wall);

            if (intersection != null) {
                double rayTravelLengthSquared = (startX - intersection[0]) * (startX - intersection[0])
                        + (startY - intersection[1]) * (startY - intersection[1]);


                if (rayTravelLengthSquared < smallestDistSquared) {
                    smallestDistSquared = rayTravelLengthSquared;
                    shortestIntersectionPoint = intersection;
                }
            }
        }

        if (shortestIntersectionPoint == null) {
            // end point of ray
            return new double[]{startX + cosAngle * length, startY + sinAngle * length};
        }
        return shortestIntersectionPoint;
    }

    @AIGenerated
    public double[] getIntersection_line(LineSegmentWall wall) {
        double x1 = startX;
        double y1 = startY;
        double x2 = startX + cosAngle * length;
        double y2 = startY + sinAngle * length;

        double x3 = wall.startX();
        double y3 = wall.startY();
        double x4 = wall.endX();
        double y4 = wall.endY();

        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (denom == 0) return null; // Lines are parallel

        double px = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom;
        double py = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom;

        if (onSegment(x1, y1, x2, y2, px, py) && onSegment(x3, y3, x4, y4, px, py)) {
            return new double[]{px, py};
        }

        return null;
    }

    @AIGenerated
    private boolean onSegment(double x1, double y1, double x2, double y2, double px, double py) {
        return px >= Math.min(x1, x2) && px <= Math.max(x1, x2) &&
                py >= Math.min(y1, y2) && py <= Math.max(y1, y2);
    }
}
