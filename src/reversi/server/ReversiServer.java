package reversi.server;

import reversi.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;


/**
 * A server which can run a game of Reversi, a flip-flop game played with two players. ReversiServer controls the
 * interactions between two players (whom are using ReversiClient).
 *
 * @author Kevin Becker
 */
public class ReversiServer implements ReversiProtocol
{
    /** the server's ServerSocket. */
    private static ServerSocket server = null;

    /** the client Socket for player one. */
    private static Socket player1 = null;
    /** the client Socket for player two. */
    private static Socket player2 = null;

    /** the master game that the server bases its running off of. */
    private static Reversi serverGame;

    /** the BufferedReader for player one. */
    private static BufferedReader player1In;
    /** the PrintWriter for player one. */
    private static PrintWriter player1Out;

    /** the BufferedReader for player two. */
    private static BufferedReader player2In;
    /** the PrintWriter for player two. */
    private static PrintWriter player2Out;

    /**
     * The main method that checks correct
     * @param args The arguments that are used for the creation of a server and Reversi board.
     *             The arguments should have the following:<br><br><em>
     *             0 => The number of rows the Reversi board should have.<br>
     *             1 => The number of columns the Reversi board should have.<br>
     *             2 => The port to which the server should be created.</em>
     */
    public static void main(String [] args)
    {
        int numRows, numCols, port;

        // if we don't have enough arguments, we don't try to run because that would just be bad
        // alerts that there was an issue and exits with code 1
        if(args.length != 3)
        {
            System.out.println("Invalid number of arguments.\nUsage: java ReversiServer #_rows #_cols port");
            System.exit(1);
        }

        // sets numRows to its integer value
        numRows = Integer.parseInt(args[0]);
        // sets numCols to its integer value
        numCols = Integer.parseInt(args[1]);
        // sets port to its integer value
        port = Integer.parseInt(args[2]);

        try
        {
            // initializes game server
            initializeGameServer(numRows, numCols, port);
            System.out.println("Server initialization completed. The game will now start.");
            // once we've initialized, we can now run the game
            runGame();
            System.out.println("The game has finished, server will now terminate.");
        }
        catch (IOException ioe)
        {
            System.out.println("An issue was encountered with IO. Halting server.");
            System.err.println(ioe.getMessage());
        }
        catch(ReversiException re)
        {
            // do the actions if we hit a ReversiException
            System.out.println("An error has occurred in Reversi (possibly with a move requested). Halting server and clients.");
            // alert the clients to stop execution
            player1Out.println(ERROR);
            player2Out.println(ERROR);
        }
        finally
        {
            try
            {
                // once done we terminate the game server
                terminateGameServer();
            }
            catch(IOException terminateIOE)
            {
                // if we catch an IOException here, we explode
                System.out.println("An error occurred while terminating server.");
                System.err.println(terminateIOE.getMessage());
                terminateIOE.printStackTrace();
            }
        }
    }

    /**
     * Builds the server and then accepts two clients.
     *
     * @param numRows The number of rows in the Reversi game.
     * @param numCols The number of columns in the Reversi game.
     * @param port The port the server should be located on.
     *
     * @throws IOException When the PrintWriter or BufferedReaders encounter an issue.
     */
    private static void initializeGameServer(int numRows, int numCols, int port) throws IOException
    {
        // creates the master game that the server uses
        serverGame = new Reversi(numRows, numCols);

        // initializes the servers on the port
        server = new ServerSocket(port);

        // waits for player one to connect to server
        System.out.print("Waiting for player 1 to connect... ");
        player1 = server.accept();
        player1In = new BufferedReader(new InputStreamReader(player1.getInputStream()));
        player1Out = new PrintWriter(player1.getOutputStream(), true);
        // we tell player1 it connected successfully, then tell it the number of rows and columns in the game
        player1Out.println(CONNECT + " " + numRows + " " + numCols);
        System.out.println("connected! (player 1 located at: " + player1.getInetAddress() + ":" + player1.getPort() + ")");

        //waits for player two to connect to server2
        System.out.print("Waiting for player 2 to connect... ");
        player2 = server.accept();
        player2In = new BufferedReader(new InputStreamReader(player2.getInputStream()));
        player2Out = new PrintWriter(player2.getOutputStream(), true);
        // we tell player2 it was connected successfully then tell it the number of rows and columns in the game
        player2Out.println(CONNECT + " " + numRows + " " + numCols);
        System.out.println("connected! (player 2 located at: " + player2.getInetAddress() + ":" + player2.getPort() + ")");
    }

