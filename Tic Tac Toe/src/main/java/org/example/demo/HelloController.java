package org.example.demo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;

public class HelloController {
    private char nowSym = 'x';

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    private char gamefield[][] = new char[3][3];

    private boolean isGame = true;


    @FXML
    void btnClick(ActionEvent event) {
        Button btn = (Button)event.getSource();

        if(!isGame || btn.getText() != "") return;

        ((Button)event.getSource()).setText(String.valueOf(nowSym));
        nowSym = nowSym == 'x' ? 'o' : 'x';

        class EndGame {
            public void Endgame() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "У нас есть победитель \"" + btn.getText() + "\"", ButtonType.OK);
                alert.showAndWait();
            }
        }

        int rowIndex = GridPane.getRowIndex(btn) == null ? 0 : GridPane.getRowIndex(btn);
        int columnIndex = GridPane.getColumnIndex(btn) == null ? 0 : GridPane.getColumnIndex(btn);

        gamefield[rowIndex][columnIndex] = nowSym;

        if(gamefield[0][0] == gamefield[0][1] && gamefield[0][0] == gamefield[0][2] && (gamefield[0][0] == 'x' || gamefield[0][0] == 'o')) {
            EndGame endGame = new EndGame();
            endGame.Endgame();
            isGame = false;
        }
        if(gamefield[1][0] == gamefield[1][1] && gamefield[1][0] == gamefield[1][2] && (gamefield[1][0] == 'x' || gamefield[1][0] == 'o')) {
            EndGame endGame = new EndGame();
            endGame.Endgame();
            isGame = false;
        }
        if(gamefield[2][0] == gamefield[2][1] && gamefield[2][0] == gamefield[2][2] && (gamefield[2][0] == 'x' || gamefield[2][0] == 'o')) {
            EndGame endGame = new EndGame();
            endGame.Endgame();
            isGame = false;
        }
        if(gamefield[0][0] == gamefield[1][0] && gamefield[0][0] == gamefield[2][0] && (gamefield[0][0] == 'x' || gamefield[0][0] == 'o')) {
            EndGame endGame = new EndGame();
            endGame.Endgame();
            isGame = false;
        }
        if(gamefield[0][1] == gamefield[1][1] && gamefield[0][1] == gamefield[2][1] && (gamefield[0][1] == 'x' || gamefield[0][1] == 'o')) {
            EndGame endGame = new EndGame();
            endGame.Endgame();
            isGame = false;
        }
        if(gamefield[0][2] == gamefield[1][2] && gamefield[0][2] == gamefield[2][2] && (gamefield[0][2] == 'x' || gamefield[0][2] == 'o')) {
            EndGame endGame = new EndGame();
            endGame.Endgame();
            isGame = false;
        }
        if(gamefield[0][0] == gamefield[1][1] && gamefield[0][0] == gamefield[2][2] && (gamefield[0][0] == 'x' || gamefield[0][0] == 'o')) {
            EndGame endGame = new EndGame();
            endGame.Endgame();
            isGame = false;
        }
        if(gamefield[2][0] == gamefield[1][1] && gamefield[2][0] == gamefield[0][2] && (gamefield[2][0] == 'x' || gamefield[2][0] == 'o')) {
            EndGame endGame = new EndGame();
            endGame.Endgame();
            isGame = false;
        }


    }



    @FXML
    void initialize() {

    }

}
