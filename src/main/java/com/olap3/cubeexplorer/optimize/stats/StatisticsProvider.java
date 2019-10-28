package com.olap3.cubeexplorer.optimize.stats;

import com.alexscode.utilities.Future;
import com.alexscode.utilities.collection.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.PrimitiveSink;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.olap3.cubeexplorer.evaluate.SQLFactory;
import com.olap3.cubeexplorer.julien.*;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import com.olap3.cubeexplorer.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.LongBinaryOperator;
import java.util.stream.Collectors;

public class StatisticsProvider {
    public static String storageDirectory = "data/cache";
    public static Gson gson;
    public static HashMap<String, Statistics> memCache;

    static {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        gson = builder.create();
        memCache = new HashMap<>();
    }

    public Statistics getForCuboid(Set<ProjectionFragment> position, MeasureFragment measure){
        Statistics stats = getCached(position, measure);
        if (stats != null)
            return stats;

        stats = computeForCuboid(position, measure);
        var path = Paths.get(storageDirectory + "/" + getFileName(position, measure));
        try {
            Files.write(path, gson.toJson(stats).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stats;
    }

    private Statistics getCached(Set<ProjectionFragment> position, MeasureFragment measure){
        var filename = getFileName(position, measure);
        if (memCache.get(filename) == null) {
            var path = Paths.get(storageDirectory + "/" + filename);
            if (!Files.exists(path))
                return null;
            try {
                var stats = gson.fromJson(Files.readString(path), Statistics.class);
                memCache.put(filename, stats);
                return stats;
            } catch (IOException e) {
                return null;
            }
        } else
            return memCache.get(filename);
    }

    private Statistics computeForCuboid(Set<ProjectionFragment> position, MeasureFragment measure){
        SQLFactory sqlFactory = new SQLFactory(CubeUtils.getDefault());
        var mset = new HashSet<MeasureFragment>(); mset.add(measure);
        var q = new Qfset(new HashSet<>(position), new HashSet<>(), mset);



        return null;//TODO
    }

    private static Set<List<ProjectionFragment>> generateLattice(){
        CubeUtils cube = CubeUtils.getDefault();
        List<Hierarchy> hierarchies = new ArrayList<>(cube.getHierarchies());
        List<Set<ProjectionFragment>> levels = new ArrayList<>(hierarchies.size());

        for (Hierarchy hierarchy : hierarchies) {
            var tmp = new HashSet<ProjectionFragment>();
            for (Level l : hierarchy.getLevels()){
                tmp.add(ProjectionFragment.newInstance(l));
            }
            levels.add(tmp);
        }

        System.out.println(levels.stream().mapToLong(Set::size).reduce(1, new LongBinaryOperator() {
            @Override
            public long applyAsLong(long left, long right) {
                return left*right;
            }
        }));

        return Sets.cartesianProduct(levels);
    }

    private static String getFileName(Set<ProjectionFragment> position, MeasureFragment measure){
        List<String> frags = position.stream().map(Fragment::toString).collect(Collectors.toList());
        Collections.sort(frags);

        frags.add(measure.toString());

        String toHash = Future.join(frags, "");
        HashCode md5 = Hashing.md5().hashString(toHash, Charset.defaultCharset());

        return md5.toString() + ".json";
    }

    public static void main(String[] args) {
        Connection olap = MondrianConfig.getMondrianConnection();
        CubeUtils utils = new CubeUtils(olap, "Cube1MobProInd");
        CubeUtils.setDefault(utils);

        var p = new ProjectionFragment(utils.getLevel("Commune de residence", "Commune de residence"));
        var set = new HashSet<ProjectionFragment>(); set.add(p);
        var m = new MeasureFragment(utils.getMeasure("Nombre total d'individus"));

        getFileName(set, m);

        System.out.println(generateLattice().size());

    }


}
