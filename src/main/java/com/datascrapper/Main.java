package com.datascrapper;

import java.util.List;

public class Main {

    public static void main(String[] args) {


        DataScraper dataScraper = new DataScraper();
        List<Station> stations = dataScraper.fetchAllStations();

        for (Station station : stations) {
            station.setInstallations(dataScraper.fetchAllInstallationsForStation(station.getId()));
            System.out.println("Station #" + station.getId() + " " + station.getName());
            for (Installation installation : station.getInstallations()) {
                System.out.println("installation #" + installation.getId() + " " + installation.getIndicatorCode());
            }
        }
    }
}

