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
import java.util.*;

public class Clients {

    static Random rand;

    public static void main(String[] args) {
        // parse args
        if (args.length != 6 && args.length != 7) {
            System.err.println("Usage: java Clients <host name> <port number> <number of clients> <input file> <mean delay> <verbose> [results file]");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        int numberOfClients = Integer.parseInt(args[2]);
        String inputFilename = args[3];
        float meanDelay = Float.parseFloat(args[4]);
        boolean verbose = Boolean.parseBoolean(args[5]);
        String outputFilename = (args.length == 7) ? args[6] : null;
        boolean saveTime = outputFilename != null;

        final List<Long> resultsList = new ArrayList<>();

        rand = new Random();

        Thread[] threads = new Thread[numberOfClients];

        List<String> requests = generateRequestList(inputFilename);

        int totalRequests = requests.size() * numberOfClients;

        try (
            final Socket socket = new Socket(hostName, portNumber);
            final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            for (int i = 0; i<numberOfClients; i++){
                final int idx = i;

                // handle sending to server
                threads[i] = new Thread(() -> {
                    try {
                        Thread sendingThread = new Thread(() -> {
                            List<String> requestsForThread = new ArrayList<>(requests);
                            Collections.shuffle(requestsForThread);

                            for (String r : requestsForThread){
                                try {
                                    Thread.sleep((long) exponential(1/meanDelay));
                                    if (saveTime) {
                                        r = new Date().getTime() + ";" + r;
                                    }
                                    synchronized (out) {
                                        out.println(r);
                                    }
                                } catch (InterruptedException e){
                                    System.err.println(e.getMessage());
                                }
                            }
                        });

                        sendingThread.start();
                        sendingThread.join();

                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                });

                threads[i].start();
            }

            Thread receivingThread = new Thread(() -> {
                int doneCount = 0;
                try {
                    String fromServer;
                    int requestCount = 0;
                    boolean newResponse = true;
                    while ((fromServer = in.readLine()) != null) {
                        requestCount++;
                        if (saveTime && newResponse) {
                            long finishingTime = new Date().getTime();
                            String[] splitResponse = fromServer.split(";", 2);
                            long timeStamp = Long.parseLong(splitResponse[0]);
                            fromServer = splitResponse[1];
                            newResponse = false;
                            logResponse(finishingTime - timeStamp, resultsList);
                        }

                        if (fromServer.equals("")) {
                            doneCount++;
                            newResponse = true;
                            if (verbose) {
                                System.out.println("Received Server Response of length : " + (requestCount - 1));
                                //System.out.println("Received Server Response : \n" + fromServer);
                            }
                            requestCount = 0;
                        }

                        if (doneCount == totalRequests)
                            break;

                    }
                    if (verbose)
                        System.out.println("All clients finished");
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            });

            receivingThread.start();

            for (Thread thread : threads) {
                thread.join();
            }
            receivingThread.join();

        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        writeResultsFile(resultsList, outputFilename+".txt", verbose);

    }


    public static double exponential(float lambda) {
        return Math.log(1-rand.nextDouble())/(-lambda);
    }

    public static synchronized void logResponse(long time, List<Long> resultsList) {
        resultsList.add(time);
    }

    public static void writeResultsFile(List<Long> resultsList, String outputFilename, boolean verbose) {
        try {
            FileWriter outputWriter = new FileWriter(outputFilename);
            for (long line : resultsList) {
                outputWriter.write(line+"\n");
            }
            outputWriter.close();
            if (verbose)
                System.out.println("Saved results to "+outputFilename);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static List<String> generateRequestList(String inputFilename) {
        List<String> requests = new ArrayList<>();
        try (
            final Scanner inputScanner = new Scanner(new File(inputFilename));
        ) {
            String line;
            while (inputScanner.hasNext()) {
                if ((line = inputScanner.nextLine()) != null)
                    requests.add(line);
            }
            return requests;
        } catch (IOException e) {
            System.err.println("Empty file or file not found");
            return requests;
        }
    }
}
