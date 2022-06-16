import java.util.*;

/**
 * A simple OthelloAI-implementation. The method to decide the next move just
 * returns the first legal move that it finds. 
 * @author Mai Ajspur
 * @version 9.2.2018
 */

//TODO: name this class after our group number

public class OurAI implements IOthelloAI{
	private int maxDepth = 8;	//8 if 12,  11 if 8/default
	private int currPlayer;		//1 or 2

	public Position decideMove(GameState s){
		currPlayer = s.getPlayerInTurn();
		return alphaBetaSearch(s);
	}
	private Position alphaBetaSearch(GameState s){
		Position pos = new Position(-1,-1);
		float v = Float.MIN_VALUE;
		float alpha = Float.MIN_VALUE;
		float beta	= Float.MAX_VALUE;
        for (var a : legalActions(s)){
			var r = minValue(result(s,a), 1 , alpha, beta);
			if(r>v){
				v=r;
				pos = a;
			}
		}
        return pos;
	}


	private float maxValue(GameState s, int currDepth, float alpha, float beta){
		if(terminalTest(s, currDepth)) return Utility(s);
		float v = Float.MIN_VALUE;
		for (var a : legalActions(s)){
			v = Math.max(v, minValue(result(s, a), currDepth+1, alpha, beta));
			if(v >= beta) return v;
			alpha = Math.max(alpha, v);
		} 
		return v;
	}
	private float minValue(GameState s,int currDepth, float alpha, float beta){
		if(terminalTest(s, currDepth)) return Utility(s);
		float v = Float.MAX_VALUE;
		for (var a : legalActions(s)){
			v = Math.max(v, maxValue(result(s, a), currDepth+1, alpha, beta));
			if(v <= alpha) return v;
			beta = Math.min(beta, v);
		} 
		return v;
	}
	private boolean terminalTest(GameState s, int currDepth){
		return s.isFinished() || currDepth>=maxDepth;
	}

	private int Utility(GameState s){
		var results = s.countTokens();
		if(currPlayer == 1) return results[0] - results[1];
		return results[1] - results[0];
	}
	
	private int Utility2(GameState s){
		var results = s.countTokens();
		if(currPlayer == 1) return results[0];
		return results[1];
	}

	private int Utility3(GameState s){
		var results = s.countTokens();
		if(currPlayer == 1) return  (results[0] > results[1])? 1 : (results[0] == results[1])? 0 : -1;
		return  (results[0] < results[1])? 1 : (results[0] == results[1])? 0 : -1;
	}
	private Collection<Position> legalActions(GameState s){
		return new HashSet<>(s.legalMoves());
	}
	private GameState result(GameState s, Position pos){
        var newState = new GameState(s.getBoard(), s.getPlayerInTurn());
		newState.insertToken(pos);
		return newState;
	}

}
