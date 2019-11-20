package net.gazeplay.ui.scenes.ingame;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GamePanelDimensionProvider;
import net.gazeplay.GazePlay;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.*;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.RandomPositionGenerator;
import net.gazeplay.ui.AnimationSpeedRatioControl;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.MusicControl;
import net.gazeplay.ui.scenes.stats.StatsContext;

import java.io.IOException;

@Slf4j
public class GameContext extends GraphicalContext<Pane> implements IGameContext {

    public static void updateConfigPane(final Pane configPane, Stage primaryStage) {
        double mainHeight = primaryStage.getHeight();

        final double newY = mainHeight - configPane.getHeight() - 30;
        log.debug("translated config pane to y : {}, height : {}", newY, configPane.getHeight());
        configPane.setTranslateY(newY);
    }

    @Setter
    private static boolean runAsynchronousStatsPersist = false;

    @Getter
    private final Translator translator;

    private Slider getSpeedSlider;

    private final Bravo bravo;

    @Getter
    private final HBox menuHBox;

    @Getter
    private final RandomPositionGenerator randomPositionGenerator;

    @Getter
    private final GamePanelDimensionProvider gamePanelDimensionProvider;

    @Getter
    private final GazeDeviceManager gazeDeviceManager;

    private final Pane configPane;

    private final Pane gamingRoot;

    protected GameContext(
        @NonNull GazePlay gazePlay,
        @NonNull Translator translator,
        @NonNull Pane root,
        @NonNull Pane gamingRoot,
        @NonNull Bravo bravo,
        @NonNull HBox menuHBox,
        @NonNull GazeDeviceManager gazeDeviceManager,
        @NonNull Pane configPane
    ) {
        super(gazePlay, root);
        this.translator = translator;
        this.gamingRoot = gamingRoot;
        this.bravo = bravo;
        this.menuHBox = menuHBox;
        this.gazeDeviceManager = gazeDeviceManager;
        this.configPane = configPane;

        this.gamePanelDimensionProvider = new GamePanelDimensionProvider(() -> root, gazePlay::getPrimaryScene);
        this.randomPositionGenerator = new RandomPanePositionGenerator(gamePanelDimensionProvider);
    }

    @Override
    public void setUpOnStage(final Scene scene) {

        super.setUpOnStage(scene);

        log.info("SETTING UP");
        updateConfigPane(configPane, getGazePlay().getPrimaryStage());
    }

    public void resetBordersToFront() {
        // rootBorderPane.setBottom(null);
        // rootBorderPane.setBottom(bottomPane);
    }

