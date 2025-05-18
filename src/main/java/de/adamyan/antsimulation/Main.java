package de.adamyan.antsimulation;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
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

    private Canvas canvas;
    private Text fpsText;

    private Text antAmountText;
    private Text generationAmountText;

    private Canvas antNetworkVisualisationCanvas;

    GameManager gameManager;

    private void start_simulation() {
        gameManager = new GameManager(1000, 800);

        gameManager.getAntPopulation().getFirst().getNetwork().draw(antNetworkVisualisationCanvas, 5, 5);

        AnimationTimer frameTimer = new AnimationTimer() {
            long lastNow;
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastNow) / 1e6;

                boolean genFinished = gameManager.act(canvas, deltaTime);

                // Game window
                fpsText.setText("FPS: " + Math.round(1000 / deltaTime));

                // Ant stats window
                antAmountText.setText("Ant count: " + gameManager.getAntPopulation().size());

                // gen stats window
                if (genFinished) {
                    generationAmountText.setText("Gen count: " + gameManager.getGenCount());
                    gameManager.getAntPopulation().getFirst().getNetwork().draw(antNetworkVisualisationCanvas, 5, 5);
                }

                lastNow = now;
            }
        };
        frameTimer.start();
    }


    @Override
    public void start(Stage stage) {
        // -------- UI stuff ----------

        // -------- Main window -------
        canvas = new Canvas(1000, 800);

        Button resetButton = new Button("New simulation");
        resetButton.setScaleX(2);
        resetButton.setScaleY(2);
        resetButton.setLayoutX(50);
        resetButton.setLayoutY(835.5);
        resetButton.setOnMouseClicked(mouseEvent -> gameManager = new GameManager(canvas.getWidth(), canvas.getHeight()));

        fpsText = new Text("FPS: undefined");
        fpsText.setFont(new Font(20));
        fpsText.setLayoutX(200);
        fpsText.setLayoutY(850);

        Text goalText = new Text("Goal: be at the right wall when the time is over");
        goalText.setFont(new Font(20));
        goalText.setLayoutX(400);
        goalText.setLayoutY(850);

        Group root = new Group(canvas, resetButton, fpsText, goalText);

        Scene scene = new Scene(root, 1000, 900);

        stage.setTitle("God simulator");
        stage.setScene(scene);
        stage.show();

        // -------- Ant stats ---------
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

        ImageView antImage = new ImageView(
                new Image(
                        "ant.png",
                        100,
                        100,
                        true,
                        false
                )
        );
        antImage.setLayoutX(50);
        antImage.setLayoutY(90);
        antImage.setSmooth(false);


        Group antStatsRoot = new Group(antAmountText, antRayAmount, antNetwork, antImage);

        Scene antStatsScene = new Scene(antStatsRoot, 500, 200);

        Stage antStatsStage = new Stage();
        antStatsStage.setX(200);
        antStatsStage.setY(600);
        antStatsStage.setTitle("Ant settings");
        antStatsStage.setScene(antStatsScene);

        antStatsStage.show();

        // -------- Gen stats ---------
        generationAmountText = new Text("Gen count: 0");
        generationAmountText.setFont(new Font(30));
        generationAmountText.setLayoutX(30);
        generationAmountText.setLayoutY(430);



        antNetworkVisualisationCanvas = new Canvas(500, 400);


        Group genStatsRoot = new Group(antNetworkVisualisationCanvas, generationAmountText);

        Scene genStatsScene = new Scene(genStatsRoot, 500, 500);

        Stage genStatsStage = new Stage();
        genStatsStage.setX(200);
        genStatsStage.setY(100);
        genStatsStage.setTitle("Gen stats");
        genStatsStage.setScene(genStatsScene);

        genStatsStage.show();

        // ----------------------------

        start_simulation();
    }

    public static void main(String[] args) {
        launch();
    }
}