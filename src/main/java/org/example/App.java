package org.example;

import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class App 
{
    public static void main( String[] args )
    {
        String fileName = "tickets.json";
        String jsonString;

        try {
            jsonString = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new Gson();

        Tickets tickets = gson.fromJson(jsonString, Tickets.class);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H[H]:mm");

        ArrayList<Long> flightTimes = new ArrayList<>();

        for (Ticket ticket : tickets.tickets) {

            // Departure
            LocalDate departureLD = LocalDate.parse(ticket.departure_date, dayFormatter);
            LocalTime departureLT = LocalTime.parse(ticket.departure_time, timeFormatter);
            LocalDateTime departureLDT = LocalDateTime.of(departureLD, departureLT);

            ZoneId vvoZoneId = ZoneId.of("Asia/Vladivostok");
            ZonedDateTime departureZDT = ZonedDateTime.of(departureLDT, vvoZoneId);


            // Arrival
            LocalDate arrivalLD = LocalDate.parse(ticket.arrival_date, dayFormatter);
            LocalTime arrivalLT = LocalTime.parse(ticket.arrival_time, timeFormatter);
            LocalDateTime arrivalLDT = LocalDateTime.of(arrivalLD, arrivalLT);

            ZoneId tlvZoneId = ZoneId.of("Asia/Tel_Aviv");
            ZonedDateTime arrivalZDT = ZonedDateTime.of(arrivalLDT, tlvZoneId);


            // Time difference in minutes
            Long flightTimeInMinutes = ChronoUnit.MINUTES.between(departureZDT, arrivalZDT);

            flightTimes.add(flightTimeInMinutes);
        }

        // Percentile 90%
        Collections.sort(flightTimes);
        int index = (int) Math.ceil(90 / 100.0 * flightTimes.size());

        // To hours:minutes
        Duration durationPercentile = Duration.ofMinutes(flightTimes.get(index - 1));
        LocalTime localTimePercentile = LocalTime.MIN.plus(durationPercentile);


        // Average time
        Double averageTime = flightTimes.stream().
                mapToDouble(value -> value).
                average().
                orElse(0.0);

        // To hours:minutes
        Duration durationAverageTime = Duration.ofMinutes((long) Math.round(averageTime));
        LocalTime localTimeAverage = LocalTime.MIN.plus(durationAverageTime);

        System.out.println("Average time: " + localTimeAverage +", percentile 90%: " + localTimePercentile);

    }
}
