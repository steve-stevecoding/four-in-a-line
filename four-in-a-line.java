import java.util.Scanner;
import java.util.HashMap;

public class FourInALine
{
    static final int BOARD_SIZE = 8;
    static final char BLANK = '-';
    static final char HUMAN = 'O';
    static final char COMPUTER = 'X';
    static final int MOVES_TO_WIN = 4;
    static final int NEGATIVE_INFINITY = -99999999;
    static final int POSITIVE_INFINITY = 99999999;

    //Represents the score rewarded in terminal states for when the computer
    //or human wins.
    static final int COMPUTER_WINNER = 100;
    static final int HUMAN_WINNER = COMPUTER_WINNER * -1;

    //Represents the time at which the timer started.
    static long startTime;
    //Represents when the ABP algorithm should stop (timesUp() method).
    static int timeLimit;
    //Represents current board.
    static char[][] state;
    //Holds all the spots in the board that have been selected by the human
    //and computer already.
    static HashMap selectedSpaces = new HashMap();

    public static void main(String[] args)
    {
        Scanner kb = new Scanner(System.in);
        state = initialize();
        String input = "";
        while(!input.equals("y") && !input.equals("n")) {
            System.out.print("Would you like to go first? (y/n): ");
            input = kb.nextLine().toLowerCase();
        }
        boolean computerFirst = input.equals("n");
        timeLimit = 30001;
        while(timeLimit > 30000 || timeLimit < 1) {
            System.out.print("How long should the computer think about its moves " +
                             "(in seconds, max = 30 seconds)? : ");
            timeLimit = kb.nextInt()*1000;
            if(timeLimit > 30000 || timeLimit < 1)
                System.out.println("Invalid response.");
        }
        if(computerFirst)
            computerMove();
        while(checkForWinner() == 0) {
            humanMove();
            if(checkForWinner() != 0)
                break;
            computerMove();
        }
        printState();
        int winner = checkForWinner();
        if(winner == 1)
            System.out.println("Draw!");
        else if(winner == HUMAN_WINNER)
            System.out.println("You win!");
        else if(winner == COMPUTER_WINNER)
            System.out.println("The computer wins!");
    }

    /**
     * This method represents the human's move (input).
     */
    public static void humanMove() {
        Scanner kb = new Scanner(System.in);
        printState();
        boolean loop = true;
        while(loop) {
            System.out.print("Choose your next move: ");
            String input = kb.nextLine();
            System.out.println();
            int[] choice = formattedRowAndColumnToIntegerArray(input);
            if(choice == null)
                System.out.println("Not a legal move!");
            else {
                state[choice[0]][choice[1]] = HUMAN;
                selectedSpaces.put(input, input);
                loop = false;
            }
        }
    }

    /**
     * This method represents the computer's move (uses Alpha-Beta Pruning
     * search).
     */
    public static void computerMove() {
        printState();
        alphaBetaSearch();
    }

    /**
     * This method performs the Alpha-Beta Pruning algorithm to make the next
     * move for the computer. It starts by considering all possible successors,
     * and tries to find the one with the highest score (higher = better for
     * computer, lower = better for human). That successor becomes the new
     * current state, which represents the computer's move. The method also
     * implements a timer. The ABP algorithm starts with a low depth (1), and
     * increases until the timer runs out while in the middle of performing
     * the ABP algorithm at some depth n. The best successor found at depth
     * (n-1) is then chosen as the computer's next move.
     */
    public static void alphaBetaSearch() {
        int depthIncrementor = 1, best, bestRow = 0,
            bestColumn = 0, score, oldBestRow = 0, oldBestColumn=0;
        startTime = System.currentTimeMillis();
        while(!timesUp()) {
            best = NEGATIVE_INFINITY;
            for(int i = 0; i < BOARD_SIZE; i++) {
                for(int j = 0; j < BOARD_SIZE; j++) {
                    if(state[i][j] == BLANK) {
                        state[i][j] = COMPUTER;
                        score = (!timesUp() ? minValue(NEGATIVE_INFINITY,
                                                       POSITIVE_INFINITY,
                                                       depthIncrementor-1) : 0);
                        if(!timesUp()) {
                            if(score > best) {
                                best = score;
                                bestRow = i;
                                bestColumn = j;
                            }
                        }
                        state[i][j] = BLANK;
                    }
                }
            }
            if(!timesUp()) {
                oldBestRow = bestRow;
                oldBestColumn = bestColumn;
            }
            depthIncrementor++;
        }
        int[] rowAndColumn = new int[2];
        rowAndColumn[0] = oldBestRow;
        rowAndColumn[1] = oldBestColumn;
        System.out.println("The computer's move is: " +
                           rowAndColumnToFormattedVersion(rowAndColumn));
        state[oldBestRow][oldBestColumn] = COMPUTER;
        selectedSpaces.put(rowAndColumnToFormattedVersion(rowAndColumn),
                           rowAndColumnToFormattedVersion(rowAndColumn));
    }

