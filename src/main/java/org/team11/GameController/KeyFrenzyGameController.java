/* ***************************************
 * CSCI 205 - Software Engineering and Design
 * Spring 2024
 * Instructor: Prof. Lily Romano / Prof. Joshua Stough
 *
 * Name: Ellyn Ngo
 * Section: 02
 * Date: 4/8/2024
 * Time: 2:35 PM
 *
 * Project: csci205_final_project
 * Package: org.team11.GameView
 * Class: KeyFrenzyGameController
 *
 * Description: This class serves as the main view controller for the Key Frenzy game,
 *  managing all elements displayed on the screen during gameplay.
 * It handles the generation and animation of ghosts carrying words for the player to type,
 * tracks the player's score and health, and provides functionality for pausing and ending the game.
 * Additionally, it initializes the scene graph for the game interface
 * Dynamically updates elements such as the message banner and level indicator.
 *
 * **************************************
 */
package org.team11.GameController;

import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.team11.Ghosts.Ghost;
import org.team11.Ghosts.GhostAnimation;
import org.team11.TypingMechanism.WordDictionary;
import org.team11.TypingMechanism.WordsSetting;

import java.io.IOException;
import java.util.*;

public class KeyFrenzyGameController {

    // A vertical box for the main view
    private VBox root;

    // Message Banner remains same for all level
    private Label labelMessageBanner;

    // Score label
    private Label currentScore;

    // Level number label
    private Label leveLbl;

    //The grid of the game
    private GridPane gamePane;

    // A list of ghosts to be generated and animated
    private List<Ghost> ghosts;

    //A hashmap that aids in animation, attaching words to the animation
    private final Map<String, GhostAnimation> wordTimers = new HashMap<>();

    //The text box where the user types the words
    private TextField userTypeBox;

    //Words from the word dictionary
    private final WordDictionary wordDictionary;
    //A random number
    private final Random rand;
    // A timer that schedules the animation time
    private final Timer globalTimer;

    // Users desired nickname
    private final String userName;

    // Keeps track of the players' lives
    private ProgressBar healthBar;

    // Number of lives
    private int lives;

    // Checks if the game is paused or not
    private boolean gamePaused = false;

    // The width of the game pane
    private double paneWidth;

    // The height of the game pane
    private double paneHeight;

    // Variable to check the score*/
    private int score;

    // Stores the level number
    private int level;


