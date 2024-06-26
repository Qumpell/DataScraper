package com.datascrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DataScraper {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DataScraper() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }


    public JsonNode fetchData(String url) throws IOException {
        HttpResponse<String> response;

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error code : " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    public JsonNode fetchDataWithRetries(String url, int maxRetries, Duration backoffDuration) throws IOException, InterruptedException {
        int retries = 0;
        while (retries <= maxRetries) {
            try{
                return fetchData(url);
            }catch (RuntimeException e) {
                if(e.getMessage().contains("429")){

//                    System.out.println("Received 429 - Too Many Requests. Retrying after backoff.");
                    Thread.sleep(backoffDuration.toMillis());
                    retries++;
                }
                else{
                    throw e;
                }
            }
        }
        throw new RuntimeException("Reached maximum retries. Unable to fetch data.");
    }

    public List<Station> fetchAllStations(){
//        System.out.println("Fetching all stations ...");
        int page = 0;
        int size = 100;

        List<Station> stations = new ArrayList<>();

        final String baseUrl = "https://api.gios.gov.pl/pjp-api/v1/rest/station/findAll";
        StringBuilder url = new StringBuilder(baseUrl);
        int totalPages = -1;
        try {
            
          do {

              url.append("?page=").append(page).append("&size=").append(size);
              final JsonNode fetchedData = fetchDataWithRetries(url.toString(), 5, Duration.ofSeconds(15));

              if(totalPages==-1){
                  totalPages = fetchedData.get("totalPages").asInt();
              }

              final JsonNode itemsNode = fetchedData.path("Lista stacji pomiarowych");

//              System.out.println("Fetched page " + page + " of " + totalPages + " with data size := "+ itemsNode.size());

              for(JsonNode item : itemsNode){

                  Station station = new Station();
                  station.setId(item.path("Identyfikator stacji").asInt());
                  station.setName(item.path("Nazwa stacji").asText());

                  stations.add(station);
              }
              page++;
              url = new StringBuilder(baseUrl);

          }while (page < totalPages);
           


        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return stations;
    }

    public List<Installation> fetchAllInstallationsForStation(int stationId){
//        System.out.println("Fetching all installations for station id:= " + stationId + " ...");
        int page = 0;
        int size = 100;

        List<Installation> installations = new ArrayList<>();

        final String baseUrl = "https://api.gios.gov.pl/pjp-api/v1/rest/station/sensors/";
        StringBuilder url = new StringBuilder(baseUrl);
        int totalPages = -1;
        try {

            do {

                url.append(+stationId).append("?page=").append(page).append("&size=").append(size);
                final JsonNode fetchedData = fetchDataWithRetries(url.toString(), 5, Duration.ofSeconds(15));

                if(totalPages==-1){
                    totalPages = fetchedData.get("totalPages").asInt();
                }

                final JsonNode itemsNode = fetchedData.path("Lista stanowisk pomiarowych dla podanej stacji");

//                System.out.println("Fetched page " + page + " of " + totalPages + " with data size := "+ itemsNode.size());

                for(JsonNode item : itemsNode){

                    Installation installation = new Installation();
                    installation.setId(item.path("Identyfikator stanowiska").asInt());
                    installation.setIndicatorCode(item.path("Wskaźnik - kod").asText());

                    installations.add(installation);
                }
                page++;
                url = new StringBuilder(baseUrl);

            }while (page < totalPages);



        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return installations;
    }

}
