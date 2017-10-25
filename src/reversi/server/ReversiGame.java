package reversi.server;

import reversi.*;

import java.net.ServerSocket;

import java.io.IOException;

public class ReversiGame implements ReversiProtocol
{
    /** the number of moves that have been made so far*/
    private int numMoves = 0;

    /** the server's ServerSocket */
    private ServerSocket server;

    /** the master game that the server bases its running off of. */
    private Reversi serverGame;

    /** the player object for player 1. */
    private ReversiPlayer reversiPlayer1;

    /** the player object for player 2. */
    private ReversiPlayer reversiPlayer2;

    /**
     * Creates a new ReversiGame object. ReversiGame exists to be a more thread friendly approach.
     *
     * @param reversiPlayer1 The ReversiPlayer of player1.
     * @param reversiPlayer2 The ReversiPlayer of player2.
     * @param numRows The number of rows in the Reversi game.
     * @param numCols The number of columns in the Reversi game.
     */
    ReversiGame(ServerSocket server, ReversiPlayer reversiPlayer1, ReversiPlayer reversiPlayer2, int numRows, int numCols)
    {
        this.server = server;

        // sets the serverGame to the game given as an argument
        this.serverGame = new Reversi(numRows, numCols);

        // sets the reversiPlayers
        this.reversiPlayer1 = reversiPlayer1;
        this.reversiPlayer2 = reversiPlayer2;
    }

    /**
     * Runs the game logic. Keeps track of whose turn it is and the moves being made.
     */
    public void run() throws IOException, ReversiException
    {
        // used to hold the client response on each pass of the loop
        String [] message;

        try
        {
            // continues looping until the game is over (as specified by the Reversi class)
            while (!serverGame.gameOver()) {
                // gets the next move from the correct player
                message = getNextMoveFromPlayer();

                // attempts to make the move requested by the client (held in move)
                // if it fails, the method throws a ReversiException to the gameIO method
                serverGame.makeMove(Integer.parseInt(message[1]), Integer.parseInt(message[2]));

                // if there was no exception thrown we can tell each client the move was okay so they can
                // update their copies of the game
                sendMoveMade(MOVE_MADE + " " + message[1] + " " + message[2]);

                // increment numMoves for the next turn
                ++numMoves;
            }

            // sends the results to the client
            sendResults();
        }
        catch (ReversiException re)
        {
            // alert the clients to stop execution
            reversiPlayer1.sendError();
            reversiPlayer2.sendError();
            // throw the error again so that the ReversiServer can output correct stuff
            throw re;
        }
    }

    /**
     * Determines the winner, and alerts each player based upon this result.
     */
    private void sendResults()
    {
        // determines who won (if anyone) and performs accordingly
        switch(serverGame.getWinner())
        {
            case PLAYER_ONE:
                reversiPlayer1.sendResult(GAME_WON);
                reversiPlayer2.sendResult(GAME_LOST);
                break;
            case PLAYER_TWO:
                reversiPlayer2.sendResult(GAME_WON);
                reversiPlayer1.sendResult(GAME_LOST);
                break;
            case NONE:
                reversiPlayer1.sendResult(GAME_TIED);
                reversiPlayer2.sendResult(GAME_TIED);
                break;
        }
    }

    /**
     * Sends each of the clients the move that has just been made.
     *
     * @param moveMade The move that was just made.
     */
    private void sendMoveMade(String moveMade)
    {
        reversiPlayer1.moveMade(moveMade);
        reversiPlayer2.moveMade(moveMade);
    }

    /**
     * Takes the number of moves that has been made and tells the appropriate player that it is their turn.
     *
     * @return An array of Strings that are the tokenized move from the player.
     *
     * @throws IOException If by come chance there was an IOException, then it is thrown by the BufferedReader or PrintWriter.
     */
    private String[] getNextMoveFromPlayer() throws IOException
    {
        // if numMoves is even, it is player one's turn
        if (numMoves % 2 == 0) return reversiPlayer1.makeMove();
            // else it is player two's turn
        else return reversiPlayer2.makeMove();
    }
}