    /**
     * Once it is time to deconstruct all of the items we needed to run the server, we do that here.
     *
     * @throws IOException If something goes awry with the closing of the readers, writers, or sockets.
     */
    private static void terminateGameServer() throws IOException
    {
        // closes all of the items
        if (player1Out != null) player1Out.close();
        if (player1In != null) player1In.close();
        if (player2Out != null) player2Out.close();
        if (player2In != null) player2In.close();
        if (player1 != null) player1.close();
        if (player2 != null) player2.close();
        if (server != null) server.close();
    }

    /**
     * This method runs the game logic. Keeps track of whose turn it is and the moves being made.
     *
     * @throws ReversiException An exception thrown if there was an issue in the ReversiGame. Usually occurs when a bad
     * move has been requested (a space taken already occupied).
     * @throws IOException An exception thrown if there was an issue communicating with Sockets (in both directions).
     */
    private static void runGame() throws ReversiException, IOException
    {
        // used to tell whose move it is
        int numMoves = 0;
        // used to hold the client response on each pass of the loop
        String [] message;

        // continues looping until the game is over (as specified by the Reversi class)
        while (!serverGame.gameOver())
        {
            // gets the next move from the correct player
            message = getNextMoveFromPlayer(numMoves);

            // attempts to make the move requested by the client (held in move)
            // if it fails, the method throws a ReversiException to the gameIO method
            serverGame.makeMove(Integer.parseInt(message[1]), Integer.parseInt(message[2]));

            // if there was no exception thrown we can tell each client the move was okay so they can
            // update their copies of the game
            sendMoveMade(MOVE_MADE + " " + message[1] + " " + message[2]);

            // increment numMoves for the next turn
            ++numMoves;
        }
        sendResults();
    }

    /**
     * Determines the winner, and alerts each player based upon this result.
     */
    private static void sendResults()
    {
        // determines who won (if anyone) and performs accordingly
        switch(serverGame.getWinner())
        {
            case PLAYER_ONE:
                player1Out.println(GAME_WON);
                player2Out.println(GAME_LOST);
                break;
            case PLAYER_TWO:
                player2Out.println(GAME_WON);
                player1Out.println(GAME_LOST);
                break;
            case NONE:
                player1Out.println(GAME_TIED);
                player2Out.println(GAME_TIED);
                break;
        }
    }

    /**
     * Sends each of the clients the move that has just been made.
     *
     * @param moveMade The move that was just made.
     */
    private static void sendMoveMade(String moveMade)
    {
        player1Out.println(moveMade);
        player2Out.println(moveMade);
    }

    /**
     * Takes the number of moves that has been made and tells the appropriate player that it is their turn.
     *
     * @param numMoves The number of moves that have been made thus far in the game.
     *
     * @return An array of Strings that are the tokenized move from the player.
     *
     * @throws IOException If by come chance there was an IOException, then it is thrown by the BufferedReader or PrintWriter.
     */
    private static String[] getNextMoveFromPlayer(int numMoves) throws IOException
    {
        // if numMoves is even, it is player one's turn
        if (numMoves % 2 == 0)
        {
            // tells player1 it's their turn
            player1Out.println(MAKE_MOVE);
            // reads their response and splits it by spaces
            return player1In.readLine().split(" ");
        }
        // otherwise it is player two's turn
        // tells player2 it's their turn
        player2Out.println(MAKE_MOVE);
        // reads their response and splits it by spaces
        return player2In.readLine().split(" ");
    }
}
