package de.adamyan.antsimulation;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Stack;

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



    private void start_simulation() {
        gameManager = new GameManager();

        // Create a new AnimationTimer, which allows us to run code every frame (like in a game loop)
        AnimationTimer frameTimer = new AnimationTimer() {

            // This will store the time of the last frame
            private long lastTime_ns;

            @Override
            public void handle(long currentTime_ns) {
                // Calculate the time passed since the last frame in milliseconds
                double deltaTime_ms = (currentTime_ns - lastTime_ns) / 1e6;

                // Call a method that updates game logic and UI every frame
                stuffThatHappensEveryFrame(deltaTime_ms);

                // Update 'lastTimeInNanoSeconds' for the next frame
                lastTime_ns = currentTime_ns;
            }
        };

        frameTimer.start();
    }


    private void stuffThatHappensEveryFrame(double deltaTime) {
        boolean genFinished = gameManager.act(canvas, deltaTime);

        // Game window
        fpsText.setText("FPS: " + Math.round(1000 / deltaTime));

        // Ant stats window
        antAmountText.setText("Ant count: " + gameManager.getAntPopulation().size());

        // gen stats window
        if (genFinished) {
            generationAmountText.setText("Gen count: " + gameManager.getGenCount());
            gameManager.getAntPopulation().getFirst().getNetwork().draw(antNetworkVisualisationCanvas, 10, 5);
        }
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
        resetButton.setOnMouseClicked(mouseEvent -> gameManager = new GameManager());

        fpsText = new Text("FPS: undefined");
        fpsText.setFont(new Font(20));
        fpsText.setLayoutX(200);
        fpsText.setLayoutY(850);

        Text goalText = new Text("Goal: be at the right wall when the time is over");
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


        Text antNetwork = new Text("Brain structure: " + Ant.DEFAULT_NETWORK);
        antNetwork.setFont(new Font(30));
        antNetwork.setLayoutX(30);
        antNetwork.setLayoutY(90);

        Button toggleRaysButton = new Button("Toggle rays on");
        // eine sigma oneline button
        toggleRaysButton.setOnMouseClicked(mouseEvent -> toggleRaysButton.setText((Ant.shouldDrawRays = !Ant.shouldDrawRays)? "Toggle rays off" : "Toggle rays on"));
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

        Stage genStatsStage = getWindow(500, 500, antNetworkVisualisationCanvas, generationAmountText);
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