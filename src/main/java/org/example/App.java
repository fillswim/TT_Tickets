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

        ArrayList<Long> flightTimes = new ArrayList<>();

        for (Ticket ticket : tickets.tickets) {

            // Departure time
            ZonedDateTime departureTime = concertFromStringToZonedDateTime(ticket.departure_date, ticket.departure_time, "Asia/Vladivostok");

            // Arrival time
            ZonedDateTime arrivalTime = concertFromStringToZonedDateTime(ticket.arrival_date, ticket.arrival_time, "Asia/Tel_Aviv");

            // Time difference in minutes
            Long flightTimeInMinutes = ChronoUnit.MINUTES.between(departureTime, arrivalTime);

            flightTimes.add(flightTimeInMinutes);
        }

        // Percentile 90%
        Collections.sort(flightTimes);
        int index = (int) Math.ceil(90 / 100.0 * flightTimes.size());

        // To hours:minutes
        LocalTime localTimePercentile = getHoursAndMinutesFromMinutes(flightTimes.get(index - 1));

        // Average time
        Double averageTime = flightTimes.stream().
                mapToDouble(value -> value).
                average().
                orElse(0.0);

        // To hours:minutes
        LocalTime localTimeAverage = getHoursAndMinutesFromMinutes((long) Math.round(averageTime));

        System.out.println("Average time: " + localTimeAverage +", percentile 90%: " + localTimePercentile);
    }


    private static ZonedDateTime concertFromStringToZonedDateTime(String date, String time, String zone) {
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H[H]:mm");

        LocalDate localDate = LocalDate.parse(date, dayFormatter);
        LocalTime localTime = LocalTime.parse(time, timeFormatter);

        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of(zone));

        return zonedDateTime;
    }

    private static LocalTime getHoursAndMinutesFromMinutes(Long minutes) {
        Duration duration = Duration.ofMinutes(minutes);
        LocalTime localTime = LocalTime.MIN.plus(duration);

        return localTime;
    }
}
