package com.olap3.cubeexplorer;

import com.alexscode.utilities.Future;
import com.olap3.cubeexplorer.castor.session.QueryRequest;
import com.olap3.cubeexplorer.castor.session.CrSession;
import com.olap3.cubeexplorer.castor.session.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StudentParser {
    static Pattern rqNumber = Pattern.compile("--(\\d*)");


    public static CrSession loadFile(String path){
        return loadFile(Paths.get(path));
    }


    public static CrSession loadFile(Path path){
        var fname = path.getFileName().toString().replace(".txt", "").split("-");
        var usr = new User(fname[1], fname[1], fname[0]);

        List<String> sesComments = new ArrayList<>();
        List<QueryRequest> reqs = new ArrayList<>();

        try {
            var lines = Files.readAllLines(path);
            List<String> comments = new ArrayList<>();
            List<String> request = new ArrayList<>();


            boolean start = true;
            QueryRequest current = null;

            for (var line : lines){
                Matcher nbMatch = rqNumber.matcher(line);

                if (line.isEmpty())
                    continue;

                if (line.startsWith("--")){
                    if (nbMatch.matches()) {
                        //System.out.println(line);
                        if (!start){
                            current.setComments(Future.join(comments, "\n"));
                            current.setQuery(Future.joinMDX(request));
                            reqs.add(current);
                        }
                        start = false;
                        current = new QueryRequest(Integer.parseInt(nbMatch.group(1)));
                        comments = new ArrayList<>();
                        request = new ArrayList<>();
                        continue;
                    }
                    if (start){
                        sesComments.add(line.replace("--", ""));
                        continue;
                    }
                    comments.add(line.replace("--", ""));
                } else {
                    request.add(line);
                }

            }
            if (current != null) {
                current.setComments(Future.join(comments, "\n"));
                current.setQuery(Future.joinMDX(request));
                reqs.add(current);
            }

        } catch (IOException e){
            System.err.printf("Error parsing student session '%s' !%n", path.toAbsolutePath().toString());
        }


        return new CrSession(usr, "Cube1MobProInd", path.getFileName().toString(), Future.join(sesComments, "\n"), reqs);
    }

    public static List<CrSession> loadDir(String path){
        if (!Files.isDirectory(Paths.get(path))){
            System.err.printf("Warning '%s' is not a valid directory !", path);
        }
        try {
            return Files.walk(Paths.get(path)).filter(p -> p.toFile().isFile()).map(StudentParser::loadFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Test code
     */
    public static void main(String[] args) throws Exception{
        CrSession session = loadFile("data/studentSessions/4-19.txt");
        System.out.println("Session size = " + session.getQueries().size());
        for (var q : session.getQueries()){
            System.out.println("--- Query ---");
            System.out.println(q.getQuery());
        }
        FrontEndFormatter.buildJson(session);
    }
}
