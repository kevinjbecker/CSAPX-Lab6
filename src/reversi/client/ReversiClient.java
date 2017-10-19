package reversi.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import reversi.*;
import java.net.Socket;

public class ReversiClient implements ReversiProtocol
{
    public static void main(String [] args)
    {
        Reversi clientGame = null;

        if(args.length != 2)
        {
            System.out.println("Invalid number of arguments.\nUsage: java ReversiClient host port");
        }
        else
        {
            try(
                Socket conn = new Socket(args[0], Integer.parseInt(args[1]))
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

                response = connIn.readLine();

                if(response.equals(MAKE_MOVE))
                {
                    connOut.println(MOVE + " " + 3 + " " + 2);
                }

                response = connIn.readLine();

                System.out.println("Client got request: " + response);


                /*
                while(response.equals())
                {

                }
                */
            }
            catch (java.io.IOException ioe)
            {
                System.out.println("An error has occurred while attempting to run the game.");
            }
        }
    }
}
