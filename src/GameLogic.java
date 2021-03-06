
public class GameLogic implements IGameLogic {
    private int cols;
    private int rows;
    private int playerID;

    private int oppID; //opponents ID

    private int[][] board;  // matrix for coins coordinates // values are id's of players
    private int[] currentSize; // the number of coins in each column

    private final int OPEN_SQUARE = 0;
    private final int MAXDEPTH = 3;

    public void initializeGame(int x, int y, int playerID) {

        cols = x;
        rows = y;
        this.playerID = playerID;
        oppID = 3 - playerID;

      //  System.out.println("player id is: " + playerID);
        board = new int[cols][rows];
        currentSize = new int[cols];

      //  System.out.println("Game initialized");

    }


    public void insertCoin(int column, int playerID) {

        board[column][currentSize[column]] = playerID;
        currentSize[column]++;

        //System.out.println("Coin inserted in column " + column+ " by a player "+playerID);
    }

    private void removeCoin(int column) {
        board[column][currentSize[column] - 1] = 0;
        currentSize[column]--;
    }


    public int decideNextMove() {


        return miniMax(board);


    }

    private int miniMax(int[][] board) {
        int alpha = -999999;
        int beta = 999999;

        int result = 0;
        int value;


        Stopwatch timer= new Stopwatch();


        for (int i = 0; i < cols; i++) {
            if (columnIsFull(board, i) != true) {
                insertCoin(i, playerID);
                value = minMove(board, 1, alpha, beta);
                removeCoin(i);
                if (value >= alpha) {

                    alpha = Math.max(alpha, value);
                    result = i;
                }
            }
        }
        System.out.println("time elapsed: "+timer.elapsedTime());
        return result;
    }

    private int maxMove(int[][] board, int depth, int alpha, int beta) {

        //cut-off test
        if (depth >= MAXDEPTH || terminalTest()){
            return evaluate(playerID);
        }


        int value = -999999;
        for (int i = 0; i < cols; i++) {
            if (columnIsFull(board, i) != true) {

                insertCoin(i, playerID);
                value = Math.max(value, minMove(board, depth++, alpha, beta));
                removeCoin(i);

                if (value >= beta) return value;
                alpha = Math.max(alpha, value);


            }
        }
        return value;
    }

    private int minMove(int[][] board, int depth, int alpha, int beta) {


        //cut-off test
        if (depth >= MAXDEPTH || terminalTest()) {
            return evaluate(oppID);
        }
        int value = 999999;

        for (int i = 0; i < cols; i++) {
            if (columnIsFull(board, i) != true) {

                insertCoin(i, oppID);
                value = Math.min(value, maxMove(board, depth++, alpha, beta));
                removeCoin(i);

                //alpha beta cut
                if (value <= alpha) return value;
                beta = Math.min(beta, value);
            }
        }
        return value;
    }
    
    /* Support Methods */

    private boolean columnIsFull(int[][] ar, int col) {
        if (ar[col][ar[col].length - 1] != 0) return true;
        return false;
    }




    public Winner gameFinished() {

        if (hasWon(playerID, board)) return Winner.PLAYER2;
        if (hasWon(oppID, board)) return Winner.PLAYER1;
        if (isTie(board)) return Winner.TIE;


        return Winner.NOT_FINISHED;

    }

    // 100 - win, -100 - lose , 0 - draw , -999 -default| no utility value so far // from AI perspective
//    private int utility(int[][] board) {
//
//        if (hasWon(oppID, board)) return -100;
//        if (hasWon(playerID, board)) return 100;
//        if (isTie(board)) return 0;
//
//        return -999;
//    }

    private boolean terminalTest(){
        if (hasWon(oppID, board)) return true;
        if (hasWon(playerID, board)) return true;
        if (isTie(board)) return true;

        return false;
    }


    private boolean isTie(int[][] gameBoard) {
        boolean tiegame = true;
        for (int col = 0; col < cols; col++) {

            for (int row = 0; row < rows; row++) {

                if (board[col][row] == 0) tiegame = false;
            }
        }
        return tiegame;
    }

