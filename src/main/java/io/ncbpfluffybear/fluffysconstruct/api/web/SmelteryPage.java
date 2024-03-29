package io.ncbpfluffybear.fluffysconstruct.api.web;

import io.ncbpfluffybear.fluffysconstruct.data.SmelterySystem;

import java.util.HashMap;
import java.util.Map;

public class SmelteryPage {

    private static final int BUCKET_HEIGHT = 10;

    private final Map<String, Object> placeholders;
    private final WebResource source;

    public SmelteryPage(SmelterySystem system) {
        this.placeholders = new HashMap<>();
        placeholders.put("%LOCATION%", system.getController());
        placeholders.put("%FUEL%", system.getFuel());
        this.source = WebUtils.getResource("smelteryview.html");
    }

//    private String buildMeltedGraphic(CustomBlockData data) {
//        int graphicHeight = Controller.getVolume(data) * BUCKET_HEIGHT;
//        Map<String, Integer> melted = Controller.getMelted(data);
//
//        for (Map.Entry<String, Integer> molten : melted.entrySet()) {
//
//        }
//
//
//    }

    public byte[] getPage() {
        return this.source.getHtml(placeholders);
    }
}
