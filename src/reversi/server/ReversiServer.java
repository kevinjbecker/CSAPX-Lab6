package reversi.server;

import reversi.*;

import java.net.ServerSocket;

import java.io.IOException;


/**
 * A server which can run a game of Reversi, a flip-flop game played with two players. ReversiServer controls the
 * interactions between two players (whom are using ReversiClient).
 *
 * @author Kevin Becker
 */
public class ReversiServer implements ReversiProtocol
{
    private static ServerSocket server;

    private static ReversiPlayer reversiPlayer1;

    private static ReversiPlayer reversiPlayer2;


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
        // if we don't have enough arguments, we don't try to run because that would just be bad
        // alerts that there was an issue and exits with code 1
        if(args.length != 3)
        {
            System.out.println("Invalid number of arguments.\nUsage: java ReversiServer #_rows #_cols port");
            System.exit(1);
        }

        // sets numRows to its integer value
        int numRows = Integer.parseInt(args[0]);
        // sets numCols to its integer value
        int numCols = Integer.parseInt(args[1]);
        // sets port to its integer value
        int port = Integer.parseInt(args[2]);
        // makes a null ReversiGame
        ReversiGame game;

        try
        {
            // initializes the server
            initializeReversiServer(numRows, numCols, port);
            // creates a ReversiGame object with the two ReversiPlayers, the number of rows and the number of columns
            game = new ReversiGame(server, reversiPlayer1, reversiPlayer2, numRows, numCols);
            // alerts that the game has finished initializing
            System.out.println("Server initialization has completed. The game will now start.");

            // starts the game
            game.run();

            // once we get here the game has completed
            System.out.println("The game has finished. Server will now terminate.");
        }
        catch (IOException ioe)
        {
            System.err.println("I/O Error - " + ioe.getMessage());
            System.out.println("An issue was encountered with IO. Halting server.");
        }
        catch(ReversiException re)
        {
            // do the actions if we hit a ReversiException
            System.err.println("Reversi Error - " + re.getMessage());
            System.out.println("An error has occurred in Reversi (probably with a requested move). Halting server and clients.");
        }
        finally
        {
            try
            {
                terminateReversiServer();
                System.out.println("Server termination completed.");
            }
            catch(IOException terminateIOE)
            {
                // if we catch an IOException here, we explode
                System.err.println("I/O Error - " + terminateIOE.getMessage());
                System.out.println("An error has occurred while terminating server.");
                terminateIOE.printStackTrace();
            }
        }
    }

    private static void initializeReversiServer(int numRows, int numCols, int port) throws IOException
    {
        // sets the server to a new ServerSocket on port
        server = new ServerSocket(port);

        // waits for player one to connect to server
        System.out.print("Waiting for player 1 to connect... ");
        reversiPlayer1 = new ReversiPlayer(server.accept(), numRows, numCols);
        // we tell reversiPlayer1 it was connected successfully then tell it the number of rows and columns in the game
        System.out.println("successfully connected! (player 1 located at: " + reversiPlayer1.getInetAddress() + ":" + reversiPlayer1.getPort() + ")");

        // waits for player two to connect to server
        System.out.print("Waiting for player 2 to connect... ");
        reversiPlayer2 = new ReversiPlayer(server.accept(), numRows, numCols);
        // we tell reversiPlayer2 it was connected successfully then tell it the number of rows and columns in the game
        System.out.println("successfully connected! (player 2 located at: " + reversiPlayer2.getInetAddress() + ":" + reversiPlayer2.getPort() + ")");
    }

    private static void terminateReversiServer() throws IOException
    {
        if (reversiPlayer1 != null) reversiPlayer1.close();
        if (reversiPlayer2 != null) reversiPlayer2.close();
        if (server != null) server.close();
    }
}