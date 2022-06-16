import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class MinMaxWizardWithPruningTests {
    @Test
    @DisplayName("alphaBetaSearch_returns_valid_position")
    public void alphaBetaSearch_returns_valid_position() {
        var gs  = new GameState(8, 1);
        var mmw = new MinMaxWizardWithPruning();

        var pos = mmw.alphaBetaSearch(gs);
        System.out.println(pos.toString());
        assertFalse(new Position(-1,-1).equals(pos));
    }
    @Test
    @DisplayName("Determine if the SmartAI can handle a draw position")
    public void alphaBetaSearch_can_handle_draw_positions() {
        var drawState = new int[][] {
                {1,2,2,0},
                {2,1,1,2},
                {2,2,2,1},
                {2,2,2,2},
        };

        var gs  = new GameState(drawState, 1);
        var mmw = new MinMaxWizardWithPruning();

        var pos = mmw.alphaBetaSearch(gs);

        assertFalse(new Position(-1,-1).equals(pos));
    }


    @Test
    @DisplayName("Playing self ends up with a result")
    public void Wizard_plays_itself_4x4_board() {
        var gs  = new GameState(8, 1);
        var mmw = new MinMaxWizardWithPruning();

        var moveTimes = new ArrayList<Long>();

        while(!gs.isFinished()) {
            var start = System.currentTimeMillis();
            if(gs.legalMoves().isEmpty()) gs.changePlayer();
            var move = mmw.alphaBetaSearch(gs);
            gs.insertToken(move);
            var end = System.currentTimeMillis();
            moveTimes.add(end - start);
        }

        var avg = moveTimes.stream().mapToLong(v -> v).sum() / moveTimes.size();
        assertFalse(avg > 1000);
    }

    @Test
    @DisplayName("ExpertAI should beat DumAI consistently over 10 games on a 6x6 board")
    public void ExpertAI_Beats_DumAI_on_6x6_board_with_6_depth() {

        var smartWins   = 0;
        var dumWins     = 0;
        var draw        = 0;

        for(var i = 0; i < 10; i++) {
            var gs = new GameState(8, 1);

            IOthelloAI AI1;
            IOthelloAI AI2;
            boolean isSmartFirst = false;

            var start = System.nanoTime();

            if (new Random().nextInt(2) == 0) {
                isSmartFirst = true;
                AI1 = new ExpertAI();
                AI2 = new DumAI();
                System.out.println(getTimestamp() + ": AI1 = ExpertAI vs AI2 = DumAI");
            } else {
                AI1 = new ExpertAI();
                AI2 = new SmartAI();
                System.out.println(getTimestamp() + ": AI1 = DumAI vs AI2 = ExpertAI");
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
                        System.out.println(getTimestamp() + ": ExpertAI Wins!");
                    }
                    else {
                        dumWins++;
                        System.out.println(getTimestamp() + ": DumAI Wins!");
                    }
                } else {
                    if (tokensBlack < tokensWhite) {
                        smartWins++;
                        System.out.println(getTimestamp() + ": ExpertAI Wins!");
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
    @DisplayName("alfaBetaSearch will beat DumAI on 8x8 board with average moves lower than 10 seconds.")
    public void alfaBetaSearch_Battles_DumAI_Increasing_Depth_Until_Unbeatable_on_8x8() {

        var moveTimes       = new ArrayList<Long>();
        var depth           = 1;
        var matchCount      = 0;
        var winStreak       = 0;
        var matchesPerDepth = 2;
        long avg            = 0;

        while(winStreak < 2) {

            for (var i = 1; i <= matchesPerDepth; i++) {

                matchCount++;

                var playerNumberSmartAI = ((i % 2) == 0) ? 1 : 2;

                var gs      = new GameState(8, 1);
                var mmw     = new MinMaxWizardWithPruning(depth); //Aka ExpertAI
                var DumAI   = new DumAI();
                avg         = 0;

                var blackPlayer         = "Black ("+(playerNumberSmartAI == 1 ? "ExpertAI" : "DumAI")+")";
                var whitePlayer         = "White ("+(playerNumberSmartAI == 2 ? "ExpertAI" : "DumAI")+")";

                System.out.println();
                System.out.println(" --- Game " + matchCount + " ---");
                System.out.println(blackPlayer + " versus " + whitePlayer);
                System.out.println();

                moveTimes     = new ArrayList<Long>();
                var startGame = System.currentTimeMillis();

                while (!gs.isFinished()) {

                    //System.out.print("Turn: " + (gs.getPlayerInTurn() == 1 ? blackPlayer : whitePlayer));
                    if (gs.legalMoves().isEmpty()) gs.changePlayer();

                    Position move;

                    if (gs.getPlayerInTurn() == playerNumberSmartAI) {
                        var start = System.currentTimeMillis();
                        move = mmw.alphaBetaSearch(gs);
                        var end = System.currentTimeMillis();
                        moveTimes.add(end - start);
                        System.out.println("ExpertAIs move took " + (end-start) + " ms");
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
                assertTrue(avg < 10000);
                if(playerNumberSmartAI == 1 && isBlackWinner) winStreak++;
                else { winStreak--; }
            }

            if(matchCount % 2 == 0)  {
                depth++;
                if(winStreak < 2) winStreak = 0;
                System.out.println("Depth is increased to " + depth);
            }

        }
        System.out.println("ExpertAI is undefeated when depth is: " + depth);


    }

    @Test
    @DisplayName("ExpertAI vs DumAI with ExpertAI starting")
    public void ExpertAI_Beats_DumAI_on_8x8() {

        var gs          = new GameState(8, 1);
        var expertAi    = new ExpertAI();
        var dumAi       = new DumAI();

        var start = System.nanoTime();
        while (!gs.isFinished()) {
            if (gs.legalMoves().isEmpty()) gs.changePlayer();
            if (gs.getPlayerInTurn() == 1) {
                gs.insertToken(expertAi.decideMove(gs));
                System.out.println("ExpertAI's turn");
            }
            else {
                gs.insertToken(dumAi.decideMove(gs));
                System.out.println("DumAI's turn");
            }
        }
        var tokens = gs.countTokens();
        System.out.println("Tokens black" + tokens[0]);
        System.out.println("Tokens white" + tokens[1]);

        var tokensBlack = gs.countTokens()[0];
        var tokensWhite = gs.countTokens()[1];

        if(tokensBlack != tokensWhite) {
            if (tokensBlack > tokensWhite) System.out.println(getTimestamp() + ": Black Wins!");
            else System.out.println(getTimestamp() + ": White Wins!");
        } else System.out.println(getTimestamp() + ": Draw!");

        var end = System.nanoTime();
        System.out.println(getTimestamp() + ": Elapsed time: " + TimeUnit.SECONDS.convert ((end-start), TimeUnit.NANOSECONDS) + " s");

    }

    String getTimestamp() {
        return new SimpleDateFormat("HH.mm.ss.ms").format(new Date());
    }
}