    public void createQuitShortcut(@NonNull GazePlay gazePlay, @NonNull Stats stats, GameLifeCycle currentGame) {
        Configuration config = ActiveConfigurationContext.getInstance();
        final Scene scene = gazePlay.getPrimaryScene();

        // gamingRoot.getChildren().add(scene);
        EventHandler buttonHandler = new EventHandler<KeyEvent>() {

            public void handle(KeyEvent event) {

                try {
                    exitGame(stats, gazePlay, currentGame);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                scene.removeEventHandler(KeyEvent.KEY_PRESSED, this);
                scene.removeEventHandler(KeyEvent.KEY_RELEASED, this);
            }
        };
        // scene.addEventHandler(KeyEvent.KEY_PRESSED, buttonHandler);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if (key.getCode().getChar().equals(config.getQuitKey())) {
                log.info("Key Value : {} ; quitkey: {}", key.getCode().getChar(), config.getQuitKey());
                scene.addEventHandler(KeyEvent.KEY_RELEASED, buttonHandler);
            }
        });
    }

    public void speedAdjust(@NonNull GazePlay gp) {
        Configuration config = ActiveConfigurationContext.getInstance();
        final Scene scene = gp.getPrimaryScene();

        Node outerNode = menuHBox.getChildren().get(2); // get SpeedEffectsPane
        if (outerNode instanceof TitledPane) {
            Node inPane = ((TitledPane) outerNode).getContent();
            if (inPane instanceof BorderPane) {
                Node inBorderPane = ((BorderPane) inPane).getCenter();
                if (inBorderPane instanceof HBox) {
                    for (Node inHBox : ((HBox) inBorderPane).getChildren()) {
                        if (inHBox instanceof Slider) {
                            getSpeedSlider = (Slider) inHBox;
                        }
                    }
                }
            }
        }

        EventHandler increaseSpeed = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                double sliderSpeedValue = getSpeedSlider.getValue();
                sliderSpeedValue = Math.floor(sliderSpeedValue);

                if (sliderSpeedValue < getSpeedSlider.getMax()) {
                    getSpeedSlider.setValue(sliderSpeedValue + 1);
                    config.getSpeedEffectsProperty().bind(getSpeedSlider.valueProperty());
                } else
                    log.info("max speed for effects reached !");
                config.saveConfigIgnoringExceptions();

                scene.removeEventHandler(KeyEvent.KEY_PRESSED, this);
                scene.removeEventHandler(KeyEvent.KEY_RELEASED, this);

            }
        };
        EventHandler decreaseSpeed = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                double sliderSpeedValue = getSpeedSlider.getValue();
                sliderSpeedValue = Math.floor(sliderSpeedValue);

                if (sliderSpeedValue > getSpeedSlider.getMin()) {
                    getSpeedSlider.setValue(sliderSpeedValue - 1);
                    config.getSpeedEffectsProperty().bind(getSpeedSlider.valueProperty());

                } else
                    log.info("min speed for effects reached !");
                config.saveConfigIgnoringExceptions();

                scene.removeEventHandler(KeyEvent.KEY_PRESSED, this);
                scene.removeEventHandler(KeyEvent.KEY_RELEASED, this);
            }
        };

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if (key.getCode().toString().equals("F")) {
                log.info("Key Value :{}", key.getCode().toString());
                scene.addEventHandler(KeyEvent.KEY_RELEASED, increaseSpeed);
            } else if (key.getCode().toString().equals("S")) {
                log.info("Key Value :{}", key.getCode().toString());
                scene.addEventHandler(KeyEvent.KEY_RELEASED, decreaseSpeed);
            }
        });

    }

    public void createControlPanel(@NonNull GazePlay gazePlay, @NonNull Stats stats, GameLifeCycle currentGame) {
        Configuration config = ActiveConfigurationContext.getInstance();
        MusicControl musicControl = getMusicControl();
        AnimationSpeedRatioControl animationSpeedRatioControl = AnimationSpeedRatioControl.getInstance();

        GridPane leftControlPane = new GridPane();
        leftControlPane.setHgap(5);
        leftControlPane.setVgap(5);
        leftControlPane.setAlignment(Pos.TOP_CENTER);
        leftControlPane.add(musicControl.createMusicControlPane(config), 0, 0);
        leftControlPane.add(musicControl.createVolumeLevelControlPane(config, gazePlay.getTranslator()), 1, 0);
        leftControlPane.add(animationSpeedRatioControl.createSpeedEffectsPane(config, gazePlay.getTranslator()), 2, 0);
        leftControlPane.getChildren().forEach(node -> {
            GridPane.setVgrow(node, Priority.ALWAYS);
            GridPane.setHgrow(node, Priority.ALWAYS);
        });


        menuHBox.getChildren().add(leftControlPane);

        I18NButton toggleFullScreenButtonInGameScreen = createToggleFullScreenButtonInGameScreen(gazePlay);
        menuHBox.getChildren().add(toggleFullScreenButtonInGameScreen);

        HomeButton homeButton = createHomeButtonInGameScreen(gazePlay, stats, currentGame);
        menuHBox.getChildren().add(homeButton);
    }

    public HomeButton createHomeButtonInGameScreen(@NonNull GazePlay gazePlay, @NonNull Stats stats,
                                                   @NonNull GameLifeCycle currentGame) {

        EventHandler<Event> homeEvent = e -> {
            root.setCursor(Cursor.WAIT); // Change cursor to wait style
            try {
                exitGame(stats, gazePlay, currentGame);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            // BackgroundMusicManager.getInstance().pause();
            root.setCursor(Cursor.DEFAULT); // Change cursor to default style
        };

        HomeButton homeButton = new HomeButton();
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);
        return homeButton;
    }

    private void exitGame(@NonNull Stats stats, @NonNull GazePlay gazePlay, @NonNull GameLifeCycle currentGame)
        throws IOException {
        currentGame.dispose();
        ForegroundSoundsUtils.stopSound(); // to stop playing the sound of Bravo
        stats.stop();
        gazeDeviceManager.clear();
        gazeDeviceManager.destroy();

        Runnable asynchronousStatsPersistTask = () -> {
            try {
                stats.saveStats();
            } catch (IOException e) {
                log.error("Failed to save stats file", e);
            }
        };

        if (runAsynchronousStatsPersist) {
            AsyncUiTaskExecutor.getInstance().getExecutorService().execute(asynchronousStatsPersistTask);
        } else {
            asynchronousStatsPersistTask.run();
        }

        StatsContext statsContext = StatsContext.newInstance(gazePlay, stats);

        this.clear();

        gazePlay.onDisplayStats(statsContext);
    }

    @Override
    public @NonNull Configuration getConfiguration() {
        return ActiveConfigurationContext.getInstance();
    }

    @Override
    public Stage getPrimaryStage() {
        return getGazePlay().getPrimaryStage();
    }

    @Override
    public Scene getPrimaryScene() {
        return getGazePlay().getPrimaryScene();
    }

    @Override
    public void showRoundStats(Stats stats, GameLifeCycle currentGame) {
        stats.stop();

        Runnable asynchronousStatsPersistTask = () -> {
            try {
                stats.saveStats();
            } catch (IOException e) {
                log.error("Failed to save stats file", e);
            }
        };

        if (runAsynchronousStatsPersist) {
            AsyncUiTaskExecutor.getInstance().getExecutorService().execute(asynchronousStatsPersistTask);
        } else {
            asynchronousStatsPersistTask.run();
        }

        CustomButton continueButton = new CustomButton("data/common/images/continue.png");
        continueButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            getGazePlay().onGameLaunch(this);
            currentGame.launch();
        });

        StatsContext statsContext = StatsContext.newInstance(getGazePlay(), stats, continueButton);

        this.clear();
        getGazePlay().onDisplayStats(statsContext);

        stats.reset();
    }

    @Override
    public void playWinTransition(long delay, EventHandler<ActionEvent> onFinishedEventHandler) {
        getChildren().add(bravo);
        bravo.toFront();
        bravo.setConfetiOnStart(this);
        bravo.playWinTransition(root, delay, onFinishedEventHandler);
    }

    @Override
    public void endWinTransition() {
        getChildren().remove(bravo);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return gamingRoot.getChildren();
    }

    @Override
    public Pane getRoot() {
        return gamingRoot;
    }

    public void onGameStarted() {
    }

}
