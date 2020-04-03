package com.olap3.cubeexplorer.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.olap3.cubeexplorer.mondrian.CubeUtils;
import mondrian.olap.Level;

import java.io.IOException;

/**
 * Handles the serialisation of {@link Fragment} to a JSON format.
 */
public class FragmentAdapter extends TypeAdapter<Fragment> {
    public static final int SELECTION = 1, PROJECTION = 0, MEASURE = 2;

    /**
     * Overrides the Default CubeUtils from @ref{CubeUtils.class}
     */
    public static CubeUtils overrideUtils = null;

    private static CubeUtils getUtils(){
        if (overrideUtils != null)
            return overrideUtils;
        else
            return CubeUtils.getDefault();
    }

    @Override
    public void write(JsonWriter out, Fragment fragment) throws IOException {
        out.beginObject();
        if (fragment instanceof SelectionFragment) {
            SelectionFragment sf = ((SelectionFragment) fragment);

            out.name("type");
            out.value(SELECTION);

            out.name("level");
            out.value(sf.getLevel().getUniqueName());

            out.name("member");
            out.value(sf.getValue().getName());
        } else if (fragment instanceof ProjectionFragment) {
            ProjectionFragment pf = ((ProjectionFragment) fragment);

            out.name("type");
            out.value(PROJECTION);

            out.name("level");
            out.value(pf.getLevel().getUniqueName());
        }
        else if (fragment instanceof MeasureFragment) {
            MeasureFragment mf = ((MeasureFragment) fragment);

            out.name("type");
            out.value(MEASURE);

            out.name("name");
            out.value(mf.getAttribute().getName());
        }

        out.endObject();
    }

    @Override
    public Fragment read(JsonReader in) throws IOException {
        in.beginObject();

        in.nextName();
        int t = in.nextInt();

        if (t == MEASURE){
            in.nextName();
            String mname = in.nextString();
            in.endObject();
            return MeasureFragment.newInstance(getUtils().getMeasure(mname));
        } else if (t == PROJECTION){
            in.nextName();
            String lname = in.nextString();
            in.endObject();
            return ProjectionFragment.newInstance(getUtils().getLevel(lname));
        }else if (t == SELECTION){
            in.nextName();
            String lname = in.nextString();
            Level l = getUtils().getLevel(lname);
            in.nextName();
            String membername = in.nextString();
            in.endObject();
            return SelectionFragment.newInstance(getUtils().getMember(l, membername));
        }

        in.endObject();
        System.err.println("Error unknown Fragment type !");
        return null;
    }
}
