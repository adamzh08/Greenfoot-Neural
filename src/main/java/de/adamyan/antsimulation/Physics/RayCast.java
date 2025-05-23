package de.adamyan.antsimulation.Physics;

import de.adamyan.antsimulation.*;

import java.util.List;
import java.util.Optional;

public record RayCast(double startX, double startY, double angle, double cosAngle, double sinAngle, double length) {
    /**
     * @return position of the nearest intersection point
     */
    public Optional<double[]> getIntersection(GameManager gameManager) {
        double smallestDist = Double.MAX_VALUE;
        // iterate over every straight wallVector in the game
        for (LineSegmentWall wall : gameManager.getStraightWalls()) {
            smallestDist = Math.min(smallestDist, getDistance_line(wall));
        }
        for (CircleWall wall : gameManager.getCircleWalls()) {
            smallestDist = Math.min(smallestDist, getDistance_circle(wall));
        }
        if (smallestDist > length) return Optional.empty();
        return Optional.of(new double[]{startX + cosAngle * smallestDist, startY + sinAngle * smallestDist});
    }


    public double getDistance_circle(CircleWall wall) {
        double m = sinAngle / cosAngle;

        List<double[]> intersections = Intersection.line_circle(m, startY - m * startX, wall);
        if (intersections.isEmpty()) return Double.MAX_VALUE;

        double minDist = Double.MAX_VALUE;

        for (double[] intersection : intersections) {
            if (rightDirection(intersection)) {
                double angle = Math.atan2(
                        intersection[1] - wall.centerY(),
                        intersection[0] - wall.centerX()
                );

                if (!wall.isWall(angle)) continue;

                double dist = Math.hypot(intersection[0] - startX, intersection[1] - startY);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }

        return minDist;
    }

    private double getDistance_line(LineSegmentWall wall) {
        double m_ray = sinAngle / cosAngle;
        double t_ray = startY - m_ray * startX;

        double m_wall = wall.wallVector().slope();
        double t_wall = wall.yIntercept();

        Optional<double[]> intersection = Intersection.line_line(m_ray, t_ray, m_wall, t_wall);

        if (intersection.isEmpty() || !rightDirection(intersection.get()) || !liesOnWall(intersection.get(), wall)) {
            return Double.MAX_VALUE; // no intersection
        }
        return Math.hypot(intersection.get()[0] - startX, intersection.get()[1] - startY);
    }

    private boolean rightDirection(double[] intersection) {
        return ((intersection[0] - startX) * cosAngle + (intersection[1] - startY) * sinAngle) > 0;
    }

    private boolean liesOnWall(double[] point, LineSegmentWall wall) {
        boolean correctX = point[0] >= Math.min(wall.startPos().x(), wall.startPos().x() + wall.wallVector().x()) && point[0] <= Math.max(wall.startPos().x(), wall.startPos().x() + wall.wallVector().x());
        boolean correctY = point[1] >= Math.min(wall.startPos().y(), wall.startPos().y() + wall.wallVector().y()) && point[1] <= Math.max(wall.startPos().y(), wall.startPos().y() + wall.wallVector().y());
        return correctX && correctY;
    }
}
