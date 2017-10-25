package reversi.client;

import reversi.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.Socket;

import java.util.Scanner;


/**
 * The ReversiClient is used by an end-user and allows them to play the game of Reversi with another player.
 *
 * @author Kevin Becker
 */
public class ReversiClient implements ReversiProtocol
{
    /** The user input scanner (used for when we're making a move. */
    private static Scanner userIn;

    /** The client's dummy game that is just used for output. */
    private static Reversi clientGame;

    /** The connection socket that connects the client and the server. */
    private static Socket conn;

    /** The BufferedReader used for communication from the server. */
    private static BufferedReader connIn;

    /** The PrintWriter used for communication to the server. */
    private static PrintWriter connOut;


    /**
     * Begins execution of the Reversi game between a server and a client.
     * @param args The arguments that are used for the creation of a connection.
     *             The arguments should have the following:<br><br><em>
     *             0 => The location where the host can be found.<br>
     *             1 => The port where the host can be found.</em>
     */
    public static void main(String [] args)
    {
        // if the number of arguments is not strictly 2, we exit because it could cause an issue.
        if(args.length != 2)
        {
            System.out.println("Invalid number of arguments.\nUsage: java ReversiClient host port");
            System.exit(1);
        }

        // this is used when its time to read in for a move
        userIn = new Scanner(System.in);

        try
        {
            // tries to initialize the client and then runs the game.
            initializeClient(args[0], Integer.parseInt(args[1]));
            run();
        }
        // if we catch an IOException alert the user and get the message
        catch(IOException ioe)
        {
            System.err.println("I/O Error - " + ioe.getMessage());
            System.out.println("An error has occurred while attempting to run the game. The client will now terminate.");
        }
        // this is only called if the port isn't in the correct range.
        catch(NumberFormatException ne)
        {
            System.err.println("Number Format Error - " + ne.getMessage());
            System.out.println("Host port must be an integer 0-65535. Client will now terminate.");
        }
        // after any catch or the end of the try we enter the finally
        finally
        {
            try
            {
                // try to terminate the client items
                terminateClient();
                System.out.println("The client has successfully terminated.");
            }
            // if we somehow catch an IOException, we tell the user it happened (very unlikely this will occur)
            catch(IOException terminateIOE)
            {
                System.err.println("I/O Error - " + terminateIOE.getMessage());
                System.out.println("An error has occurred while trying to terminate the client.");
            }
        }
    }

    /**
     * Initializes the client so that it is ready.
     *
     * @param host The host of the ServerSocket where the ReversiServer is found.
     * @param port The port of the ServerSocket where the ReversiServer is found.
     *
     * @throws IOException If there is an issue initializing the BufferedReader or PrintWriter.
     * @throws NumberFormatException If the port does not fit in the proper range (0-65535).
     */
    private static void initializeClient(String host, int port) throws IOException, NumberFormatException
    {
        // creates a new Socket connecting to host: host on port: port
        conn = new Socket(host, port);
        // creates a new BufferedReader reading in from the server
        connIn = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
        // creates a new PrintWriter sending to the server
        connOut = new PrintWriter( conn.getOutputStream(), true );
        // as soon as it connects it should immediately create the game so we can do that here
        String [] connectMessage = connIn.readLine().split(" ");
        // creates the client Reversi game
        clientGame = new Reversi(Integer.parseInt(connectMessage[1]), Integer.parseInt(connectMessage[2]));
    }

    /**
     * Terminates the client at the end of execution.
     *
     * @throws IOException If there is an issue closing any of the IO-based items.
     */
    private static void terminateClient() throws IOException
    {
        // closes all of the items
        if (userIn != null) userIn.close();
        if (connIn != null) connIn.close();
        if (connOut != null) connOut.close();
        if (conn != null) conn.close();
    }

    /**
     * Runs the actual game and communications between the client and server.
     */
    private static void run()
    {
        // used for the messages received by the server
        String [] message;
        // continueRunning is used for the loop so that we know whether we need to stop or not
        // this method is preferred because we only stop once the server tells us the game is over (either through
        // ERROR or with the game result)
        boolean continueRunning = true;

        try
        {
            // prints the initial game to the screen
            System.out.println(clientGame);

            while (continueRunning)
            {
                // reads in the server's message and splits it by spaces
                message = connIn.readLine().split(" ");
                // has a switch on the keyword (the first index in the message array)
                switch(message[0])
                {
                    // if the message is MAKE_MOVE, we call the makeMove method to perform further actions
                    // NO UPDATE IS MADE TO THE CLIENT GAME, WE ONLY MODIFY OUR VERSION WHEN THE SERVER TELLS US TO
                    case MAKE_MOVE:
                        makeMove();
                        break;
                    // if the message is MOVE_MADE we call the moveMade method with the received message
                    case MOVE_MADE:
                        moveMade(message);
                        break;
                    // if it isn't one of the upper two cases, we've hit an ending-case
                    // which we do further inspection with a new, nested switch
                    default:
                        endAction(message);
                        continueRunning = false;
                        break;
                }
            }
        }
        catch(IOException ioe)
        {
            System.err.println("I/O Error - " + ioe.getMessage());
            System.out.println("An error has occurred while attempting to run the game. The client will now terminate.");
        }
        // if we catch a ReversiException (somehow), we alert the user and exit the game.
        catch (ReversiException re)
        {
            System.err.println("Reversi Error - " + re.getMessage());
            System.out.println("We should never get here but the server has hit an error. The client will now terminate.");
        }
    }

    /**
     * Prompts the user to make a move, and sends that to the server.
     */
    private static void makeMove()
    {
        System.out.print("It is your turn to move! Enter row column: ");
        connOut.println(MOVE + " " +userIn.nextLine());
    }

    /**
     * Determines the move that was made, tells the user and updates the game that the client has.
     *
     * @param message The message that was received from the server.
     */
    private static void moveMade(String [] message) throws ReversiException
    {
        // make the move (throw the error if somehow it happens)
        clientGame.makeMove(Integer.parseInt(message[1]), Integer.parseInt(message[2]));
        // alert the client to the new move
        System.out.println("A move has been made in row: " + message[1] + " column: " + message[2]);
        // show the game so the user knows what's going on
        System.out.println(clientGame);
    }

    /**
     * This determines which end action arrived at and alerts the user to it.
     *
     * @param message The message that was received from the server.
     */
    private static void endAction(String [] message)
    {
        switch(message[0])
        {
            // if the message is GAME_WON, tell the user they won
            case GAME_WON:
                System.out.println("The game is over. You won!");
                break;
            // if the message is GAME_LOST, tell the user they lost
            case GAME_LOST:
                System.out.println("The game is over. You lost!");
                break;
            // if the message is GAME_TIED, tell the user they tied
            case GAME_TIED:
                System.out.println("The game is over. You tied!");
                break;
            // if the message is ERROR, tell the user about it
            case ERROR:
                System.out.println("The server hit an issue. The client will now terminate.");
                break;
        }
    }
}
