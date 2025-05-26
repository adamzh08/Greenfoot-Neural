package GameUtils;

import Physics.CircleWall;
import Physics.LineSegmentWall;
import Physics.Vector2D;

import java.util.*;
import java.util.stream.IntStream;

public class MazeGenerator {

    private static boolean[][] canWalkFromNodeToNode;

    private static int ringCount;

    private static int totalCellAmount;

    private static double radius;
    private static double centerX;
    private static double centerY;


    private static List<Integer> cellsPerRing;

    public static void generateAndSet(int ringCount, GameManager gameManager) {
        MazeGenerator.ringCount = ringCount;

        cellsPerRing = new ArrayList<>();
        cellsPerRing.add(1);

        for (int ringIdx = 1; ringIdx < ringCount; ringIdx++) {
            int cellCount = (int)Math.pow(2, (int)Math.floor(Math.log(ringIdx + 1) / Math.log(2)) + 3);
            cellsPerRing.add(cellCount);
        }

        totalCellAmount = cellsPerRing.stream().mapToInt(i -> i).sum();

        canWalkFromNodeToNode = new boolean[totalCellAmount][totalCellAmount];


        Random r = new Random();
        boolean[] visited = new boolean[totalCellAmount];

        Stack<Integer> stack = new Stack<>();
        // center cell
        stack.push(0);
        while (!stack.isEmpty()) {
            int currentId = stack.peek();
            visited[currentId] = true;
            List<Integer> neighbors = getNeighbors(currentId)
                    .stream()
                    .filter(i -> !visited[i])
                    .toList();

            if (!neighbors.isEmpty()) {
                int nextId = neighbors.get(r.nextInt(neighbors.size()));
                stack.push(nextId);
                addEdge(currentId, nextId);
            } else {
                stack.pop();
            }
        }

        for (boolean v : visited) {
            if (!v) {
                throw new RuntimeException();
            }
        }
        showToGameManager(gameManager);
    }


    private static List<Integer> getNeighbors(int id) {
        if (id == 0) {
            // center tile
            return IntStream.range(0, cellsPerRing.get(1))
                    .mapToObj(i -> toId(1, i))
                    .toList();
        }

        int cellIdx = toCellIndex(id);
        int ringIdx = toRingIndex(id);

        int cellCount = cellsPerRing.get(ringIdx);

        double startAngle = cellIdx / (double) cellCount * Math.TAU;
        double endAngle = (cellIdx + 1) / (double) cellCount * Math.TAU;


        List<Integer> innerNeighborIds = new ArrayList<>();
        int prevRingCellCount = cellsPerRing.get(ringIdx - 1);
        for (int i = 0; i < prevRingCellCount; i++) {
            double startAngle_other = i / (double) prevRingCellCount * Math.TAU;
            double endAngle_other = (i + 1) / (double) prevRingCellCount * Math.TAU;

            if (angleIntervalsOverlap(startAngle, endAngle, startAngle_other, endAngle_other))  {
                innerNeighborIds.add(toId(ringIdx - 1, i));
            }
        }

        List<Integer> outerNeighborIds = new ArrayList<>();
        if (ringIdx < ringCount - 1) {
            int nextRingCellCount = cellsPerRing.get(ringIdx + 1);
            for (int i = 0; i < nextRingCellCount; i++) {
                double startAngle_other = i / (double) nextRingCellCount * Math.TAU;
                double endAngle_other = (i + 1) / (double) nextRingCellCount * Math.TAU;

                if (angleIntervalsOverlap(startAngle, endAngle, startAngle_other, endAngle_other))  {
                    outerNeighborIds.add(toId(ringIdx + 1, i));
                }
            }
        }

        List<Integer> candidates = new ArrayList<>();
        candidates.add(toId(ringIdx, Math.floorMod(cellIdx - 1, cellCount)));
        candidates.add(toId(ringIdx, (cellIdx + 1) % cellCount));
        candidates.addAll(outerNeighborIds);
        candidates.addAll(innerNeighborIds);

        return candidates.stream()
                .toList();
    }

