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

    GameManager gameManager;

    private void start_simulation() {
        gameManager = new GameManager(1000, 800);

        AnimationTimer frameTimer = new AnimationTimer() {
            long lastNow;
            @Override
            public void handle(long now) {
                gameManager.act(canvas);

                double deltaTime = (now - lastNow) / 1e6;
                fpsText.setText("FPS: " + Math.round(1000 / deltaTime));

                antAmountText.setText("Ant count: " + gameManager.getAntPopulation().size());

                lastNow = now;
            }
        };
        frameTimer.start();
    }


    @Override
    public void start(Stage stage) {
        // -------- UI stuff ----------

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


        Group root = new Group(canvas, resetButton, fpsText);



        // ----------------------------
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
        antStatsStage.setY(500);
        antStatsStage.setTitle("Ant stats");
        antStatsStage.setScene(antStatsScene);

        antStatsStage.show();
        // ----------------------------


        Scene scene = new Scene(root, 1000, 900);

        stage.setTitle("God simulator");
        stage.setScene(scene);
        stage.show();

        start_simulation();
    }

    public static void main(String[] args) {
        launch();
    }
}