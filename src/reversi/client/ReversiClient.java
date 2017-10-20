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
        String response;
        String [] moveTokens;

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
            response = connIn.readLine();
            System.out.println("Client got request: " + response);

            String [] initialCommand = response.split(" ");

            // Constructs the client game
            clientGame = new Reversi(Integer.parseInt(initialCommand[1]), Integer.parseInt(initialCommand[2]));

            //Prints the initial game
            System.out.println(clientGame);
            response = connIn.readLine();

            boolean continueRunning = true;

            while(continueRunning)
            {
                switch (response)
                {
                    case MAKE_MOVE:
                        System.out.print("It is your turn to move! Enter row column: ");
                        connOut.println(userIn.nextLine());
                        // we don't update our game because the server is used to check if its valid or not.
                        break;
                    case MOVE_MADE:
                        moveTokens = response.split(" ");
                        System.out.println("Opponent placed piece at row: " + moveTokens[1] + " column: " + moveTokens[2]);
                        clientGame.makeMove(Integer.parseInt(moveTokens[1]), Integer.parseInt(moveTokens[2]));
                        System.out.println(clientGame);
                        break;
                    case GAME_WON:
                        System.out.println("You won!");
                        continueRunning = false;
                        break;
                    case GAME_LOST:
                        System.out.println("You lost!");
                        continueRunning = false;
                        break;
                    case GAME_TIED:
                        System.out.println("The game was a draw.");
                        continueRunning = false;
                        break;
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
