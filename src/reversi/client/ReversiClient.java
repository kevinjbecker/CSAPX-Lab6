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
        String [] moveTokens;

        if(args.length != 2)
        {
            System.out.println("Invalid number of arguments.\nUsage: java ReversiClient host port");
        }
        else
        {
            try(
                    Socket conn = new Socket(args[0], Integer.parseInt(args[1]));
                    Scanner userIn = new Scanner(System.in)
            )
            {
                BufferedReader connIn = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
                PrintWriter connOut = new PrintWriter( conn.getOutputStream(), true );

                //As soon as it connects it should immediately create the game so we can do that here
                String response = connIn.readLine();
                System.out.println("Client got request: " + response);

                String [] initialCommand = response.split(" ");

                // Constructs the client game
                clientGame = new Reversi(Integer.parseInt(initialCommand[1]), Integer.parseInt(initialCommand[2]));

                System.out.println(clientGame);
                response = connIn.readLine();

                while(!response.equals(ERROR) && !response.equals(GAME_WON))
                {
                    switch (response)
                    {
                        case MAKE_MOVE:
                            System.out.print("It is your turn to move! Enter row column: ");
                            connOut.println(userIn.nextLine());
                        case MOVE_MADE:
                            moveTokens = response.split(" ");
                            System.out.println("Opponent placed piece at row: " + moveTokens[1] + " column: " + moveTokens[2]);
                            clientGame.makeMove(Integer.parseInt(moveTokens[1]), Integer.parseInt(moveTokens[2]));
                            break;
                        case GAME_WON:
                            break;
                        case GAME_LOST:
                            break;
                        case GAME_TIED:
                            break;
                    }
                }
            }
            catch (java.io.IOException ioe)
            {
                System.out.println("An error has occurred while attempting to run the game.");
            }
            catch (ReversiException re)
            {
                System.out.println("We should never get here but the server has made an error... Quitting.");
            }
        }
    }
}
