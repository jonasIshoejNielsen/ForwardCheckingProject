import java.util.*;
import java.util.stream.Collectors;

public class MinMaxWizardWithPruning {
    //Default settings based on tests.
    private int MAX_DEPTH = 5; //7 defeats DumAI. 8 is very unbeatable. ranging between 1 - 17 seconds (avg 5s)

    //Notes for heuristics.

    private int[] heuristicWeights = new int[] //Todo - Adjust to become killer heuristics. Currently they good.
            {   6, -3, 2, 2, 2, 2, -3, 6,
                -3, -4, -1, -1, -1, -1, -4, -3,
                2, -1, 1, 0, 0, 1, -1, 2,
                2, -1, 0, 1, 1, 0, -1, 2,
                2, -1, 0, 1, 1, 0, -1, 2,
                2, -1, 1, 0, 0, 1, -1, 2,
                -3, -4, -1, -1, -1, -1, -4, -3,
                6, -3, 2, 2, 2, 2, -3, 6
            };

    MinMaxWizardWithPruning() { }

    public MinMaxWizardWithPruning(int MAX_DEPTH) {
        this.MAX_DEPTH = MAX_DEPTH;
    }

    Position alphaBetaSearch(GameState s) {
        return actions(s)
                .parallelStream()
                .max(Comparator.comparing(p -> minValue(result(s, p), Integer.MIN_VALUE, Integer.MAX_VALUE, 0)))
                .orElse(new Position(-1,-1));
    }


    private int maxValue(GameState s, int alpha, int beta, int depth) {
        if(terminalTest(s, depth)) return utility(s);

        var a = alpha;
        var v  = Integer.MIN_VALUE;
        depth++;

        for(var p : actions(s)) {
            v = Math.max(v, minValue(result(s, p), a, beta, depth));
            if(v >= beta) return v;
            a = Math.max(a, v);
        }

        return v;
    }

    private int minValue(GameState s, int alpha, int beta, int depth) {
        if(terminalTest(s, depth)) return utility(s);

        var b = beta;
        var v  = Integer.MAX_VALUE;
        depth++;

        for(var p : actions(s)) {
            v = Math.min(v, maxValue(result(s, p), alpha, b, depth));
            if(v <= alpha) return v;
            b = Math.min(b, v);
        }

        return v;
    }

    private GameState result(GameState s, Position p) {
        var tmp = new GameState(s.getBoard(), s.getPlayerInTurn());
        tmp.insertToken(p);
        return tmp;
    }

    private List<Position> actions(GameState s) {
        return s.legalMoves().stream().distinct().collect(Collectors.toList());
    }

    private boolean terminalTest(GameState s, int depth) {
        return s.isFinished() || (depth >= MAX_DEPTH);
    }

    private int utility(GameState s) {
        return calculateActionWeight(s) + calculateActionCost(s);
    }

    private int calculateActionWeight(GameState gs) {
        var b = gs.getBoard();
        var w = 0;
        for(var i = 0; i < 64; i++) {
            w += Integer.compare(b[i / 8][i % 8], gs.getPlayerInTurn()) > -1 ? heuristicWeights[i] : -heuristicWeights[i];
        }
        return w;
    }

    private int calculateActionCost(GameState gs) {
        var t = gs.countTokens();
        var tMax = gs.getPlayerInTurn() == 1 ? t[0] : t[1];
        var tMin = gs.getPlayerInTurn() == 1 ? t[1] : t[0];
        return tMax - tMin;
    }
}
