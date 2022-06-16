import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MinMaxWizard implements IMinMaxWizard {
    //Default settings based on tests.
    private int maxDepth             = 4;       //Maximum depth of minMax game tree calculations

    //Statistics tools. Enabled by setting isDiagnosticMode to true.
    private boolean isDebugging      = false;   //Will write all actions into log file.
    private boolean isDiagnosticMode = false;

    private boolean isStatisticsMode = false;
    private long recursionCalls      = 0;
    private long terminalTestCalls   = 0;
    private long utilityCalls        = 0;
    private long actionCalls         = 0;
    private long resultCalls         = 0;

    //Enum to describe every action made by the wizard.
    public enum WizardAction {
        ACTION, RESULT, UTILITY, TERMINAL, MIN_VALUE, MAX_VALUE, MIN_MAX_DECISION;
    }

    //Will record the time between every WizardAction if diagnostic mode is on.
    private HashMap<WizardAction, ArrayList<Long>> eventTimeLog = new HashMap<>();


    //Constructor choices
    MinMaxWizard() {}

    MinMaxWizard(int depth) {
        maxDepth = depth;
    }

    MinMaxWizard(boolean debug) {
        isDebugging = debug;
    }

    MinMaxWizard(boolean debug, int depth) {
        isDebugging = debug;
        maxDepth = depth;
    }

    MinMaxWizard(int maxDepth, boolean isDebugging, boolean isDiagnosticMode, boolean isStatisticsMode) {
        this.maxDepth = maxDepth;
        this.isDebugging = isDebugging;
        this.isDiagnosticMode = isDiagnosticMode;
        this.isStatisticsMode = isStatisticsMode;
    }

    public Position minMaxDecision(GameState s) {
        Position pos = new Position(-1,-1);
        var v = -2;
        for(var a : actions(s)) {
            //System.out.println("Checking action: " + a.toString());
            var rs = minValue(result(s,a), 0);
            if(rs > v) {
                v = rs;
                pos = a;
            }
            //System.out.println("v: " + rs);
        }
       // System.out.println("Best action: " + pos.toString());
        return pos;
    }

    public int maxValue(GameState s, int depth) {

        if(isStatisticsMode) recursionCalls++;
        var start = System.currentTimeMillis();

        if(terminalTest(s, depth)) return utility(s);

        var v = -1;
        var d = depth + 1;
        for(var a : actions(s)) v = Math.max(v, minValue(result(s, a), d));

        if(isDiagnosticMode) {
            var end = System.currentTimeMillis();
            addToEventLog(WizardAction.MAX_VALUE, (end - start));
        }

        return v;
    }

    public int minValue(GameState s, int depth) {
        if(isStatisticsMode) recursionCalls++;
        var start = System.currentTimeMillis();

        if(terminalTest(s, depth)) return utility(s);

        var v = 1;
        var d = depth + 1;
        for(var a : actions(s)) v = Math.min(v, maxValue(result(s, a), d));

        if(isDiagnosticMode) {
            var end = System.currentTimeMillis();
            addToEventLog(WizardAction.MIN_VALUE, (end - start));
        }
        return v;
    }

    public GameState result(GameState s, Position p) {
        if(isStatisticsMode) resultCalls++;
        var start = System.currentTimeMillis();
        var tmp = new GameState(s.getBoard(), s.getPlayerInTurn());

        if(isDebugging) {
            System.out.println();
            System.out.println();
            System.out.println("Turn: " + ((tmp.getPlayerInTurn() == 1) ? "Black" : "White"));

            System.out.println("Game state info:");
            System.out.println(" - IsTerminal: " + tmp.isFinished());
            System.out.println(" - Legal moves count: " + tmp.legalMoves().size());
            System.out.println(" - Legal moves:" + legalMovesToString(tmp));
            printBoard(tmp.getBoard());

            System.out.println();
            System.out.println("Selected action: " + p.toString());
        }

        var rs = tmp.insertToken(p);

        if(isDebugging) {
            System.out.println("Legal: " + rs);
            System.out.println();

            printBoard(tmp.getBoard());

            var isFinished = tmp.isFinished();
            System.out.println("IsFinished: " + isFinished);
            System.out.println("Black tokens: " + tmp.countTokens()[0]);
            System.out.println("White tokens: " + tmp.countTokens()[1]);
            if(isFinished) System.out.println("Winner: " + ((utility(s) == 0) ? "Draw" : utility(s) == 1 ? "Black" : "White"));

            System.out.println();
            System.out.println("---------------------------------------------------------------");
            System.out.println();
        }
        if(isDiagnosticMode) {
            var end = System.currentTimeMillis();
            addToEventLog(WizardAction.RESULT, (end - start));
        }
        return tmp;
    }

    public List<Position> actions(GameState s) {
        if(isStatisticsMode) actionCalls++;

        var start = System.currentTimeMillis();

        var actions = s.legalMoves().stream().distinct().collect(Collectors.toList());
        if(isDiagnosticMode) {
            var end = System.currentTimeMillis();
            addToEventLog(WizardAction.ACTION, (end - start));
        }
        return actions;
    }

    public boolean terminalTest(GameState s, int depth) {
        if(isStatisticsMode) terminalTestCalls++;
        var start = System.currentTimeMillis();

        var rs = s.isFinished();

        if(isDiagnosticMode) {
            var end = System.currentTimeMillis();
            addToEventLog(WizardAction.TERMINAL, (end - start));
        }

        return rs || (depth >= maxDepth);
    }

    public int utility(GameState s) {
        if(isStatisticsMode) utilityCalls++;
        var start = System.currentTimeMillis();

        var t = s.countTokens();
        var rs = s.getPlayerInTurn() == 1 ? Integer.compare(t[0], t[1]) : Integer.compare(t[1], t[0]);

        if(isDiagnosticMode) {
            var end = System.currentTimeMillis();
            addToEventLog(WizardAction.UTILITY, (end - start));
        }

        return rs;
    }



    //Helper tools

    //Todo - Prettify code if needed. Right now it's just for writing a log file.
    void printBoard(int[][] board) {
        var sb = new StringBuilder();
        sb.append("    ");
        for(var j = 0; j < board.length; j++) {
            sb.append(" ");
            sb.append(j);
            sb.append(" ");
        }

        sb.append("\n");
        sb.append("   ");
        sb.append("+");
        for(var j = 0; j < board.length; j++) {
            sb.append("---");
        }
        sb.append("+");

        for(var i = 0; i < board.length; i++) {
            sb.append("\n");
            sb.append(i);
            sb.append("  ");
            sb.append("|");
            for(var j = 0; j < board.length; j++) {
                if(board[i][j] == 0) sb.append("   ");
                else if(board[i][j] == 1) sb.append(" b ");
                else sb.append(" w ");
            }
            sb.append("|");
        }
        sb.append("\n");
        sb.append("   ");
        sb.append("+");
        for(var j = 0; j < board.length; j++) {
            sb.append("---");
        }
        sb.append("+");

        System.out.println(sb.toString());
    }

    String legalMovesToString(GameState s) {
        var sb = new StringBuilder();
        for(var a : actions(s)) {
            sb.append("[" + a.toString() + "]");
        }
        return sb.toString();
    }

    void logEvent(String title) {
        var t = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        System.out.println(MessageFormat.format("{0} : {1}", t, title));
    }

    void addToEventLog(WizardAction action, long elapsed) {
        var list = eventTimeLog.get(action);
        if(list == null) list = new ArrayList<>();
        list.add(elapsed);
        eventTimeLog.put(action, list);
    }

    HashMap<WizardAction, ArrayList<Long>> getEventTimeLog() {
        return eventTimeLog;
    }

    HashMap<String, Long> getStatistics() {
        var map = new HashMap<String, Long>();
        map.put("utilityCalls", utilityCalls);
        map.put("terminalTestCalls", terminalTestCalls);
        map.put("actionCalls", actionCalls);
        map.put("recursionCalls", recursionCalls);
        map.put("resultCalls", resultCalls);
        return map;
    }
}
