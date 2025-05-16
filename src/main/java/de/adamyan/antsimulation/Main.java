package de.adamyan.antsimulation;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    private Canvas canvas;
    private Group root;
    private Text fpsText;

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
        resetButton.setLayoutX(0);
        resetButton.setLayoutY(850);
        resetButton.setOnMouseClicked(mouseEvent -> gameManager = new GameManager(canvas.getWidth(), canvas.getHeight()));

        fpsText = new Text("FPS: undefined");
        fpsText.setFont(new Font(20));
        fpsText.setLayoutX(200);
        fpsText.setLayoutY(850);

        root = new Group(canvas, resetButton, fpsText);

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