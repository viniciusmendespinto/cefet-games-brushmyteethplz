package br.cefetmg.games.logic.chooser;

import br.cefetmg.games.minigames.MiniGame;
import br.cefetmg.games.minigames.util.DifficultyCurve;
import br.cefetmg.games.minigames.factories.MiniGameFactory;
import br.cefetmg.games.screens.BaseScreen;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.Set;
import br.cefetmg.games.minigames.util.GameStateObserver;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * Monta uma sequência de minigames a serem jogados.
 * 
 * @author fegemo <coutinho@decom.cefetmg.br>
 */
public class GameSequencer {

    private final int numberOfGames;
    private final Set<MiniGameFactory> availableGames;
    private final ArrayList<MiniGameFactory> previousGames;
    private final BaseScreen screen;
    private final GameStateObserver observer;
    private Integer[] indexSequence;

    /**
     * Cria um novo sequenciador com um número de minigames igual a 
     * {@code numberOfGames}, a partir de um <em>pool</em> de minigames
     * {@code availableGames}.
     * @param numberOfGames total de jogos que será criado para o jogador.
     * @param availableGames os tipos de minigames disponíveis para o 
     * sequenciador.
     * @param screen a tela dona destes jogos.
     * @param observer um observador da mudança de estado dos jogos.
     */
    public GameSequencer(int numberOfGames,
            Set<MiniGameFactory> availableGames, BaseScreen screen,
            GameStateObserver observer) {
        if (numberOfGames <= 0) {
            throw new IllegalArgumentException("Tentou-se criar um "
                    + "GameSequencer com 0 jogos. Deve haver ao menos 1.");
        }
        this.numberOfGames = numberOfGames;
        this.availableGames = availableGames;
        this.screen = screen;
        this.observer = observer;
        previousGames = new ArrayList<MiniGameFactory>();
        indexSequence = new Integer[numberOfGames];
        determineGameSequence();
        preloadAssets();
    }

    public boolean hasNextGame() {
        return previousGames.size() < numberOfGames;
    }

    private void determineGameSequence() {
        for (int i = 0; i < numberOfGames; i++) {
            indexSequence[i] = MathUtils.random(availableGames.size() - 1);
        }
    }

    private float getSequenceProgress() {
        return ((float) previousGames.size() - 1) / numberOfGames;
    }

    /**
     * Pré-carrega os <em>assets</em> dos minigames que foram selecionados.
     */
    private void preloadAssets() {
        HashMap<String, Class> allAssets = new HashMap<String, Class>();
        HashSet<Integer> allFactoriesIndices = new HashSet<Integer>(
                Arrays.asList(indexSequence));
        for (Integer i : allFactoriesIndices) {
            allAssets.putAll(((MiniGameFactory) availableGames.toArray()[i])
                    .getAssetsToPreload());
        }
        for (Entry<String, Class> asset : allAssets.entrySet()) {
            screen.assets.load(asset.getKey(), asset.getValue());
        }
    }

    /**
     * Retorna uma instância do próximo jogo.
     * @return uma instância do próximo jogo.
     */
    public MiniGame nextGame() {
        MiniGameFactory factory = (MiniGameFactory) availableGames
                .toArray()[indexSequence[getGameNumber()]];

        previousGames.add(factory);
        return factory.createMiniGame(screen, observer,
                DifficultyCurve.LINEAR.getCurveValue(getSequenceProgress()));
    }

    /**
     * Retorna o índice deste jogo na série de jogos criados para o jogador.
     * @return o índice deste jogo na série de jogos criados para o jogador.
     */
    public int getGameNumber() {
        return previousGames.size();
    }

}
