package net.gazeplay.games.ninja;

import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

/**
 * Created by schwab on 26/12/2016.
 */
public class Ninja implements GameLifeCycle {

    private final GameContext gameContext;

    private final ShootGamesStats stats;

    public Ninja(GameContext gameContext, ShootGamesStats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
    }

    @Override
    public void launch() {
        Target portrait = new Target(gameContext, gameContext.getRandomPositionGenerator(), stats,
                Portrait.loadAllImages());

        gameContext.getChildren().add(portrait);
    }

    @Override
    public void dispose() {

    }
}
