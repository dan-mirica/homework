package org.example.tests;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import org.example.models.Pojo;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static com.jayway.jsonpath.JsonPath.parse;

public class HomeworkTC {

    @Test
    public void homeworkTC() throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource("downloads.txt");
        assert resource != null;
        List<String> lines = Files.readAllLines(Path.of(resource.toURI()));
//        System.out.println(lines);

        //get to know the json a little bit better :) vs mapping to an object
        viaJsonPath(lines);

        String first = lines.getFirst();
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(first, Map.class);
        Pojo pojo = gson.fromJson(first, Pojo.class);

        // an alternative way is to transform the json to a pojo and get on from there with the asserts
        System.out.println("---------------------------------");
        System.out.println("via gson to a map: " + map.toString());
        System.out.println("via gson to pojo: " + pojo.toString());
    }

    private static void viaJsonPath(List<String> lines) {
        System.out.println("via json path:");
        SoftAssertions soft = new SoftAssertions();
        String cityP = "$.city";
        String nameP = "$.downloadIdentifier.showId";
        String deviceP = "$.deviceType";
        String aw0ais = "$.opportunities[*].positionUrlSegments['aw_0_ais.adBreakIndex'][*]";
        String originalEventTimeP = "$.opportunities[*].originalEventTime";

        Map<String, Long> sanFranciscoMap = new HashMap<>();
        Map<String, Long> devicesMap = new HashMap<>();
        Map<String, Long> prerollOppMap = new HashMap<>();
        Map<String, List<Long>> airingMap = new HashMap<>();

        for (String line : lines) {
            String city = JsonPath.read(line, cityP).toString();
            String name = JsonPath.read(line, nameP).toString();

            if (city.equals("san francisco")) {
                Long beforeName = sanFranciscoMap.getOrDefault(name, 0L);
                sanFranciscoMap.put(name, beforeName + 1);
            }
            String device = JsonPath.read(line, deviceP).toString();
            Long beforeDevice = devicesMap.getOrDefault(device, 0L);
            devicesMap.put(device, beforeDevice + 1);

            List<String> awList = parse(line).read(aw0ais, List.class);
            long preroll = awList.stream().filter(s -> s.equals("preroll")).count();
            Long beforePO = prerollOppMap.getOrDefault(name, 0L);
            prerollOppMap.put(name, beforePO + preroll);

            List<Long> timestamps = parse(line).read(originalEventTimeP, List.class);
            Long t1 = timestamps.getFirst();
            if (airingMap.containsKey(name)) {
                airingMap.get(name).add(t1);
            } else {
                ArrayList<Long> init = new ArrayList<>();
                init.add(t1);
                airingMap.put(name, init);
            }
        }

        Optional<Map.Entry<String, Long>> sfPopularData = sanFranciscoMap.entrySet().stream().max(Map.Entry.comparingByValue());
        String expName = "Who Trolled Amber";
        Long expDown = 24L;
        if (sfPopularData.isPresent()) {
            String show = sfPopularData.get().getKey();
            Long down = sfPopularData.get().getValue();
            System.out.println("Most popular show is: " + show);
            System.out.println("Number of downloads is: " + down);
//            Long expDown = 114L; //wrong
            soft.assertThat(show).as("san francisco popular show name check").isEqualTo(expName);
            soft.assertThat(down).as("san francisco popular show #downloads check").isEqualTo(expDown);
        } else {
            System.out.println("whoops names"); // throw exception or something appropriate to the context
        }
        //note: for maximums maybe a better approach would be to find the maximum value and then filter the entries with that value
        //the above approach I think it doesn't cover the case where we have multiple shows with the max value
        Long maximum = sfPopularData.orElseThrow(() -> new RuntimeException("something went wrong finding the maximum...")).getValue();
        List<Map.Entry<String, Long>> popularShowsData = sanFranciscoMap.entrySet().stream().filter(e -> e.getValue().equals(maximum)).toList();
        // now we should assert that popularShowsData contains expected data
        soft.assertThat(popularShowsData.getFirst().getKey()).as("san francisco popular show name check (2)").isEqualTo(expName);
        soft.assertThat(popularShowsData.getFirst().getValue()).as("san francisco popular show #downloads check (2)").isEqualTo(expDown);

        Optional<Map.Entry<String, Long>> devicesPopular = devicesMap.entrySet().stream().max(Map.Entry.comparingByValue());
        if (devicesPopular.isPresent()) {
            String devices = devicesPopular.get().getKey();
            System.out.println("Most popular device is: " + devices);
            Long down = devicesPopular.get().getValue();
            System.out.println("Number of downloads is: " + down);
            String expDevices = "mobiles & tablets";
            Long expDownDevices = 60L;
//            Long expDown = 70L; //wrong
            soft.assertThat(devices).as("popular devices check").isEqualTo(expDevices);
            soft.assertThat(down).as("popular devices #downloads check").isEqualTo(expDownDevices);
        } else {
            System.out.println("whoops devices"); // throw exception or something appropriate to the context
        }

        Stream<Map.Entry<String, Long>> sorted = prerollOppMap.entrySet().stream().sorted(Map.Entry.comparingByValue());
        List<Map.Entry<String, Long>> reversed = sorted.toList().reversed();
        Map<String, Long> expPreRoll = Map.ofEntries(
                Map.entry("Stuff You Should Know", 40L),
//                Map.entry("Stuff You Should Know", 50L), //wrong
                Map.entry("Who Trolled Amber", 40L),
                Map.entry("Crime Junkie", 30L),
                Map.entry("The Joe Rogan Experience", 10L)
        );
        //this one can be optional depending on context
        soft.assertThat(reversed.size()).as("actual preroll vs expected preroll size check").isEqualTo(expPreRoll.size());
        for (Map.Entry<String, Long> datum : reversed) {
            String name = datum.getKey();
            Long preroll = datum.getValue();
            System.out.println("Show Id: " + name + " Preroll Opportunity Number: " + preroll);
            soft.assertThat(expPreRoll.get(name)).as("preroll check for " + name).isEqualTo(preroll);
        }

        Map<String, String> expWeeklies = Map.ofEntries(
                Map.entry("Crime Junkie","Wed 22:00"),
//                Map.entry("Crime Junkie","Wed 17:00"), //wrong
                Map.entry("Who Trolled Amber","Mon 20:00")
        );

        System.out.println("Weekly shows are:");
        long noWeekliesFound = 0;
        for (Map.Entry<String, List<Long>> entry : airingMap.entrySet()) {
            //here is a corner case that maybe the list has only one value ??? that needs to be treated
            long diffInMillis = Math.abs(entry.getValue().getFirst() - entry.getValue().get(1));
            SimpleDateFormat sdf = new SimpleDateFormat("EEE HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            if (diffInMillis == 7 * 24 * 60 * 60 * 1000) {
                String pretty = sdf.format(entry.getValue().getFirst());
                String name = entry.getKey();
                System.out.println(name + " - " + pretty);
                soft.assertThat(expWeeklies.get(name)).as("weekly check for " + name).isEqualTo(pretty);
                noWeekliesFound++;
            }
        }
        //this one can be optional depending on context
        soft.assertThat(noWeekliesFound).as("actual no of weeklies vs expected check").isEqualTo(expWeeklies.size());

        soft.assertAll();
    }
}