     /**
     * This method is part of the Alpha-Beta Pruning algorithm. The method
     * considers all the possible moves the other player will make (it choosing
     * the lowest values possible) and returns the highest score. It's able to
     * use the alpha and beta values from the higher minValue method to prune
     * the tree, eliminating possible branches to make the search algorithm
     * faster.
     * @param alpha The alpha value from the higher minValue method.
     * @param beta The beta value from the higher minValue method.
     * @param depth The current depth (if the depth is 0, the evaluation
     *              function or terminal score is used to find the score.
     * @return The highest score of all successors.
     */
    public static int maxValue(int alpha, int beta, int depth) {
        int best = NEGATIVE_INFINITY, score;
        int winnerStatus = checkForWinner();
        if(winnerStatus != 0)
            return winnerStatus;
        if(depth == 0)
            return evaluationFunction();
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                if(timesUp())
                    return best;
                if(state[i][j] == BLANK) {
                    state[i][j] = COMPUTER;
                    score = minValue(alpha, beta, depth-1);
                    if(score > best)
                        best = score;
                    state[i][j] = BLANK;
                    if (best >= beta)
                        return best;
                    if (best > alpha)
                        alpha = best;
                }
            }
        }
        return best;
    }
    /**
     * This method is part of the Alpha-Beta Pruning algorithm. The method
     * considers all the possible moves the other player will make (it choosing
     * the highest values possible) and returns the lowest score. It's able to
     * use the alpha and beta values from the higher maxValue method to prune
     * the tree, eliminating possible branches to make the search algorithm
     * faster.
     * @param alpha The alpha value from the higher maxValue method.
     * @param beta The beta value from the higher maxValue method.
     * @param depth The current depth (if the depth is 0, the evaluation
     *              function or terminal score is used to find the score.
     * @return The lowest score of all successors.
     */

    public static int minValue(int alpha, int beta, int depth) {
        int best = POSITIVE_INFINITY, score;
        int winnerStatus = checkForWinner();
        if(winnerStatus != 0)
            return winnerStatus;
        if(depth == 0)
            return evaluationFunction();
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                if(timesUp())
                    return best;
                if(state[i][j] == BLANK) {
                    state[i][j] = HUMAN;
                    score = maxValue(alpha, beta, depth-1);
                    if(score < best)
                        best = score;
                    state[i][j] = BLANK;
                    if (best <= alpha)
                        return best;
                    if (best < beta)
                        beta = best;
                }
            }
        }
        return best;
    }

    /**
     * This method returns a score given the current state of the board. This
     * is needed for the Alpha-Beta Pruning algorithm when the depth is at its
     * deepest but the state encountered is not a terminal state (or a state
     * where there is a winner).
     *
     * The algorithm looks at each row and column of the board, and determines
     * if the human or computer can win that row or column. If the computer can
     * win, a counter (rowAndColumnCounter) is incremented by one. If the human
     * can win, that same counter is decremented by one (resulting in a positive
     * value if the board provides more winning opportunities for the computer,
     * a negative value if the board provides more winning opportunities for the
     * human, and 0 if it is equally matched). Along with this, each row and
     * column is also checked for adjacent moves (where there is a HUMAN char
     * and COMPUTER char next to each other, representing a blocked move). This
     * encourages the computer to block moves as often as possible.
     *
     * After this, the score returned is the sum of rowAndColumnCounter and
     * the total number of adjacent chars (adjacentBonus).
     * @return The evaluation score
     */
    public static int evaluationFunction() {
        int rowAndColumnCounter = 0;
        int adjacentBonus = 0;
        for(int i = 0; i < BOARD_SIZE; i++) {
            char[] charsInRowOrColumn = new char[BOARD_SIZE];
            for(int j = 0; j < BOARD_SIZE; j++) {
                charsInRowOrColumn[j] = state[i][j];
                if(j < (BOARD_SIZE-1)) {
                    if((state[i][j] == HUMAN && state[i][j+1] == COMPUTER) ||
                       (state[i][j] == COMPUTER && state[i][j+1] == HUMAN))
                        adjacentBonus++;
                }
            }
            String fullRowOrColumn = new String(charsInRowOrColumn);
            if(!(fullRowOrColumn.indexOf(COMPUTER) != -1 &&
                 fullRowOrColumn.indexOf(HUMAN) != -1 )) {
                if(canWinRow(COMPUTER, charsInRowOrColumn.clone()))
                    rowAndColumnCounter++;
                if(canWinRow(HUMAN, charsInRowOrColumn.clone()))
                    rowAndColumnCounter--;
            }
            for(int j = 0; j < BOARD_SIZE; j++) {
                charsInRowOrColumn[j] = state[j][i];
                if(j < (BOARD_SIZE-1)) {
                    if((state[j][i] == HUMAN && state[j+1][i] == COMPUTER) ||
                       (state[j][i] == COMPUTER && state[j+1][i] == HUMAN))
                        adjacentBonus++;
                }
            }
            fullRowOrColumn = new String(charsInRowOrColumn);
            if(!(fullRowOrColumn.indexOf(COMPUTER) != -1 &&
                 fullRowOrColumn.indexOf(HUMAN) != -1 )) {
                if(canWinRow(COMPUTER, charsInRowOrColumn.clone()))
                    rowAndColumnCounter++;
                if(canWinRow(HUMAN, charsInRowOrColumn.clone()))
                    rowAndColumnCounter--;
            }
        }
        return rowAndColumnCounter + adjacentBonus;
    }

    /**
     * Given a row (or column) represented in a char array and the current
     * player's char (HUMAN or COMPUTER), the method returns true if the current
     * player has the ability to win in that row or column, and false otherwise.
     * @param player The current player's char
     * @param row The row or column represented in a char array
     * @return True if the current player has the ability to win in the row or
     *         column, false otherwise.
     */
    public static boolean canWinRow(char player, char[] row) {
        for(int i = 0; i < row.length; i++) {
            if(row[i] == '-')
                row[i] = player;
        }
        String result = new String(row);
        char[] winningCharArray = new char[4];
        for(int i = 0; i < winningCharArray.length; i++)
            winningCharArray[i] = player;
        String winningRow = new String(winningCharArray);
        return result.contains(winningRow);
    }

    /**
     * This method checks if there is a winner or not. If the computer wins,
     * the value COMPUTER_WINNER is returned. If the human wins, the value
     * HUMAN_WINNER is returned. If there is a draw, 1 is returned. If the
     * game is still continuing, 0 is returned.
     * @return COMPUTER_WINNER = computer won, HUMAN_WINNER = human won,
     *         1 = draw, 0 = no winner yet/game is still in play.
     */
    public static int checkForWinner() {
        int humanCounter = 0, computerCounter = 0, blankCounter = 0;
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                if(state[i][j] == COMPUTER) {
                    humanCounter = 0;
                    computerCounter++;
                }
                else if(state[i][j] == HUMAN) {
                    humanCounter++;
                    computerCounter = 0;
                }
                else if(state[i][j] == BLANK) {
                    blankCounter++;
                    humanCounter = 0;
                    computerCounter = 0;
                }
                if(computerCounter == MOVES_TO_WIN)
                    return COMPUTER_WINNER;
                if(humanCounter == MOVES_TO_WIN)
                    return HUMAN_WINNER;
            }
            computerCounter = 0;
            humanCounter = 0;
        }

        for(int j = 0; j < BOARD_SIZE; j++) {
            for(int i = 0; i < BOARD_SIZE; i++) {
                if(state[i][j] == COMPUTER) {
                    humanCounter = 0;
                    computerCounter++;
                }
                else if(state[i][j] == HUMAN) {
                    humanCounter++;
                    computerCounter = 0;
                }
                else if(state[i][j] == BLANK) {
                    humanCounter = 0;
                    computerCounter = 0;
                }
                if(computerCounter == MOVES_TO_WIN)
                    return COMPUTER_WINNER;
                if(humanCounter == MOVES_TO_WIN)
                    return HUMAN_WINNER;
            }
            computerCounter = 0;
            humanCounter = 0;
        }
        return (blankCounter == 0 ? 1 : 0);
    }

    /**
     * This method acts as a timer. Given a time limit by the user (timeLimit)
     * and the initial time that the timer started (startTime), the method
     * returns true if the time has passed the limit, otherwise, it returns
     * false.
     * @return True if the timer limit has passed, false otherwise.
     */
    public static boolean timesUp() {
        return (System.currentTimeMillis() - startTime) >= timeLimit;
    }

    /**
     * This method creates the initial board, by making a new double char array
     * of BOARD_SIZE x BOARD_SIZE, and making each element equal to BLANK.
     * @return The initialized double char array
     */
    public static char[][] initialize() {
        char[][] result = new char[BOARD_SIZE][BOARD_SIZE];
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++)
                result[i][j] = BLANK;
        }
        return result;
    }

    /**
     * This method prints the current state, along with the row letters and
     * column numbers.
     */
    public static void printState() {
        System.out.print("  ");
        for(int i = 0; i < BOARD_SIZE; i++)
            System.out.print((i+1) + (i == (BOARD_SIZE-1) ? "\n" : " "));
        for(int i = 0; i < BOARD_SIZE; i++) {
            System.out.print(((char) (i+65)) + " ");
            for(int j = 0; j < BOARD_SIZE; j++)
                System.out.print(state[i][j] + " ");
            System.out.println();
        }
    }

    /**
     * This method takes an integer array of two integers, the first being the
     * row number and the second being the column number, and returns a String
     * that represents a space on the board ([0,0] -> "a1").
     * @param rowAndColumn The row and column input
     * @return The String that represents the row and column.
     */
    public static String rowAndColumnToFormattedVersion(int[] rowAndColumn) {
        char[] charArrayResult = new char[2];
        charArrayResult[0] = (char) (rowAndColumn[0] + 97);
        charArrayResult[1] = (char) (rowAndColumn[1] + 49);
        return new String(charArrayResult);
    }

    /**
     * This method takes a String that represents a space on the board, and
     * returns an integer array of two integers, the first being the row number
     * and the second being the column number ("a1" -> [0,0]).
     * @param input The String input
     * @return The row and column the String represents
     */
    public static int[] formattedRowAndColumnToIntegerArray(String input) {
        int[] result = new int[2];
        if(input.length() != 2)
            return null;
        if((input.charAt(0) < 'a') || (input.charAt(0) > ('a' +
                                                          (BOARD_SIZE-1))))
            return null;
        if((input.charAt(1) < '1') || (input.charAt(1) >
                                       Character.forDigit(BOARD_SIZE, 10)))
            return null;
        result[0] = (int) input.charAt(0) - 97;
        result[1] = (int) input.charAt(1) - 49;
        if(selectedSpaces.containsKey(input))
            return null;
        return result;
    }
}
