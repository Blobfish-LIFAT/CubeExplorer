package it.unibo.csr.big.cubeload.schema;

import it.unibo.csr.big.cubeload.generator.OlapGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * This class implements a hierarchy's level, and the methods
 * to manipulate its fields.
 *
 * @author Luca Spadazzi
 */
public class Level {
    private static final HashMap<String, Level> backingMap = new HashMap<>();

    private String name;
    private Set<String> values = new HashSet<String>();

    @Getter @Setter
    private mondrian.olap.Level md;


    /**
     * Class constructor.
     *
     * @param name The name of the level.
     */
    private Level(String name) {
        this.name = name;
    }

    public static Level newLevel(String name){
        return backingMap.computeIfAbsent(name, Level::new);
    }

    /**
     * Getter method for the level name.
     *
     * @return The name of the level.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter method for the number of distinct values.
     *
     * @return The size of the level's values list.
     */
    public int getValuesCount() {
        return values.size();
    }

    /**
     * Getter method for the level's values.
     *
     * @return The list of distinct values.
     */
    public List<String> getValues() {
        List<String> tempList = new ArrayList<String>();

        for (String s : values) {
            tempList.add(s);
        }

        return tempList;
    }

    /**
     * This method adds a value to the level's
     * values list, if it's not already present.
     *
     * @param value The value to be added.
     */
    public void addDistinctValues(String value) {
        values.add(value);
    }

    /**
     * Getter method for a random level value.
     *
     * @return The randomly chosen value.
     */
    public String getRandomValue() {
        int toGo = OlapGenerator.rand.nextInt(getValuesCount());
        Iterator<String> it = values.iterator();
        for (int i = 0; i != toGo; i++)
            it.next();
        return it.next();
    }


    /**
     * This method checks if the current level is a leaf in the given hierarchy.
     *
     * @param hierarchy The hierarchy in which we look for the current level.
     * @return True if the current level is the leaf in the given hierarchy, False otherwise.
     */
    public boolean isLeaf(Hierarchy hierarchy) {
        return (hierarchy.findPosition(this.name) == hierarchy.getLevelCount() - 1 ? true : false);
    }

    /**
     * This method checks if the current level is the root in the given hierarchy.
     *
     * @param hierarchy The hierarchy in which  we look for the current level.
     * @return True if the current level is the root in the given hierarchy, False otherwise.
     */
    public boolean isRoot(Hierarchy hierarchy) {
        return (hierarchy.findPosition(this.name) == 0 ? true : false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Level level = (Level) o;
        return Objects.equals(name, level.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
