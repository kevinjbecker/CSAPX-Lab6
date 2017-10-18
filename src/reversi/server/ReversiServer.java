package reversi.server;

import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.security.ntlm.Server;
import reversi.Reversi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;

import java.io.IOException;
import java.net.Socket;

public class ReversiServer
{


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

            gameIO(numRows, numCols, port);
        }
    }

    public static void gameIO(int numRows, int numCols, int port)
    {
        Reversi game = new Reversi(numRows, numCols);

        ServerSocket server1 = null;
        ServerSocket server2 = null;
        Socket conn1 = null;
        Socket conn2 = null;

        try
        {
            System.out.println("Server is being initialized...");

            server1 = new ServerSocket(port);
            server2 = new ServerSocket(port);

            System.out.println("Server initialized on port" + port + ".");
            System.out.print("Waiting for player 1 to connect...");

            conn1 = server1.accept();

            System.out.println("Player 1 connected.");
            System.out.print("Waiting for player 2 to connect...");

            conn2 = server2.accept();

            System.out.println("Player 2 connected.\nGame will start now!");

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
                if (server1 != null) server1.close();

                // Closing server2 if it's not already closed.
                if (server2 != null) server2.close();

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
