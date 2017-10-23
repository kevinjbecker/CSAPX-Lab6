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

        // sends control to the gameIO method to construct the server and get all of the players set up
        gameIO(numRows, numCols, port);
    }

    /**
     * Builds the server and then accepts two clients.
     * @param numRows The number of rows in the Reversi game.
     * @param numCols The number of columns in the Reversi game.
     * @param port The port the server should be located on.
     */
    private static void gameIO(int numRows, int numCols, int port)
    {
        // creates a server version of the
        Reversi serverGame = new Reversi(numRows, numCols);

        // creates a null ServerSocket used for the server
        ServerSocket server = null;

        // creates null Sockets used for the two clients
        Socket player1 = null;
        Socket player2 = null;

        // creates BufferedReaders for each of the players
        BufferedReader player1In;
        BufferedReader player2In;

        // creates a PrintWriter for each of the players
        PrintWriter player1Out = null;
        PrintWriter player2Out = null;

        try
        {
            // initializes the servers on the port
            server = new ServerSocket(port);

            // waits for player one to connect to server
            System.out.print("Waiting for player 1 to connect... ");
            player1 = server.accept();
            // creates a BufferedReader and PrintWriter for the player1 communication
            player1In = new BufferedReader(new InputStreamReader(player1.getInputStream()));
            player1Out = new PrintWriter(player1.getOutputStream(), true);

            // we tell player1 it connected successfully, then tell it the number of rows and columns in the game
            player1Out.println(CONNECT + " " + numRows + " " + numCols);

            // once player1 is fully connected we output this
            System.out.println("connected!");

            // waits for player two to connect to server2
            System.out.print("Waiting for player 2 to connect... ");
            player2 = server.accept();
            // once player2 is fully connected we output this
            player2In = new BufferedReader(new InputStreamReader(player2.getInputStream()));
            player2Out = new PrintWriter(player2.getOutputStream(), true);

            // We tell player2 it was connected successfully then tell it the number of rows and columns in the game
            player2Out.println(CONNECT + " " + numRows + " " + numCols);

            // once player2 is fully connected we output this
            System.out.println("connected!\nGame is starting!");

            // runs the game using the created items
            runGame(serverGame, player1In, player1Out, player2In, player2Out);
        }
        catch (IOException gameRunIOE)
        {
            // look into this
            System.out.println("An error has occurred in communication. Halting server.");
            System.out.println(gameRunIOE.getMessage());
            gameRunIOE.printStackTrace();
        }
        catch(ReversiException re)
        {
            // do the actions if we hit a ReversiException
            System.out.println("An error has occurred in Reversi (possibly with a move requested). Halting server and clients.");
            player1Out.println(ERROR);
            player2Out.println(ERROR);
        }
        finally
        {
            // after the try, no matter what, we try to close the connections and the server socket if they're not already so
            try
            {
                // closing server1 if it's not already closed
                if (server != null) server.close();

                // closing conn1 if it's not already closed
                if (player1 != null) player1.close();

                // closing conn2 if it's not already closed
                if (player2 != null) player2.close();
            }
            catch (IOException socketCloseIOE)
            {
                // if we catch an IOException here, we explode
                System.out.println("An error occurred while closing sockets. Halting server.");
                System.out.println(socketCloseIOE.getMessage());
                socketCloseIOE.printStackTrace();
            }
        }
    }

    /**
     * This method runs the game logic. Keeps track of whose turn it is and the moves being made.
     * @param serverGame The server's version of the ReversiGame that is considered the "master"
     * @param player1In Player one's BufferedReader pipeline (allows player1 to send to the server)
     * @param player1Out Player one's PrintWriter pipeline (allows the server to send to player1)
     * @param player2In Player two's BufferedReader pipeline (allows player1 to send to the server)
     * @param player2Out Player two's PrintWriter pipeline (allows the server to send to player1)
     * @throws ReversiException An exception thrown if there was an issue in the ReversiGame. Usually occurs when a bad
     * move has been requested (a space taken already occupied)
     * @throws IOException An exception thrown if there was an issue communicating with Sockets (in both directions)
     */
    private static void runGame(Reversi serverGame, BufferedReader player1In, PrintWriter player1Out,
                                BufferedReader player2In, PrintWriter player2Out) throws ReversiException, IOException
    {
        // used to tell whose move it is
        int numMoves = 0;
        // used to hold the client response on each pass of the loop
        String [] message;

        // continues looping until the game is over (as specified by the Reversi class)
        while (!serverGame.gameOver())
        {
            // if numMoves is even, it is player one's turn
            if (numMoves % 2 == 0)
            {
                // tells player1 it's their turn
                player1Out.println(MAKE_MOVE);
                // reads their response and splits it by spaces
                message = player1In.readLine().split(" ");
            }
            // else it is player two's turn
            else
            {
                // tells player2 it's their turn
                player2Out.println(MAKE_MOVE);
                // reads their response and splits it by spaces
                message = player2In.readLine().split(" ");
            }

            // attempts to make the move requested by the client (held in move)
            // if it fails, the method throws a ReversiException to the gameIO method
            serverGame.makeMove(Integer.parseInt(message[1]), Integer.parseInt(message[2]));

            // if there was no exception thrown we can tell each client the move was okay so they can
            // update their copies of the game
            player1Out.println(MOVE_MADE + " " + message[1] + " " + message[2]);
            player2Out.println(MOVE_MADE + " " + message[1] + " " + message[2]);

            // increment numMoves for the next turn
            ++numMoves;
        }

        // once we exit the loop we get the winner
        Reversi.Move winner = serverGame.getWinner();

        // if the winner is player one, do the associated actions
        if(winner.equals(Reversi.Move.PLAYER_ONE))
        {
            player1Out.println(GAME_WON);
            player2Out.println(GAME_LOST);
        }
        // else if the winner is player two, do the associated actions
        else if (winner.equals(Reversi.Move.PLAYER_TWO))
        {
            player2Out.println(GAME_WON);
            player1Out.println(GAME_LOST);
        }
        // otherwise it was a tie
        else
        {
            player1Out.println(GAME_TIED);
            player2Out.println(GAME_TIED);
        }
    }
}
