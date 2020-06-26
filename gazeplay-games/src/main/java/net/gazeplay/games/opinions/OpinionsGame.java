package net.gazeplay.games.opinions;

import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.components.ProgressButton;

import java.util.List;

@Slf4j
public class OpinionsGame implements GameLifeCycle {

    private final OpinionsGameStats opinionGameStats;
    private final IGameContext gameContext;
    private final Dimension2D dimension2D;
    private final Configuration configuration;
    private final Group backgroundLayer;
    private final Group middleLayer;
    private final OpinionsGameStats stats;

    private final ImageLibrary backgroundImage;
    private final ImageLibrary thumbImage;

    private Rectangle background;

    private int score = 0;

    private ProgressButton thumbUp;
    private ProgressButton thumbDown;
    private ProgressButton noCare;

    public OpinionsGame(final IGameContext gameContext, final OpinionsGameStats stats) {
        this.stats = stats;
        this.opinionGameStats = this.stats;
        this.gameContext = gameContext;
        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.configuration = gameContext.getConfiguration();

        thumbImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions/thumbs"));
        this.backgroundImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions"));
        this.backgroundLayer = new Group();
        this.middleLayer = new Group();
        final Group foregroundLayer = new Group();
        gameContext.getChildren().add(foregroundLayer);

    }

    @Override
    public void launch() {

        this.backgroundLayer.getChildren().clear();
        this.middleLayer.getChildren().clear();

        background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());

        backgroundLayer.getChildren().add(background);
        background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));

        thumbDown = new ProgressButton();
        thumbDown.setLayoutX(dimension2D.getWidth() * 18 / 20);
        thumbDown.setLayoutY(dimension2D.getHeight() * 2 / 5);
        thumbDown.getButton().setRadius(70);

        ImageView thumbDo = new ImageView(new Image("data/opinions/thumbs/thumbdown.png"));
        thumbDo.setFitWidth(dimension2D.getWidth() / 10);
        thumbDo.setFitHeight(dimension2D.getHeight() / 5);
        thumbDown.setImage(thumbDo);

        thumbDown.assignIndicator(event -> {
            background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
            stats.incrementNumberOfGoalsReached();
            updateScore();
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(thumbDown);
        thumbDown.active();

        noCare = new ProgressButton();
        noCare.setLayoutX(dimension2D.getWidth() / 2 - dimension2D.getWidth() / 20);
        noCare.setLayoutY(0);
        noCare.getButton().setRadius(70);
        ImageView noCar = new ImageView(new Image("data/opinions/thumbs/nocare.png"));
        noCar.setFitWidth(dimension2D.getWidth() / 10);
        noCar.setFitHeight(dimension2D.getHeight() / 5);
        noCare.setImage(noCar);

        noCare.assignIndicator(event -> {
            background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
            stats.incrementNumberOfGoalsReached();
            updateScore();
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(noCare);
        noCare.active();

        thumbUp = new ProgressButton();
        thumbUp.setLayoutX(0);
        thumbUp.setLayoutY(dimension2D.getHeight() * 2 / 5);
        thumbUp.getButton().setRadius(70);
        ImageView thumbU = new ImageView(new Image("data/opinions/thumbs/thumbup.png"));
        thumbU.setFitWidth(dimension2D.getWidth() / 10);
        thumbU.setFitHeight(dimension2D.getHeight() / 5);
        thumbUp.setImage(thumbU);

        thumbUp.assignIndicator(event -> {
            background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));
            stats.incrementNumberOfGoalsReached();
            updateScore();
        }, configuration.getFixationLength());
        gameContext.getGazeDeviceManager().addEventFilter(thumbUp);
        thumbUp.active();

        List<Image> Picture = thumbImage.pickAllImages();
        for (Image I : Picture) {
            log.info("coucou: " + I.getUrl());
            if (I.getUrl().equals("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbdown.png")) {
                thumbDo = new ImageView(new ImagePattern(new Image("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbdown.png")).getImage());
                thumbDown.setImage(thumbDo);
                thumbDo.setFitWidth(dimension2D.getWidth() / 10);
                thumbDo.setFitHeight(dimension2D.getHeight() / 5);
            }
            if (I.getUrl().equals("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbup.png")) {
                thumbU = new ImageView(new ImagePattern(new Image("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/thumbup.png")).getImage());
                thumbUp.setImage(thumbU);
                thumbU.setFitWidth(dimension2D.getWidth() / 10);
                thumbU.setFitHeight(dimension2D.getHeight() / 5);
            }
            if (I.getUrl().equals("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/nocare.png")) {
                noCar = new ImageView(new ImagePattern(new Image("file:/C:/Users/MATOU/GazePlay/files/images/opinions/thumbs/nocare.png")).getImage());
                noCare.setImage(noCar);
                noCar.setFitWidth(dimension2D.getWidth() / 10);
                noCar.setFitHeight(dimension2D.getHeight() / 5);
            }
        }

        middleLayer.getChildren().addAll(thumbUp, thumbDown, noCare);

        gameContext.getChildren().addAll(backgroundLayer, middleLayer);

        opinionGameStats.notifyNewRoundReady();
    }

    private void updateScore() {
        score = score + 1;
        if (score == 10) {
            gameContext.playWinTransition(0, event1 -> gameContext.showRoundStats(opinionGameStats, this));
        }
    }

    @Override
    public void dispose() {

    }
}
