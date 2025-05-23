package de.adamyan.antsimulation.Physics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Intersection {
    private Intersection() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param m1 slope of the first line
     * @param t1 y-Intercept of the first line
     * @param m2 slope of the second line
     * @param t2 y-Intercept of the second line
     * @return the intersection coordinates {x, y} if there are any
     */
    public static Optional<double[]> line_line(double m1, double t1, double m2, double t2) {
        if (m1 == m2) {
            return Optional.empty(); // parallel
        }
        double intersection_x = (t2 - t1) / (m1 - m2);
        double intersection_y = m1 * intersection_x + t1;
        return Optional.of(new double[]{intersection_x, intersection_y});
    }

    public static List<double[]> line_circle(double m, double t, CircleWall circle) {
        List<double[]> intersections = new ArrayList<>();

        double r = circle.radius();
        double t_relative = t - circle.centerY() - m * (-circle.centerX());

        // solving equation: mx+t=sqrt(r^2-centerX^2)
        // wolfram alphas response:
        double discriminant = r * r * (m * m + 1) - t_relative * t_relative;
        if (discriminant < 0) {
            return intersections;
        }
        double x1_relative = (-m * t_relative + Math.sqrt(discriminant)) / (m * m + 1);
        double x2_relative = (-m * t_relative - Math.sqrt(discriminant)) / (m * m + 1);

        double y1_relative = m * x1_relative + t_relative;
        double y2_relative = m * x2_relative + t_relative;

        intersections.add(new double[]{x1_relative + circle.centerX(), y1_relative + circle.centerY()});
        intersections.add(new double[]{x2_relative + circle.centerX(), y2_relative + circle.centerY()});

        return intersections;
    }
}