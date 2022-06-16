import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OurAITests {
    @Test
    @DisplayName("test time of random move, size 8")
    public void OurAI_Given_RandomState_on_Size8_Runs_in_time()
    {
        int reachedLimitCounter = 0;
        var totalTimeTaken = 0;
        int repeatTimes = 30;
        for(int repeats=0; repeats<repeatTimes; repeats++) {
            GameState s = new GameState(8, 1);
            for (int i = 0; i < new Random().nextInt(50); i++) {
                if (s.isFinished()) break;
                if (s.insertToken(new RandomAI().decideMove(s))) ;
            }
            if (s.isFinished()) { repeats--; continue;}
            var start = System.currentTimeMillis();
            new OurAI().decideMove(s);
            var timeTaken = System.currentTimeMillis() - start;
            if(timeTaken >=10000) reachedLimitCounter ++;
            totalTimeTaken += timeTaken;
        }
        assertTrue(totalTimeTaken/repeatTimes < 10000);
        assertTrue(reachedLimitCounter <= repeatTimes/5);
    }
    @Test
    @DisplayName("test constant win over dumbAI at different board sizes")
    public void OurAI__winrate_dumbAI_size8()
    {
        RunGame(8);
    }
    @Test
    @DisplayName("test constant win over dumbAI at different board sizes")
    public void OurAI__winrate_dumbAI_size10()
    {
        RunGame(10);
    }
    private  void  RunGame(int size){
        GameState s = new GameState(size, 1);
        while (!s.isFinished()) {
            if (s.insertToken(new OurAI().decideMove(s))) ;
            if (s.isFinished()) break;
            if (s.insertToken(new RandomAI().decideMove(s))) ;
        }
        assertTrue(s.countTokens()[0] > s.countTokens()[1]);
    }
}
