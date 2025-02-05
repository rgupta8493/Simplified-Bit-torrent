package client1;

import client2.Client2;
import count.counter;
import splitfiles.Splitfile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Firstclient {

    private static Socket sockserver;
    private static String fileName;
    public static String chunklist[];
    public static int chunkcount=0;
    private static BufferedReader stdin;
    private static PrintStream os;
    private static ServerSocket csock1;
    private static Socket csock=null;
    private static ArrayList<String> list;
    private static ObjectInputStream odd;
    public static int k =0;
    public static int timeout;
    public  static int mcount;
    public static int mtime=0;




    public static void main(String[] args) throws IOException {

        counter cc = new counter();
        Mergefiles m = new Mergefiles();
        try {
            sockserver = new Socket("localhost", 4450);
            stdin = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e) {
            System.err.println("Cannot connect to the server, try again later.");
            System.exit(1);
        }

        os = new PrintStream(sockserver.getOutputStream());
        int i;

        try {
            switch (Integer.parseInt(selectAction())) {
                // case 1:
                // os.println("1");
                //sendFile();
                // break;
                case 1:
                    os.println("2");
                    os.println("client 1");
                    cc.noofchunks();

                    chunklist = new String[cc.countchunks];
                    for (i = 1; i <= cc.countchunks; i += 5) {
                        fileName = receiveFile(fileName, "server", sockserver);
                        chunklist[k++] = fileName;


                    }




                    break;
                default:
                    System.out.println("Nothing");
            }
        } catch (Exception e) {
            System.err.println("not valid input");
        }


        sockserver.close();
        int o;
        timeout = 0;
        while (true) {
            uploadtopeer();
            o = downloadpeer();
            System.out.println(mcount);
            if(mcount==chunklist.length){
                m.merge();
            }

    }
   }
    public static void uploadtopeer() throws IOException {
     //Client1 now acts as a server for client 2
        counter cc = new counter();

        try {

            csock1 = new ServerSocket(5000);
            System.out.println("Client1 uploader thread is started");
            csock = csock1.accept();
            System.out.println("connection made " + csock);
            Thread t = new Thread(new Peer1connection(csock  ,chunklist));
            t.start();



        } catch (Exception e) {
            System.out.println("Error"+e);
        }

        csock1.close();



    }

    public static int downloadpeer(){
        while(true) {

            try {
                Socket sock5 = new Socket("localhost", 5002);
                stdin = new BufferedReader(new InputStreamReader(System.in));
                os = new PrintStream(sock5.getOutputStream());
                odd = new ObjectInputStream(sock5.getInputStream());
                String chunklist5[] = (String[]) odd.readObject();

                System.out.println("Downloading from neighbour client 5\n ");

                for (int i = 0; i < chunklist.length; i++) {
                    if (chunklist[i] == null)
                        break;
                    int flag = 0;
                    for (int j = 0; j < k; j++) {

                        if (chunklist5[i].equalsIgnoreCase(chunklist[j])) {
                           // System.out.println("already has this chunk");
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        os.println("yes");
                        receiveFile(chunklist5[i], "Client5", sock5);
                        chunklist[k++] = chunklist5[i];
                        mcount=k;
                    } else
                        os.println("no");


                }
                System.out.println("\nFinished downloading !!!");

            } catch (Exception e) {
                timeout++;
                if (timeout > 5) {
                    timeout = 0;
                    return 1;
                }

                System.out.println("Ping client5 to connect");

            }
        }
      //  System.out.println("I am trying to return");
    }

    public static String selectAction() throws IOException {
        System.out.println("1. Get files from server");
        return stdin.readLine();
    }

    public static String receiveFile(String fileName,String peer,Socket sock) {
        try {
            int bytesRead;
            InputStream in = sock.getInputStream();

            DataInputStream clientData = new DataInputStream(in);

            fileName = clientData.readUTF();
            OutputStream output = new FileOutputStream("src/client1/" + fileName);
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }


          output.flush();


            System.out.println("File " + fileName + " received from"+" "+peer);

        } catch (IOException ex) {
            // Logger.getLogger(src/Main.Mainserver.CLIENTConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileName;


    }

}