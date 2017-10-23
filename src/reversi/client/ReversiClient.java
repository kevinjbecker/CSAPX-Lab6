package reversi.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import reversi.*;
import java.net.Socket;
import java.util.Scanner;

public class ReversiClient implements ReversiProtocol
{
    public static void main(String [] args)
    {
        Reversi clientGame;
        String [] serverMessage;

        if(args.length != 2)
        {
            System.out.println("Invalid number of arguments.\nUsage: java ReversiClient host port");
            System.exit(1);
        }

        try(
                Socket conn = new Socket(args[0], Integer.parseInt(args[1]));
                Scanner userIn = new Scanner(System.in)
        )
        {
            BufferedReader connIn = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
            PrintWriter connOut = new PrintWriter( conn.getOutputStream(), true );

            //As soon as it connects it should immediately create the game so we can do that here
            serverMessage = connIn.readLine().split(" ");

            // Constructs the client game
            clientGame = new Reversi(Integer.parseInt(serverMessage[1]), Integer.parseInt(serverMessage[2]));

            //Prints the initial game
            System.out.println(clientGame);
            boolean continueRunning = true;

            while(continueRunning)
            {
                serverMessage = connIn.readLine().split(" ");
                switch (serverMessage[0])
                {
                    case MAKE_MOVE:
                        System.out.print("It is your turn to move! Enter row column: ");
                        connOut.println(MOVE + " " + userIn.nextLine());
                        break;
                    case MOVE_MADE:
                        System.out.println("A move has been made in row: " + serverMessage[1] + " column: " + serverMessage[2]);
                        clientGame.makeMove(Integer.parseInt(serverMessage[1]), Integer.parseInt(serverMessage[2]));
                        System.out.println(clientGame);
                        break;
                    case GAME_WON:
                        System.out.println("The game is over. You won!");
                        continueRunning = false;
                        break;
                    case GAME_LOST:
                        System.out.println("The game is over. You lost!");
                        continueRunning = false;
                        break;
                    case GAME_TIED:
                        System.out.println("The game is over. You tied!");
                        continueRunning = false;
                        break;
                    case ERROR:
                        System.out.println("The server hit an issue. The game will now exit.");
                        continueRunning = false;
                }
            }
        }
        catch (java.io.IOException ioe)
        {
            System.out.println("An error has occurred while attempting to run the game.");
            System.out.println(ioe.getMessage());
        }
        catch(NumberFormatException ne)
        {
            System.err.println("Host port must be an integer 0-65535.");
        }
        catch (ReversiException re)
        {
            System.out.println("We should never get here but the server has made an error... Quitting.");
        }
    }
}
