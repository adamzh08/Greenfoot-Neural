package de.adamyan.antsimulation;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {

    // ----------------------- UI fields -----------------------

    // main window fields
    private Canvas canvas;
    private Text fpsText;

    // ant settings fields
    private Text antAmountText;
    private Text generationAmountText;

    // gen stats fields
    private Canvas antNetworkVisualisationCanvas;

    // ---------------------- other fields ---------------------------
    private GameManager gameManager;

    private AnimationTimer frameTimer_visible;
    private Timer frameTimer_invisible;
    private boolean visibleMode = true;


    private void start_simulation() {
        gameManager = new GameManager();

        if (frameTimer_visible != null) {
            frameTimer_visible.stop();
        }
        stopInvisibleFrameTimer();

        reDrawGenStatsWindow();

        // Create a new AnimationTimer, which allows us to run code every frame (like in a game loop)
        frameTimer_visible = new AnimationTimer() {

            // This will store the time of the last frame
            private long lastTime_ns;

            @Override
            public void handle(long currentTime_ns) {
                // Calculate the time passed since the last frame in milliseconds
                double deltaTime_ms = (currentTime_ns - lastTime_ns) / 1e6;

                // Call a method that updates game logic and UI every frame
                stuffThatHappensEveryFrame(deltaTime_ms, true);

                // Update 'lastTimeInNanoSeconds' for the next frame
                lastTime_ns = currentTime_ns;
            }
        };
        frameTimer_visible.start();
    }


    private void changeVisibilityMode() {
        visibleMode = !visibleMode;

        if (visibleMode) {
            stopInvisibleFrameTimer();
            frameTimer_visible.start();
            reDrawGenStatsWindow();
        } else {
            frameTimer_visible.stop();
            stopInvisibleFrameTimer();

            frameTimer_invisible.scheduleAtFixedRate(new TimerTask() {
                long lastTime_ns = System.nanoTime();

                @Override
                public void run() {
                    long currTime_ns = System.nanoTime();
                    stuffThatHappensEveryFrame((currTime_ns - lastTime_ns) / 1e6, false);
                    lastTime_ns = currTime_ns;
                }
            }, 0, 1);

            // pause triangle
            canvas.getGraphicsContext2D().setFill(new Color(0.4, 0.4, 0.4, 0.4));
            canvas.getGraphicsContext2D().fillPolygon(
                    new double[]{
                            400,
                            500 + Math.sqrt(3) / 2 * 100,
                            400
                    },
                    new double[]{
                            500,
                            400,
                            300
                    },
                    3
            );
        }
    }

    private void stopInvisibleFrameTimer() {
        if (frameTimer_invisible != null) {
            frameTimer_invisible.cancel();
        }
        frameTimer_invisible = new Timer();
    }

    private void stuffThatHappensEveryFrame(double deltaTime, boolean shouldDraw) {
        double startTime = System.nanoTime();
        boolean genFinished = gameManager.frame_logic();
        System.out.println("Logic: " + (System.nanoTime() - startTime) / 1e6 + "ms");

        Platform.runLater(() -> {
            // Game window
            fpsText.setText("FPS: " + Math.round(1000 / deltaTime));

            // Ant stats window
            antAmountText.setText("Ant count: " + gameManager.getAntPopulation().stream().filter(Objects::nonNull).count() + "/" + GameManager.POPULATION_SIZE);

            // gen stats window
            if (genFinished) {
                reDrawGenStatsWindow();
            }
        });

        if (shouldDraw) {
            startTime = System.nanoTime();
            gameManager.draw(canvas);
            System.out.println("Draw: " + (System.nanoTime() - startTime) / 1e6 + "ms");
        }
    }

    private void reDrawGenStatsWindow() {
        generationAmountText.setText("Gen count: " + gameManager.getGenCount());
        gameManager.getBestAnt().getNetwork().draw(antNetworkVisualisationCanvas, 10, 5);
    }

    @Override
    public void start(Stage stage) {
        // -------- Window instantiations ----------
        generateWindow_main();
        generateWidow_antSettings();
        generateWindow_genStats();

        start_simulation();
    }

    // ---------------------------- UI Window generator methods -----------------------------

    public void generateWindow_main() {
        canvas = new Canvas(1000, 800);

        Button resetButton = new Button("New simulation");
        resetButton.setScaleX(2);
        resetButton.setScaleY(2);
        resetButton.setLayoutX(50);
        resetButton.setLayoutY(835.5);
        resetButton.setOnMouseClicked(mouseEvent -> start_simulation());

        fpsText = new Text("FPS: undefined");
        fpsText.setFont(new Font(20));
        fpsText.setLayoutX(200);
        fpsText.setLayoutY(850);

        Text goalText = new Text("Goal: be at the right wallVector when the time is over");
        goalText.setFont(new Font(20));
        goalText.setLayoutX(400);
        goalText.setLayoutY(850);

        Stage mainStage = getWindow(1000, 900, canvas, resetButton, fpsText, goalText);
        mainStage.setTitle("God simulator");
    }

    public void generateWidow_antSettings() {
        antAmountText = new Text("Ant count: undefined");
        antAmountText.setFont(new Font(30));
        antAmountText.setLayoutX(30);
        antAmountText.setLayoutY(30);

        Text antRayAmount = new Text("Ray count: " + Ant.RAY_COUNT);
        antRayAmount.setFont(new Font(30));
        antRayAmount.setLayoutX(30);
        antRayAmount.setLayoutY(60);


        Text antNetwork = new Text("Brain structure: " + Arrays.toString(Ant.LAYERS));
        antNetwork.setFont(new Font(30));
        antNetwork.setLayoutX(30);
        antNetwork.setLayoutY(90);

        Button toggleRaysButton = new Button("Toggle rays on");
        toggleRaysButton.setOnMouseClicked(mouseEvent -> toggleRaysButton.setText((GameManager.shouldDrawRays = !GameManager.shouldDrawRays) ? "Toggle rays off" : "Toggle rays on"));
        toggleRaysButton.setFont(new Font(20));
        toggleRaysButton.setLayoutX(300);
        toggleRaysButton.setLayoutY(120);

        // big image of ant
        ImageView antImage = new ImageView(new Image(
                "ant.png",
                100,
                100,
                true,
                false
        ));
        antImage.setLayoutX(50);
        antImage.setLayoutY(90);
        antImage.setSmooth(false);


        Stage antStatsStage = getWindow(500, 200, antAmountText, antRayAmount, antNetwork, antImage, toggleRaysButton);
        antStatsStage.setTitle("Ant settings");
        antStatsStage.setX(200);
        antStatsStage.setY(600);

        antStatsStage.show();
    }

    public void generateWindow_genStats() {
        antNetworkVisualisationCanvas = new Canvas(500, 400);

        generationAmountText = new Text("Gen count: 0");
        generationAmountText.setFont(new Font(30));
        generationAmountText.setLayoutX(30);
        generationAmountText.setLayoutY(430);

        Button visibilityButton = new Button("Change visibility");
        visibilityButton.setLayoutX(250);
        visibilityButton.setLayoutY(400);
        visibilityButton.setFont(new Font(20));
        visibilityButton.setOnMouseClicked(mouseEvent -> changeVisibilityMode());

        Stage genStatsStage = getWindow(500, 500, antNetworkVisualisationCanvas, generationAmountText, visibilityButton);
        genStatsStage.setX(200);
        genStatsStage.setY(100);
        genStatsStage.setTitle("Gen stats");
    }

    public Stage getWindow(int width, int height, Node... nodes) {
        Group root = new Group(nodes);
        Scene scene = new Scene(root, width, height);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();

        return stage;
    }


    public static void main(String[] args) {
        launch();
    }
}