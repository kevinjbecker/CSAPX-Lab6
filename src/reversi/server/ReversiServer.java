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

    private static int numMoves = 0;

    public static void main(String [] args)
    {
        int numRows, numCols, port;

        if(args.length < 3)
        {
            System.out.println("Invalid number of arguments.\nUsage: java ReversiServer #_rows #_cols port");
            System.exit(1);
        }

        numRows = Integer.parseInt(args[0]);
        numCols = Integer.parseInt(args[1]);
        port = Integer.parseInt(args[2]);

        gameIO(port, numRows, numCols);
    }

    private static void gameIO(int port, int numRows, int numCols)
    {
        Reversi serverGame = new Reversi(numRows, numCols);

        ServerSocket server = null;

        Socket conn1 = null;
        Socket conn2 = null;

        BufferedReader conn1In;
        PrintWriter conn1Out = null;
        BufferedReader conn2In;
        PrintWriter conn2Out = null;

        String [] move;

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
            conn1In = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
            conn1Out = new PrintWriter(conn1.getOutputStream(), true);

            // We tell conn1 it was connected then the numRows and numCols
            conn1Out.println(CONNECT + " " + numRows + " " + numCols);

            System.out.println("connected!");

            // Allows for player 2 to connect to server2.
            System.out.print("Waiting for player 2 to connect... ");

            conn2 = server.accept();
            conn2In = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
            conn2Out = new PrintWriter(conn2.getOutputStream(), true);

            // We tell conn2 it was connected then the numRows and numCols
            conn2Out.println(CONNECT + " " + numRows + " " + numCols);

            System.out.println("connected!\nGame is starting!");

            while (!serverGame.gameOver())
            {

                if (numMoves % 2 == 0)
                {
                    conn1Out.println(MAKE_MOVE);
                    move = conn1In.readLine().split(" ");
                }
                else
                {
                    conn2Out.println(MAKE_MOVE);
                    move = conn2In.readLine().split(" ");
                }

                // attempts to make the move here
                serverGame.makeMove(Integer.parseInt(move[1]), Integer.parseInt(move[2]));
                // if there was no exception thrown we can tell each client the move was okay so they can
                // update their versions of the game.
                conn1Out.println(MOVE_MADE + " " + move[1] + " " + move[2]);
                conn2Out.println(MOVE_MADE + " " + move[1] + " " + move[2]);

                // DEBUGGING ONLY
                System.out.println("A move has been made. Master game reads: ");
                System.out.println(serverGame);

                ++numMoves;
            }

            Reversi.Move winner = serverGame.getWinner();

            if(winner.equals(Reversi.Move.PLAYER_ONE))
            {
                conn1Out.println(GAME_WON);
                conn2Out.println(GAME_LOST);
            }
            else if (winner.equals(Reversi.Move.PLAYER_TWO))
            {
                conn2Out.println(GAME_WON);
                conn1Out.println(GAME_LOST);
            }
            else
            {
                conn1Out.println(GAME_TIED);
                conn2Out.println(GAME_TIED);
            }
        }
        catch (IOException gameRunIOE)
        {
            System.out.println("An error has occurred while attempting to run the game.");
            System.out.println(gameRunIOE.getMessage());
            gameRunIOE.printStackTrace();
        }
        catch(ReversiException re)
        {
            System.out.println("Reversi hit an issue. Halting clients.");
            conn1Out.println(ERROR);
            conn2Out.println(ERROR);
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
