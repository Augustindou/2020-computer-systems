/* LINGI2241 - Computer Systems
 *      Valentin Lemaire - 1634 1700
 *      Augustin d'Oultremont - 2239 1700
 *
 *      This class is heavily based on this tutorial :
 *      https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
 *
 *      Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 *
 *      Redistribution and use in source and binary forms, with or without
 *      modification, are permitted provided that the following conditions
 *      are met:
 *
 *          - Redistributions of source code must retain the above copyright
 *            notice, this list of conditions and the following disclaimer.
 *
 *          - Redistributions in binary form must reproduce the above copyright
 *            notice, this list of conditions and the following disclaimer in the
 *            documentation and/or other materials provided with the distribution.
 *
 *          - Neither the name of Oracle or the names of its
 *            contributors may be used to endorse or promote products derived
 *            from this software without specific prior written permission.
 *
 *      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *      IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *      THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *      PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *      CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *      EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *      PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *      PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *      LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *      NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *      SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

public class Clients {
    public static void main(String[] args) throws IOException {
        // parse args
        if (args.length != 5) {
            System.err.println(
                    "Usage: java Clients <host name> <port number> <number of clients> <input file> <mean delay>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        int numberOfClients = Integer.parseInt(args[2]);
        float meanDelay = Float.parseFloat(args[3]);
        String inputFilename = args[3];

        Thread[] threads = new Thread[numberOfClients];

        for (int i = 0; i<numberOfClients; i++){
            // handle sending to server
            threads[i] = new Thread(() -> {
                try (
                        final Socket socket = new Socket(hostName, portNumber);
                        final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        final Scanner inputScanner = new Scanner(new File(inputFilename));
                ) {
                    final var ref = new Object() {
                        boolean done = false;
                    };
                    Thread sendingThread = new Thread(() -> {
                        String inputCommand;
                        while (inputScanner.hasNext()) {
                            inputCommand = inputScanner.nextLine();
                            if (inputCommand != null) {
                                try {
                                    Thread.sleep(poisson(meanDelay));
                                    out.println(inputCommand);
                                } catch (InterruptedException e){
                                    System.err.println(e.getMessage());
                                }
                            }
                        }
                        synchronized (ref) {
                            ref.done = true;
                        }
                    });

                    Thread receivingThread = new Thread(() -> {
                        try {
                            String fromServer;
                            while ((fromServer = in.readLine()) != null) {
                                System.out.println("Server: " + fromServer);
                                synchronized (ref) {
                                    if (ref.done) {
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                            System.exit(1);
                        }

                    });
                    sendingThread.start();
                    receivingThread.start();
                    sendingThread.join();
                    receivingThread.join();

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            });

            threads[i].start();
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

    }

    private static int poisson(double mean) {
        Random random = new Random();
        int r = 0;
        double a = random.nextDouble();
        double p = Math.exp(-mean);

        while (a > p) {
            r++;
            a = a - p;
            p = p * mean / r;
        }
        return r;
    }
}