    private static boolean angleIntervalsOverlap(double start1, double end1, double start2, double end2) {
        // Normalize angles
        start1 = (start1 + Math.TAU) % Math.TAU;
        end1 = (end1 + Math.TAU) % Math.TAU;
        start2 = (start2 + Math.TAU) % Math.TAU;
        end2 = (end2 + Math.TAU) % Math.TAU;

        // Handle intervals that wrap around 0
        if (end1 < start1) end1 += Math.TAU;
        if (end2 < start2) end2 += Math.TAU;

        return !(end1 <= start2 || end2 <= start1);
    }

    public static void addEdge(int id1, int id2) {
        canWalkFromNodeToNode[id1][id2] = true;
        canWalkFromNodeToNode[id2][id1] = true;
    }

    public static int toId(int ringIdx, int cellIdx) {
        int ret = 0;
        for (int i = 0; i < ringIdx; i++) {
            ret += cellsPerRing.get(i);
        }
        return ret + cellIdx;
    }

    public static int toCellIndex(int id) {
        int ringIdx = toRingIndex(id);

        int baseId = 0;
        for (int i = 0; i < ringIdx; i++) {
            baseId += cellsPerRing.get(i);
        }
        return id - baseId;
    }

    public static int toRingIndex(int id) {
        if (id == 0) return 0;

        int total = 0;
        for (int ringIdx = 0; ringIdx < ringCount; ringIdx++) {
            total += cellsPerRing.get(ringIdx);
            if (id < total) return ringIdx;
        }
        throw new RuntimeException("Invalid ID: " + id);
    }


    // --------------------------- methods for the game --------------------------

    private static void showToGameManager(GameManager gameManager) {
        for (int ringIdx = 1; ringIdx < ringCount; ringIdx++) {
            gameManager.getCircleWalls().add(getCircleWall(ringIdx));
            gameManager.getStraightWalls().addAll(getLineWalls(ringIdx));
        }
    }

    private static List<LineSegmentWall> getLineWalls(int ringIdx) {
        List<LineSegmentWall> ret = new ArrayList<>();

        int cellCount = cellsPerRing.get(ringIdx);
        for (int cellIdx = 0; cellIdx < cellCount; cellIdx++) {

            if (!canWalkFromNodeToNode
                    [toId(ringIdx, cellIdx)]
                    [toId(ringIdx, (cellIdx + 1) % cellCount)]
            ) {
                double angle = cellIdx / (double) cellCount * Math.TAU;
                double currentRadius = getRingRadius(ringIdx);
                double nextRadius = getRingRadius(ringIdx + 1);

                Vector2D startPos = new Vector2D(
                        centerX + Math.cos(angle) * currentRadius,
                        centerY + Math.sin(angle) * currentRadius
                );
                Vector2D wallVector = new Vector2D(
                        Math.cos(angle) * (nextRadius - currentRadius),
                        Math.sin(angle) * (nextRadius - currentRadius)
                );
                ret.add(new LineSegmentWall(
                        startPos,
                        wallVector
                ));
            }
        }
        return ret;
    }

    private static CircleWall getCircleWall(int ringIdx) {
        List<Double> angles = new ArrayList<>();

        int cellCount = cellsPerRing.get(ringIdx);

        for (int cellIdx = 0; cellIdx < cellCount; cellIdx++) {
            int id1 = toId(ringIdx, cellIdx);
            int id2 = toId(ringIdx, (cellIdx + 1) % cellCount);

            if (!canWalkFromNodeToNode[id1][id2]) {
                double startAngle = cellIdx / (double) cellCount * Math.TAU;
                double endAngle = ((cellIdx + 1)) / (double) cellCount * Math.TAU;

                angles.add(startAngle);
                angles.add(endAngle);
            }
        }

        return CircleWall.create(
                centerX,
                centerY,
                getRingRadius(ringIdx),
                angles
        );
    }

    private static double getRingRadius(int ringIdx) {
        return ringIdx / (double) ringCount * radius;
    }

    public static void setRadius(double newRadius) {
        MazeGenerator.radius = newRadius;
    }
    public static void setCenter(double x, double y) {
        centerX = x;
        centerY = y;
    }
}