    // true is returned if playerID has won. false - if not, IT DOES NOT MEAN AUTOMATICALLY THAT HE LOST. it means that
    // current positions of coins don't make playerID a winner.
    private boolean hasWon(int playerID, int[][] board) {


        int[] h_count = new int[board[0].length];
        // check vertically and horizontal
        for (int i = 0; i < board.length; i++) {
            int v_count = 0;
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == playerID) {
                    h_count[j]++;
                    v_count++;
                } else {
                    v_count = 0;
                    h_count[j] = 0;
                }
                //System.out.println("column: "+i+"v_count: "+v_count);
               // System.out.println("h_count j: "+j+" "+h_count[j]);
                if (v_count >= 4 || h_count[j] >= 4) {
                    //System.out.println("horizontal or vertical win");
                    return true;
                }
            }
        }

        // diagonal upper left to lower right
        for (int col = 0; col < this.cols - 3; col++)
            for (int row = 0; row < this.rows - 3; row++)
                if (board[col][row] != OPEN_SQUARE
                        && board[col][row] == board[col + 1][row + 1]
                        && board[col][row] == board[col + 2][row + 2]
                        && board[col][row] == board[col + 3][row + 3]) {
                    //System.out.println("upper left to lower right win");
                   return true;
                }




        // diagonal lower left to upper right
        for (int row = 3; row<rows; row++)
            for (int col = 0; col < this.cols - 3; col++)
                if (board[col][row] != OPEN_SQUARE
                        && board[col][row] == board[col + 1][row - 1]
                        && board[col][row] == board[col + 2][row - 2]
                        && board[col][row] == board[col + 3][row - 3])
                {     //System.out.println("lower left to right win");
                    //System.out.println("order "+board[col][row]+" "+board[col + 1][row - 1]+ " "+board[col + 2][row - 2]+ " "+board[col + 3][row - 3] );

                    return true;
                }

        return false;
    }


    // EVALUATION based on R. L. Rivest, Game Tree Searching by Min/Max Approximation, AI 34 [1988]
    protected int evaluate(int pid) {

        int row, column;
        int score = 0;

        // For each possible starting spot, calculate the value of the spot for
        // a potential four-in-a-row, heading down, left, and to the lower-left.

        // Value moving down(vertical) from each spot:
        for (row = 3; row < rows; ++row)
            for (column = 0; column < cols; ++column) {
                score += value(row, column, -1, 0);
            }

        // Value moving left(horizontal) from each spot:
        for (row = 0; row < rows; ++row)
            for (column = 3; column < cols; ++column)
                score += value(row, column, 0, -1);

       // Value heading diagonal (lower-left) from each spot:
        for (row = 3; row < rows; ++row)
            for (column = 3; column < cols; ++column)
                score += value(row, column, -1, -1);
        // Value heading diagonal (lower-right) from each spot:
        for (row = 3; row < rows; ++row)
            for (column = 0; column <= cols-4; ++column)
                score += value(row, column, -1, +1);


         if(pid==playerID){
             score+=16;
         }else if(pid==oppID) {
             score-=16;
         }

        return score;
    }


    private int value(int row, int column, int deltar, int deltac) {

        // NOTE: Positive return value is good for the computer.
        int i;
        int endRow = row + 3 * deltar;
        int endColumn = column + 3 * deltac;


        int playerCount = 0;
        int opponentCount = 0;

        if (
                (row < 0) || (column < 0) || (endRow < 0) || (endColumn < 0)
                        ||
                        (row >= rows) || (endRow >= rows)
                        ||
                        (column >= cols) || (endColumn >= cols)
                )
            return 0;

        for (i = 1; i <= 4; ++i) {
            if (board[column][row] == playerID)
            {
                ++playerCount;
                opponentCount = 0;
            } else if(board[column][row] == oppID ){
                ++opponentCount;
                playerCount = 0;
            }

            row += deltar;
            column += deltac;

        }

        //evaluation of scores based on R.L.Rivest
        if(playerCount==0 &&opponentCount!=0){
            switch (opponentCount){

                case 4:
                    return -512;
                case 3:
                    return -50;
                case 2:
                    return -10;
                case 1:
                    return -1;

            }
        }

        if(opponentCount==0 &&playerCount!=0){
            switch (playerCount){

                case 4:
                    return 512;
                case 3:
                    return  50;
                case 2:
                    return 10;
                case 1:
                    return 1;

            }
        }
        return 0;
    }




}




