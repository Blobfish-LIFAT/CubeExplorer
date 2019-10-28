package com.olap3.cubeexplorer.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.olap3.cubeexplorer.StudentParser;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExportForConversion {
    static String inDir = "data/studentSessions",
                  outDir = "data/export_ideb/";

    public static void main(String[] args) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.setPrettyPrinting().create();

        var sessions = StudentParser.loadDir(inDir);
        for (var session : sessions) {
            System.out.println("Exporting session " + session.getTitle());
            var path = Paths.get(outDir + session.getTitle().replace(".txt", ".json"));
            Files.write(path, gson.toJson(session).getBytes());
        }
    }
}


