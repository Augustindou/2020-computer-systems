/*
 * LINGI2241 - Computer Systems
 *      Augustin d'Oultremont - 2239 1700
 *      Valentin Lemaire - 1634 1700
 *
 *      This class is heavily based on this tutorial:
 *      https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
 *
 *      Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
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

import utils.Buffer;
import utils.Request;
import utils.Cache;

import java.net.*;
import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptimizedServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 3 && args.length != 4) {
            System.err.println("Usage: java OptimizedServer <port number> <database text file> <number of threads> [result text file]");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        String dbfile = args[1];
        final int N_THREADS = Integer.parseInt(args[2]);
        String resultsFile = (args.length == 4) ? args[3] : null;

        ServerSocket serverSocket = new ServerSocket(portNumber);
        Buffer<Request> buffer = new Buffer<>(2000);
        OptimizedServerProtocol osp = new OptimizedServerProtocol(initArray(dbfile), 20, 5);

        System.out.println("Server is up");

        Socket clientSocket = serverSocket.accept();
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        List<Long> queueTimes = new ArrayList<>();
        List<Long> serviceTimes = new ArrayList<>();


        // Initializing worker threads
        Thread[] threads = new Thread[N_THREADS];
        for (int i=0; i < N_THREADS; i++) {
            threads[i] = new Thread(() -> {
                try {
                    Request r;
                    while ((r = buffer.take()) != null) {
                        if (r.getRequestValue().equals("Done")) break;
                        r.setFinishedQueuingTime(new Date());
                        r.setStartingToTreatRequestTime(new Date());
                        String outputLine = osp.processInput(r.getRequestValue());
                        r.setFinishedTreatingRequestTime(new Date());
                        if (r.getSentByClientTime() != null) {
                            outputLine = r.getSentByClientTime().getTime() + ";" + outputLine;
                        }
                        synchronized (out) {
                            out.println(outputLine);
                            out.flush();
                        }
                        if (resultsFile != null) {
                            logResponse(r.computeQueuingTime(), queueTimes);
                            logResponse(r.computeServiceTime(), serviceTimes);
                        }
                    }
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            });
            // starting the threads right away
            threads[i].start();
        }

        // Loop to fill buffer
        try {
            String fromClient;
            while ((fromClient = in.readLine()) != null) {
                Request received;
                if (resultsFile != null) {
                    String[] splitRequest = fromClient.split(";", 2);
                    received = new Request(splitRequest[1]);
                    received.setSentByClientTime(new Date(Long.parseLong(splitRequest[0])));
                } else {
                    received = new Request(fromClient);
                }
                received.setStartingQueuingTime(new Date());
                if (!buffer.add(received)) System.err.println("Full buffer, had to drop a request");
            }
            for (int i = 0; i < N_THREADS; i++) {
                if (!buffer.add(new Request("Done"))) System.err.println("Could not stop a thread");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Waiting for the threads to finish to return
        for (int i = 0; i < N_THREADS; i++) {
            threads[i].join();
        }

        in.close();
        out.close();
        serverSocket.close();
        clientSocket.close();

        if (resultsFile != null) {
            writeResultsFile(queueTimes, resultsFile+"_queue.txt");
            writeResultsFile(serviceTimes, resultsFile+"_service.txt");
        }

    }

    public static String[][] initArray(String filename) {
        try {
            File file = new File(filename);

            Scanner reader = new Scanner(file);
            Map<Integer, Set<String>> map = new HashMap<>();

            // Parsing file and adding it to hashmap with category as key
            String[] data;
            while (reader.hasNextLine()) {
                data = reader.nextLine().split("@@@");
                int idx = Integer.parseInt(data[0]);
                if (!map.containsKey(idx)) {
                    map.put(idx, new HashSet<>());
                }
                map.get(idx).add(data[1]);
            }
            reader.close();

            String[][] finalData = new String[map.size()][];
            for (Map.Entry<Integer, Set<String>> e : map.entrySet()) {
                finalData[e.getKey()] = e.getValue().toArray(new String[0]);
            }
            return finalData;
            /*Map<Integer, List<String>> mapOfLists = new HashMap<>();
            for (Map.Entry<Integer, Set<String>> e : map.entrySet()) {
                mapOfLists.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
            return mapOfLists;*/
        } catch (FileNotFoundException e) {
            System.err.println("No such file");
            return null;
        }

    }

    public static int[] arange(int N) {
        int[] res = new int[N];
        for (int i = 0; i < N; i++) {
            res[i] = i;
        }
        return res;
    }

    public static synchronized void logResponse(long time, List<Long> times) {
        times.add(time);
    }

    public static void writeResultsFile(List<Long> resultsList, String outputFilename) {
        try {
            FileWriter outputWriter = new FileWriter(outputFilename);
            for (long line : resultsList) {
                outputWriter.write(line+"\n");
            }
            outputWriter.close();
            System.out.println("Saved results to "+outputFilename);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static class OptimizedServerProtocol {
        private final String[][] dataMap;
        private final Cache cache;

        public OptimizedServerProtocol(String[][] dataMap, int N, float theta) {
            this.dataMap = dataMap;
            this.cache = new Cache(N, theta);
        }

        public String processInput(String command) throws InterruptedException {
            if (command == null)
                return null;

            String[] data = command.split(";", 2);
            if (data.length != 2) {
                System.err.println("Wrong command format.");
                return null;
            }

            // Checking if item is in cache
            String cacheResponse = this.cache.get(command);
            if (cacheResponse != null) {
                return cacheResponse;
            }

            // If not in cache doing some processing
            String[] types = data[0].split(",");
            String regex = data[1];
            Pattern pattern = Pattern.compile(regex);

            int[] intTypes;
            if (types.length == 1 && types[0].equals("")) {
                // If no types we search them all
                intTypes = arange(this.dataMap.length);
            } else {
                intTypes = new int[types.length];
                for (int i = 0; i < types.length; i++) {
                    intTypes[i] = Integer.parseInt(types[i]);
                }
            }

            // Search in each type are independent, it can be done in concurrent threads
            StringBuilder builder = new StringBuilder();
            String[][] map = OptimizedServerProtocol.this.dataMap;
            for (int idx : intTypes) {
                for (String s : map[idx]) {
                    Matcher matcher = pattern.matcher(s);
                    if (matcher.find()) {
                        builder.append(idx).append("@@@").append(s).append("\n");
                    }
                }
            }

            /*for (int idx : intTypes) {
                String matches = idx+"@@@"+map.get(idx).stream().filter(pattern.asPredicate()).collect(Collectors.joining("\n"+idx+"@@@"));
                matches = matches.substring(0, matches.length()-(idx+"@@@").length());
                if (matches.length() > 0)
                    builder.append(matches+"\n");
            }*/

            String response;
            if (builder.length() > 0)
                response = builder.toString();
            else
                response = "";

            // The item was not in cache so it is added
            this.cache.add(command, response);
            return response;
        }
    }


}
