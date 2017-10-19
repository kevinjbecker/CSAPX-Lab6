package reversi.server;
import reversi.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;

public class ReversiServer implements ReversiProtocol
{

    public static int numMoves = 0;

    public static void main(String [] args)
    {
        int numRows, numCols, port;

        if(args.length < 3)
        {
            System.out.println("Invalid number of arguments.\nUsage: java ReversiServer #_rows #_cols port");
        }
        else
        {
            numRows = Integer.parseInt(args[0]);
            numCols = Integer.parseInt(args[1]);
            port = Integer.parseInt(args[2]);

            gameIO(port, numRows, numCols);
        }
    }

    private static void gameIO(int port, int numRows, int numCols)
    {
        Reversi serverGame = new Reversi(numRows, numCols);

        ServerSocket server = null;
        Socket conn1 = null;
        Socket conn2 = null;

        try
        {
            // Alerts the start of the program.
            //System.out.println("Server is being initialized on port "+ port + " ...");
            // Initializes the servers on the port.
            server = new ServerSocket(port);
            // Alerts the user what port the server is on.
            //System.out.println("Server initialization complete.");

            // Allows for player 1 to connect to server1.
            System.out.print("Waiting for player 1 to connect... ");

            conn1 = server.accept();
            BufferedReader conn1In = new BufferedReader( new InputStreamReader( conn1.getInputStream() ) );
            PrintWriter conn1Out = new PrintWriter( conn1.getOutputStream(), true );

            // We tell conn1 it was connected then the numRows and numCols
            conn1Out.println(CONNECT + " " + numRows + " " + numCols);

            System.out.println("connected!");

            // Allows for player 2 to connect to server2.
            System.out.print("Waiting for player 2 to connect... ");

            conn2 = server.accept();
            BufferedReader conn2In = new BufferedReader( new InputStreamReader( conn2.getInputStream() ) );
            PrintWriter conn2Out = new PrintWriter( conn2.getOutputStream(), true );

            // We tell conn2 it was connected then the numRows and numCols
            conn2Out.println(CONNECT + " " + numRows + " " + numCols);

            System.out.println("connected!\nGame is starting!");

            conn1Out.println(MAKE_MOVE);
            String move1 = conn1In.readLine();
            System.out.println(move1);

            conn2Out.println(MAKE_MOVE);
            String move2 = conn2In.readLine();
            System.out.println(move2);

            conn1Out.println(GAME_LOST);
            conn2Out.println(GAME_WON);


            /*
            while(!serverGame.gameOver())
            {
                // Adds one to the number of moves made
                ++numMoves;

            }
            */
        }
        catch (IOException gameRunIOE)
        {
            System.out.println("An error has occurred while attempting to run the game.");
            System.out.println(gameRunIOE.getMessage());
            gameRunIOE.printStackTrace();
        }
        finally
        {
            try
            {
                // Closing server1 if it's not already closed.
                if (server != null) server.close();

                // Closing conn1 if it's not already closed.
                if (conn1 != null) conn1.close();

                // Closing conn2 if it's not already closed.
                if (conn2 != null) conn2.close();
            }
            catch (IOException socketCloseIOE)
            {
                System.out.println("An error occurred while closing sockets.");
                System.out.println(socketCloseIOE.getMessage());
                socketCloseIOE.printStackTrace();
            }
        }
    }
}
