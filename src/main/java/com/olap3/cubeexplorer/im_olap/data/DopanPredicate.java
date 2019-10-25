package com.olap3.cubeexplorer.im_olap.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class DopanPredicate {
    @SerializedName("level")
    @Expose
    private String level;
    @SerializedName("value")
    @Expose
    private String value;
}
