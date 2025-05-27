package Physics;

import GameUtils.GameManager;

import java.util.List;
import java.util.Optional;


public record RayCast(
        double startX,
        double startY,
        double angle,
        double cosAngle,
        double sinAngle,
        double m,

        double length
) {

    public static RayCast of(Vector2D startPos, double angle, double length) {
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);
        return new RayCast(
                startPos.x(),
                startPos.y(),
                angle,
                dx,
                dy,
                dy / dx,
                length
        );
    }

    /**
     * @return position of the nearest intersection point
     */
    public Optional<Vector2D> getIntersection(GameManager gameManager) {
        double smallestDistSquared = Double.MAX_VALUE;
        // iterate over every straight wall in the game
        for (LineSegmentWall wall : gameManager.getStraightWalls()) {
            smallestDistSquared = Math.min(smallestDistSquared, getDistanceSquared_line(wall));
        }
        // iterate over every circle wall in the game
        for (CircleWall wall : gameManager.getCircleWalls()) {
            smallestDistSquared = Math.min(smallestDistSquared, getDistance_circle(wall));
        }
        if (smallestDistSquared > length * length) return Optional.empty();
        return Optional.of(new Vector2D(
                startX + cosAngle * Math.sqrt(smallestDistSquared),
                startY + sinAngle * Math.sqrt(smallestDistSquared)
        ));
    }


    public double getDistance_circle(CircleWall wall) {
        List<Vector2D> intersections = Intersection.line_circle(m, startY - m * startX, wall);
        if (intersections.isEmpty()) return Double.MAX_VALUE;

        double minDistSquared = Double.MAX_VALUE;

        for (Vector2D intersection : intersections) {
            if (rightDirection(intersection)) {
                double angle = Math.atan2(
                        intersection.y() - wall.centerY(),
                        intersection.x() - wall.centerX()
                );

                if (!wall.isWall(angle)) continue;

                double distSquared = Utils.distSquared(intersection.x() - startX, intersection.y() - startY);
                if (distSquared < minDistSquared) {
                    minDistSquared = distSquared;
                }
            }
        }

        return minDistSquared;
    }

    private double getDistanceSquared_line(LineSegmentWall wall) {
        double t_ray = startY - m * startX;

        double m_wall = wall.wallVector().slope();
        double t_wall = wall.yIntercept();

        Optional<Vector2D> intersection;

        if (Double.isInfinite(m_wall)) {
            double x = wall.startPos().x();
            double y = m * x + t_ray;
            intersection = Optional.of(new Vector2D(x, y));
        } else {
            intersection = Intersection.line_line(m, t_ray, m_wall, t_wall);
        }

        if (intersection.isEmpty() ||
                !rightDirection(intersection.get()) ||
                !Utils.inBounds(intersection.get(), wall.startPos(), wall.endPos())) {
            return Double.MAX_VALUE;
        }
        return Utils.distSquared(intersection.get().x() - startX, intersection.get().y() - startY);
    }


    private boolean rightDirection(Vector2D intersection) {
        return ((intersection.x() - startX) * cosAngle + (intersection.y() - startY) * sinAngle) >= 0;
    }
}
