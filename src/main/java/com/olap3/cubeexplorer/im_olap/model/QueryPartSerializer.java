package com.olap3.cubeexplorer.im_olap.model;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class QueryPartSerializer extends TypeAdapter<QueryPart> {//implements JsonSerializer<QueryPart>, JsonDeserializer<QueryPart> {
   /* @Override
    public JsonElement serialize(QueryPart queryPart, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        object.addProperty("value", queryPart.getValue());
        object.addProperty("type", queryPart.t.getValue());
        return object;
    }

    @Override
    public QueryPart deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject obj = jsonElement.getAsJsonObject();
        String val = obj.getAsJsonObject("value").getAsString();
        int t = obj.get("type").getAsInt();
        QueryPart qp;
        switch (t) {
            case 0 -> qp = QueryPart.newDimension(val);
            case 1 -> qp = QueryPart.newFilter(val);
            case 2 -> qp = QueryPart.newMeasure(val);
            default -> qp = null;
        }
        return qp;
    }*/

    @Override
    public void write(JsonWriter out, QueryPart part) throws IOException {
        out.beginObject();
        out.name("type");
        out.value(part.getType().getValue());
        out.name("value");
        out.value(part.getValue());
        out.endObject();
    }

    @Override
    public QueryPart read(JsonReader in) throws IOException {
        in.beginObject();
        in.nextName();
        int t = in.nextInt();
        in.nextName();
        String val = in.nextString();
        in.endObject();

        QueryPart qp;
        switch (t) {
            case 0 -> qp = QueryPart.newDimension(val);
            case 1 -> qp = QueryPart.newFilter(val);
            case 2 -> qp = QueryPart.newMeasure(val);
            default -> qp = null;
        }
        return qp;
    }
}
