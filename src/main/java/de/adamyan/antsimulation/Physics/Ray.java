package de.adamyan.antsimulation.Physics;

import de.adamyan.antsimulation.Ant;

public class Ray {

    public static RayCast cast(Ant ant, int rayIdx) {

        double d = rayIdx / (double) Ant.RAY_COUNT;
        double offsetAngle = (-Ant.FIELD_OF_VIEW_PERCENTAGE / 2 + d * Ant.FIELD_OF_VIEW_PERCENTAGE) * Math.TAU;

        return new RayCast(
                ant.getX(),
                ant.getY(),
                Math.cos(ant.getAngle() + offsetAngle),
                Math.sin(ant.getAngle() + offsetAngle),
                Ant.MAX_RAY_TRAVEL_DISTANCE
        );
    }
}
