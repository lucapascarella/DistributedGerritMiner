package org.lucapascarella.utils;

public class ExecTime {

    long millis;

    public ExecTime() {
        millis = System.currentTimeMillis();
    }

    public void printExecutionTime(String programName) {
        long execTime, secs = 0, mins = 0, hours = 0, days = 0;

        execTime = System.currentTimeMillis() - millis;
        if (execTime > 1000) {
            secs = execTime / 1000;
            if (secs > 60) {
                mins = secs / 60;
                secs = secs % 60;
                if (mins > 60) {
                    hours = mins / 60;
                    mins = mins % 60;
                    if (hours > 24) {
                        days = hours / 24;
                        hours = hours % 24;
                        System.out.println("\n*** " + programName + " ended in: " + days + " day(s) " + hours + ":" + mins + ":" + secs + " ***");
                    } else {
                        System.out.println("\n*** " + programName + " ended in: " + hours + ":" + mins + ":" + secs + " ***");
                    }
                } else {
                    System.out.println("\n*** " + programName + " ended in: " + mins + ":" + secs + " ***");
                }
            } else {
                System.out.println("\n*** " + programName + " ended in: " + secs + " seconds ***");
            }
        } else {
            System.out.println("\n*** " + programName + " ended in: " + execTime + " milliseconds ***");
        }
    }

}
