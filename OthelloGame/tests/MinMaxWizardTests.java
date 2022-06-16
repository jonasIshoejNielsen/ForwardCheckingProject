import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class MinMaxWizardTests {
    @Test
    @DisplayName("Checking if GameState can return legal moves with duplicates in the list.")
    public void GameState_LegalMoves_Returns_Duplicate_Moves() {
        var gs  = new GameState(8, 1);

        var hasDuplicates = false;

        for(var i = 0; i < 10; i++) {
            var actions = gs.legalMoves();

            for (var a : actions) {
                if (Collections.frequency(actions, a) > 1) hasDuplicates = true;
            }
            gs.insertToken(actions.get(0));
        }

        assertTrue(hasDuplicates);
    }
    @Test
    @DisplayName("minMaxDecision will return a valid position")
    public void minMaxDecision_given_GameState_returns_a_valid_position() {
        var gs  = new GameState(8, 1);
        var mmw = new MinMaxWizard(5);

        var pos = mmw.minMaxDecision(gs);

        assertFalse(new Position(-1,-1).equals(pos));
    }

    @Test
    @DisplayName("Determine if the SmartAI can handle a draw position")
    public void minMaxDecision_can_handle_draw_positions() {
        var drawState = new int[][] {
                {1,2,2,0},
                {2,1,1,2},
                {2,2,2,1},
                {2,2,2,2},
        };

        var gs  = new GameState(drawState, 1);
        var mmw = new MinMaxWizard();

        var pos = mmw.minMaxDecision(gs);

        assertFalse(new Position(-1,-1).equals(pos));
    }

    @Test
    @DisplayName("MinMaxWizard plays against itself on a 4x4 board and returns a winner in less than 1 second")
    public void minMaxDecision_Given_4x4_GameBoard() {
        var gs  = new GameState(4, 1);
        var mmw = new MinMaxWizard(4);

        var moveTimes = new ArrayList<Long>();

        while(!gs.isFinished()) {
            var start = System.currentTimeMillis();
            //System.out.println("Turn: " + (gs.getPlayerInTurn() == 1 ? "Black" : "White"));
            if(gs.legalMoves().isEmpty()) gs.changePlayer();

            var move = mmw.minMaxDecision(gs);
            gs.insertToken(move);;

/*            if(gs.insertToken(move)) System.out.println("Move: " + move.toString());
            else System.out.println("Illegal move!");*/

            var end = System.currentTimeMillis();
            moveTimes.add(end - start);

            //mmw.printBoard(gs.getBoard());
        }
/*        var tokensBlack = gs.countTokens()[0];
        var tokensWhite = gs.countTokens()[1];*/

/*        System.out.println("Winner: " + ((tokensBlack == tokensWhite) ? "Draw" : (tokensBlack > tokensWhite) ?  "Black" : "White"));
        System.out.println("Tokens black: " + tokensBlack);
        System.out.println("Tokens white: " + tokensWhite);*/

        var avg = moveTimes.stream().mapToLong(v -> v).sum() / moveTimes.size();
        assertFalse(avg > 1000);
    }

    @Test
    @DisplayName("SmartAI should beat DumAI consistently over 10 games on a 6x6 board where minmaxwizard has default depth")
    public void SmartAI_Beats_DumAI_on_6x6_board_with_6_depth() {

        var smartWins   = 0;
        var dumWins     = 0;
        var draw        = 0;

        for(var i = 0; i < 10; i++) {
            var gs = new GameState(6, 1);

            IOthelloAI AI1;
            IOthelloAI AI2;
            boolean isSmartFirst = false;

            var start = System.nanoTime();

            if (new Random().nextInt(2) == 0) {
                isSmartFirst = true;
                AI1 = new SmartAI();
                AI2 = new DumAI();
                System.out.println(getTimestamp() + ": AI1 = SmartAI vs AI2 = DumAI");
            } else {
                AI1 = new DumAI();
                AI2 = new SmartAI();
                System.out.println(getTimestamp() + ": AI1 = DumAI vs AI2 = SmartAI");
            }

            while (!gs.isFinished()) {
                if (gs.legalMoves().isEmpty()) gs.changePlayer();

                if (gs.getPlayerInTurn() == 1) gs.insertToken(AI1.decideMove(gs));
                else gs.insertToken(AI2.decideMove(gs));
            }

            var tokensBlack = gs.countTokens()[0];
            var tokensWhite = gs.countTokens()[1];

            if(tokensBlack != tokensWhite) {
                if (isSmartFirst) {
                    if (tokensBlack > tokensWhite) {
                        smartWins++;
                        System.out.println(getTimestamp() + ": SmartAI Wins!");
                    }
                    else {
                        dumWins++;
                        System.out.println(getTimestamp() + ": DumAI Wins!");
                    }
                } else {
                    if (tokensBlack < tokensWhite) {
                        smartWins++;
                        System.out.println(getTimestamp() + ": SmartAI Wins!");
                    }
                    else {
                        dumWins++;
                        System.out.println(getTimestamp() + ": DumAI Wins!");
                    }
                }
            } else {
                draw++;
                System.out.println(getTimestamp() + ": Draw!");
            }
            var end = System.nanoTime();
            System.out.println(getTimestamp() + ": Elapsed time: " + TimeUnit.SECONDS.convert ((end-start), TimeUnit.NANOSECONDS) + " s");

        }

        assertTrue(smartWins > dumWins);
        assertTrue(dumWins == 0);
        assertTrue(draw == 0);
    }

    @Test
    @DisplayName("MinMaxWizard beats DumAI on 8x8 board with average moves lower than 10 seconds.")
    public void SmartAI_DumAI_8x8() {

        var moveTimes       = new ArrayList<Long>();
        var depth           = 5;
        var matchCount      = 0;
        var winStreak       = 0;
        var matchesPerDepth = 2;
        long avg            = 0;

        while(winStreak < 2) {

            for (var i = 1; i <= matchesPerDepth; i++) {

                matchCount++;

                var playerNumberSmartAI = ((i % 2) == 0) ? 1 : 2; //Always let DumAI start, to avoid wasting time if first game is lost to DumAI.

                if(i == 2 && winStreak < 1) {
                    var c = matchCount;
                    System.out.println("--- Game " + (c-1) +  " skipped. ----");
                    continue; //Skip depth if already lost first match.
                }

                var gs      = new GameState(8, 1);
                var mmw     = new MinMaxWizard(depth); //Aka SmartAI
                var DumAI   = new DumAI();
                avg         = 0;

                var blackPlayer         = "Black ("+(playerNumberSmartAI == 1 ? "SmartAI" : "DumAI")+")";
                var whitePlayer         = "White ("+(playerNumberSmartAI == 2 ? "SmartAI" : "DumAI")+")";

                System.out.println();
                System.out.println(" --- Game " + matchCount + " ---");
                System.out.println(blackPlayer + " versus " + whitePlayer);
                System.out.println("Depth: " + depth);
                System.out.println();

                moveTimes     = new ArrayList<Long>();
                var startGame = System.currentTimeMillis();

                while (!gs.isFinished()) {

                    //System.out.print("Turn: " + (gs.getPlayerInTurn() == 1 ? blackPlayer : whitePlayer));
                    if (gs.legalMoves().isEmpty()) gs.changePlayer();

                    Position move;

                    if (gs.getPlayerInTurn() == playerNumberSmartAI) {
                        var start = System.currentTimeMillis();
                        move = mmw.minMaxDecision(gs);
                        var end = System.currentTimeMillis();
                        moveTimes.add(end - start);
                        System.out.println("SmartAI move took " + (end-start) + " ms");
                    } else {
                        move = DumAI.decideMove(gs);
                    }

                    var legalMove = gs.insertToken(move);
                }

                var endGame = System.currentTimeMillis();
                var tokensBlack = gs.countTokens()[0];
                var tokensWhite = gs.countTokens()[1];
                var isBlackWinner = tokensBlack > tokensWhite;
                System.out.println();
                System.out.println("Winner: " + ((tokensBlack == tokensWhite) ? "Draw" : (tokensBlack > tokensWhite) ? blackPlayer : whitePlayer));
                System.out.println("Tokens black: " + tokensBlack);
                System.out.println("Tokens white: " + tokensWhite);
                System.out.println("Depth: " + depth);
                System.out.println("Time elapsed: " + (endGame - startGame) + " ms");
                System.out.println();
                //mmw.printBoard(gs.getBoard());
                System.out.println();

                avg = (avg + moveTimes.stream().mapToLong(v -> v).sum() / moveTimes.size());

                System.out.println("Average move count is: " + avg + " ms");

                if(playerNumberSmartAI == 1 && isBlackWinner) winStreak++;
                else { winStreak--; }
            }

            if(matchCount % 2 == 0)  {
                depth++;
                if(winStreak < 2) winStreak = 0;
                System.out.println("Depth is increased to " + depth);
            }

        }
        System.out.println("SmartAI is undefeated when depth is: " + depth);

        assertFalse(avg < 10000);
    }

    @Test
    @DisplayName("Utility is faster than 1 ms")
    public void utility_speed_test() {
        var avg = averageActionTime(8, 100000, MinMaxWizard.WizardAction.UTILITY, 6, 20);
        System.out.println("Average time for Utility calls: " + (avg / 1000) + " ms");
        assertTrue(1000 > avg);
    }

    @Test
    @DisplayName("Actions is faster than 1 ms")
    public void actions_speed_test() {
        var avg = averageActionTime(8, 100000, MinMaxWizard.WizardAction.ACTION, 6, 20);
        System.out.println("Average time for Actions calls: " + (avg / 1000) + " ms");
        assertTrue(1000 > avg);
    }

    @Test
    @DisplayName("Result is faster than 1 ms")
    public void result_speed_test() {
        var avg = averageActionTime(8, 100000, MinMaxWizard.WizardAction.RESULT, 6, 20);
        System.out.println("Average time for Actions calls: " + (avg / 1000) + " ms");
        assertTrue(1000 > avg);
    }

    @Test
    @DisplayName("maxValue is faster than 10 seconds")
    public void maxValue_speed_test() {
        var avg = averageActionTime(8, 100, MinMaxWizard.WizardAction.MAX_VALUE, 6, 20);
        System.out.println("Average time for maxValue calls: " + avg  + " ms");
        assertTrue(10000 > avg);
    }

    @Test
    @DisplayName("minValue is faster than 1 ms")
    public void minValue_speed_test() {
        var avg = averageActionTime(8, 100000, MinMaxWizard.WizardAction.MIN_VALUE, 6, 20);
        System.out.println("Average time for minValue calls: " +  avg + " ms");
        assertTrue(1000 > avg);
    }

    @Test
    @DisplayName("terminalTest is faster than 1 ms")
    public void terminalTest_speed_test() {
        var avg = averageActionTime(8, 1, MinMaxWizard.WizardAction.TERMINAL, 6, 20);
        System.out.println("Average time for terminalTest calls: " + avg  + " ms");
        assertTrue(1000 > avg);
    }

    @Test
    @DisplayName("minMaxDecision on 8x8 is faster than 10 seconds")
    public void minMaxDecision_speed_test() {
        var avg = averageActionTime(8, 100, MinMaxWizard.WizardAction.MIN_MAX_DECISION, 5, 50);
        System.out.println("Average time for minMaxDecision: " + avg  + " ms");
        assertTrue(10000 > avg);
    }

    @Test
    @DisplayName("All methods called individually to describe average call time.")
    public void method_call_time_test_coverage() {
        var b = 8;
        var n = 100;
        var d = 4;
        var mb = 50;

        var MAX_VALUE   = averageActionTime(b, n, MinMaxWizard.WizardAction.MAX_VALUE, d, mb);
        var MIN_VALUE   = averageActionTime(b, n, MinMaxWizard.WizardAction.MIN_VALUE, d, mb);
        var ACTION      = averageActionTime(b, n, MinMaxWizard.WizardAction.ACTION, d, mb);
        var RESULT      = averageActionTime(b, n, MinMaxWizard.WizardAction.RESULT, d, mb);
        var TERMINAL    = averageActionTime(b, n, MinMaxWizard.WizardAction.TERMINAL, d, mb);
        var UTILITY     = averageActionTime(b, n, MinMaxWizard.WizardAction.UTILITY, d, mb);

        System.out.println("MAX_VALUE avg: " + MAX_VALUE  + " ms");
        System.out.println("MIN_VALUE avg: " + MIN_VALUE  + " ms");
        System.out.println("ACTION avg: " + ACTION  + " ms");
        System.out.println("RESULT avg: " + RESULT  + " ms");
        System.out.println("TERMINAL avg: " + TERMINAL  + " ms");
        System.out.println("UTILITY avg: " + UTILITY  + " ms");
    }

    @Test
    @DisplayName("Statistical analysis of every decision in MinMaxWizard")
    public void getStatistics() {
        var gs  = new GameState(4, 1);
        var mmw = new MinMaxWizard();

        gs.insertToken(mmw.minMaxDecision(gs));

        var log = mmw.getStatistics();

        System.out.println("Utility calls: " + log.get("utilityCalls"));
        System.out.println("Action calls: " + log.get("actionCalls"));
        System.out.println("Terminal calls: " + log.get("terminalTestCalls"));
        System.out.println("Recursion calls: " + log.get("recursionCalls"));
        System.out.println("Result calls: " + log.get("resultCalls"));

        assertFalse(true);
    }

    @Test
    @DisplayName("Statistical analysis of every decision in MinMaxWizard")
    public void getEventLog_returns_map_of_times() {
        var gs  = new GameState(6, 1);
        var mmw = new MinMaxWizard();

        gs.insertToken(mmw.minMaxDecision(gs));

        var log = mmw.getEventTimeLog();

        System.out.println("Action time: " + log.get(MinMaxWizard.WizardAction.ACTION).stream().mapToLong(v -> v).sum() + " ms");
        System.out.println("Utility time: " + log.get(MinMaxWizard.WizardAction.UTILITY).stream().mapToLong(v -> v).sum() + " ms");
        System.out.println("Min-value time: " + log.get(MinMaxWizard.WizardAction.MIN_VALUE).stream().mapToLong(v -> v).sum() + " ms");
        System.out.println("Max-value time: " + log.get(MinMaxWizard.WizardAction.MAX_VALUE).stream().mapToLong(v -> v).sum() + " ms");
        System.out.println("Terminal time: " + log.get(MinMaxWizard.WizardAction.TERMINAL).stream().mapToLong(v -> v).sum() + " ms");
        System.out.println("Result time: " + log.get(MinMaxWizard.WizardAction.RESULT).stream().mapToLong(v -> v).sum() + " ms");


        assertFalse(true);
    }

    //Helper tools for testing.
    GameState getRandomGameState(int size, int playerToStart, int movesBound) {
        var gs = new GameState(size, playerToStart);

        var rnd = new Random();
        var AI = new DumAI();
        for(var i = 0; i < rnd.nextInt(movesBound); i++) {
            if (gs.legalMoves().isEmpty()) gs.changePlayer();
            gs.insertToken(AI.decideMove(gs));
        }

        return gs;
    }

    String getTimestamp() {
        return new SimpleDateFormat("HH.mm.ss.ms").format(new Date());
    }

    long averageActionTime(int boardSize, int n, MinMaxWizard.WizardAction a, int depth, int movesBound) {
        var times = new ArrayList<Long>();

        for(var i = 0; i < n; i++) {
            var gs = getRandomGameState(boardSize, 1, movesBound);
            var m  = new MinMaxWizard(depth);

            var start = System.currentTimeMillis();

            switch(a) {
                case ACTION:
                    m.actions(gs);
                    break;
                case UTILITY:
                    m.utility(gs);
                    break;
                case TERMINAL:
                    m.terminalTest(gs, 0);
                    break;
                case RESULT:
                    m.result(gs, gs.legalMoves().get(0));
                    break;
                case MAX_VALUE:
                    m.maxValue(gs, 0);
                    break;
                case MIN_VALUE:
                    m.minValue(gs, 0);
                    break;
                case MIN_MAX_DECISION:
                    m.minMaxDecision(gs);
                    break;
                default:
                    break;
            }
            var end = System.currentTimeMillis();
            times.add(end-start);
        }
        return (long) times.stream().mapToLong(v -> v).sum() / times.size();
    }

}
