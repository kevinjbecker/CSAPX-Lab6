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

        // runs the game if we have completed our check and it passes
        runGame(args[0], Integer.parseInt(args[1]));
    }

    /**
     * Runs the actual game and communications between the client and server.
     * @param host The host of the ServerSocket where the ReversiServer is found
     * @param port The port of the ServerSocket where the ReversiServer is found
     */
    private static void runGame(String host, int port)
    {
        // creates a clientGame (used to show the board after each move)
        Reversi clientGame;
        // used for the messages received by the server
        String [] message;
        // continueRunning is used for the loop so that we know whether we need to stop or not
        // this method is preferred because we only stop once the server tells us the game is over (either through
        // ERROR or with the game result
        boolean continueRunning = true;

        // try-with-resources creating a connection, communication pipelines to and from the server, and a userIn
        try(
                Socket conn = new Socket(host, port);
                BufferedReader connIn = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
                PrintWriter connOut = new PrintWriter( conn.getOutputStream(), true );
                Scanner playerIn = new Scanner(System.in)
        )
        {
            // as soon as it connects it should immediately create the game so we can do that here
            message = connIn.readLine().split(" ");

            // constructs the client game using the server's given message
            clientGame = new Reversi(Integer.parseInt(message[1]), Integer.parseInt(message[2]));

            // prints the initial game to the screen
            System.out.println(clientGame);

            while(continueRunning)
            {
                // reads in the server's message and splits it by spaces
                message = connIn.readLine().split(" ");
                // has a switch on the keyword (the first index in the message array)
                switch (message[0])
                {
                    // if the message is MAKE_MOVE, prompt the user to make a move and send it to the server
                    // NO UPDATE IS MADE TO THE CLIENT GAME, WE ONLY MODIFY OUR VERSION WHEN THE SERVER TELLS US TO
                    case MAKE_MOVE:
                        System.out.print("It is your turn to move! Enter row column: ");
                        connOut.println(MOVE + " " + playerIn.nextLine());
                        break;
                    // if the message is MOVE_MADE we tell the user what move was made and then update our game
                    case MOVE_MADE:
                        System.out.println("A move has been made in row: " + message[1] + " column: " + message[2]);
                        clientGame.makeMove(Integer.parseInt(message[1]), Integer.parseInt(message[2]));
                        System.out.println(clientGame);
                        break;
                    // if the message is GAME_WON, tell the user they won and then exit the loop
                    case GAME_WON:
                        System.out.println("The game is over. You won!");
                        continueRunning = false;
                        break;
                    // if the message is GAME_LOST, tell the user they lost and then exit the loop
                    case GAME_LOST:
                        System.out.println("The game is over. You lost!");
                        continueRunning = false;
                        break;
                    // if the message is GAME_TIED, tell the user they tied and then exit the loop
                    case GAME_TIED:
                        System.out.println("The game is over. You tied!");
                        continueRunning = false;
                        break;
                    // if the message is ERROR, alert the user that the server hit an issue an that the game is exiting
                    case ERROR:
                        System.out.println("The server hit an issue. The game will now exit.");
                        continueRunning = false;
                }
            }
        }
        // if we catch an IOException on client side, we do that here
        catch (IOException ioe)
        {
            System.err.println("An error has occurred while attempting to run the game. The client will now halt.");
            System.out.println(ioe.getMessage());
        }
        // if we catch a NumberFormatException (i.e. the server port is bad) we tell the user here.
        catch(NumberFormatException ne)
        {
            System.err.println("Host port must be an integer 0-65535. The client will now halt.");
        }
        // if we catch a ReversiException (somehow), we alert the user and exit the game.
        catch (ReversiException re)
        {
            System.err.println("We should never get here but the server has hit an error. The client will now halt.");
        }
    }
}
