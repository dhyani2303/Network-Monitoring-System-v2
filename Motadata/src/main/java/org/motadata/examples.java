package org.motadata;

public class examples {

        public static boolean isDeviceAvailable(String fPingOutput) {
            String[] lines = fPingOutput.split("\n");
            for (String line : lines) {
                if (line.contains("xmt/rcv/%loss"))
                {
                    String[] parts = line.split("=",2);

                    System.out.println(parts[0]+ " "+parts[1]);
                    if (parts.length == 2) {
                        String lossPercentage = parts[1].split("%")[0].trim();
                        System.out.println(lossPercentage);
                        int loss = Integer.parseInt(lossPercentage.split("/")[2]);
                        return loss != 100;
                    }
                }
            }
            return false;
        }

        public static void main(String[] args) {
            String fPingOutput = "10.20.40.128 : xmt/rcv/%loss = 3/3/0%, min/avg/max = 0.024/0.034/0.046";
            boolean isAvailable = isDeviceAvailable(fPingOutput);
            System.out.println("Device is available: " + isAvailable);
        }
    }