    /**
     * This is the "view" in the MVC design for the game Key Frenzy. A view class
     * does nothing more than initializes all nodes for the scene graph for this view.
     */
    public KeyFrenzyGameController(String username) {
        this.userName = username;
        this.score = 0;

        this.wordDictionary = new WordDictionary();
        this.rand = new Random(System.currentTimeMillis());
        this.level = 1;


        initSceneGraph();

        globalTimer = new Timer();
        globalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                generateNewWord();
            }
        }, 5, WordsSetting.WORD_DELAY); // 5 is the time delayed before the first ghost appears
    }

    /**
     * Initialize the entire scene graph
     */
    public void initSceneGraph() {

        // Initialize the root pane
        this.root = new VBox();

        // Create and configure the game pane
        gamePane = new GridPane();
        paneWidth = 750;
        paneHeight = 570;

        // Set minimum size for the gamePane
        gamePane.setMinSize(paneWidth, paneHeight); // Set minimum width

        this.gamePane.getStyleClass().add("game-pane"); // Apply CSS class to gamePane


        // Display the username and time used in the corner of the view
        Label usernameLabel = new Label("Username: " + userName);
        usernameLabel.getStyleClass().add("user-nickname");
        usernameLabel.setFont(Font.font(18));

        // Create and configure the message banner
        configuringMessageBanner();

        // Create and configure the level banner
        configureLevelBanner();

        // Initialize ghosts
        this.ghosts = new ArrayList<>();


        // Create Pause and Stop Game buttons
        Button pauseButton = new Button("Pause");
        pauseButton.getStyleClass().add("pause");
        Button stopButton = new Button("Stop Game");
        stopButton.getStyleClass().add("stop");

        // Add action handlers for the buttons
        // TODO for later: need to fix the pauseGame button
        pauseButton.setOnAction(event -> pauseGame());
        stopButton.setOnAction(event -> gameOver());

        // Create a health bar (progress bar) to display remaining health
        this.healthBar = new ProgressBar(1.0); // Full health initially
        this.healthBar.setPrefWidth(200); // Set preferred width
        this.healthBar.getStyleClass().add("health-bar");
        Label healthLabel = new Label("Health: ");
        healthLabel.getStyleClass().add("health-label");

        // Initialize lives counter
        this.lives = 3;

        // Layout for health bar and lives counter
        HBox healthBox = new HBox(10, healthLabel, healthBar);
        VBox.setMargin(healthBox, new Insets(10));

        // Layout for buttons
        HBox buttonBox = new HBox(10, pauseButton, stopButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout for bottom pane
        HBox bottomPane = new HBox(20, healthBox, buttonBox);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(0, 0, 20, 0)); // Padding bottom only
        HBox gameInfo = new HBox(10, usernameLabel, leveLbl);
        gameInfo.setAlignment(Pos.CENTER);
        gameInfo.setPadding(new Insets(0, 0, 0, 0)); // Padding bottom only

        //Adding the text box to the game
        this.root.getChildren().addAll(labelMessageBanner, currentScore, gameInfo, gamePane, userTypeBox, bottomPane);
//
//        //Adding audio
//        Media media = new Media(new File("audio/chippy.mp3").toURI().toString());
//        MediaPlayer mediaPlayer = new MediaPlayer(media);
//        mediaPlayer.setAutoPlay(true);
//        }
    }


    /**
     * Adds the message banner into the home screen of the came
     */
    private void configuringMessageBanner() {
        labelMessageBanner = new Label("Type words on ghosts to destroy them!");
        this.labelMessageBanner.getStyleClass().add("instruct-banner");
        currentScore = new Label("Current Score: ");
        this.currentScore.getStyleClass().add("current-score");
        this.userTypeBox = new TextField();
        userTypeBox.getStyleClass().add("user-type-box");


        // Add the labels to the game pane
        currentScore.getStyleClass().add("current-score");
        this.labelMessageBanner.getStyleClass().add("instruct-banner");
        userTypeBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {

                handleUserInput(userTypeBox.getText().trim());
                userTypeBox.clear();

            }
        });
    }

    /**
     * Updates the level banner
     */
    private void configureLevelBanner(){
        leveLbl = new Label();
        updateLevelLbl(level);
    }

    /**
     * Handle the user input when prompted
     * @param userInput the String input from user
     */
    public void handleUserInput(String userInput) {

        Iterator<Ghost> iterator = ghosts.iterator();
        while (iterator.hasNext()) {
            Ghost ghost = iterator.next();
            if (ghost.getWord().equalsIgnoreCase(userInput)) {
                // Word matched, remove the ghost from the game pane
                destroy(ghost);
                iterator.remove();

                // Update the score and score label
                score += 10;
                currentScore.setText("Current Score: " + score);

                //update the level and check if level has reached max level
                updateLevel();
                break;
            }
        }
    }


    /**
     * Updates the level as the game is played
     */
    public void updateLevel() {

        // change level after score reaches LEVEL_SCORE
        int LEVEL_SCORE = 80;
        if (score % LEVEL_SCORE == 0){
            // update the level number
            level ++;
            int MAX_LEVEL = 7;
            if(level <= MAX_LEVEL) {
                // update label
                updateLevelLbl(level);
            }
            else{
                // Reached the max level
                gameOver();
            }
        }
    }

    /**
     *
     * @param level, current main
     */
    public void updateLevelLbl(int level){
        leveLbl.setText("Level: " + level);
        leveLbl.setFont(Font.font(18));
        leveLbl.getStyleClass().add("level-label");
    }


    /**
     * Starts the timer and generates a new word to be typed by the player
     */
    private void generateNewWord() {
        // Generate the new word

        String word = wordDictionary.getWord(level);

        // Create a timer that ends the game if the player does not type the word in time.
        Timer wordTimer = new Timer();
        wordTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // When the timer runs out (8 seconds), that means the player loses
                gameOver();
            }
        }, WordsSetting.GAME_LENGTH);

        // Add the words to the global map and
        // draw it on the screen
        List<Ghost> ghostsOnScreen = createAnimation();
        ghosts.add(ghostsOnScreen.get(0));
        ghosts.add(ghostsOnScreen.get(1));

        //Start Ghost Animations
        startGhostAnimation(ghostsOnScreen.get(0));
        startGhostAnimation(ghostsOnScreen.get(1));

        //Store ghost animation in the map
        storeGhostAnimation(word, wordTimer, ghostsOnScreen, 0);
        storeGhostAnimation(word, wordTimer, ghostsOnScreen, 1);
    }

    /**
     * Stores the ghost animation into the map
     */
    private void storeGhostAnimation(String word, Timer wordTimer, List<Ghost> ghostsOnScreen, int index) {
        wordTimers.put(word, new GhostAnimation(wordTimer, ghostsOnScreen.get(index)));
    }

    /**
     * Starts the ghost animations
     *
     * @param ghost to be animated
     */
    private void startGhostAnimation(Ghost ghost) {
        GhostAnimation animation = wordTimers.get(ghost.getWord());

        if (animation != null) {
            animation.start();
        }
    }

    /**
     * Ties the text on top of the ghost,
     * Generates a path and an animation, and adds it to the game pane
     * @return text on top of the ghost that was destroyed
     */
    private List<Ghost> createAnimation() {

        // Create the text object
        List<Ghost> ghostsOnScreen = new ArrayList<>();

        long creationTime = System.currentTimeMillis();

        Ghost ghost1 = new Ghost(wordDictionary.getWord(level));
        Ghost ghost2 = new Ghost(wordDictionary.getWord(level));
        //Starts the timer
        ghost1.setCreationTime(creationTime);
        ghost2.setCreationTime(creationTime);
        ghostsOnScreen.add(ghost1);
        ghostsOnScreen.add(ghost2);


        // Run the animation on the FX App thread
        Platform.runLater(() -> {

            // Get y coords of the words
            double y1 = rand.nextDouble() * paneHeight;
            double y2 = rand.nextDouble() * paneHeight;

            // Generate a path for ghosts coming from left side
            Path path1 = new Path();
            path1.getElements().add(new MoveTo(-50, y1));

            // Moves the path to the middle of the pane
            moveToCenter(ghost1, path1);

            // Generate a path for ghosts coming from right side
            Path path2 = new Path();
            path2.getElements().add(new MoveTo(paneWidth + 50, y2));
            // Moves the path to the middle of the pane
            moveToCenter(ghost2, path2);

            // Add to pane
            gamePane.getChildren().add(ghost1.getNode());
            gamePane.getChildren().add(ghost2.getNode());

        });

        return ghostsOnScreen;

    }

    /**
     * Moves the ghosts to the center of the game pane
     *
     * @param path to be moved
     */
    private void moveToCenter(Ghost ghost, Path path) {

        double centerX = paneWidth/2;
        double centerY = paneHeight/2;

        path.getElements().add(new LineTo(centerX, centerY));

        // Create a PathTransition to animate the ghost along the path
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(WordsSetting.WORD_DURATION)); // Set animation duration
        pathTransition.setPath(path); // Set the path for the animation
        pathTransition.setNode(ghost.getNode()); // Set the node (ghost) to animate
        pathTransition.setCycleCount(1); // Animation plays once

        // Event handler to handle animation completion (ghost reaches center)
        if (ghost.isActive()) {
            pathTransition.setOnFinished(event -> {
                // Check if the ghost is still in the game pane
                if (gamePane.getChildren().contains(ghost.getNode())) {
                    // Ghost is still present in the game pane
                    lives--; // Decrease health
                    updateHealthBar(); // Update health bar

                    // Remove the ghost from the game pane
                    destroy(ghost);
                }
            });

            // Start the animation
            pathTransition.play();
        }
    }


    /**
     * Updates the health
     */

    public void updateHealthBar() {
        double healthPercentage = (double) lives / 3.0; // Assuming 3 lives in total
        healthBar.setProgress(healthPercentage);

        if (lives <= 0) {
            // Game over logic (no more health)
            gameOver();
        }
    }

    /**
     * Pauses the game
     */
    // Doesn't work well though
    private void pauseGame() {
        if (!gamePaused) {
            gamePaused = true;
            // Pause any ongoing animations or timers
            //animationTimer.stop();
            globalTimer.cancel();
            // Stop any ghost animations
            stopGhostAnimations();

        } else {
            gamePaused = false;
            // Resume animations or timers
            // animationTimer.start();
            // Resume ghost animations
            resumeGhostAnimations();
        }
    }


    /**
     * Stops the ghost animations
     */
    private void stopGhostAnimations() {
        for (Ghost ghost : ghosts) {
            GhostAnimation animation = wordTimers.get(ghost.getWord());
            if (animation != null) {
                animation.stop();
            }
        }
    }

    /**
     * Stops the ghost animations
     */
    private void resumeGhostAnimations() {
        for (Ghost ghost : ghosts) {
            GhostAnimation animation = wordTimers.get(ghost.getWord());
            if (animation != null) {
                animation.start();
            }
        }
    }

    /**
     * Terminates the game
     */

    private void gameOver() {
        // Perform actions on the main thread
        Platform.runLater(() -> {
            // Stop all timers
            globalTimer.cancel();
            for (GhostAnimation wa : wordTimers.values()) {
                wa.stop();
            }

            try {
                // Load the FXML file. Obtain the root of the scene graph
                FXMLLoader loader = new FXMLLoader();

                loader.setLocation(getClass().getResource("/fxml/gameOverView.fxml"));

                Parent root = loader.load();
                Stage primaryStage = new Stage();
                // Set up the stage and show it
                primaryStage.setTitle("Hello FXML!");
                primaryStage.setScene(new Scene(root));
                primaryStage.sizeToScene();
                primaryStage.show();

                // Close the current WelcomeMenu window
                Stage currentStage = (Stage) labelMessageBanner.getScene().getWindow();
                currentStage.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    /**
     * Makes the Ghosts disappear from the game pane,
     * @param ghost ,the ghost to be destroyed
     */
    public void destroy(Ghost ghost) {
        gamePane.getChildren().remove(ghost.getNode());
    }

    /*
    Getter method
     */

    /**
     * @return the root
     */
    public VBox getRoot() {
        return root;
    }

    /**
     * Sets the game pane
     * @param gamePane, the current game pane
     */
    public void setGamePane(GridPane gamePane) {
        this.gamePane = gamePane;
    }

    /**
     * @return the gamePane
     */
    public GridPane getGamePane() {
        return this.gamePane;
    }

    public Label getLeveLbl() {
        return leveLbl;
    }

    public TextField getUserTypeBox() {
        return userTypeBox;
    }

    public ProgressBar getHealthBar() {
        return healthBar;
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public Label getCurrentScore() {
        return currentScore;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
