package de.adamyan.antsimulation;

import de.adamyan.antsimulation.Physics.CircleWall;
import de.adamyan.antsimulation.Physics.LineSegmentWall;
import de.adamyan.antsimulation.Physics.Vector2D;

import java.util.*;
import java.util.stream.IntStream;

public class MazeGenerator {

    private static boolean[][] canWalkFromNodeToNode;

    private static int ringCount;

    private static int totalCellAmount;

    public static void generateAndSet(int ringCount, GameManager gameManager) {
        MazeGenerator.ringCount = ringCount;

        totalCellAmount = (ringCount - 1) * (ringCount) * 3 + 1; // n * (n + 1) / 2 * 6 + <center cell = 1>

        canWalkFromNodeToNode = new boolean[totalCellAmount][totalCellAmount];

        Random r = new Random();
        boolean[] visited = new boolean[totalCellAmount];

        Stack<Integer> stack = new Stack<>();
        // center cell
        stack.push(0);
        visited[0] = true;
        while (!stack.isEmpty()) {
            int currentId = stack.peek();
            List<Integer> neighbors = getNeighbors(currentId, visited);
            if (!neighbors.isEmpty()) {
                int nextId = neighbors.get(r.nextInt(neighbors.size()));
                stack.push(nextId);
                visited[nextId] = true;
                addEdge(currentId, nextId);
            } else {
                stack.pop();
            }
        }


        showToGameManager(gameManager);
    }

    private static List<Integer> getNeighbors(int id, boolean[] visited) {
        if (id == 0) {
            // center tile
            return IntStream.range(0, 6)
                    .filter(i -> !visited[toId(1, i)])
                    .boxed()
                    .toList();
        }

        int cellIdx = toCellIndex(id);
        int ringIdx = toRingIndex(id);

        int cellCount = ringIdx * 6;

        double startAngle = cellIdx / (double) cellCount * Math.TAU;
        double endAngle = (cellIdx + 1) / (double) cellCount * Math.TAU;

        int prevRingCellCount = ringIdx > 1? cellCount - 6 : 1;
        int nextRingCellCount = cellCount + 6;

        List<Integer> innerNeighborIds = new ArrayList<>();
        if (ringIdx > 1) {
            for (int i = 0; i < prevRingCellCount; i++) {
                double startAngle_other = i / (double) prevRingCellCount * Math.TAU;
                double endAngle_other = (i + 1) / (double) prevRingCellCount * Math.TAU;

                if (angleIntervalsOverlap(startAngle, endAngle, startAngle_other, endAngle_other))  {
                    innerNeighborIds.add(toId(ringIdx - 1, i));
                }
            }
        }

        List<Integer> outerNeighborIds = new ArrayList<>();
        if (ringIdx < ringCount - 1) {
            for (int i = 0; i < nextRingCellCount; i++) {
                double startAngle_other = i / (double) nextRingCellCount * Math.TAU;
                double endAngle_other = (i + 1) / (double) nextRingCellCount * Math.TAU;

                if (angleIntervalsOverlap(startAngle, endAngle, startAngle_other, endAngle_other))  {
                    outerNeighborIds.add(toId(ringIdx + 1, i));
                }
            }
        }

        List<Integer> candidates = new ArrayList<>();
        candidates.add(toId(ringIdx, Math.floorMod(cellIdx - 1, ringIdx * 6)));
        candidates.add(toId(ringIdx, (cellIdx + 1) % (ringIdx * 6)));
        candidates.addAll(outerNeighborIds);
        candidates.addAll(innerNeighborIds);

        return candidates.stream()
                .filter(i -> !visited[i])
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
        if (ringIdx == 0) return 0; // center tile id
        return (ringIdx - 1) * (ringIdx) * 3 + 1 + cellIdx;
    }

    public static int toCellIndex(int id) {
        int ring = toRingIndex(id);
        int baseId = (ring - 1) * (ring) * 3 + 1;
        return id - baseId;
    }

    public static int toRingIndex(int id) {
        if (id == 0) return 0; // center
        int ring = 1;
        while (true) {
            int startId = (ring - 1) * (ring) * 3 + 1;
            int nextStartId = ring * (ring + 1) * 3 + 1;
            if (id >= startId && id < nextStartId) {
                return ring;
            }
            ring++;
        }
    }


    // --------------------------- methods for the game --------------------------

    private static void showToGameManager(GameManager gameManager) {
        for (int ringIdx = 1; ringIdx < ringCount - 1; ringIdx++) {
            gameManager.getCircleWalls().add(getCircleWall(ringIdx));
            gameManager.getStraightWalls().addAll(getLineWalls(ringIdx));
        }
        gameManager.getCircleWalls().add(getCircleWall(ringCount - 1));
        System.out.println();
    }

    private static List<LineSegmentWall> getLineWalls(int ringIdx) {
        List<LineSegmentWall> ret = new ArrayList<>();

        double cellCount = ringIdx * 6;
        for (int cellIdx = 0; cellIdx < cellCount; cellIdx++) {

            if (!canWalkFromNodeToNode[toId(ringIdx, cellIdx)][toId(ringIdx + 1, cellIdx)]) {
                double angle = cellIdx / cellCount * Math.TAU;
                double currentRadius = ringIdx / (double) ringCount * 400;
                double nextRadius = (ringIdx + 1) / (double) ringCount * 400;

                Vector2D startPos = new Vector2D(500 + Math.cos(angle) * currentRadius, 400 + Math.sin(angle) * currentRadius);
                Vector2D wallVector = new Vector2D(Math.cos(angle) * (nextRadius - currentRadius), Math.sin(angle) * (nextRadius - currentRadius));
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

        int firstCellId = toId(ringIdx, 0);

        boolean last = canWalkFromNodeToNode[firstCellId][firstCellId + 1];
        double cellCount = ringIdx * 6;

        for (int cellIdx = 1; cellIdx < cellCount - 1; cellIdx++) {
            boolean current = canWalkFromNodeToNode[toId(ringIdx, cellIdx)][toId(ringIdx, cellIdx + 1)];
            if (current != last) {
                double cellAngle = cellIdx / cellCount * Math.TAU;
                angles.add(cellAngle);
            }
            last = current;
        }

        double radius = ringIdx / (double) ringCount * 400;
        return CircleWall.create(
                500,
                400,
                radius,
                angles
        );
    }
}
