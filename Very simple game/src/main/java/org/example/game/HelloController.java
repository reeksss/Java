package org.example.game;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class HelloController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label labelPause, labelLose;

    @FXML
    private ImageView bg1, bg2, player, enemy;

    private final int BG_WIDTH = 1285;

    private ParallelTransition parallelTransition;
    private TranslateTransition enemyTransition;

    public static boolean rigth = false;
    public static boolean left = false;
    public static boolean jump = false;
    public static boolean isPause = false;

    private int playerSpeed = 1, jumpDownSpeed = 2;

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long l) {
            if(jump && player.getLayoutY() > 250f) {
                player.setLayoutY(player.getLayoutY() - playerSpeed);
            }
            else if(player.getLayoutY() <= 422f) {
                jump= false;
                player.setLayoutY(player.getLayoutY() + jumpDownSpeed);
            }
            if(left) {
                player.setLayoutX(player.getLayoutX() - playerSpeed);
            }
            if(rigth) {
                player.setLayoutX(player.getLayoutX() + playerSpeed);
            }
            if(isPause && !labelPause.isVisible() && !labelLose.isVisible()) {
                playerSpeed = 0;
                jumpDownSpeed = 0;
                parallelTransition.pause();
                enemyTransition.pause();
                labelPause.setVisible(true);
            }
            else if(!isPause && labelPause.isVisible()) {
                playerSpeed = 1;
                jumpDownSpeed = 2;
                parallelTransition.play();
                enemyTransition.play();
                labelPause.setVisible(false);
            }
            if(player.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                labelLose.setVisible(true);
                playerSpeed = 0;
                jumpDownSpeed = 0;
                parallelTransition.pause();
                enemyTransition.pause();
            }
        }
    };

    @FXML
    void initialize() {
        TranslateTransition bgOneTransition = new TranslateTransition(Duration.millis(5000), bg1);
        bgOneTransition.setFromX(0);
        bgOneTransition.setToX(BG_WIDTH * -1);
        bgOneTransition.setInterpolator(Interpolator.LINEAR);

        TranslateTransition bgTwoTransition = new TranslateTransition(Duration.millis(5000), bg2);
        bgTwoTransition.setFromX(0);
        bgTwoTransition.setToX(BG_WIDTH * -1);
        bgTwoTransition.setInterpolator(Interpolator.LINEAR);

        enemyTransition = new TranslateTransition(Duration.millis(3500), enemy);
        enemyTransition.setFromX(0);
        enemyTransition.setToX(BG_WIDTH * -1 -300);
        enemyTransition.setInterpolator(Interpolator.LINEAR);
        enemyTransition.setCycleCount(Animation.INDEFINITE);
        enemyTransition.play();

        parallelTransition = new ParallelTransition(bgOneTransition, bgTwoTransition);
        parallelTransition.setCycleCount(Animation.INDEFINITE);
        parallelTransition.play();

        timer.start();
    }

}
