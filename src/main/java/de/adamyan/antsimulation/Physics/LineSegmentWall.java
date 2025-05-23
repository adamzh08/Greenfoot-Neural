package de.adamyan.antsimulation.Physics;

/**
 * Represents a straight wallVector as a line segment
 * @param startPos
 * @param wallVector the vector from the startPos to the endPos
 */
public record LineSegmentWall(Vector2D startPos, Vector2D wallVector) {
    public double slope() {
        return wallVector.slope();
    }
    public double yIntercept() {
        return startPos.y() - startPos.x() * slope();
    }
}
